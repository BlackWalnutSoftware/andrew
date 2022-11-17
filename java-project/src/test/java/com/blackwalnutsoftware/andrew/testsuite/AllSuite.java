package com.blackwalnutsoftware.andrew.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.blackwalnutsoftware.andrew.AppTest;
import com.blackwalnutsoftware.andrew.AveragePercentageScoringMachineTest;
import com.blackwalnutsoftware.andrew.ExampleTests;
import com.blackwalnutsoftware.andrew.GoalTest;
import com.blackwalnutsoftware.andrew.MutatorTest;
import com.blackwalnutsoftware.andrew.OperationsTest;
import com.blackwalnutsoftware.andrew.SimpleCrossoverTest;
import com.blackwalnutsoftware.andrew.ThoughtTest;
import com.blackwalnutsoftware.andrew.dataset.DateUtilsTest;
import com.blackwalnutsoftware.andrew.loader.CSVLoaderTest;
import com.blackwalnutsoftware.andrew.loader.LinearDatasetTest;
import com.blackwalnutsoftware.andrew.loader.LoadPerformanceTest;
import com.blackwalnutsoftware.andrew.process.EvaluatorTest;
import com.blackwalnutsoftware.andrew.process.ProcessUtilsTest;
import com.blackwalnutsoftware.andrew.process.ThoughtScoreTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({ AppTest.class, LinearDatasetTest.class, ThoughtTest.class, EvaluatorTest.class, OperationsTest.class,
      AveragePercentageScoringMachineTest.class, CSVLoaderTest.class, LoadPerformanceTest.class, GoalTest.class, DateUtilsTest.class,
      ExampleTests.class, ThoughtScoreTest.class, MutatorTest.class, SimpleCrossoverTest.class, ProcessUtilsTest.class })

public class AllSuite {
}