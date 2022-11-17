package com.blackwalnutsoftware.andrew;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.Element;
import com.blackwalnutsoftware.kgraph.engine.ElementHelper;
import com.blackwalnutsoftware.kgraph.engine.Node;
import com.blackwalnutsoftware.kgraph.engine.QueryClause;

/**
 * The Class Thought.
 */
public class Thought {

   protected Node _thoughtNode;
   protected Node _goal;

   private static Logger logger = LogManager.getLogger(Thought.class);

   public Thought(Node thoughtNode) {
      _thoughtNode = thoughtNode;

      // set the thought's goal
      List<Triple<Node, Edge, Node>> triple = App.getGardenGraph().expandLeft(_thoughtNode, "approach", null, null);
      _goal = triple.get(0).getRight();
   }

   public String getKey() {
      return _thoughtNode.getKey();
   }

   public Node getThoughtNode() {
      return _thoughtNode;
   }

   public Thought(String thoughtKey) {
      // set the thought node
      _thoughtNode = App.getGardenGraph().getNodeByKey(thoughtKey, "thought");

      // set the thought's goal
      List<Triple<Node, Edge, Node>> triple = App.getGardenGraph().expandLeft(_thoughtNode, "approach", null, null);
      _goal = triple.get(0).getRight();
   }

   public Map<String, Object> forecast2(Map<String, Object> workingMemory) throws Exception {
      logger.debug("workingMemory = " + workingMemory);

      Map<String, Object> result = new HashMap<>();

      // get the initial layer inputs from the goal

      workingMemory = addGoalContext(workingMemory, _goal.getProperties());
      logger.debug("workingMemory = " + workingMemory);

      List<Set<Node>> layeredOperations = getOperationsByMaxLayer();

      // process the thought by layer...
      for (Set<Node> currentOperations : layeredOperations) {

         // process the nodes of a layer
         Set<String> nextLevelInputNodeKeys = new HashSet<>();
         for (Node node : currentOperations) {
            logger.debug("add nextLevelInputNodeKeys: " + node);
            nextLevelInputNodeKeys.add(node.getKey());

            String thoughtType = node.getType();

            if ("thought_operation".equals(thoughtType)) {

               // process the operation
               logger.debug("workingMemory = " + workingMemory);
               Map<String, Object> opResult = processOperation(node, workingMemory);
               logger.debug("workingMemory = " + workingMemory);

               // add the result of the operation to working memory
               logger.debug("workingMemory = " + workingMemory);
               workingMemory = addContext(workingMemory, opResult, node.getKey());
               logger.debug("workingMemory = " + workingMemory);

            } else if ("thought".equals(thoughtType)) {
               logger.debug("thought node = " + node);

               // have the thought consume some goal details
               List<Triple<Node, Edge, Node>> goalTriples = App.getGardenGraph().expandLeft(node, "approach", null, null);
               Node goal = goalTriples.get(0).getRight();
               logger.debug("goal node = " + goal);
               logger.debug("workingMemory = " + workingMemory);
               workingMemory = addGoalAttributes(workingMemory, goal);
               logger.debug("workingMemory = " + workingMemory);

            } else if ("thought_result".equals(thoughtType)) {
               Map<String, Object> opResult = processOperation(node, workingMemory);
               result = addResultContext(result, opResult, node.getKey());
               return result;
            } else {
               throw new RuntimeException("Invalid Node type. (" + node.getType() + ")");
            }

            // use the tailing edges to add next-level inputs to working memory
            for (String nextLevelInputNodeKey : nextLevelInputNodeKeys) {
               logger.debug("generating inputs based on outputs from " + nextLevelInputNodeKey + ".");
               QueryClause queryClause = new QueryClause("_from", QueryClause.Operator.EQUALS, node.getId());
               logger.debug("inputEdges queryClause name and value: " + queryClause.getName() + ", " + queryClause.getValue());
               List<Edge> inputEdges = App.getGardenGraph().queryEdges("thought_sequence", queryClause);
               logger.debug("next level input edges: " + inputEdges);
               for (Edge inputEdge : inputEdges) {
                  String inputProp = (String) inputEdge.getAttribute("input");
                  String edgeName = (String) inputEdge.getAttribute("name");
                  logger.debug("edgeName = " + edgeName);
                  String outputProp = (String) inputEdge.getAttribute("output");
                  String fromKey = inputEdge.getFrom().split("/")[1];
                  Object value = getInputValue(workingMemory, fromKey, inputProp);

                  if (null == value) {
                     logger.debug("workingMemory = " + workingMemory);
                     logger.error("Edge input value is NULL for '" + inputProp + "'  (edge = " + inputEdge + ")");
                  }

                  Double mutationFactor = (Double) inputEdge.getAttribute("mutation_factor");
                  if (null != mutationFactor) {
                     value = ((Number) value).floatValue() + mutationFactor;
                  }
                  String toKey = (String) inputEdge.getTo().split("/")[1];
                  logger.debug("copying value '" + value + "': " + fromKey + "." + inputProp + " -> " + toKey + "." + outputProp);
                  workingMemory.put(toKey + "." + outputProp, value);
               }
            }
         }
      }

      throw new RuntimeException("Thought has no end.");
   }

