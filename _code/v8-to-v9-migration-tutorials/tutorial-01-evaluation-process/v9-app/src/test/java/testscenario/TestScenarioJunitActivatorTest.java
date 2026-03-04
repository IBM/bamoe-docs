/**
* IBM Confidential
* PID 5900-AR4
* Copyright IBM Corp. 2025 
*/
package testscenario;

import org.drools.scenariosimulation.backend.runner.TestScenarioActivator;

/**
 * TestScenarioJunitActivator is a custom JUnit runner that enables the execution of Test Scenario files (*.scesim).
 * This activator class, when executed, will load all scesim files available in the project and run them.
 * Each row of the scenario will generate a test JUnit result.
 */
@TestScenarioActivator
public class TestScenarioJunitActivatorTest {

}