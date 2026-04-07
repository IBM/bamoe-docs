# BAMOE v8 to v9 Migration Tutorials

Comprehensive, hands-on tutorials for migrating IBM Business Automation Manager Open Edition (BAMOE) applications from version 8 to version 9.

## Overview

These tutorials provides practical, step-by-step migration guides covering common scenarios and advanced features. Each tutorial includes working code examples (both v8 and v9 versions) and addresses real-world migration challenges discovered during actual application migrations.

These tutorials complement the [official BAMOE v8 to v9 upgrade guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x) by providing concrete, hands-on examples.

## Repository Structure

```
v8-to-v9-upgrade-tutorials/
├── README.md (this file)
├── tutorial-01-evaluation-process/
│   ├── README.md (tutorial content)
│   ├── v8-app/ (original v8 application code)
│   └── v9-app/ (migrated v9 application code)
├── tutorial-02-mortgage-application/
├── tutorial-03-traffic-violation/
└── ... (13 tutorials total)
```

## Tutorial List

### Business Central Samples (3 tutorials)

1. **[Tutorial 1: Evaluation Process Migration](./tutorial-01-evaluation-process/README.md)**
   - Simple approval process with user tasks and gateways
   - Covers basic BPMN process migration

2. **[Tutorial 2: Mortgage Application Migration](./tutorial-02-mortgage-application/README.md)**
   - BPMN process with DRL rules integration
   - Demonstrates process and rules working together

3. **[Tutorial 3: Traffic Violation (DMN) Migration](./tutorial-03-traffic-violation/README.md)**
   - DMN decision table migration and testing
   - Shows decision service migration patterns

### Upgrade Guide Scenarios (1 tutorial)

4. **[Tutorial 4: Upgrade Guide Scenarios](./tutorial-04-upgrade-guide-scenarios/README.md)**
   - Process ID constraints (handling `-` and `.` characters)
   - javax → jakarta package migration
   - Variable Context replacement with alternative APIs

### Event Listeners (1 tutorial)

5. **[Tutorial 5: Event Listeners Migration](./tutorial-05-event-listeners/README.md)**
   - Process, task, and rule event listeners
   - Registration and configuration in v9

### Advanced Features (3 tutorials)

6. **[Tutorial 6: Process Variable Persistence](./tutorial-06-process-variable-persistence/README.md)**
   - Database schema changes and variable marshalling
   - Persistence configuration in v9

7. **[Tutorial 7: Human Task (Escalation & Reassignment)](./tutorial-07-human-task-escalation/README.md)**
   - Deadline handlers, escalation rules, and SLA monitoring
   - Delegation and dynamic task assignment

8. **[Tutorial 8: Email Notifications](./tutorial-08-email-notifications/README.md)**
   - Work item handlers and template-based notifications
   - Custom work item handler migration

### Custom Scenarios (4 tutorials)

9. **[Tutorial 9: Complex Data Models](./tutorial-09-complex-data-models/README.md)**
   - JPA entities with @Entity annotations and Hibernate validation
   - Data model migration patterns

10. **[Tutorial 10: Multi-Module Projects](./tutorial-10-multi-module-projects/README.md)**
    - Maven multi-module structure and dependency management
    - Project organization in v9

11. **[Tutorial 11: Database Integration & Async Processing](./tutorial-11-database-async-processing/README.md)**
    - JDBC connections and transaction management
    - Async work item handlers and callbacks

### Common Issues & Solutions (1 tutorial)

12. **[Tutorial 12: Common Migration Issues and Solutions](./tutorial-12-common-migration-issues/README.md)**
    - Comprehensive guide to known migration issues
    - Problem descriptions, solutions, and code examples
    - Quick reference for troubleshooting

## Tutorial Structure

Each tutorial follows a consistent 6-part format:

1. **Introduction** - What we're migrating and why this scenario matters
2. **Prerequisites** - What you need before starting (tools, knowledge, setup)
3. **Step-by-Step Migration** - Detailed instructions with code examples and explanations
4. **Testing** - How to verify the migration worked correctly
5. **Common Issues** - Problems you might encounter and their solutions
6. **Summary** - What was accomplished and key takeaways

## Getting Started

### Prerequisites

Before starting any tutorial, ensure you have:

