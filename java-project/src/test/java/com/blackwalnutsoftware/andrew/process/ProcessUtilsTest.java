package com.blackwalnutsoftware.andrew.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessUtilsTest {

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Test
   public void getGroupingByThoughtKey_hasGroups_grouped() {
      ProcessUtils processUtils = new ProcessUtils();
      List<ThoughtScore> scores = new ArrayList<>();
      ThoughtScore score1 = new ThoughtScore("id_A", 0.5f);
      ThoughtScore score2 = new ThoughtScore("id_A", 0.6f);
      ThoughtScore score3 = new ThoughtScore("id_B", 0.6f);
      scores.add(score1);
      scores.add(score2);
      scores.add(score3);
      Map<String, List<Float>> results = processUtils.getGroupingByThoughtKey(scores);
      assert(2 == results.size()): results;
      assert(2 == results.get("id_A").size()): results.get("id_A");
      assert(1 == results.get("id_B").size()): results.get("id_B");
   }

}
