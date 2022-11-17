package com.blackwalnutsoftware.andrew;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleCrossoverTest {

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      String databaseName = "andrew_test_database";
      App.initialize(databaseName);
      App.getGardenGraph().flush();
      App.getDataGraph().flush();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Test
   public void createCrossover_seedThoughts_mutantNotSeed() throws Exception {
      Crossover crossover = new SimpleCrossover();

      // load the seed thought
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought rootThought = App.loadThoughtFromJson("linear_growth_thought", content);

      assert ((Boolean) (rootThought.getThoughtNode().getAttribute("seedThought")) == true) : rootThought.getThoughtNode();

      Thought crossThought = crossover.crossover(rootThought, rootThought);
      assert ((Boolean) (crossThought.getThoughtNode().getAttribute("seedThought")) == false);
      assert ((Boolean) (rootThought.getThoughtNode().getAttribute("seedThought")) == true) : rootThought.getThoughtNode();
   }

}
