package com.blackwalnutsoftware.andrew;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackwalnutsoftware.andrew.dataset.CSVLoader;
import com.blackwalnutsoftware.andrew.dataset.LinearDataset;
import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.ElementHelper;
import com.blackwalnutsoftware.kgraph.engine.Node;

import nl.altindag.log.LogCaptor;

public class ThoughtTest {

   private static Logger logger = LogManager.getLogger(ThoughtTest.class);

   private static Mutator mutator = null;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      String databaseName = "andrew_test_database";
      App.initialize(databaseName);
      App.getGardenGraph().flush();
      App.getDataGraph().flush();
      App.loadDatasetToDataGraph(new LinearDataset());
      mutator = new Mutator();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Test
   public void forecast2_nullEdgeInput_errorMessage() throws Exception {
      // see: https://stackoverflow.com/a/63546258/2418261
      // see: https://github.com/Hakky54/log-captor

      LogCaptor logCaptor = LogCaptor.forClass(Thought.class);

      Thought thought = TestHelper.buildModifiedInitialTestThought();

      Map<String, Object> workingMemory = new HashMap<>();
      try {
         thought.forecast2(workingMemory);
      } catch (Exception e) {
         // expected. ignore.
      }

      Boolean msgFound = false;

      List<String> errorMessages = logCaptor.getErrorLogs();
      for (String errorMessage : errorMessages) {
         if (errorMessage.contains("Edge input value is NULL for")) {
            msgFound = true;
            break;
         }
      }

      assert (true == msgFound);

   }

   @Test
   public void forecast_multiplyThought_success() throws Exception {

      Float multiplier = 1.04f;

      Thought thought = TestHelper.buildMultiplicationThought(multiplier);

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> workingMemory = generateLegacyDataWorkingMemory(thought, startingNode);
      Map<String, Object> result = thought.forecast2(workingMemory);

      assert (null != result);
      logger.debug("result = " + result);

      Number guess = (Number) result.get("RESULT.output");
      logger.debug("guess = " + guess);

      Number initialValue = (Number) startingNode.getAttribute("value");
      logger.debug("initialValue = " + initialValue);

      assert (Math.abs(guess.floatValue() - (initialValue.floatValue() * multiplier)) < 0.5) : "{" + guess + ", " + initialValue + "}";

   }

