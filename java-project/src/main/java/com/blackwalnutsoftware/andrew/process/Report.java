package com.blackwalnutsoftware.andrew.process;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.blackwalnutsoftware.andrew.App;
import com.blackwalnutsoftware.andrew.Thought;
import com.blackwalnutsoftware.andrew.TrainingCriteria;

public class Report {

   private static List<Float> _generationAverageScoreList = new ArrayList<>();
   private static List<Map<String, Thought>> _generationThoughtsList = new ArrayList<>();
   private static List<List<ThoughtScore>> _generationScoresList = new ArrayList<>();
   private static List<Float> _generationSeedMinusNonSeedScoreAverageList = new ArrayList<>();
   private static List<List<Evaluation>> _generationEvualationResultsList = new ArrayList<>();
   private static List<Map<String, Object>> _generationIterationParametersList  = new ArrayList<>();

   private static Long _durationSeconds = null;

   private static Logger logger = LogManager.getLogger(Report.class);
   private static TrainingCriteria _trainingCriteria = null;
   private static String _name = null;

   private Report() {
      // do nothing. Static global class.
   }

   public static void print() throws Exception {
      // TODO Auto-generated method stub
      String path = "./target/report/";
      Files.createDirectories(Paths.get(path));
      File dir = new File(path);
      File file = new File(dir, "Andrew_training_report-" + _name + "-" + System.currentTimeMillis() + ".txt");
      FileWriter reportWriter = new FileWriter(file);
      reportWriter.write("Andrew Training Report" + "\n");
      reportWriter.write("----------------------" + "\n");
      reportWriter.write("training criteria : \n" + _trainingCriteria + "\n\n");

      reportWriter.write("processing time : " + _durationSeconds + " seconds" + "\n\n");

      Integer numGens = _generationAverageScoreList.size();
      for (int i = 0; i < numGens; i++) {
         Integer genNumber = i + 1;
         reportWriter.write("generation " + genNumber + "\t" + "(pop. " + _generationThoughtsList.get(i).size() + ") \t" + "average score: "
               + _generationAverageScoreList.get(i) + "\n");
      }

      double overallSlope = getSlope(_generationAverageScoreList);
      reportWriter.write("\n" + "overall slope = " + overallSlope + "\n");

      double evolutionSlope = getSlope(_generationSeedMinusNonSeedScoreAverageList);
      reportWriter.write("\n" + "evolution slope = " + evolutionSlope + "\n");

      reportWriter.write("\n" + "-------------------------------------------" + "\n");
      
      for (int i = 0; i < _generationScoresList.size(); i++) {

         Integer genNum = i + 1;
         reportWriter.write("\n" + "details for generation " + genNum + ": \n" + "   scores : " + _generationScoresList.get(i) + "\n");
         reportWriter.write("\n" + "   iteration_parameters : " + _generationIterationParametersList.get(i) + "\n");
         reportWriter.write("\n" + "   evaluation_results : " + _generationEvualationResultsList.get(i) + "\n");
      }

      reportWriter.close();
   }

   private static double getSlope(List<Float> values) {
      // see: https://stackoverflow.com/a/67370754/2418261
      SimpleRegression regression = new SimpleRegression();
      int dataListSize = values.size();
      double dataArray[][] = new double[dataListSize][2];
      for (int i = 0; i < dataListSize; i++) {
         dataArray[i][0] = i + 1;
         dataArray[i][1] = values.get(i);
      }
      regression.addData(dataArray);
      regression.regress();
      double overallSlope = regression.getSlope();
      return overallSlope;
   }

   public static void registerGeneration(Float averageGenScore, Map<String, Thought> genThoughts, List<ThoughtScore> genScores,
         List<Evaluation> evualationResults, Map<String, Object> iterationParameters) {
      _generationAverageScoreList.add(averageGenScore);
      _generationThoughtsList.add(genThoughts);
      _generationScoresList.add(genScores);
      _generationEvualationResultsList.add(evualationResults);
      _generationIterationParametersList.add(iterationParameters);

      Collection<Thought> allThoughts = genThoughts.values();
      Set<String> seedThoughtKeys = new HashSet<>();
      Set<String> nonseedThoughtKeys = new HashSet<>();
      List<Float> seedScores = new ArrayList<>();
      List<Float> nonSeedScores = new ArrayList<>();

      for (Thought t : allThoughts) {
         Boolean isSeedThought = (Boolean) t.getThoughtNode().getAttribute("seedThought");
         if (null == isSeedThought || false == isSeedThought) {
            nonseedThoughtKeys.add(t.getKey());
         } else {
            seedThoughtKeys.add(t.getKey());
         }
      }

      for (ThoughtScore score : genScores) {
         if (nonseedThoughtKeys.contains(score.getThoughtKey())) {
            nonSeedScores.add(score.getThoughtScore());
         } else {
            seedScores.add(score.getThoughtScore());
         }
      }

      Float seedAverageScore = App.calculateAverage(seedScores);
      Float nonSeedAverageScore = App.calculateAverage(nonSeedScores);

      _generationSeedMinusNonSeedScoreAverageList.add(seedAverageScore - nonSeedAverageScore);
   }

   public static void setElapsedSeconds(long duration) {
      _durationSeconds = duration;
   }

   public static void setTrainingCriteria(TrainingCriteria trainingCriteria) {
      // TODO Auto-generated method stub
      _trainingCriteria = trainingCriteria;
   }

   public static void clear() {
      _generationAverageScoreList = new ArrayList<>();
      _generationThoughtsList = new ArrayList<>();
      _generationScoresList = new ArrayList<>();
      _generationSeedMinusNonSeedScoreAverageList = new ArrayList<>();
      _generationEvualationResultsList = new ArrayList<>();
      _durationSeconds = null;
      _trainingCriteria = null;
   }

   public static void setName(String name) {
      _name = name;
   }

}