   /**
    * Adds the goal attributes.  This method does not overwrite any existing GOAL attributes.
    *
    * @param workingMemory the working memory
    * @param goal the goal
    * @return the map
    */
   protected Map<String, Object> addGoalAttributes(Map<String, Object> workingMemory, Node goal) {
      Map<String, Object> goalAttributes = goal.getProperties();
      Set<String> goalKeyset = goalAttributes.keySet();
      for (String goalKey : goalKeyset) {
         String goalName = "GOAL." + goalKey;
         if (!workingMemory.containsKey(goalName)) {
            workingMemory.put(goalName, goalAttributes.get(goalKey));
         }
      }
      return workingMemory;
   }

   private Object getInputValue(Map<String, Object> workingMemory, String fromKey, String inputProp) {
      logger.debug("getInputValue inputProp = " + inputProp);
      Object result = null;

      if (inputProp.startsWith("NUMBER.")) {
         String[] numStringArray = inputProp.split("\\.", 2);
         String numString = numStringArray[1];
         result = Float.valueOf(numString);
      } else {
         if (inputProp.startsWith("GOAL.")) {
            result = workingMemory.get(inputProp);
         } else {
            result = workingMemory.get(fromKey + "." + inputProp);
         }

      }
      return result;
   }

   private Map<String, Object> processOperation(Node currentNode, Map<String, Object> workingMemory) throws Exception {
      String operationName = (String) currentNode.getAttribute("operationName");
      logger.debug("processOperation for Node Name: " + currentNode.getAttribute("name"));
      logger.debug("operation Name = " + operationName);
      logger.debug("workingMemory = " + workingMemory);

      // reflection to call the method with inputs.
      Method operationMethod = Operations.class.getMethod(operationName, Node.class, Map.class);

      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) operationMethod.invoke(null, currentNode, workingMemory);

