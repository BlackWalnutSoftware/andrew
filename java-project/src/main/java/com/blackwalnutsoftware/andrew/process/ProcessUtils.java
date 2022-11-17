package com.blackwalnutsoftware.andrew.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessUtils {

   public Map<String, List<Float>> getGroupingByThoughtKey(List<ThoughtScore> scores) {
      Map<String, List<Float>> results = new HashMap<>();
      for(ThoughtScore score : scores) {
         String thoughtKey = score.getThoughtKey();
         if(!(results.containsKey(thoughtKey))) {
            List<Float> values = new ArrayList<>();
            values.add(score.getThoughtScore());
            results.put(thoughtKey, values);
         }
         else {
            // TODO: add the value to the current list of values
            List<Float> values = results.get(thoughtKey);
            values.add(score.getThoughtScore());
         }
      }
      return results;
   }

}
