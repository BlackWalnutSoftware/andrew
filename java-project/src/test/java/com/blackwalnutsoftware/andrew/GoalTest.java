package com.blackwalnutsoftware.andrew;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackwalnutsoftware.andrew.dataset.LinearDataset;
import com.blackwalnutsoftware.kgraph.engine.Node;

public class GoalTest {

   private static Logger logger = LogManager.getLogger(GoalTest.class);

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      String databaseName = "andrew_test_database";
      App.initialize(databaseName);
      App.loadDatasetToDataGraph(new LinearDataset());
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Test
   public void getThoughts_hasOne_success() throws Exception {
   // load a thought
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought rootThought = App.loadThoughtFromJson("linear_growth_thought", content);
      Goal goal = rootThought.getGoal();
      
      List<Thought> thoughts = goal.getThoughts();
      
      assert(1 == thoughts.size()): thoughts;
      logger.debug("thoughts[0] = " + thoughts.get(0).exportJson());
   }

   @Test
   public void getNode_hasOne_success() throws Exception {
   // load a thought
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought rootThought = App.loadThoughtFromJson("linear_growth_thought", content);
      Goal goal = rootThought.getGoal();
      
      Node goalNode = goal.getNode();
      
      assert(null != goalNode);
   }
}