      return result;
   }

   private Map<String, Object> addResultContext(Map<String, Object> workingMemory, Map<String, Object> propertyMap, String elementKey) {
      Set<String> keyset = propertyMap.keySet();
      for (String key : keyset) {
         Object value = propertyMap.get(key);
         if (value instanceof Node) {
            Map<String, Object> valueProps = ((Node) value).getProperties();
            Set<String> valuePropsKeyset = valueProps.keySet();
            for (String valueKey : valuePropsKeyset) {
               String resultKey = (key + "." + valueKey).replace(elementKey, "RESULT");
               workingMemory = addContext(workingMemory, resultKey, valueProps.get(valueKey), "RESULT");
            }
         } else {
            String resultKey = key.replace(elementKey, "RESULT");
            logger.debug("adding result to working memory: " + resultKey + " =  " + value);
            workingMemory.put(resultKey, value);
         }
      }
      return workingMemory;
   }

   public Map<String, Object> addContext(Map<String, Object> workingMemory, String propertyName, Object propertyValue, String elementKey) {
      String varName = elementKey + "." + propertyName;
      logger.debug("addContext: " + varName + " =  " + propertyValue);
      workingMemory.put(varName, propertyValue);
      return workingMemory;
   }

   /**
    * Adds the goal context if a value is not already set for that GOAL key.
    * 
    * This method does not override values.  This allows preprocessing configuration
    * settings to be retained.
    *
    * @param workingMemory the working memory
    * @param propertyMap the property map
    * @return the map
    */
   protected Map<String, Object> addGoalContext(Map<String, Object> workingMemory, Map<String, Object> propertyMap) {
      Set<String> keyset = propertyMap.keySet();
      String varName = null;
      for (String key : keyset) {
         varName = "GOAL." + key;
         if (!workingMemory.containsKey(varName)) {
            Object value = propertyMap.get(key);
            logger.debug("adding to working memory: " + varName + " =  " + value);
            workingMemory.put(varName, value);
         }
      }
      return workingMemory;
   }

   public Map<String, Object> addContext(Map<String, Object> workingMemory, Map<String, Object> propertyMap, String elementKey) {
      Set<String> keyset = propertyMap.keySet();
      String varName = null;
      for (String key : keyset) {
         varName = elementKey + "." + key;
         Object value = propertyMap.get(key);
         if (value instanceof Node) {
            Map<String, Object> valueProps = ((Node) value).getProperties();
            Set<String> valuePropsKeyset = valueProps.keySet();
            for (String valueKey : valuePropsKeyset) {
               workingMemory = addContext(workingMemory, key + "." + valueKey, valueProps.get(valueKey), elementKey);
            }
         } else {
            logger.debug("adding to working memory: " + varName + " =  " + value);
            workingMemory.put(varName, value);
         }
      }
      return workingMemory;
   }

   protected List<Set<Node>> getOperationsByMaxLayer() {
      // Note: see p.14 of LBB for details.
      Map<Integer, Set<String>> resultsMap = new HashMap<>();

      Integer currentLevel = 0;
      List<Node> startingPoints = new ArrayList<>();

      // The max distance from the start node for any path
      Map<String, Integer> nodeMaxLevel = new HashMap<>();
      nodeMaxLevel.put(_thoughtNode.getKey() + ":" + _thoughtNode.getType(), currentLevel);
      startingPoints.add(_thoughtNode);

      while (startingPoints.size() > 0) {
         List<Node> nextStartingPoints = new ArrayList<>();
         currentLevel += 1;
         for (Node startingPoint : startingPoints) {
            List<Triple<Node, Edge, Node>> expansions = App.getGardenGraph().expandRight(startingPoint, "thought_sequence", null, null);
            for (Triple<Node, Edge, Node> expansion : expansions) {
               Node right = expansion.getRight();
               nextStartingPoints.add(right);
               nodeMaxLevel.put(right.getKey() + ":" + right.getType(), currentLevel);
            }
         }
         startingPoints = nextStartingPoints;
      }

      Iterator<String> maxLevelIterator = nodeMaxLevel.keySet().iterator();
      while (maxLevelIterator.hasNext()) {
         String current = maxLevelIterator.next();
         Integer currentIdLevel = nodeMaxLevel.get(current);
         Set<String> nodeLevelcontents = resultsMap.get(currentIdLevel);

         if (null == nodeLevelcontents) {
            nodeLevelcontents = new HashSet<>();
         }
         nodeLevelcontents.add(current);
         resultsMap.put(currentIdLevel, nodeLevelcontents);
      }
      Integer numLayers = resultsMap.size();
      List<Set<Node>> results = new ArrayList<>();
      for (int i = 0; i < numLayers; i++) {
         Set<String> thisLayer = resultsMap.get(i);
         Set<Node> thisLayerNodes = new HashSet<>();
         for (String nodeString : thisLayer) {
            String nodeKey = nodeString.split(":")[0];
            String nodeType = nodeString.split(":")[1];
            thisLayerNodes.add(App.getGardenGraph().getNodeByKey(nodeKey, nodeType));
         }
         results.add(thisLayerNodes);
      }
      return results;
   }

   public Node getGoalNode() {
      return _goal;
   }

   public Goal getGoal() {
      return new Goal(_goal);
   }
   
   
   public Thought clone() {
      String clonedNodeKey = ElementHelper.generateKey();
      return clone(clonedNodeKey);
   }

   /**
    * Clone.
    * 
    * @param thoughtKey the cloned node key
    * @return the thought
    */
   private Thought clone(String thoughtKey) {

      Map<String, String> idMapping = new HashMap<>();

      // clone the thought node
      Node clonedThoughtNode = cloneNode(_thoughtNode, thoughtKey);
      App.getGardenGraph().upsert(clonedThoughtNode);
      idMapping.put(_thoughtNode.getId(), clonedThoughtNode.getId());

      // clone the "approach" edge
      QueryClause thoughtKeyQueryClause = new QueryClause("thought_key", QueryClause.Operator.EQUALS, _thoughtNode.getKey());
      List<Edge> approachEdges = App.getGardenGraph().queryEdges("approach", thoughtKeyQueryClause);
      if (approachEdges.size() != 1) {
         throw new RuntimeException("expected one Approach (" + approachEdges + ")");
      }
      Edge clonedApproachEdge = cloneEdge(approachEdges.get(0));
      clonedApproachEdge.setTo(clonedThoughtNode.getId());
      clonedApproachEdge.addAttribute("thought_key", thoughtKey);
      logger.debug("clonedApproachEdge: " + clonedApproachEdge);
      App.getGardenGraph().upsert(clonedApproachEdge);

      // clone the set of "thought_operation" nodes
      List<Node> operationNodes = App.getGardenGraph().queryNodes("thought_operation", thoughtKeyQueryClause);
      for (Node operationNode : operationNodes) {
         Node clonedOpNode = cloneNode(operationNode);
         clonedOpNode.addAttribute("thought_key", thoughtKey);
         App.getGardenGraph().upsert(clonedOpNode);
         idMapping.put(operationNode.getId(), clonedOpNode.getId());
      }

      // clone the "thought_result" node
      List<Node> resultNodes = App.getGardenGraph().queryNodes("thought_result", thoughtKeyQueryClause);
      if (resultNodes.size() != 1) {
         throw new RuntimeException("expected one Result (" + resultNodes + ")");
      }
      Node clonedResultNode = cloneNode(resultNodes.get(0));
      clonedResultNode.addAttribute("thought_key", thoughtKey);
      App.getGardenGraph().upsert(clonedResultNode);
      idMapping.put(resultNodes.get(0).getId(), clonedResultNode.getId());

      // clone the "thought_sequence" edges
      List<Edge> sequenceEdges = App.getGardenGraph().queryEdges("thought_sequence", thoughtKeyQueryClause);
      for (Edge sequenceEdge : sequenceEdges) {
         Edge clonedSequenceEdge = cloneEdge(sequenceEdge);
         clonedSequenceEdge.setFrom(idMapping.get(sequenceEdge.getFrom()));
         clonedSequenceEdge.setTo(idMapping.get(sequenceEdge.getTo()));
         clonedSequenceEdge.addAttribute("thought_key", thoughtKey);
         App.getGardenGraph().upsert(clonedSequenceEdge);
      }

      return new Thought(clonedThoughtNode);
   }

