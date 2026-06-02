<!--
  IBM Confidential
  PID 5900-AR4
  Copyright IBM Corp. 2026
-->

# Engineering Guide for Custom BAMOE Container Images

## Introduction

This guide helps you customize BAMOE container images by overriding their base operating system images while maintaining full functionality and compatibility. Whether you need to comply with corporate security policies, patch vulnerabilities quickly, or standardize your container infrastructure, this guide provides comprehensive instructions for all BAMOE container images.

## Why Override Base Images?

Organizations often need to customize container base images for various reasons:

- **Security Compliance**: Use specific OS versions that meet your organization's security policies
- **Vulnerability Management**: Quickly patch vulnerabilities by using updated base images
- **Standardization**: Maintain consistency across your container infrastructure
- **Corporate Policies**: Comply with internal requirements for approved base images
- **Performance Optimization**: Use optimized base images for specific environments
- **Regional Requirements**: Meet specific regulatory or compliance requirements

## BAMOE Container Images

BAMOE provides six container images, organized into two categories:

### Development Environment Images

These images support the BAMOE development and authoring experience:

1. **Canvas Image** - Web-based business automation canvas application
2. **CORS Proxy Image** - Cross-Origin Resource Sharing proxy service
3. **Extended Services Image** - Quarkus-based backend services
4. **Maven Repository Image** - Maven artifact repository server

### Runtime Environment Images

These images are used in production runtime environments:

1. **Management Console Image** - Runtime management and monitoring console
2. **MCP Server Image** - Model Context Protocol server for AI/LLM integration

## Prerequisites

Before you begin overriding BAMOE container images, ensure you have the following tools and access:

### Tools Required

