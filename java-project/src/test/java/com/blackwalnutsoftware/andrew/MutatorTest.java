package com.blackwalnutsoftware.andrew;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MutatorTest {

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
   public void createMutant_seedThought_mutantNotSeed() throws Exception {
      Mutator mutator = new Mutator();
      
      // load the seed thought
      String filePath = "./src/main/resources/initial_thoughts/linear_growth/linear_growth_thought.json";
      String content = new String(Files.readAllBytes(Paths.get(filePath)));
      Thought rootThought = App.loadThoughtFromJson("linear_growth_thought", content);
      List<Thought> thoughts = new ArrayList<>();
      thoughts.add(rootThought);
      
      assert((Boolean)(rootThought.getThoughtNode().getAttribute("seedThought")) == true);
      
      List<Thought> mutants = mutator.createMutant(thoughts, 1);
      Thought mutant = mutants.get(0);
      assert((Boolean)(mutant.getThoughtNode().getAttribute("seedThought")) == false);
      assert((Boolean)(rootThought.getThoughtNode().getAttribute("seedThought")) == true);

   }
}