// TODO: move to KGraph
   private Node cloneNode(Node node, String clonedNodeKey) {
      // deep copy
      final Node clonedNode = new Node(clonedNodeKey, node.getType());
      Map<String, Object> nodeProps = node.getProperties();
      
      Map<String, Object> propsCopy = new HashMap<>();
      for(String nodeKey : nodeProps.keySet()) {
         propsCopy.put(nodeKey, nodeProps.get(nodeKey));
      }
            
      clonedNode.setProperties(propsCopy);
      return clonedNode;
   }

   // TODO: move to KGraph
   private Edge cloneEdge(Edge edge) {
      String clonedEdgeKey = ElementHelper.generateKey();
      /*
       * TODO: the following string splits is a hack, there should be a KGraph 
       * Method to create a new edge given IDs instead of Keys.
       */
      String edgeFrom = edge.getFrom().split("/", 2)[1];
      String edgeTo = edge.getTo().split("/", 2)[1];
      Edge clonedEdge = new Edge(clonedEdgeKey, edgeFrom, edgeTo, edge.getLeftType(), edge.getRightType(), edge.getType());
      Map<String, Object> nodeProps = edge.getProperties();
      clonedEdge.setProperties(nodeProps);
      return clonedEdge;
   }

   // TODO: move to KGraph
   private Node cloneNode(Node node) {
      String clonedNodeKey = ElementHelper.generateKey();
      return cloneNode(node, clonedNodeKey);
   }
   

   public String exportJson() throws Exception {
      List<Element> elements = gatherContents();
      String result = Element.toJson(elements);
      return result;
   }

   private List<Element> gatherContents() {
      String thoughtKey = _thoughtNode.getKey();
      QueryClause keyQueryClause = new QueryClause("thought_key", QueryClause.Operator.EQUALS, thoughtKey);
      List<Edge> sequenceEdges = App.getGardenGraph().queryEdges("thought_sequence", keyQueryClause);
      List<Node> operationNodes = App.getGardenGraph().queryNodes("thought_operation", keyQueryClause);
      List<Node> resultNodes = App.getGardenGraph().queryNodes("thought_result", keyQueryClause);
      List<Edge> approachEdges = App.getGardenGraph().queryEdges("approach", keyQueryClause);

      List<Element> elements = new ArrayList<>();
      elements.add(_thoughtNode);
      elements.add(_goal);
      elements.addAll(operationNodes);
      elements.addAll(resultNodes);
      elements.addAll(approachEdges);
      elements.addAll(sequenceEdges);
      return elements;
   }

   public String exportDot() throws Exception {
      List<Element> elements = gatherContents();
      String result = Element.toDot(elements);
      return result;
   }

}