- **Docker** (20.10+) or **Podman** (3.0+)
- **Docker Buildx** (for multi-architecture builds)
- Access to source BAMOE images (quay.io/bamoe/*)
- Access to your desired base images (e.g., Red Hat Container Catalog)

### Verify Your Setup

```bash
# Check Docker version
docker --version

# Check Buildx availability
docker buildx version

# Test access to BAMOE images
docker pull quay.io/bamoe/canvas:main

# Check Podman version (if using Podman)
podman --version
```

## Understanding Multi-Stage Builds

All override approaches in this guide use Docker/Podman multi-stage builds.

### How Multi-Stage Builds Work

```dockerfile
# Stage 1: Source - Pull the existing BAMOE image
FROM quay.io/bamoe/canvas:main AS source

# Stage 2: Custom - Build with your desired base OS
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.6

# Copy files from source and add your configurations
COPY --from=source /path/to/files /path/to/files
```

### Critical Concept: What Gets Lost

**Important**: When using `COPY --from=source`, only files and directories are copied. The following Dockerfile directives are **NOT inherited**:

- ❌ `ENV` - Environment variables
- ❌ `ARG` - Build arguments
- ❌ `EXPOSE` - Port declarations
- ❌ `USER` - Runtime user context
- ❌ `WORKDIR` - Working directory
- ❌ `HEALTHCHECK` - Health monitoring
- ❌ `ENTRYPOINT` / `CMD` - Startup commands
- ❌ `LABEL` - Metadata
- ❌ `VOLUME` - Volume declarations

**You must explicitly recreate all these configurations in your override Containerfile.**

## Common Override Scenarios

This section demonstrates three common scenarios for overriding BAMOE container images, showing how each applies to different image types.

### Scenario 1: Override Operating System Version

**Use Case**: Your organization requires a specific OS version (e.g., UBI 9.6 instead of 9.7) for security compliance or standardization.

**Applies to**: All images

#### Common Dockerfile Template for httpd-Based Images (Canvas, Management Console, and Maven Repository)

Canvas, Management Console, and Maven Repository images all use httpd and follow a similar Dockerfile structure. The following template works for all three images. Simply replace the placeholders with the appropriate values for your target image:

```dockerfile
FROM quay.io/bamoe/<image-name>:<tag> AS source

# Use your desired base OS version
FROM registry.access.redhat.com/ubi9/ubi-minimal:<version>

<ARG_DECLARATIONS>
<ENV_DECLARATIONS>

# Install httpd on the new OS version
RUN microdnf install httpd curl && microdnf clean all <HTTPD_CONFIG_ADJUSTMENTS>

# Copy application files from source
<COPY_COMMANDS>

# Recreate permissions for httpd-based images
<PERMISSION_COMMANDS>

EXPOSE ${<PORT_ENV_VAR>}
USER 1000
HEALTHCHECK --interval=1m --timeout=5s CMD curl -f http://localhost:${<PORT_ENV_VAR>}/ || exit 1
<ENTRYPOINT_OR_CMD>
```

**Placeholder Values:**

| Placeholder | Canvas Image | Management Console Image | Maven Repository Image |
|-------------|--------------|--------------------------|------------------------|
| `<image-name>` | `canvas` | `management-console` | `maven-repository` |
| `<tag>` | `main` (or specific version) | `main` (or specific version) | `main` (or specific version) |
| `<version>` | `9.6`, `9.7`, etc. | `9.6`, `9.7`, etc. | `9.6`, `9.7`, etc. |
| `<ARG_DECLARATIONS>` | `ARG TARGETARCH` | `ARG TARGETARCH` | `ARG MAVEN_REPO_PORT=8080` |
| `<ENV_DECLARATIONS>` | `ENV TARGETARCH=$TARGETARCH`<br>`ENV BAMOE_CANVAS_DEFAULT_PORT=8080` | `ENV TARGETARCH=$TARGETARCH`<br>`ENV BAMOE_MANAGEMENT_CONSOLE_PORT=8080` | `ENV MAVEN_REPO_PORT=$MAVEN_REPO_PORT` |
| `<PORT_ENV_VAR>` | `BAMOE_CANVAS_DEFAULT_PORT` | `BAMOE_MANAGEMENT_CONSOLE_PORT` | `MAVEN_REPO_PORT` |
| `<HTTPD_CONFIG_ADJUSTMENTS>` | `&& sed -i '/Listen 80/d' /etc/httpd/conf/httpd.conf` | `&& sed -i '/Listen 80/d' /etc/httpd/conf/httpd.conf` | (none - httpd.conf copied from source) |
| `<COPY_COMMANDS>` | `COPY --from=source /kie-sandbox /kie-sandbox`<br>`COPY --from=source /etc/httpd/conf.d/custom.conf /etc/httpd/conf.d/custom.conf` | `COPY --from=source /management-console /management-console`<br>`COPY --from=source /etc/httpd/conf.d/custom.conf /etc/httpd/conf.d/custom.conf` | `COPY --from=source /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf`<br>`COPY --from=source /etc/httpd/conf.d/ /etc/httpd/conf.d/`<br>`COPY --from=source /var/www/html/ /var/www/html/` |
| `<PERMISSION_COMMANDS>` | `RUN chgrp -R 0 /var/log/httpd /var/run/httpd /var/www/html /kie-sandbox \`<br>`  && chmod -R g=u /var/log/httpd /var/run/httpd /var/www/html /kie-sandbox` | `RUN chgrp -R 0 /var/log/httpd /var/run/httpd /var/www/html /management-console \`<br>`  && chmod -R g=u /var/log/httpd /var/run/httpd /var/www/html /management-console` | `RUN chgrp -R 0 /var/log/httpd /var/run/httpd /var/www/html \`<br>`  && chmod -R g=u /var/log/httpd /var/run/httpd /var/www/html` |
| `<ENTRYPOINT_OR_CMD>` | `ENTRYPOINT ["/kie-sandbox/entrypoint.sh"]` | `ENTRYPOINT ["/management-console/entrypoint.sh"]` | `CMD ["httpd", "-D", "FOREGROUND"]` |

**Key Differences:**

- **Canvas & Management Console**: Have dedicated application directories (`/kie-sandbox`, `/management-console`) with entrypoint scripts
- **Maven Repository**: Serves Maven artifacts directly from `/var/www/html` and uses httpd configuration from source image

**Recreate Permissions for httpd-Based Images:**

For images that use httpd (Canvas, Management Console, and Maven Repository), you must recreate proper permissions after copying files from the source image. This ensures the application can write to necessary directories when running with non-root privileges in OpenShift or other container platforms.

OpenShift and other container platforms run containers with arbitrary UIDs but always use group ID 0 (root group). This permission setup allows the application to function correctly regardless of the assigned UID.


#### Common Dockerfile Template for Node.js-Based Images (CORS Proxy)

CORS Proxy is the only Node.js-based image in BAMOE. It uses NVM (Node Version Manager) to manage Node.js runtime. The following template shows common operations:

```dockerfile
FROM quay.io/bamoe/cors-proxy:<tag> AS source

# Use your desired base OS version
FROM registry.access.redhat.com/ubi9/ubi-minimal:<version>

# Copy application with Node.js runtime from source
COPY --from=source /home/kie-sandbox /home/kie-sandbox

# Set PATH to Node.js (bundled via NVM in copied files)
ENV PATH=/home/kie-sandbox/.nvm/versions/node/v24.13.0/bin:$PATH

USER 1000
CMD ["node", "/home/kie-sandbox/cors-proxy/index.js"]
```

**Common Operations:**

1. **Override OS Version**: Copy entire `/home/kie-sandbox` directory (includes Node.js runtime)
2. **Change Node.js Version**: Install NVM and desired Node.js version in new base image, copy only application code
3. **Security Patches**: Apply `microdnf update -y` to base OS; Node.js runtime is copied from source
4. **Performance Tuning**: Set `NODE_ENV=production` and Node.js memory limits

#### Common Dockerfile Template for Java-Based Images (Extended Services and MCP Server)

Both Extended Services and MCP Server are Quarkus-based Java applications that use OpenJDK base images. The following template works for both images when overriding the OS version:

```dockerfile
FROM quay.io/bamoe/<image-name>:<tag> AS source

# Use your desired Java runtime base image (includes OS)
FROM <java-base-image>:<version>

# Copy application from source
COPY --from=source --chown=1000:0 /<application-directory> /<application-directory>

# Set environment variables
<ENV_DECLARATIONS>

USER 1000
WORKDIR /<application-directory>

# Run Quarkus application
<CMD_OR_ENTRYPOINT>
```

**Placeholder Values:**

| Placeholder | Extended Services Image | MCP Server Image |
|-------------|------------------------|------------------|
| `<image-name>` | `extended-services` | `mcp-server` |
| `<tag>` | `main` (or specific version) | `main` (or specific version) |
| `<java-base-image>` | `registry.access.redhat.com/ubi9/openjdk-17-runtime` | `registry.access.redhat.com/ubi9/openjdk-17-runtime` |
| `<version>` | `1.19`, `1.20`, etc. | `1.19`, `1.20`, etc. |
| `<application-directory>` | `kie-sandbox/bamoe_extended_services` | `mcp-server` |
| `<ENV_DECLARATIONS>` | `ENV EXTENDED_SERVICES_PORT=21345` | `ENV MCP_SERVER_DEBUG_LEVEL="INFO"`<br>`ENV MCP_SERVER_PORT=8080`<br>`ENV MCP_SERVER_OPENAPI_URLS="http://localhost:8080/q/openapi"`<br>`ENV MCP_SERVER_SECURITY_ENABLED="false"`<br>`ENV MCP_SERVER_SECURITY_AUTH_PERMISSION="permit"` |
| `<CMD_OR_ENTRYPOINT>` | `CMD java -Dquarkus.http.port=$EXTENDED_SERVICES_PORT -jar quarkus-run.jar` | `CMD ["java", "-Dquarkus.log.level=${MCP_SERVER_DEBUG_LEVEL}", "-Dquarkus.http.port=${MCP_SERVER_PORT}", "-Dbamoe.mcpserver.openapi.urls=${MCP_SERVER_OPENAPI_URLS}", "-Dquarkus.oidc.auth-server-url=${MCP_SERVER_SECURITY_AUTH_SERVER_URL}", "-Dquarkus.oidc.tenant-enabled=${MCP_SERVER_SECURITY_ENABLED}", "-Dquarkus.oidc.client-id=${MCP_SERVER_SECURITY_OIDC_CLIENT_ID}", "-Dquarkus.oidc.credentials.secret=${MCP_SERVER_SECURITY_AUTH_SECRET}", "-Dquarkus.http.auth.permission.authenticated.policy=${MCP_SERVER_SECURITY_AUTH_PERMISSION}", "-jar", "quarkus-run.jar"]` |

**Key Characteristics:**
- Java-based images use OpenJDK base images that include both the OS and Java runtime
- Changing the base image version updates both the OS and potentially the Java version
- Quarkus applications are packaged as `quarkus-run.jar` with dependencies
- Both images use similar structure but different environment variables and startup commands

#### Extended Services Image (Java-based)

```dockerfile
FROM quay.io/bamoe/extended-services:main AS source

# Change to UBI 9 OpenJDK 17 runtime with different version
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.19

# Copy application
COPY --from=source --chown=1000:0 /kie-sandbox/bamoe_extended_services /kie-sandbox/bamoe_extended_services

ENV EXTENDED_SERVICES_PORT=21345
WORKDIR /kie-sandbox/bamoe_extended_services
USER 1000
CMD java -Dquarkus.http.port=$EXTENDED_SERVICES_PORT -jar quarkus-run.jar
```

**Key Point**: The base image provides Java runtime, so changing the base image version also updates the underlying OS. See common template above for more details.

### Scenario 2: Override Runtime Versions (JDK, Node.js)

**Use Case**: Your organization requires a specific runtime version (e.g., Java 21 instead of Java 17, or Node.js 20 instead of 24).

**Applies to**: Extended Services, MCP Server (Java), CORS Proxy (Node.js)

#### Java-Based Images (Extended Services and MCP Server)

**For Java-based images, use the same process as Scenario 1** (see "Common Dockerfile Template for Java-Based Images" in Scenario 1 above), but simply change the Java version in the base image:

- **To upgrade to Java 21**: Use `registry.access.redhat.com/ubi9/openjdk-21-runtime:1.20` instead of `openjdk-17-runtime`
- **To use alternative Java distributions**: Use base images like `eclipse-temurin:17-jre-alpine` or `amazoncorretto:17`

The Dockerfile structure remains the same - only the `FROM` line changes to specify the desired Java version.

#### CORS Proxy Image - Use Different Node.js Version

**Note**: For CORS Proxy, Node.js is bundled in the source image via NVM. To use a different Node.js version, you would need to:

1. Install NVM in your base image
2. Install the desired Node.js version
3. Copy only the application code (not the Node.js runtime)

```dockerfile
FROM quay.io/bamoe/cors-proxy:main AS source

FROM registry.access.redhat.com/ubi9/ubi-minimal:9.6

# Install NVM and Node.js 20 instead of 24
RUN microdnf install -y curl tar gzip && \
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash && \
    export NVM_DIR="$HOME/.nvm" && \
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" && \
    nvm install 20 && \
    nvm use 20

# Copy only application code
COPY --from=source /home/kie-sandbox/cors-proxy /home/kie-sandbox/cors-proxy

ENV PATH=/root/.nvm/versions/node/v20.0.0/bin:$PATH
USER 1000
CMD ["node", "/home/kie-sandbox/cors-proxy/index.js"]
```

**Key Point**: Node.js version changes require rebuilding the Node.js runtime in your base image.

### Scenario 3: Override for Security Patch Upgrades

**Use Case**: A critical security vulnerability (CVE) is discovered in the base OS or runtime. You need to patch immediately without waiting for an official BAMOE release.

**Applies to**: All images

**General Approach**: Use the same Dockerfile templates from Scenario 1, but add security update commands to apply patches.

#### httpd-Based Images (Canvas, Management Console, Maven Repository)

**Use the common template from Scenario 1**, but add the following security update step after installing httpd:

```dockerfile
# Install httpd and apply all security updates
RUN microdnf install httpd curl && \
    microdnf update -y && \
    microdnf clean all && \
    sed -i '/Listen 80/d' /etc/httpd/conf/httpd.conf
```

**Key Addition**: The `microdnf update -y` command applies all available security patches to the base OS and installed packages.

#### Java-Based Images (Extended Services, MCP Server)

**Use the common template from Scenario 1**, but add the following security update step:

```dockerfile
# Use the latest patched OpenJDK runtime
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.20

# Apply additional OS security updates
USER root
RUN microdnf update -y && microdnf clean all
USER 1000
```

**Key Additions**:
- Use the latest version of the OpenJDK runtime base image (e.g., `1.20` instead of `1.19`)
- Add `microdnf update -y` with root privileges to apply additional OS-level patches

#### Node.js-Based Images (CORS Proxy)

**Use the common template from Scenario 1**, but add the following security update step after the FROM statement:

```dockerfile
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.7

# Apply all security updates
RUN microdnf update -y && microdnf clean all
```

**Key Addition**: The `microdnf update -y` command patches the base OS. Node.js runtime is copied from source and doesn't need separate patching.

---

### Scenario 4: Performance Optimization

**Use Case**: Your organization needs to optimize container images for better performance, reduced size, or specific hardware configurations.

**Applies to**: All images

**General Approach**: Use the same Dockerfile templates from Scenario 1, but add performance optimization flags and settings.

#### httpd-Based Images (Canvas, Management Console, Maven Repository)

**Use the common template from Scenario 1**, but add these performance optimizations:

**1. Optimize package installation:**
```dockerfile
# Install with optimization flags
RUN microdnf install httpd curl --nodocs --setopt=install_weak_deps=0 && \
    microdnf clean all && \
    rm -rf /var/cache/yum && \
    sed -i '/Listen 80/d' /etc/httpd/conf/httpd.conf
```

**2. Combine permission commands into single layer:**
```dockerfile
# Optimize permissions in single layer
RUN chgrp -R 0 /var/log/httpd /var/run/httpd /var/www/html /<application-directory> && \
    chmod -R g=u /var/log/httpd /var/run/httpd /var/www/html /<application-directory>
```

**Key Optimizations**:
- `--nodocs`: Skip documentation to reduce image size
- `--setopt=install_weak_deps=0`: Don't install weak dependencies
- `rm -rf /var/cache/yum`: Remove package manager cache
- Combined RUN commands: Reduce image layers

#### Java-Based Images (Extended Services, MCP Server)

**Use the common template from Scenario 1**, but replace the CMD with JVM performance tuning:

```dockerfile
# Optimize JVM for container environment with performance tuning
CMD java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -XX:+UseStringDeduplication \
    -Dquarkus.http.port=$EXTENDED_SERVICES_PORT \
    -jar quarkus-run.jar
```

**Key JVM Optimizations**:
- `-XX:+UseContainerSupport`: Respect container memory limits
- `-XX:MaxRAMPercentage=75.0`: Use 75% of container memory for heap
- `-XX:InitialRAMPercentage=50.0`: Start with 50% of container memory
- `-XX:+UseG1GC`: Use G1 garbage collector for better performance
- `-XX:MaxGCPauseMillis=200`: Target max GC pause time of 200ms
- `-XX:+ParallelRefProcEnabled`: Parallel reference processing
- `-XX:+UseStringDeduplication`: Reduce memory footprint by deduplicating strings

**Note**: For MCP Server, include all the Quarkus-specific `-D` parameters from the Scenario 1 template along with the JVM flags.

#### Node.js-Based Images (CORS Proxy)

**Use the common template from Scenario 1**, but add these Node.js performance optimizations:

**1. Optimize base OS installation:**
```dockerfile
# Install only essential packages
RUN microdnf install --nodocs --setopt=install_weak_deps=0 && \
    microdnf clean all && \
    rm -rf /var/cache/yum
```

**2. Add Node.js performance environment variables:**
```dockerfile
# Set Node.js performance environment variables
ENV NODE_ENV=production
ENV NODE_OPTIONS="--max-old-space-size=512"
```

**Key Node.js Optimizations**:
- `NODE_ENV=production`: Enable production optimizations (minification, caching, etc.)
- `--max-old-space-size=512`: Limit Node.js heap memory to 512MB
- `--optimize-for-size`: Alternative option to optimize for smaller memory footprint

---

**Complete Example (Canvas Image with Optimizations):**

```dockerfile
FROM quay.io/bamoe/canvas:main AS source

FROM registry.access.redhat.com/ubi9/ubi-minimal:9.7

ARG TARGETARCH
ENV TARGETARCH=$TARGETARCH
ENV BAMOE_CANVAS_DEFAULT_PORT=8080

# Install with optimization flags
RUN microdnf install httpd curl --nodocs --setopt=install_weak_deps=0 && \
    microdnf clean all && \
    rm -rf /var/cache/yum && \
    sed -i '/Listen 80/d' /etc/httpd/conf/httpd.conf

# Copy application files
COPY --from=source /kie-sandbox /kie-sandbox
COPY --from=source /etc/httpd/conf.d/custom.conf /etc/httpd/conf.d/custom.conf

# Optimize permissions in single layer
RUN chgrp -R 0 /var/log/httpd /var/run/httpd /var/www/html /kie-sandbox && \
    chmod -R g=u /var/log/httpd /var/run/httpd /var/www/html /kie-sandbox

EXPOSE 8080
USER 1000
HEALTHCHECK --interval=1m --timeout=5s CMD curl -f http://localhost:8080/ || exit 1
ENTRYPOINT ["/kie-sandbox/entrypoint.sh"]
```

### Performance Optimization Best Practices

#### For All Images

1. **Minimize Layers**: Combine RUN commands to reduce image layers
   ```dockerfile
   # ❌ Multiple layers
   RUN microdnf install httpd
   RUN microdnf clean all
   RUN rm -rf /var/cache/yum

   # ✅ Single layer
   RUN microdnf install httpd && \
       microdnf clean all && \
       rm -rf /var/cache/yum
   ```

2. **Remove Unnecessary Files**: Clean up after package installation
   ```dockerfile
   RUN microdnf install package && \
       microdnf clean all && \
       rm -rf /var/cache/yum /tmp/*
   ```

3. **Use Specific Package Versions**: Avoid unnecessary updates
   ```dockerfile
   RUN microdnf install httpd-2.4.57 curl-8.0.1
   ```

#### For Java-Based Images (Extended Services, MCP Server)

1. **Container-Aware JVM Settings**:
   ```dockerfile
   CMD java -XX:+UseContainerSupport \
       -XX:MaxRAMPercentage=75.0 \
       -jar app.jar
   ```

2. **Choose Appropriate GC**:
   - G1GC: Good for most workloads (`-XX:+UseG1GC`)
   - ZGC: For low-latency requirements (`-XX:+UseZGC`)
   - Parallel GC: For throughput (`-XX:+UseParallelGC`)

3. **Tune GC Pause Times**:
   ```dockerfile
   CMD java -XX:+UseG1GC \
       -XX:MaxGCPauseMillis=200 \
       -jar app.jar
   ```

4. **Enable String Deduplication**:
   ```dockerfile
   CMD java -XX:+UseStringDeduplication -jar app.jar
   ```

#### For Node.js-Based Images (CORS Proxy)

1. **Set Production Mode**:
   ```dockerfile
   ENV NODE_ENV=production
   ```

2. **Limit Memory Usage**:
   ```dockerfile
   ENV NODE_OPTIONS="--max-old-space-size=512"
   ```

3. **Enable V8 Optimizations**:
   ```dockerfile
   ENV NODE_OPTIONS="--optimize-for-size"
   ```

#### For httpd-Based Images (Canvas, Management Console, Maven Repository)

1. **Disable Unnecessary Modules**: Edit httpd configuration to disable unused modules

2. **Optimize MPM Settings**: Tune worker processes and threads based on workload

3. **Enable Compression**: Configure mod_deflate for response compression


### Scenario Summary

| Scenario | Canvas | CORS Proxy | Extended Services | Maven Repository | Management Console | MCP Server |
|----------|--------|------------|-------------------|------------------|-------------------|------------|
| **Override OS** | Change UBI version | Change UBI version | Change UBI version in Java base | Change UBI version | Change UBI version | Change UBI version in Java base |
| **Override Runtime** | N/A (httpd from OS) | Rebuild with different Node.js | Change Java base image | N/A (httpd from OS) | N/A (httpd from OS) | Change Java base image |
| **Security Patches** | `microdnf update -y` | `microdnf update -y` | Use latest base + update | `microdnf update -y` | `microdnf update -y` | Use latest base + update |
| **Performance Optimization** | Minimize layers, clean cache | NODE_ENV=production, memory limits | JVM tuning, G1GC | Minimize layers, clean cache | Minimize layers, clean cache | JVM tuning, G1GC |

### Scenario 5: Enterprise Certificate Integration

**Use Case**: Your organization requires custom SSL/TLS certificates, internal Certificate Authority (CA) certificates, or corporate proxy certificates to be trusted by BAMOE containers.

**Applies to**: All images, especially those making external HTTPS connections

**General Approach**: Use the same Dockerfile templates from Scenario 1, but add certificate installation and trust configuration steps.

#### httpd-Based Images (Canvas, Management Console, Maven Repository)

**Use the common template from Scenario 1**, but add these certificate integration steps:

**1. Copy enterprise CA certificates:**
```dockerfile
# Copy enterprise CA certificates
COPY corporate-ca-bundle.crt /etc/pki/ca-trust/source/anchors/
```

**2. Update httpd installation to include ca-certificates and update trust:**
```dockerfile
# Install httpd and update CA trust
RUN microdnf install httpd curl ca-certificates && \
    update-ca-trust extract && \
    microdnf clean all && \
    sed -i '/Listen 80/d' /etc/httpd/conf/httpd.conf
```

**Key Points**:
- Copy CA certificates to `/etc/pki/ca-trust/source/anchors/`
- Install `ca-certificates` package
- Run `update-ca-trust extract` to update system trust store
- System trust is usually sufficient for httpd applications

#### Java-Based Images (Extended Services, MCP Server)

**Use the common template from Scenario 1**, but add these certificate integration steps:

**1. Copy enterprise CA certificates:**
```dockerfile
# Copy enterprise CA certificates
COPY corporate-ca-bundle.crt /tmp/corporate-ca.crt
```

**2. Import certificates into Java truststore (before copying application):**
```dockerfile
# Import certificates into Java truststore
USER root
RUN keytool -importcert -noprompt \
    -keystore /etc/pki/java/cacerts \
    -storepass changeit \
    -alias corporate-ca \
    -file /tmp/corporate-ca.crt && \
    rm /tmp/corporate-ca.crt && \
    update-ca-trust extract
USER 1000
```

**Key Points**:
- Import certificates into Java truststore using `keytool`
- Default Java truststore location: `/etc/pki/java/cacerts`
- Default truststore password: `changeit`
- Update system CA trust as well with `update-ca-trust extract`
- Must run with root privileges to modify truststore

**Alternative - Custom truststore at runtime:**
```dockerfile
CMD java -Djavax.net.ssl.trustStore=/path/to/custom-truststore.jks \
    -Djavax.net.ssl.trustStorePassword=password \
    -jar quarkus-run.jar
```

#### Node.js-Based Images (CORS Proxy)

**Use the common template from Scenario 1**, but add these certificate integration steps:

**1. Copy enterprise CA certificates:**
```dockerfile
# Copy enterprise CA certificates
COPY corporate-ca-bundle.crt /etc/pki/ca-trust/source/anchors/
```

**2. Install ca-certificates and update trust:**
```dockerfile
# Install ca-certificates and update trust
RUN microdnf install ca-certificates && \
    update-ca-trust extract && \
    microdnf clean all
```

**3. Set Node.js to use system CA certificates:**
```dockerfile
# Set Node.js to use system CA certificates
ENV NODE_EXTRA_CA_CERTS=/etc/pki/tls/certs/ca-bundle.crt
```

**Key Points**:
- Set `NODE_EXTRA_CA_CERTS` environment variable to point to system CA bundle
- Node.js will trust all certificates in this file
- Update system CA trust store with `update-ca-trust extract`

**Alternative - Use custom CA file:**
```dockerfile
ENV NODE_EXTRA_CA_CERTS=/path/to/custom-ca-bundle.crt
```

---

**Complete Example (Extended Services with Certificates):**

```dockerfile
FROM quay.io/bamoe/extended-services:main AS source

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.20

# Copy enterprise CA certificates
COPY corporate-ca-bundle.crt /tmp/corporate-ca.crt

# Import certificates into Java truststore
USER root
RUN keytool -importcert -noprompt \
    -keystore /etc/pki/java/cacerts \
    -storepass changeit \
    -alias corporate-ca \
    -file /tmp/corporate-ca.crt && \
    rm /tmp/corporate-ca.crt && \
    update-ca-trust extract
USER 1000

# Copy application
COPY --from=source --chown=1000:0 /kie-sandbox/bamoe_extended_services /kie-sandbox/bamoe_extended_services

ENV EXTENDED_SERVICES_PORT=21345
WORKDIR /kie-sandbox/bamoe_extended_services

CMD java -Dquarkus.http.port=$EXTENDED_SERVICES_PORT -jar quarkus-run.jar
```

### Common Certificate Scenarios

#### Scenario A: Corporate Proxy with SSL Inspection

```dockerfile
# Add proxy CA certificate
COPY proxy-ca.crt /etc/pki/ca-trust/source/anchors/
RUN update-ca-trust extract

# Configure proxy environment variables
ENV HTTP_PROXY=http://proxy.corporate.com:8080
ENV HTTPS_PROXY=http://proxy.corporate.com:8080
ENV NO_PROXY=localhost,127.0.0.1,.corporate.com
```

#### Scenario B: Internal Certificate Authority

```dockerfile
# Add internal CA root and intermediate certificates
COPY internal-root-ca.crt /etc/pki/ca-trust/source/anchors/
COPY internal-intermediate-ca.crt /etc/pki/ca-trust/source/anchors/
RUN update-ca-trust extract
```

#### Scenario C: Multiple Certificate Authorities

```dockerfile
# Add multiple CA certificates
COPY ca-certificates/*.crt /etc/pki/ca-trust/source/anchors/
RUN update-ca-trust extract

# For Java, import each certificate
RUN for cert in /tmp/ca-certificates/*.crt; do \
      keytool -importcert -noprompt \
        -keystore /etc/pki/java/cacerts \
        -storepass changeit \
        -alias $(basename $cert .crt) \
        -file $cert; \
    done
```
### Scenario Summary

| Scenario | Canvas | CORS Proxy | Extended Services | Maven Repository | Management Console | MCP Server |
|----------|--------|------------|-------------------|------------------|-------------------|------------|
| **Override OS** | Change UBI version | Change UBI version | Change UBI version in Java base | Change UBI version | Change UBI version | Change UBI version in Java base |
| **Override Runtime** | N/A (httpd from OS) | Rebuild with different Node.js | Change Java base image | N/A (httpd from OS) | N/A (httpd from OS) | Change Java base image |
| **Security Patches** | `microdnf update -y` | `microdnf update -y` | Use latest base + update | `microdnf update -y` | `microdnf update -y` | Use latest base + update |
| **Performance Optimization** | Minimize layers, clean cache | NODE_ENV=production, memory limits | JVM tuning, G1GC | Minimize layers, clean cache | Minimize layers, clean cache | JVM tuning, G1GC |
| **Enterprise Certificates** | System CA trust | NODE_EXTRA_CA_CERTS | Java truststore import | System CA trust | System CA trust | Java truststore import |

**Key Takeaway**: Multi-stage builds provide the flexibility to address all five scenarios while maintaining full BAMOE functionality and enterprise security requirements.


## Deploying Your Custom Images

Once you've built your custom BAMOE images with overridden base images, you need to deploy them in your environment. This section covers two primary deployment methods.

### Method 1: Using direct Openshift deployment

You can use your custom images directly in two common scenarios: Docker Compose for local/development environments and OpenShift for production deployments.

#### Scenario 1: Using with Docker Compose

Update your `docker-compose.yml` to reference your custom images:

```yaml
version: '3.8'
services:
  canvas:
    image: myregistry.com/bamoe-canvas-custom:v1.0.0
    ports:
      - "8080:8080"
    restart: unless-stopped

  cors-proxy:
    image: myregistry.com/bamoe-cors-proxy-custom:v1.0.0
    ports:
      - "8081:8080"
    restart: unless-stopped

  extended-services:
    image: myregistry.com/bamoe-extended-services-custom:v1.0.0
    ports:
      - "21345:21345"
    restart: unless-stopped
```
Start your environment:
```bash
docker-compose up -d
```

#### Scenario 2: Using in OpenShift Deployments

For OpenShift, update your DeploymentConfig to use custom images:

```yaml
apiVersion: apps.openshift.io/v1
kind: DeploymentConfig
metadata:
  name: bamoe-canvas
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: canvas
        image: myregistry.com/bamoe-canvas-custom:v1.0.0
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
  triggers:
  - type: ConfigChange
  - type: ImageChange
    imageChangeParams:
      automatic: true
      containerNames:
      - canvas
      from:
        kind: ImageStreamTag
        name: bamoe-canvas-custom:v1.0.0
```
Deploy to OpenShift:
```bash
oc apply -f deployment-config.yaml
```

### Method 2: Using with Helm Charts

BAMOE provides Helm charts as OCI artifacts that you can pull from the registry and use to deploy your custom images.

#### Development Environment Helm Chart

The Development Environment Helm chart deploys multiple BAMOE images together (Canvas, CORS Proxy, Extended Services, Maven Repository).

**Pull the Helm chart:**

```bash
helm pull oci://quay.io/bamoe/pamoe-dev-environment-helm-chart --version 9.1.0
```

**Create a custom values file** (`custom-dev-values.yaml`):

```yaml
# Override Canvas image
canvas:
  image:
    registry: myregistry.com
    account: ""
    name: bamoe-canvas-custom
    tag: v1.0.0

# Override CORS Proxy image
corsProxy:
  image:
    registry: myregistry.com
    account: ""
    name: bamoe-cors-proxy-custom
    tag: v1.0.0

# Override Extended Services image
extendedServices:
  image:
    registry: myregistry.com
    account: ""
    name: bamoe-extended-services-custom
    tag: v1.0.0

# Override Maven Repository image
mavenRepository:
  image:
    registry: myregistry.com
    account: ""
    name: bamoe-maven-repository-custom
    tag: v1.0.0

# Image pull secrets for private registry
imagePullSecrets:
  - name: myregistry-secret
```
**Install the development environment:**
```bash
helm install bamoe-dev oci://quay.io/bamoe/pamoe-dev-environment-helm-chart \
  --version 9.1.0 \
  -f custom-dev-values.yaml \
  --namespace bamoe-dev \
  --create-namespace
```

#### Runtime Environment Helm Chart

For production runtime environments with Management Console and MCP Server:

**Pull the Helm chart:**

```bash
helm pull oci://quay.io/bamoe/pamoe-runtime-environment-helm-chart --version 9.1.0
```

**Create a custom values file** (`custom-runtime-values.yaml`):

```yaml
# Override Management Console image
managementConsole:
  image:
    registry: myregistry.com
    account: ""
    name: bamoe-management-console-custom
    tag: v1.0.0

# Override MCP Server image
mcpServer:
  image:
    registry: myregistry.com
    account: ""
    name: bamoe-mcp-server-custom
    tag: v1.0.0
  env:
    - name: MCP_SERVER_OPENAPI_URLS
      value: "http://my-business-service:8080/q/openapi"

# Image pull secrets
imagePullSecrets:
  - name: myregistry-secret
```
**Install the runtime environment:**
```bash
helm install bamoe-runtime oci://quay.io/bamoe/pamoe-runtime-environment-helm-chart \
  --version 9.1.0 \
  -f custom-runtime-values.yaml \
  --namespace bamoe-runtime \
  --create-namespace
```

### Overall process summary

1. **Choose Your Scenario**: Identify which override scenario applies to your needs
2. **Select Your Images**: Determine which BAMOE images need customization
3. **Build Custom Images**: Follow the patterns in this guide to create your custom images
4. **Test Thoroughly**: Complete the testing checklist before production deployment
5. **Deploy**: Use Docker Compose or Helm charts to deploy your custom images
6. **Monitor**: Continuously monitor your custom images in production
7. **Maintain**: Keep your custom images updated with security patches and BAMOE releases

## Conclusion

This guide provides comprehensive instructions for overriding base images in BAMOE container images. By following the patterns and best practices outlined here, you can successfully customize BAMOE images to meet your organization's requirements while maintaining functionality and supportability.

### Additional Resources

- **BAMOE Documentation**: Official BAMOE product documentation
- **Image-Specific Guides**: Detailed guides in `https://quay.io/organization/bamoe`
- **Helm Charts**: BAMOE Helm charts at `oci://quay.io/bamoe/pamoe-*-helm-chart`

---