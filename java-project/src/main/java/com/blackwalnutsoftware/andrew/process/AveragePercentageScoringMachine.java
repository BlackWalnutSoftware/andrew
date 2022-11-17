package com.blackwalnutsoftware.andrew.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AveragePercentageScoringMachine implements ScoringMachine {

   @Override
   public List<ThoughtScore> scoreAndRank(List<Evaluation> evualationResults) {
      Map<String, List<Float>> runningTotals = new HashMap<>();
      List<ThoughtScore> results = new ArrayList<>();

      for (Evaluation evualationResult : evualationResults) {
         Float score = null;

         String thoughtKey = evualationResult.getThought().getKey();
         Number guessNum = evualationResult.getGuess();

         // if no guess, than score is zero.
         if (null == guessNum) {
            score = 0f;
         }

         if (null == score) {
            Float guess = ((Number) evualationResult.getGuess()).floatValue();
            Number actualNum = ((Number) evualationResult.getActual());
            Float actual = null;
            actual = actualNum.floatValue();
            // see also: https://www.calculatorsoup.com/calculators/algebra/percent-error-calculator.php
            score = 1 - (Math.abs(guess-actual) / Math.abs(actual));
         }
         if (runningTotals.containsKey(thoughtKey)) {
            List<Float> currentTotal = runningTotals.get(thoughtKey);
            currentTotal.add(score);
            runningTotals.put(thoughtKey, currentTotal);
         } else {
            List<Float> currentTotal = new ArrayList<>();
            currentTotal.add(score);
            runningTotals.put(thoughtKey, currentTotal);
         }
      }

      for (String thoughtKey : runningTotals.keySet()) {
         List<Float> totals = runningTotals.get(thoughtKey);
         Float sum = 0f;
         for (Float total : totals) {
            sum += total;
         }
         results.add(new ThoughtScore(thoughtKey, sum / totals.size()));
      }

      Collections.sort(results, Collections.reverseOrder());

      return results;
   }

}
