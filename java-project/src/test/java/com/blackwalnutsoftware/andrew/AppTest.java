package com.blackwalnutsoftware.andrew;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackwalnutsoftware.andrew.dataset.CSVLoader;
import com.blackwalnutsoftware.andrew.dataset.LinearDataset;
import com.blackwalnutsoftware.andrew.process.ThoughtScore;
import com.blackwalnutsoftware.kgraph.engine.Node;

/**
 * Unit test for simple App.
 */
public class AppTest {

   private static Logger logger = LogManager.getLogger(AppTest.class);
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      logger.debug("begin AppTest.setUpBeforeClass()");
      App.initialize("linear_dataset_tests");
      App.getDataGraph().flush();
      App.getGardenGraph().flush();
      App.loadDatasetToDataGraph(new LinearDataset());
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      App.getDataGraph().cleanup();
      App.getGardenGraph().flush();
   }

   @Test
   public void getGarderGraphConnection_valid_success() throws Exception {

      Long graphCount = App.getGardenGraph().getTotalCount();
      assert (0 == graphCount) : "graphCount = " + graphCount;
   }

   @Test
   public void load_datasetNotpreviouslyLoaded_loaded() throws Exception {
      Long graphCount = App.getDataGraph().getTotalCount();

      // 1000 nodes, 999 edges, 1 dataSet_info node
      assert (graphCount == 2000) : "graphCount = " + graphCount;
   }

   @Test
   public void load_datasetPreviouslyLoaded_loaded() throws Exception {
      App.getDataGraph().flush();
      App.loadDatasetToDataGraph(new LinearDataset());
      Long graphCount1 = App.getDataGraph().getTotalCount();

      // 1000 nodes, 999 edges, 1 dataSet_info node
      assert (graphCount1 == 2000) : "graphCount1 = " + graphCount1;
      Long graphCount2 = App.getDataGraph().getTotalCount();

      // 1000 nodes, 999 edges, 1 dataSet_info node
      assert (graphCount1.equals(graphCount2)) : "{graphCount1, graphCount2} = " + graphCount1 + ", " + graphCount2;
   }

   @Test(expected = RuntimeException.class)
   public void load_doubleLoaded_exception() throws Exception {
      App.getDataGraph().flush();
      LinearDataset dataset = new LinearDataset();
      Node datasetInfoNode1 = new Node("firstNode", "dataSet_info");
      datasetInfoNode1.addAttribute("dataset_id", dataset.getDatasetInfoID());

      Node datasetInfoNode2 = new Node("secondNode", "dataSet_info");
      datasetInfoNode2.addAttribute("dataset_id", dataset.getDatasetInfoID());

      App.getDataGraph().upsert(datasetInfoNode1, datasetInfoNode2);
      App.loadDatasetToDataGraph(dataset);
   }

   @Test
   public void train_happyPath_results() throws Exception {
      long startTime = System.currentTimeMillis();
      
      // load the test data
      CSVLoader stockLoader = new CSVLoader();
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
      TrainingParameters trainingParameters = new TrainingParameters(trainingParametersMap);


      goal.setProperty("targetDistance", 20);
      goal.setProperty("targetRel", "stockOnDate");
      goal.setProperty("otherSidePrefix", "stockSymbol/");
      
      Integer maturationAge = 3;
      Integer maxPopulation = 10;
      Integer numGenerations = 12;  //2
      Integer questsPerGeneration = 2;  //2

      TrainingCriteria trainingCriteria = new TrainingCriteria(numGenerations, questsPerGeneration, startDate, endDate, maturationAge, maxPopulation);
      
      List<ThoughtScore> scores = App.train(goal, trainingParameters, trainingCriteria);

      // compare the thoughts for times in the future
      
      long endTime = System.currentTimeMillis();
      long duration = (endTime - startTime)/1000;
      logger.info("train_happyPath_results.duration = " + duration + " seconds");

      logger.info("train_happyPath_results.scores = " + scores);
      
      assert (scores.size() > 10) : scores.size();
   }
   
   @Test
   public void calculateAverage_empty_zero() throws Exception { 
      List<Float> scores = new ArrayList<>();
      Float result = App.calculateAverage(scores);
      assert (result == 0f) : result;
   }
   
   @Test(expected = AssertionError.class)
   public void getRandom_empty_zero() throws Exception { 
      List<Float> scores = new ArrayList<>();
      App.getRandomMember(scores);
   }
   
   @Test
   public void train_dupScores_mergedScores() throws Exception {
      long startTime = System.currentTimeMillis();
      
      // load the test data
      CSVLoader stockLoader = new CSVLoader();
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
      stockLoader.loadStocks(files);
      
      // load the seed thought
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought rootThought = App.loadThoughtFromJson("linear_growth_thought", content);
      Goal goal = rootThought.getGoal();
      
      // clone the seed thought
      rootThought.clone();
      
      
      LocalDate startDate = LocalDate.parse("1990-01-01");
      LocalDate endDate = LocalDate.parse("2020-01-01");

      Map<String, List<Object>> trainingParametersMap = new HashMap<>();
      List<Object> symbolValues = new ArrayList<>();
      symbolValues.add("AIG");
      symbolValues.add("AAPL");
      symbolValues.add("AMAT");
      trainingParametersMap.put("symbol", symbolValues);
      TrainingParameters trainingParameters = new TrainingParameters(trainingParametersMap);


      goal.setProperty("targetDistance", 20);
      goal.setProperty("targetRel", "stockOnDate");
      goal.setProperty("otherSidePrefix", "stockSymbol/");
      
      Integer maturationAge = 1;
      Integer maxPopulation = 3;
      Integer numGenerations = 4;  //2
      Integer questsPerGeneration = 1;  //2

      TrainingCriteria trainingCriteria = new TrainingCriteria(numGenerations, questsPerGeneration, startDate, endDate, maturationAge, maxPopulation);
      
      List<ThoughtScore> scores = App.train(goal, trainingParameters, trainingCriteria);

      // compare the thoughts for times in the future
      
      long endTime = System.currentTimeMillis();
      long duration = (endTime - startTime)/1000;
      logger.info("train_happyPath_results.duration = " + duration + " seconds");

      logger.info("train_happyPath_results.scores = " + scores);
      
      assert (scores.size() > 10) : scores;
   }
}