   @Test(expected = RuntimeException.class)
   public void forecast_invalidNodeType_exception() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThoughtWithBadNode();

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> workingMemory = generateLegacyDataWorkingMemory(thought, startingNode);
      thought.forecast2(workingMemory);
   }

   @Test(expected = RuntimeException.class)
   public void forecast_noEnd_exception() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThoughtWithNoEnd();

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> workingMemory = generateLegacyDataWorkingMemory(thought, startingNode);
      thought.forecast2(workingMemory);
   }

   @Test
   public void getOperationsByMaxLayer_initialTestThought_success() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThought();

      List<Set<Node>> opsByLayer = thought.getOperationsByMaxLayer();

      assert (null != opsByLayer);
      assert (5 == opsByLayer.size()) : opsByLayer;

      Set<Node> layerTwo = opsByLayer.get(2);
      assert (1 == layerTwo.size()) : "size = " + layerTwo.size() + ", contents = " + layerTwo;
   }

   @Test
   public void clone_goodThought_getClone() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThought();
      Thought clonedThought = thought.clone();

      assert (null != thought);
      assert (null != clonedThought);

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> workingMemory1 = generateLegacyDataWorkingMemory(thought, startingNode);
      Map<String, Object> origResult = thought.forecast2(workingMemory1);

      Map<String, Object> workingMemory2 = generateLegacyDataWorkingMemory(clonedThought, startingNode);
      Map<String, Object> cloneResult = clonedThought.forecast2(workingMemory2);

      Number origGuess = (Number) origResult.get("RESULT.output");
      Number cloneGuess = (Number) cloneResult.get("RESULT.output");

      assert (Math.abs(origGuess.floatValue() - cloneGuess.floatValue()) < .01) : origGuess + ", " + cloneGuess;
   }

   @Test(expected = RuntimeException.class)
   public void clone_noEnd_getException() throws Exception {
      Thought thought = TestHelper.buildModifiedInitialTestThoughtWithNoEnd();
      thought.clone();
   }

   @Test(expected = RuntimeException.class)
   public void clone_noResult_getException() throws Exception {
      String thoughtKey = ElementHelper.generateKey();
      final Node n1 = new Node(thoughtKey, "thought");
      n1.addAttribute("name", "n1");
      final Node goalNode = new Node(ElementHelper.generateKey(), "goal");
      Edge e0 = new Edge(ElementHelper.generateKey(), goalNode, n1, "approach");
      e0.addAttribute("thought_key", n1.getKey());
      e0.addAttribute("name", "e0");
      App.getGardenGraph().upsert(goalNode, n1, e0);

      Thought thought = new Thought(n1);
      thought.clone();
   }

   @Test(expected = RuntimeException.class)
   public void clone_noApproach_getException() throws Exception {
      String thoughtKey = ElementHelper.generateKey();
      final Node n1 = new Node(thoughtKey, "thought");
      n1.addAttribute("name", "n1");
      final Node goalNode = new Node(ElementHelper.generateKey(), "goal");
      Edge e0 = new Edge(ElementHelper.generateKey(), goalNode, n1, "approach");
      e0.addAttribute("thought_key", n1.getKey());
      e0.addAttribute("name", "e0");
      App.getGardenGraph().upsert(goalNode, n1, e0);

      Thought thought = new Thought(n1);

      e0.addAttribute("thought_key", "bad");
      App.getGardenGraph().upsert(e0);

      thought.clone();
   }

   @Test
   public void createMutant_goodStartingThought_resultImpacted() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThought();
      Thought clonedThought = thought.clone();

      logger.debug("thought.exportJson() = " + thought.exportJson());
      logger.debug("clonedThought.exportJson() = " + clonedThought.exportJson());

      assert (null != thought);
      assert (null != clonedThought);

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");
      logger.debug("startingNode = " + startingNode);

      Map<String, Object> workingMemory1 = generateLegacyDataWorkingMemory(thought, startingNode);
      Map<String, Object> origResult = thought.forecast2(workingMemory1);

      Map<String, Object> workingMemory2 = generateLegacyDataWorkingMemory(clonedThought, startingNode);
      Map<String, Object> cloneResult = clonedThought.forecast2(workingMemory2);

      Number origGuess = (Number) origResult.get("RESULT.output");
      Number cloneGuess = (Number) cloneResult.get("RESULT.output");

      assert (Math.abs(origGuess.floatValue() - cloneGuess.floatValue()) < .01) : origGuess + ", " + cloneGuess;

      Boolean pass = false;
      for (int i = 0; i < 10; i++) {
         Thought mutatedClonedThought = mutator.createMutant(clonedThought, 2);

         Map<String, Object> workingMemory3 = generateLegacyDataWorkingMemory(mutatedClonedThought, startingNode);
         Map<String, Object> mutatedCloneResult = mutatedClonedThought.forecast2(workingMemory3);

         Number mutatedCloneGuess = (Number) mutatedCloneResult.get("RESULT.output");

         if (Math.abs(origGuess.floatValue() - mutatedCloneGuess.floatValue()) > .00001) {
            pass = true;
            break;
         }
      }

      /* 
       * Note: If the following assertion fails, run it again.  There is a small chance 
       * that the mutation is so small as to not be detected.
       */
      assert (true == pass);
   }

   @Test
   public void createMutant_goodStartingThoughtList_resultsImpacted() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThought();
      Thought clonedThought = thought.clone();

      logger.debug("thought.exportJson() = " + thought.exportJson());
      logger.debug("clonedThought.exportJson() = " + clonedThought.exportJson());

      assert (null != thought);
      assert (null != clonedThought);

      List<Thought> thoughtList = new ArrayList<>();
      thoughtList.add(thought);
      thoughtList.add(clonedThought);

      List<Thought> mutatedClonedThoughts = mutator.createMutant(thoughtList, 2);

      assert (2 == mutatedClonedThoughts.size()) : mutatedClonedThoughts;

      String thoughtJson = thought.exportJson();
      String clonedThoughtJson = clonedThought.exportJson();
      String m0 = mutatedClonedThoughts.get(0).exportJson();
      String m1 = mutatedClonedThoughts.get(1).exportJson();

      assert (null != thoughtJson);
      assert (null != clonedThoughtJson);
      assert (null != m0);
      assert (null != m1);

      assert (thoughtJson.length() > 0);
      assert (clonedThoughtJson.length() > 0);
      assert (m0.length() > 0);
      assert (m1.length() > 0);

      assert (!thoughtJson.equals(m0)) : thoughtJson;
      assert (!clonedThoughtJson.equals(m1)) : clonedThoughtJson;
   }

   @Test
   public void crossover_twoThoughts_combinedThought() throws Exception {

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Thought thought1 = TestHelper.buildModifiedInitialTestThought();
      Thought thought2 = TestHelper.buildMultiplicationThought(thought1.getGoalNode(), 1.5f);

      Crossover crossover = new SimpleCrossover();
      Thought childThought = crossover.crossover(thought1, thought2);

      Map<String, Object> workingMemory1 = generateLegacyDataWorkingMemory(thought1, startingNode);
      Map<String, Object> t1Result = thought1.forecast2(workingMemory1);

      Map<String, Object> workingMemory2 = generateLegacyDataWorkingMemory(thought2, startingNode);
      Map<String, Object> t2Result = thought2.forecast2(workingMemory2);

      logger.debug("### Start: Forcasting the Child Thought ###");
      Map<String, Object> workingMemory3 = generateLegacyDataWorkingMemory(childThought, startingNode);
      Map<String, Object> t3Result = childThought.forecast2(workingMemory3);
      Number t1Guess = (Number) t1Result.get("RESULT.output");
      Number t2Guess = (Number) t2Result.get("RESULT.output");
      Number t3Guess = (Number) t3Result.get("RESULT.output");

      assert (Math.abs(t1Guess.floatValue() - t2Guess.floatValue()) > .00001) : t1Guess + ", " + t2Guess;
      assert (Math.abs(t1Guess.floatValue() - t3Guess.floatValue()) > .00001) : t1Guess + ", " + t3Guess;
      assert (Math.abs(t2Guess.floatValue() - t3Guess.floatValue()) > .00001) : t2Guess + ", " + t3Guess;

      logger.debug("t1Guess = " + t1Guess.floatValue());
      logger.debug("t2Guess = " + t2Guess.floatValue());
      logger.debug("t3Guess = " + t3Guess.floatValue());

      assert (Math.abs(t1Guess.floatValue() + t2Guess.floatValue() - (2 * t3Guess.floatValue())) < .00001);
   }

   @Test
   public void crossover_twoCrossoverThoughts_combinedThought() throws Exception {

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Thought thought1 = TestHelper.buildModifiedInitialTestThought();
      Thought thought2 = TestHelper.buildMultiplicationThought(thought1.getGoalNode(), 1.5f);
      Thought thought3 = TestHelper.buildMultiplicationThought(thought1.getGoalNode(), 3.5f);

      Crossover crossover = new SimpleCrossover();
      Thought child1Thought = crossover.crossover(thought1, thought2);
      Thought child2Thought = crossover.crossover(thought1, thought3);
      Thought grandchildThought = crossover.crossover(child1Thought, child2Thought);

      Map<String, Object> workingMemory1 = generateLegacyDataWorkingMemory(thought1, startingNode);
      Map<String, Object> t1Result = thought1.forecast2(workingMemory1);

      Map<String, Object> workingMemory2 = generateLegacyDataWorkingMemory(thought2, startingNode);
      Map<String, Object> t2Result = thought2.forecast2(workingMemory2);

      Map<String, Object> workingMemory3 = generateLegacyDataWorkingMemory(child1Thought, startingNode);
      Map<String, Object> t3Result = child1Thought.forecast2(workingMemory3);

      Map<String, Object> workingMemory4 = generateLegacyDataWorkingMemory(child2Thought, startingNode);
      Map<String, Object> t4Result = child2Thought.forecast2(workingMemory4);

      Map<String, Object> workingMemory5 = generateLegacyDataWorkingMemory(grandchildThought, startingNode);
      Map<String, Object> grandchildResult = grandchildThought.forecast2(workingMemory5);

      Number t1Guess = (Number) t1Result.get("RESULT.output");
      Number t2Guess = (Number) t2Result.get("RESULT.output");
      Number t3Guess = (Number) t3Result.get("RESULT.output");
      Number t4Guess = (Number) t4Result.get("RESULT.output");
      Number grandchildGuess = (Number) grandchildResult.get("RESULT.output");

      logger.debug("t1Guess = " + t1Guess.floatValue());
      logger.debug("t2Guess = " + t2Guess.floatValue());
      logger.debug("t3Guess = " + t3Guess.floatValue());
      logger.debug("t4Guess = " + t4Guess.floatValue());
      logger.debug("grandchildGuess = " + grandchildGuess.floatValue());

      assert (Math.abs(t3Guess.floatValue() + t4Guess.floatValue() - (2 * grandchildGuess.floatValue())) < .00001);
   }

   @Test
   public void exportJson_goodThought_goodJson() throws Exception {

      Thought thought1 = TestHelper.buildModifiedInitialTestThought();
      Thought thought2 = TestHelper.buildMultiplicationThought(thought1.getGoalNode(), 1.5f);

      Crossover crossover = new SimpleCrossover();
      Thought child1Thought = crossover.crossover(thought1, thought2);

      String jsonString = child1Thought.exportJson();

      assert (null != jsonString);

      logger.debug("jsonString = " + jsonString);

      assert (jsonString.contains("\"properties\" : {"));
      assert (jsonString.contains("\"name\" : \"n1\","));
      assert (jsonString.contains("\"_id\" : \"thought_operation/KEY_"));
      assert (jsonString.contains("\"_id\" : \"approach/KEY_"));
      assert (jsonString.contains("\"_id\" : \"thought_sequence/KEY_"));
      assert (jsonString.contains("\"_id\" : \"goal/KEY_"));

      String dirString = "./target/exports/";
      String fileString = dirString + "exportJson_goodThought_goodJson.json";
      Path path = Paths.get(dirString);
      Files.createDirectories(path);
      try (PrintWriter out = new PrintWriter(fileString)) {
         out.println(jsonString);
      }
   }

   @Test
   public void exportJson_linearGrowthThought_goodJson() throws Exception {

      Thought thought1 = TestHelper.buildModifiedInitialTestThought();

      String jsonString = thought1.exportJson();

      assert (null != jsonString);

      String dirString = "./target/exports/";
      String fileString = dirString + "linear_growth_thought.json";
      Path path = Paths.get(dirString);
      Files.createDirectories(path);
      try (PrintWriter out = new PrintWriter(fileString)) {
         out.println(jsonString);
      }
   }

   @Test
   public void exportDot_goodThought_goodDot() throws Exception {

      Thought thought1 = TestHelper.buildModifiedInitialTestThought();
      Thought thought2 = TestHelper.buildMultiplicationThought(thought1.getGoalNode(), 1.5f);

      Crossover crossover = new SimpleCrossover();
      Thought child1Thought = crossover.crossover(thought1, thought2);

      String dotString = child1Thought.exportDot();

      assert (null != dotString);

      logger.debug("dotString = " + dotString);

      assert (dotString.contains("digraph G {")) : dotString;
      assert (dotString.contains("node [shape=record fontname=Arial];")) : dotString;
      assert (dotString.contains("\\\"\\lname = \\\"n1\\\"")) : dotString;
      assert (dotString.contains("\\l__type = \\\"thought\\\"")) : dotString;
      assert (dotString.contains("\\lseedThought = false")) : dotString;
      assert (dotString.contains(" [label=\"type = \\\"thought_operation\\\"\\lid = \\\"thought_operation/KEY_")) : dotString;
      assert (dotString.contains("[shape=oval label=\"type = \\\"thought_sequence\\\"\\lid = \\\"thought_sequence/KEY_")) : dotString;
      assert (dotString.contains(" -> thought_sequence_KEY_")) : dotString;
      assert (dotString.contains(" -> thought_operation_KEY_")) : dotString;
      assert (dotString.contains(" [label=\"type = \\\"thought_result\\\"\\lid = \\\"thought_result/KEY_")) : dotString;

      String dirString = "./target/exports/";
      String fileString = dirString + "exportDot_goodThought_goodDot.dot";
      Path path = Paths.get(dirString);
      Files.createDirectories(path);
      try (PrintWriter out = new PrintWriter(fileString)) {
         out.println(dotString);
      }
   }

   @Test
   public void importJson_goodThought_loaded() throws Exception {
      // hint: use andrew\java-project\src\test\resources/test_load_data.json
      String filePath = "./src/test/resources/test_load_data.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));

      Thought t = App.loadThoughtFromJson("test_load_data", content);

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> workingMemory = generateLegacyDataWorkingMemory(t, startingNode);
      Map<String, Object> result = t.forecast2(workingMemory);

      Number guess = (Number) result.get("RESULT.output");

      assert (guess.intValue() == 641) : "guess = " + guess;
   }

   @Test
   public void importJson_linearGrowthThought_loaded() throws Exception {
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));

      Thought t = App.loadThoughtFromJson("linear_growth_thought", content);

      // TODO: modify the goal to specify the startDate and symbol

      // load the test data
      // flushing should not be necessary
      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/stock_data_fetcher/data/AACG_2020-05-07.txt"));
      files.add(new File("../fetchers/stock_data_fetcher/data/AAPL_2020-05-07.txt"));
      CSVLoader stockLoader = new CSVLoader();
      stockLoader.loadStocks(files);

      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> workingMemory = generateLegacyDataWorkingMemory(t, startingNode);
      // 14614 ~= Jan 5, 2010
      workingMemory.put("GOAL.startDate", 14614);
      workingMemory.put("GOAL.symbol", "AAPL");
      Map<String, Object> result = t.forecast2(workingMemory);

      Number guess = (Number) result.get("RESULT.output");

      assert (guess.intValue() - 42.93 < 0.01) : "guess = " + guess;
   }

   @Test
   public void forecast2_valid_success() throws Exception {

      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought thought = App.loadThoughtFromJson("linear_growth_thought", content);

      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/stock_data_fetcher/data/AAPL_2020-05-07.txt"));
      CSVLoader stockLoader = new CSVLoader();
      stockLoader.loadStocks(files);

      // populate starting conditions
      Map<String, Object> workingMemory = new HashMap<>();
      workingMemory = thought.addContext(workingMemory, "symbol", "AAPL", "GOAL");
      workingMemory = thought.addContext(workingMemory, "distance", 90, "GOAL");
      workingMemory = thought.addContext(workingMemory, "targetProperty", "dayClose", "GOAL");

      // 14614 ~= Jan 5, 2010
      workingMemory = thought.addContext(workingMemory, "startDate", 14614, "GOAL");

      logger.debug("workingMemory = " + workingMemory);

      Map<String, Object> result = thought.forecast2(workingMemory);

      assert (null != result);
      logger.debug("result = " + result);

      Number guess = (Number) result.get("RESULT.output");

      assert (Math.abs(guess.floatValue() - 34.072853) < 0.01) : guess;
   }

   public Map<String, Object> generateLegacyDataWorkingMemory(Thought thought, Node startingNode) {
      String targetPropName = (String) thought.getGoalNode().getAttribute("targetProperty");
      Object startingTargetPropValue = startingNode.getAttribute(targetPropName);
      Integer distance = (Integer) thought.getGoalNode().getAttribute("distance");
      String direction = (String) thought.getGoalNode().getAttribute("direction");
      String relationType = (String) thought.getGoalNode().getAttribute("relationType");

      Map<String, Object> workingMemory = new HashMap<>();
      workingMemory = thought.addContext(workingMemory, "startingNode", startingNode, "GOAL");
      workingMemory = thought.addContext(workingMemory, startingNode.getProperties(), startingNode.getKey());
      workingMemory = thought.addContext(workingMemory, thought.getGoalNode().getProperties(), "GOAL");
      workingMemory = thought.addContext(workingMemory, "targetPropValue", startingTargetPropValue, thought.getThoughtNode().getKey());
      workingMemory = thought.addContext(workingMemory, "distance", distance, thought.getThoughtNode().getKey());
      workingMemory = thought.addContext(workingMemory, "direction", direction, thought.getThoughtNode().getKey());
      workingMemory = thought.addContext(workingMemory, "startingNode", startingNode, thought.getThoughtNode().getKey());
      workingMemory = thought.addContext(workingMemory, "relationType", relationType, thought.getThoughtNode().getKey());

      return workingMemory;
   }

   @Test
   public void addGoalAttributes_newAttr_added() throws Exception {
      Thought thought = TestHelper.buildModifiedInitialTestThought();
      Map<String, Object> workingMemory = new HashMap<>();
      Node goal = new Node(ElementHelper.generateKey(), "goal");
      goal.addAttribute("foo", "bar");
      workingMemory = thought.addGoalAttributes(workingMemory, goal);

      assert ("bar".equals(workingMemory.get("GOAL.foo"))) : workingMemory;
   }

   @Test
   public void createCrossovers_twoThoughts_combinedThought() throws Exception {

      Thought thought1 = TestHelper.buildModifiedInitialTestThought();
      Thought thought2 = TestHelper.buildMultiplicationThought(thought1.getGoalNode(), 1.5f);
      List<Thought> inputs = new ArrayList<>();
      inputs.add(thought1);
      inputs.add(thought2);

      int thought1Length = thought1.exportJson().split("\r\n|\r|\n").length;
      int thought2Length = thought2.exportJson().split("\r\n|\r|\n").length;

      Crossover crossover = new SimpleCrossover();
      List<Thought> childThoughts = crossover.createCrossovers(inputs);

      assert (2 == childThoughts.size()) : childThoughts;

      int childThought1Length = childThoughts.get(0).exportJson().split("\r\n|\r|\n").length;
      int childThought2Length = childThoughts.get(1).exportJson().split("\r\n|\r|\n").length;

      assert (thought1Length < childThought1Length);
      assert (thought2Length < childThought2Length);
   }

   @Test
   public void getKey_goodThought_hasKey() throws Exception {

      Thought thought = TestHelper.buildModifiedInitialTestThought();

      String key = thought.getKey();
      assert (key.length() > 1) : key;
   }
}
