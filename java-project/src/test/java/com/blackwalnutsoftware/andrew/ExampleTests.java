package com.blackwalnutsoftware.andrew;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackwalnutsoftware.andrew.dataset.CSVLoader;
import com.blackwalnutsoftware.andrew.process.Report;
import com.blackwalnutsoftware.andrew.process.ThoughtScore;

public class ExampleTests {

   private static Logger logger = LogManager.getLogger(ExampleTests.class);
   private static Level defaultLevel;
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      String databaseName = "examples_test_database";
      App.initialize(databaseName);
      App.getGardenGraph().flush();
      App.getDataGraph().flush();
      
      Logger rootLogger = LogManager.getRootLogger();
      defaultLevel = rootLogger.getLevel();
      LogManager.getRootLogger().atLevel(Level.WARN);
      
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      App.getDataGraph().cleanup();
      App.getGardenGraph().cleanup();
      LogManager.getRootLogger().atLevel(defaultLevel);
   }

   @Test
   public void train_happyPath_results() throws Exception {
      long startTime = System.currentTimeMillis();
      Report.clear();
      Report.setName("train_happyPath_results");
      
      // load the test data
      List<File> files = new ArrayList<>();
      List<String> tickers = new ArrayList<>();
      String baseDir = "../fetchers/stock_data_fetcher/data/";
      File f = new File(baseDir);
      String[] baseFileNames = f.list();
      for (String baseFileName : baseFileNames) {
         if(baseFileName.startsWith("AAPL_") || baseFileName.startsWith("AIG_") || baseFileName.startsWith("AMAT_")) {
            String filePath = baseDir + baseFileName;
            String ticker = baseFileName.substring(0, baseFileName.indexOf("_")-1);
            tickers.add(ticker);
            files.add(new File(filePath));
            logger.debug("adding ticker " + ticker + "(" + baseFileName + ")");
         }
      }
      CSVLoader stockLoader = new CSVLoader();
      stockLoader.loadStocks(files);
      
      // load the seed thought
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought rootThought = App.loadThoughtFromJson("linear_growth_thought", content);
      Goal goal = rootThought.getGoal();
      
      
      LocalDate startDate = LocalDate.parse("1990-01-01");
      LocalDate endDate = LocalDate.parse("2020-01-01");

      Map<String, List<Object>> trainingParametersMap = new HashMap<>();
      List<Object> symbolValues = new ArrayList<>();
      symbolValues.add("AIG");
      symbolValues.add("AAPL");
      symbolValues.add("AMAT");
      trainingParametersMap.put("symbol", symbolValues);
      trainingParametersMap.put("targetDistance", Arrays.asList(182)); // ~six months & divisible by 7
      trainingParametersMap.put("targetRel", Arrays.asList("stockOnDate"));
      trainingParametersMap.put("otherSidePrefix", Arrays.asList("stockSymbol/"));
      
      TrainingParameters trainingParameters = new TrainingParameters(trainingParametersMap);
      
      Integer maturationAge = 2;
      Integer maxPopulation = 10;
      Integer numGenerations = 10;  //2
      Integer questsPerGeneration = 6;  //2

      TrainingCriteria trainingCriteria = new TrainingCriteria(numGenerations, questsPerGeneration, startDate, endDate, maturationAge, maxPopulation);
      
      List<ThoughtScore> scores = App.train(goal, trainingParameters, trainingCriteria);

      // compare the thoughts for times in the future
      
      long endTime = System.currentTimeMillis();
      long duration = (endTime - startTime)/1000;
      logger.info("train_happyPath_results.duration = " + duration + " seconds");
      
      Report.setElapsedSeconds(duration);
      Report.print();
      
      assert (scores.size() > 10) : scores.size();
   }

}
