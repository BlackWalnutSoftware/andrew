package com.blackwalnutsoftware.andrew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arangodb.model.TraversalOptions.Direction;
import com.blackwalnutsoftware.andrew.dataset.CSVLoader;
import com.blackwalnutsoftware.andrew.dataset.LinearDataset;
import com.blackwalnutsoftware.kgraph.engine.ElementHelper;
import com.blackwalnutsoftware.kgraph.engine.Node;

public class OperationsTest {

   private static Logger logger = LogManager.getLogger(OperationsTest.class);

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      logger.debug("OperationsTest.setUpBeforeClass()");
      App.initialize("linear_dataset_tests");
      App.getDataGraph().flush();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      App.getDataGraph().cleanup();
   }

   @Test(expected = IllegalArgumentException.class)
   public void traverse_badDirection_exception() {

      // setup data
      App.loadDatasetToDataGraph(new LinearDataset());
      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> inputs = new HashMap<>();
      inputs.put(startingNode.getKey() + "." + "startingNode", startingNode);
      inputs.put(startingNode.getKey() + "." + "direction", Direction.any.toString());
      inputs.put(startingNode.getKey() + "." + "traversalEdgeType", "LinearDatasetEdge");
      inputs.put(startingNode.getKey() + "." + "distance", 1);

      Operations.traverse(startingNode, inputs);
   }

   @Test
   public void traverse_negativeDistance_getResult() {

      // setup data
      App.loadDatasetToDataGraph(new LinearDataset());
      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> inputs = new HashMap<>();
      inputs.put(startingNode.getKey() + "." + "startingNode", startingNode);
      inputs.put(startingNode.getKey() + "." + "direction", Direction.inbound.toString());
      inputs.put(startingNode.getKey() + "." + "traversalEdgeType", "LinearDatasetEdge");
      inputs.put(startingNode.getKey() + "." + "distance", -1);

      Map<String, Object> results = Operations.traverse(startingNode, inputs);

      assert ("LinearDatasetNode_501".equals(((Node) results.get("RESULT")).getKey())) : "{" + results + "}";
   }

   @Test
   public void traverse_inboundDirection_getResult() {

      // setup data
      App.loadDatasetToDataGraph(new LinearDataset());
      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> inputs = new HashMap<>();
      inputs.put(startingNode.getKey() + "." + "startingNode", startingNode);
      inputs.put(startingNode.getKey() + "." + "direction", Direction.inbound.toString());
      inputs.put(startingNode.getKey() + "." + "traversalEdgeType", "LinearDatasetEdge");
      inputs.put(startingNode.getKey() + "." + "distance", -1);

      Map<String, Object> results = Operations.traverse(startingNode, inputs);

      assert ("LinearDatasetNode_501".equals(((Node) results.get("RESULT")).getKey())) : "{" + results + "}";
   }

   @Test(expected = IllegalArgumentException.class)
   public void traverse_invalidDirection_getResult() {

      // setup data
      App.loadDatasetToDataGraph(new LinearDataset());
      Node startingNode = App.getDataGraph().getNodeByKey("LinearDatasetNode_500", "LinearDatasetNode");

      Map<String, Object> inputs = new HashMap<>();
      inputs.put(startingNode.getKey() + "." + "startingNode", startingNode);
      inputs.put(startingNode.getKey() + "." + "direction", "invalid_direction");
      inputs.put(startingNode.getKey() + "." + "traversalEdgeType", "LinearDatasetEdge");
      inputs.put(startingNode.getKey() + "." + "distance", 1);

      Operations.traverse(startingNode, inputs);
   }

   @Test
   public void add_validInputs_getResult() {
      // setup data
      App.loadDatasetToDataGraph(new LinearDataset());
      Node startingNode = new Node(ElementHelper.generateKey(), "testNodeType");
      Map<String, Object> inputs = new HashMap<>();
      inputs.put(startingNode.getKey() + "." + "floatA", 1);
      inputs.put(startingNode.getKey() + "." + "floatB", 1);

      Map<String, Object> results = Operations.add(startingNode, inputs);

      logger.debug("results = " + results);

      Float result = (Float) results.get("RESULT");

      assert (2.0f == result);
   }

   @Test
   public void getGreatestDateLessThan_validInputs_getResult() throws FileNotFoundException {
      // setup data
      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/stock_data_fetcher/data/AAPL_2020-05-07.txt"));
      CSVLoader stockLoader = new CSVLoader();
      stockLoader.loadStocks(files);

      App.loadDatasetToDataGraph(new LinearDataset());
      Node startingNode = new Node(ElementHelper.generateKey(), "testNodeType");
      Map<String, Object> inputs = new HashMap<>();
      // XXXXX is on a Sunday
      inputs.put(startingNode.getKey() + "." + "dateNumber", 14619);

      Map<String, Object> results = Operations.getGreatestDateLessThan(startingNode, inputs);

      logger.debug("results = " + results);

      Node result = (Node) results.get("RESULT");

      Integer dateNumber = (Integer) result.getAttribute("dateNumber");
      assert (dateNumber == 14617) : "dateNumber = " + dateNumber;
   }

   @Test (expected = RuntimeException.class)
   public void getSymbolDateRel_badInputs_exception() {
      Node currentNode = new Node(ElementHelper.generateKey(), "testNodeType");
      Map<String, Object> inputs = new HashMap<>();;

      Operations.getSymbolDateRel(currentNode, inputs);
   }
}
