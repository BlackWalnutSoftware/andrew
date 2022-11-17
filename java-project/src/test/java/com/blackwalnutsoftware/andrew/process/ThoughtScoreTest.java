package com.blackwalnutsoftware.andrew.process;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ThoughtScoreTest {

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Test
   public void getThoughtKey_hasKey_success() {
      ThoughtScore thoughtScore = new ThoughtScore("dummyKey", 0.5f);
      String key = thoughtScore.getThoughtKey();
      
      assert("dummyKey".equals(key)) : key;
   }

}
