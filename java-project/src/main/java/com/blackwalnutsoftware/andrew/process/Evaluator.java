package com.blackwalnutsoftware.andrew.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.blackwalnutsoftware.andrew.App;
import com.blackwalnutsoftware.andrew.Thought;
import com.blackwalnutsoftware.andrew.TrainingCriteria;
import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.Node;
import com.blackwalnutsoftware.kgraph.engine.QueryClause;
import com.blackwalnutsoftware.kgraph.engine.QueryClause.Operator;

public class Evaluator {

   private static Integer numEvalFailures = 0;
   private static Integer numEval = 0;

   private Node _goal;
   private static Logger logger = LogManager.getLogger(Evaluator.class);

   public Evaluator(Node goal) {
      _goal = goal;
   }

   public List<Evaluation> evaluateThoughts2(Map<String, Thought> thoughtMap, TrainingCriteria trainingCriteria, Map<String, Object> initialWorkingMemory) throws Exception {

      Long minTime = trainingCriteria.getStartDateLong();
      Long maxTime = trainingCriteria.getEndDateLong();
      Integer numTests = trainingCriteria.getQuestsPerGeneration();

      Collection<Thought> thoughts = thoughtMap.values();
      List<Node> thoughtNodes = new ArrayList<>();
      for(Thought thought : thoughts) {
         thoughtNodes.add(thought.getThoughtNode());
      }
      
      
      List<Evaluation> results = new ArrayList<>();

      Random random = new Random();

      String otherSidePrefix = (String) _goal.getAttribute("otherSidePrefix");

      for (Integer i = 1; i <= numTests; i++) {
         logger.info("starting quest #" + i);

         Long randomTime = random.nextLong(maxTime - minTime) + minTime;
         Number forecastResult = null;

         for (Node thoughtNode : thoughtNodes) {
            numEval++;
            Number actual = null;
            Map<String, Object> workingMemory = null;
            try {
               Thought thought = new Thought(thoughtNode);
               workingMemory = clone(initialWorkingMemory);
               workingMemory = thought.addContext(workingMemory, "startDate", randomTime, "GOAL");
               logger.debug("workingMemory before forecast2 = " + workingMemory);
               Map<String, Object> forecastOutput = thought.forecast2(workingMemory);
               logger.debug("workingMemory after forecast2 = " + workingMemory);
               forecastResult = (Number) forecastOutput.get("RESULT.output");
               logger.debug("randomTime = " + randomTime + ",       forecastResult = " + forecastResult);

               // TODO: fix this hack - should be a generic attribute
               String otherSideID = (String) workingMemory.get("GOAL.symbol");
               actual = getActual(randomTime, otherSidePrefix + otherSideID);
            } catch (Exception e) {
               numEvalFailures++;
               logger.error("forecast2 failed.  (failure rate = " + numEvalFailures + "/" + numEval + ")", e);
               forecastResult = null;
            }
            finally {
               results.add(new Evaluation(thoughtNode, forecastResult, actual, workingMemory));
            }
         }
      }

      return results;
   }

   public Number getActual(Long startingTime, String otherSideID) {
      Integer distance = (Integer) _goal.getAttribute("targetDistance");
      String targetProperty = (String) _goal.getAttribute("targetProperty");
      String relType = (String) _goal.getAttribute("targetRel");
      Long targetTime = startingTime + distance;

      String query = "FOR t IN date FILTER t.dateNumber <= @time SORT t.dateNumber DESC LIMIT 1 RETURN t";
      logger.debug("query: " + query);
      Map<String, Object> bindVars = Collections.singletonMap("time", targetTime);
      logger.debug("bindVars: " + bindVars);
      List<Node> queryResults = null;
      Node dateNode = null;
      try {
         queryResults = App.getDataGraph().queryNodes(query, bindVars);
         dateNode = queryResults.get(0);
      } catch (Exception e) {
         logger.error("no date results");
         logger.error("query: " + query);
         logger.error("bindVars: " + bindVars);
         throw new RuntimeException("no date results", e);
      }

      List<Triple<Node, Edge, Node>> expansions = null;
      Edge targetEdge = null;
      Number actualResult = null;
      try {
         // traverse from the date through the target rel
         // TODO: support reverse direction traversals.
         List<QueryClause> relClauses = new ArrayList<>();
         // stockSymbol/AAPL
         relClauses.add(new QueryClause("_from", Operator.EQUALS, otherSideID));
         expansions = App.getDataGraph().expandLeft(dateNode, relType, relClauses, null);

         // get the target object
         targetEdge = expansions.get(0).getMiddle();

         // get the target attribute
         actualResult = (Number) targetEdge.getAttribute(targetProperty);
      } catch (Exception e) {
         logger.error("no target object results");
         logger.error("dateNode: " + dateNode);
         logger.error("relType: " + relType);
         logger.error("targetObject: " + targetEdge);
         throw new RuntimeException("no target object results", e);
      }

      return actualResult;
   }

   public static <K, V> Map<K, V> clone(Map<K, V> original) {
      Map<K, V> copy = new HashMap<>();

      for (Map.Entry<K, V> entry : original.entrySet()) {
         copy.put(entry.getKey(), entry.getValue());
      }

      return copy;
   }

}
