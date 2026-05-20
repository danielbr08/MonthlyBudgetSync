package com.brosh.finance.monthlybudgetsync.automation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master instrumented test suite for CI and local automation runs.
 *
 * Run: ./gradlew connectedDebugAndroidTest
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LoginScreenEspressoTest.class,
        StabilityRegressionInstrumentedTest.class,
        com.brosh.finance.monthlybudgetsync.ProductionEdgeCasesInstrumentedTest.class,
        com.brosh.finance.monthlybudgetsync.MonthlyBudgetSyncInstrumentedTest.class
})
public class AppAutomationTestSuite {
}