- **BAMOE v9.x Maven repository** configured in your `settings.xml`
- **Java 17+** (required for BAMOE v9)
- **Maven 3.8.1+**
- **IDE**: Either BAMOE Canvas (web-based) or VS Code with BAMOE Developer Tools extension
- Basic understanding of BPMN, DMN, and/or DRL (depending on tutorial)

**Note:** You do NOT need BAMOE v8 installed. The tutorials provide v8 application code in the repository for reference, and you'll create new v9 applications using accelerators.

### Creating BAMOE v9 Projects with Accelerators

All tutorials use the BAMOE Canvas Quarkus accelerator to create v9 projects. You have two options:

#### Option A: Clone the Accelerator Directly (Recommended)

Use the one-liner command to clone the appropriate accelerator:

**For DMN projects:**
```bash
git clone git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git -b 9.3.1-ibm-0006-quarkus-dmn YOUR-PROJECT-NAME
cd YOUR-PROJECT-NAME
```

**For DRL (Rules) projects:**
```bash
git clone git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git -b 9.3.1-ibm-0006-quarkus-drl YOUR-PROJECT-NAME
cd YOUR-PROJECT-NAME
```

**Command breakdown:**
- `git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git` - The public IBM accelerator repository
- `-b 9.3.1-ibm-0006-quarkus-dmn` (or `-drl`) - The branch/tag for BAMOE 9.3.1
- `YOUR-PROJECT-NAME` - Your project directory name (customize as needed)

**Important:** Use the accelerator version that corresponds to your target BAMOE release:
- For BAMOE 9.3.1: `9.3.1-ibm-0006-quarkus-dmn` or `9.3.1-ibm-0006-quarkus-drl`
- For other versions, check available tags: `git ls-remote --tags git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git`

#### Option B: Use BAMOE Canvas Web Interface

Alternatively, create your project using the BAMOE Canvas web interface:

1. **Open BAMOE Canvas** in your browser
2. **Create or open your DMN/BPMN files** in Canvas
3. **Apply the accelerator:**
   - Click on the "Accelerators" menu
   - Select the appropriate accelerator (Quarkus DMN, Quarkus DRL, etc.)
   - Configure project settings (group ID, artifact ID, etc.)
4. **Download the generated project** as a ZIP file
5. **Extract and build:**
   ```bash
   unzip your-project.zip
   cd your-project
   ```

**Benefits of Option B:**
- Visual, web-based workflow
- No need for Git commands
- Immediate preview of your DMN/BPMN models
- Easy to share and collaborate

**Note:** Both options create the same project structure. Choose based on your workflow preference.

### How to Use These Tutorials

1. **Start with the basics**: If you're new to migration, begin with Tutorial 1 (Evaluation Process)
2. **Choose relevant tutorials**: Select tutorials that match your application's features
3. **Follow step-by-step**: Each tutorial is self-contained with complete instructions
4. **Test as you go**: Verify each step works before proceeding
5. **Reference common issues**: Check Tutorial 13 for quick troubleshooting

## Known Migration Issues

During tutorial development, we've identified and documented these common issues:

1. **Process IDs** - Can't use `-` or `.` characters; breaks code generation
2. **Java Packages** - Need to change `javax` to `jakarta`
3. **Context Variable** - Doesn't exist in v9 anymore
4. **Java Lists** - Must specify type (can't use `List` without `<Type>`)
5. **Boolean Methods** - Changed from `getSomeBoolean()` to `isSomeBoolean()`
6. **Database Entities** - `@Entity` annotation causes Hibernate validation errors
7. **Error Handling** - NPE in catch blocks
8. **Rules** - Can't mix legacy rules with new rule units
9. **Process Variables** - Must have types defined
10. **Null Values** - Prints `null$` in output
11. **Package Names** - Many packages renamed

See [Tutorial 13](./tutorial-13-common-migration-issues/README.md) for detailed solutions.

## Additional Resources

- [Official BAMOE v8 to v9 Upgrade Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)
- [BAMOE Documentation](https://www.ibm.com/docs/en/ibamoe)
- [Business Central Sample Applications](https://github.com/kiegroup/jbpm-playground)

## Support

For questions or issues:

1. Check [Tutorial 13: Common Migration Issues](./tutorial-13-common-migration-issues/README.md)
2. Review the [official upgrade guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)
3. Contact BAMOE support


---

**Note**: These tutorials are actively maintained and updated based on customer feedback and new migration patterns discovered over time.