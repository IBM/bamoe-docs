<!--
  IBM Confidential
  PID 5900-AR4
  Copyright IBM Corp. 2025
-->

# Evaluation Process - BAMOE v9 Application

> _This project was migrated from BAMOE v8 to v9 using the BAMOE Canvas Accelerator `Quarkus (jBPM with Drools)`, and enables Processes, Decisions and Rules. It's built on [Quarkus](https://quarkus.io/), the Supersonic Subatomic Java Framework._
>
> **NOTE**: This is a tutorial project demonstrating v8 to v9 migration. For complete migration instructions, see the [Tutorial README](../README.md).

# Description

Employee Performance Evaluation Process demonstrating BAMOE v9 capabilities:
- BPMN process with human tasks
- Parallel gateway for concurrent evaluations
- JDBC persistence with H2 (dev) / PostgreSQL (prod)
- Embedded services: Data Index, Jobs Service, Data Audit
- Stateful workflow support

# Building and running

### Prerequisites

- Java 17 or later
- Maven 3.8.1 or later

### In dev mode

```shell script
mvn clean quarkus:dev -Pdevelopment
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

Dev mode enables a number of helpful features while developing the project:

- Incremental compilation
- Live-reloading both in the browser and for Java code
- Automatic test execution
- OpenAPI specifications for HTTP endpoints
- Dev UI at http://localhost:8080/q/dev-ui
- Swagger UI at http://localhost:8080/q/swagger-ui

### As a JAR

```shell script
mvn clean package
```

This command produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The Business Service is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
mvn clean package -Dquarkus.package.type=uber-jar
```

The Business Service, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

### As a native executable

```shell script
mvn clean package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
mvn clean package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/*-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

If you are using Gradle instead of Maven, the `mvn clean package -Dnative` and `mvn clean package -Dnative -Dquarkus.native.container-build=true` Maven commands can be replaced with the equivalent Gradle command:

```shell
./gradlew clean build \
  -Dquarkus.native.enabled=true \
  -Dquarkus.package.jar.enabled=false
```

GraalVM is required for native builds. 

NOTE: Maven places all build artifacts in the `/target` directory, while Gradle places them in the `build/` directory.

---

# Testing the Process

### Start a Process Instance

```bash
curl -X POST http://localhost:8080/evaluation \
  -H "Content-Type: application/json" \
  -d '{
    "employee": "John Doe",
    "reason": "Annual Review",
    "performance": 0,
    "initiator": "manager@company.com"
  }'
```

### Query Process Instances

```bash
curl http://localhost:8080/evaluation
```

### Access Dev UI

Navigate to http://localhost:8080/q/dev-ui for:
- Process instance management
- Task management
- Data Index queries
- Jobs Service monitoring

---

### _Notes on provided code and how to evolve this Business Service_

> The `src/main/resources/application.properties` file contains the basic properties for the project, enabling:
>
> - CORS protection
> - OpenAPI Specifications
> - Swagger UI
> - JDBC persistence (H2 for dev, PostgreSQL for prod)
> - Embedded services (Data Index, Jobs Service, Data Audit)
> - Flyway database migrations
>
> Add any additional code, BAMOE resource files, and/or properties to their appropriate places following Apache Maven's standard project layout:
>
> - `src/main/java/`
>   - For Java production code.
> - `src/main/resources/`
>   - For production configuration files and Processes (`.bpmn`), Decisions (`.dmn`), Rules (`.drl`), Excel Decision Tables (`.xlsx`), and others.
> - `src/test/java/`
>   - For Java test code.
> - `src/test/resources/`
>   - For test configuration files and Test Scenarios (`.scesim`).
>
> For more information about BAMOE, please refer to [the official BAMOE Documentation](https://www.ibm.com/docs/en/ibamoe).

---

# Migration Tutorial

For complete step-by-step migration instructions from BAMOE v8 to v9, including:
- Migration scenarios
- Runtime fixes
- Testing guide
- Troubleshooting

Please see the [Tutorial README](../README.md).
