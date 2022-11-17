package com.blackwalnutsoftware.andrew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import com.blackwalnutsoftware.andrew.dataset.Dataset;
import com.blackwalnutsoftware.andrew.process.AveragePercentageScoringMachine;
import com.blackwalnutsoftware.andrew.process.Evaluation;
import com.blackwalnutsoftware.andrew.process.Evaluator;
import com.blackwalnutsoftware.andrew.process.ProcessUtils;
import com.blackwalnutsoftware.andrew.process.Report;
import com.blackwalnutsoftware.andrew.process.ScoringMachine;
import com.blackwalnutsoftware.andrew.process.ThoughtScore;
import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.Element;
import com.blackwalnutsoftware.kgraph.engine.KnowledgeGraph;
import com.blackwalnutsoftware.kgraph.engine.Node;
import com.blackwalnutsoftware.kgraph.engine.QueryClause;

/**
 * The Class App.
 * <p/>
 * This is the main class for Andrew.
 * 
 */
public class App {

   /** The data graph. */
   private static KnowledgeGraph _dataGraph = null;

   /** The garden graph. */
   private static KnowledgeGraph _gardenGraph = null;

   /** The logger. */
   private static Logger logger = LogManager.getLogger(App.class);

   /** The thought cache. */
   private static Map<String, Thought> thoughtCache = new HashMap<>();

   /** The mutator. */
   private static Mutator mutator = new Mutator();

   /** The process utils. */
   private static ProcessUtils processUtils = new ProcessUtils();

   /**
    * Instantiates a new app.
    */
   private App() {
      // do nothing. Static global class.
   }

   /**
    * Initialize.
    *
    * @param databaseName the database name
    * @throws Exception the exception
    */
   public static void initialize(String databaseName) throws Exception {
      _dataGraph = new KnowledgeGraph(databaseName);
      _gardenGraph = new KnowledgeGraph(databaseName + "_garden");
      thoughtCache = new HashMap<>();
      mutator = new Mutator();
   }

   /**
    * Gets the data graph.
    *
    * @return the data graph
    */
   public static KnowledgeGraph getDataGraph() {
      return _dataGraph;
   }

   /**
    * Gets the garden graph.
    *
    * @return the garden graph
    */
   public static KnowledgeGraph getGardenGraph() {
      return _gardenGraph;
   }

   /**
    * Load dataset to data graph.
    *
    * @param dataset the dataset
    */
   public static void loadDatasetToDataGraph(Dataset dataset) {
      String datasetInfoID = dataset.getDatasetInfoID();
      QueryClause datasetInfoQuery = new QueryClause("dataset_id", QueryClause.Operator.EQUALS, datasetInfoID);
      List<Node> datasetInfo = getDataGraph().queryNodes("dataSet_info", datasetInfoQuery);
      if (datasetInfo.size() > 1) {
         throw new RuntimeException("Dataset " + datasetInfoID + " misloaded.");
      } else if (0 == datasetInfo.size()) {
         // load dataset nodes
         List<Node> nodes = dataset.getNodesToLoad();
         Node[] nodesArray = new Node[nodes.size()];
         nodesArray = nodes.toArray(nodesArray);
         _dataGraph.upsert(nodesArray);

         // load dataset edges
         List<Edge> edges = dataset.getEdgesToLoad();
         Edge[] edgesArray = new Edge[edges.size()];
         edgesArray = edges.toArray(edgesArray);
         _dataGraph.upsert(edgesArray);

         // lastly, create the dataSet_info node
         Node datasetInfoNode = new Node(datasetInfoID, "dataSet_info");
         datasetInfoNode.addAttribute("dataset_id", datasetInfoID);
         datasetInfoNode.addAttribute("max_time", dataset.getMaxTime());
         _dataGraph.upsert(datasetInfoNode);
      }
   }

   /**
    * Load thought from json.
    *
    * @param thoughtName the thought name
    * @param content the content
    * @return the thought
    */
   public static Thought loadThoughtFromJson(String thoughtName, String content) {
      if (thoughtCache.containsKey(thoughtName)) {
         return thoughtCache.get(thoughtName);
      } else {
         Thought thought = loadThoughtFromJson(content);
         thoughtCache.put(thoughtName, thought);
         return thought;
      }
   }

   /**
    * Load thought from json.
    *
    * @param content the content
    * @return the thought
    */
   public static Thought loadThoughtFromJson(String content) {
      JSONArray jsonArr = new JSONArray(content);
      Thought result = null;

      List<Element> loadedElements = _gardenGraph.loadFromJson(jsonArr);

      logger.debug("loadedElements = " + loadedElements);

      for (Element element : loadedElements) {
         String type = element.getType();
         logger.debug("type = " + type);
         if ("thought".equals(type)) {
            logger.debug("element = " + element);
            result = new Thought((Node) element);
         }
      }
      return result;
   }

   /**
    * Train.
    *
    * @param goal the goal
    * @param trainingParameters the training parameters
    * @param trainingCriteria the training criteria
    * @return the list
    * @throws Exception the exception
    */
   public static List<ThoughtScore> train(Goal goal, TrainingParameters trainingParameters,
         TrainingCriteria trainingCriteria) throws Exception {

      Report.setTrainingCriteria(trainingCriteria);
      Map<String, Thought> currentThoughts = new HashMap<>();
      List<Thought> goalThoughts = goal.getThoughts();
      for (Thought goalThought : goalThoughts) {
         currentThoughts.put(goalThought.getKey(), goalThought);
      }

      ScoringMachine scoringMachine = new AveragePercentageScoringMachine();
      List<ThoughtScore> scores = new ArrayList<>();
      Crossover simpleCrossover = new SimpleCrossover();

      // repeat
      Integer currentGen = 0;
      do {

         Map<String, Object> iterationParameters = goal.setTrainingParameters(trainingParameters);

         currentGen++;

         logger.info("processing generation " + currentGen);

         logger.debug("currentThoughts.size() = " + currentThoughts.size());

         // loop through a set of test cases
         Evaluator evaluator = new Evaluator(goal.getNode());
         List<Evaluation> evualationResults = evaluator.evaluateThoughts2(currentThoughts, trainingCriteria, iterationParameters);
         logger.debug("evualationResults = " + evualationResults);

         // sum the score for each thought
         List<ThoughtScore> newScores = scoringMachine.scoreAndRank(evualationResults);
         scores.addAll(newScores);

         logger.debug("scores = " + scores);
         // cull the herd of thought/goal when limited for resources.
         // Have culling be statistical some sometimes bad thoughts survive.
         Map<String, List<Float>> scoreGroupings = processUtils.getGroupingByThoughtKey(scores);

         Set<Thought> nextThoughts = new HashSet<>();
         Integer nextPopSize = 0;

         // carry forward the seed thoughts
         for (Thought t : currentThoughts.values()) {
            if (true == (Boolean) t.getThoughtNode().getAttribute("seedThought")) {
               nextThoughts.add(t);
               nextPopSize += 1;
            }
         }

         // carry forward the baby thoughts
         for (String groupKey : scoreGroupings.keySet()) {
            Integer age = scoreGroupings.get(groupKey).size();
            if (age < trainingCriteria.getMaturationAge()) {
               Thought babyKey = currentThoughts.get(groupKey);
               nextThoughts.add(babyKey);
               nextPopSize++;
            }
         }

         // carry forward the remaining best thoughts
         // TODO: make this statistical to sometimes allow bad thoughts to live
         Map<Float, List<String>> rankings = new HashMap<>();
         for (String scoreGroupingKey : scoreGroupings.keySet()) {
            if (scoreGroupings.get(scoreGroupingKey).size() >= trainingCriteria.getMaturationAge()) {
               Float average = calculateAverage(scoreGroupings.get(scoreGroupingKey));
               if (!rankings.containsKey(average)) {
                  List<String> thoughtIDs = new ArrayList<>();
                  thoughtIDs.add(scoreGroupingKey);
                  rankings.put(average, thoughtIDs);
               } else {
                  List<String> thoughtIDs = rankings.get(average);
                  thoughtIDs.add(scoreGroupingKey);
                  rankings.put(average, thoughtIDs);
               }
            }
         }
         ArrayList<Float> scoresList = new ArrayList<Float>(rankings.keySet());
         Collections.sort(scoresList, Collections.reverseOrder());
         for (Float score : scoresList) {
            if (nextPopSize >= trainingCriteria.getMaxPopulation()) {
               break;
            }
            List<String> values = rankings.get(score);
            for (String value : values) {
               if (currentThoughts.containsKey(value)) {
                  if (nextPopSize >= trainingCriteria.getMaxPopulation()) {
                     break;
                  }
                  Thought currentThought = currentThoughts.get(value);
                  // add the smart thought
                  nextThoughts.add(currentThought);
                  // add a smart offspring
                  Thought randomSpouse = getRandomMember(currentThoughts.values());
                  Thought child = simpleCrossover.crossover(currentThought, randomSpouse);
                  nextThoughts.add(child);
                  // generate mutants
                  Thought mutant = mutator.createMutant(currentThought, 1);
                  nextThoughts.add(mutant);
                  nextPopSize += 3;
               }

            }
         }

         Map<String, Thought> nextThoughtsMap = new HashMap<>();
         for (Thought nextThought : nextThoughts) {
            nextThoughtsMap.put(nextThought.getKey(), nextThought);
         }

         // report results from the current generation
         Float averageGenScore = getGenerationAverage(currentThoughts, scores);
         Report.registerGeneration(averageGenScore, currentThoughts, newScores, evualationResults, iterationParameters);

         currentThoughts = nextThoughtsMap;

         // until things don't get better (end of repeat-until)
      } while (currentGen < trainingCriteria.getNumGenerations());



      return scores;
   }

   /**
    * Gets the generation average.
    *
    * @param currentThoughts the current thoughts
    * @param scores the scores
    * @return the generation average
    */
   private static Float getGenerationAverage(Map<String, Thought> currentThoughts, List<ThoughtScore> scores) {
      Set<String> currentThoughtIDs = currentThoughts.keySet();
      int size = 0;
      float sum = 0f;
      for (ThoughtScore score : scores) {
         String thoughtKey = score.getThoughtKey();
         if (currentThoughtIDs.contains(thoughtKey)) {
            sum += score.getThoughtScore();
            size++;
         }
      }
      return sum / size;
   }
   

   /**
    * Gets a random member of the input collection..
    *
    * @param <T> the generic type
    * @param collection the coll
    * @return the random
    */
   public static <T> T getRandomMember(Collection<T> collection) {
      int num = (int) (Math.random() * collection.size());
      for (T t : collection)
         if (--num < 0)
            return t;
      throw new AssertionError();
   }
   
   
   /**
    * Calculate average.
    *
    * @param scores the scores
    * @return the float
    */
   public static Float calculateAverage(List<Float> scores) {
      Float sum = 0f;
      if (!scores.isEmpty()) {
         for (Float score : scores) {
            sum += score;
         }
         return sum / scores.size();
      }
      return sum;
   }
}
