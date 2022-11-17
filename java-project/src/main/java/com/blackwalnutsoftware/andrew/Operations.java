package com.blackwalnutsoftware.andrew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.arangodb.model.TraversalOptions.Direction;
import com.blackwalnutsoftware.andrew.dataset.DateUtils;
import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.Node;

/*
 * TODO: This class is an abomination.  The contents of the methods and the 
 * descriptions for I/O interfaces should be managed within the thought_operation
 * nodes themselves.  Andrew should have no special knowledge of the operations,
 * just know if and where they are useful in constructing a good thought.
 * 
 * The post-version-one implementation should relegate Andrew as a processor
 * of thought operation logic.  (The likely implementation having Andrew
 * running a Groovy processor for the thought operation methods.)
 */

/**
 * The Class Operations.
 */
public class Operations {

   private static DateUtils dateUtils = new DateUtils();

   /** The logger. */
   private static Logger logger = LogManager.getLogger(Operations.class);

   /**
    * Instantiates a new operations.
    */
   private Operations() {
      // static class
   }

   /**
    * Traverse.
    *
    * @param currentNode the current node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> traverse(Node currentNode, Map<String, Object> inputs) {
      logger.debug("Operation.traverse() ");
      Map<String, Object> results = new HashMap<>();

      Node startingNode = (Node) inputs.get(currentNode.getKey() + "." + "startingNode");
      String direction = (String) inputs.get(currentNode.getKey() + "." + "direction");
      String relationType = (String) inputs.get(currentNode.getKey() + "." + "traversalEdgeType");
      Integer distance = ((Number) inputs.get(currentNode.getKey() + "." + "distance")).intValue();

      Node previousNode = traverse(startingNode, direction, relationType, distance);

      results.put("RESULT", previousNode);
      return results;
   }

   /**
    * Traverse.
    *
    * @param startingNode the starting node
    * @param direction the direction
    * @param relationType the relation type
    * @param distance the distance
    * @return the node
    */
   public static Node traverse(Node startingNode, String direction, String relationType, Integer distance) {
      logger.debug("traverse() ");
      logger.debug("   startingNode = " + startingNode);
      logger.debug("   direction = " + direction);
      logger.debug("   relationType = " + relationType);
      logger.debug("   distance = " + distance);

      // support negative distances
      if (distance < 0) {
         if (Direction.outbound.toString().equals(direction)) {
            direction = Direction.inbound.toString();
         } else if (Direction.inbound.toString().equals(direction)) {
            direction = Direction.outbound.toString();
         }
         distance = distance * (-1);
      }

      Node previousNode = startingNode;
      List<Triple<Node, Edge, Node>> expansions = null;

      for (int i = 0; i < distance; i++) {
         if (Direction.outbound.toString().equals(direction)) {
            expansions = App.getDataGraph().expandRight(previousNode, relationType, null, null);
         } else if (Direction.inbound.toString().equals(direction)) {
            expansions = App.getDataGraph().expandLeft(previousNode, relationType, null, null);
         } else {
            throw new IllegalArgumentException("Invalid direction. (" + direction + ")");
         }
         previousNode = expansions.get(0).getRight();
      }
      return previousNode;
   }

   /**
    * Gets the "stockOnDate" relation between the given "date" object that is the greatest date less than
    * the given value, and the "stockSymbol" object with the input symbol value.
    * <p>
    * This method is a hack.  In reality it should be a set of nodes and edges, but for the POC
    * code it simplifies what would otherwise be a much larger thought graph.  (I wonder if this
    * will eventually face a similar problem as the Wikipedia-size proof.)
    *
    * @param node the node
    * @param inputs the inputs
    * @return the symbol date rel
    */
   public static Map<String, Object> getSymbolDateRel(Node currentNode, Map<String, Object> inputs) {
      logger.debug("getSymbolDateRel()");

      try {
         Map<String, Object> results = new HashMap<>();

         // get the inputs
         String symbol = (String) inputs.get(currentNode.getKey() + "." + "symbol");
         Integer dateNumber = ((Number) inputs.get(currentNode.getKey() + "." + "dateNumber")).intValue();
         String prop = (String) inputs.get("GOAL.targetProperty");

         // get the date node
         String query1 = "FOR t IN date FILTER t.dateNumber <= @time SORT t.dateNumber DESC LIMIT 1 RETURN t";
         logger.debug("query: " + query1);
         logger.debug("dateNumber: " + dateNumber);
         Map<String, Object> bindVars1 = Collections.singletonMap("time", dateNumber);
         List<Node> queryResults1 = App.getDataGraph().queryNodes(query1, bindVars1);
         Node dateNode = queryResults1.get(0);
         logger.debug("dateNode = " + dateNode);

         // get the stockSymbol node
         String query2 = "FOR t IN stockSymbol FILTER t.symbol == @symbol LIMIT 1 RETURN t";
         logger.debug("query2: " + query2);
         logger.debug("symbol: " + symbol);
         Map<String, Object> bindVars2 = Collections.singletonMap("symbol", symbol);
         List<Node> queryResults2 = App.getDataGraph().queryNodes(query2, bindVars2);
         Node stockNode = queryResults2.get(0);
         logger.debug("stockNode = " + stockNode);

         // get the stockOnDate rel
         Map<String, Object> bindVars3 = new HashMap<>();
         String query3 = null;

         query3 = "FOR t IN stockOnDate FILTER t._from == @left AND t._to == @to RETURN t";
         logger.debug("query: " + query3);

         bindVars3.put("left", stockNode.getId());
         bindVars3.put("to", dateNode.getId());
         logger.debug("@left = " + stockNode.getId());
         logger.debug("@to = " + dateNode.getId());

         List<Edge> queryResults3 = App.getDataGraph().queryEdges(query3, bindVars3);
         Edge relEdge = queryResults3.get(0);
         logger.debug("relEdge = " + relEdge);

         Float result = ((Number) relEdge.getAttribute(prop)).floatValue();

         results.put("RESULT", result);
         return results;
      } catch (Exception e) {
         // currentNode, Map<String, Object> inputs
         logger.error(e.getMessage() + "\n   currentNode = " + currentNode + "\n   inputs = " + inputs);
         throw new RuntimeException(e);
      }

   }

   /**
    * Gets the greatest date less than.
    *
    * @param node the node
    * @param inputs the inputs
    * @return the greatest date less than
    */
   public static Map<String, Object> getGreatestDateLessThan(Node node, Map<String, Object> inputs) {
      logger.debug("getGreatestLessThan() ");
      Map<String, Object> results = new HashMap<>();
      Float dateNumber = ((Number) inputs.get(node.getKey() + "." + "dateNumber")).floatValue();

      String query = "FOR t IN date FILTER t.dateNumber <= @dateNumber SORT t.time DESC LIMIT 1 RETURN t";
      logger.debug("query: " + query);
      Map<String, Object> bindVars = Collections.singletonMap("dateNumber", dateNumber);

      List<Node> queryResults = App.getDataGraph().queryNodes(query, bindVars);

      results.put("RESULT", queryResults.get(0));
      return results;
   }

   /**
    * Multiply.
    *
    * @param node the node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> multiply(Node node, Map<String, Object> inputs) {
      logger.debug("multiply(" + node.getKey() + ", ...)");
      Map<String, Object> results = new HashMap<>();
      Float floatA = ((Number) inputs.get(node.getKey() + "." + "floatA")).floatValue();
      Float floatB = ((Number) inputs.get(node.getKey() + "." + "floatB")).floatValue();
      Float result = floatA * floatB;
      results.put("RESULT", result);
      return results;
   }

   /**
    * Average.
    *
    * @param node the node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> average(Node node, Map<String, Object> inputs) {
      logger.debug("average() ");
      Map<String, Object> results = new HashMap<>();

      Boolean hasInputs = true;
      ArrayList<Double> values = new ArrayList<>();
      Integer inputNumber = 1;
      while (hasInputs) {
         Number inputValue = (Number) inputs.get(node.getKey() + "." + "input" + inputNumber);
         if (null != inputValue) {
            Double value = inputValue.doubleValue();
            values.add(value);
            logger.debug("values = " + values);
            inputNumber++;
         } else {
            hasInputs = false;
         }
      }

      OptionalDouble result = values.stream().mapToDouble(a -> a).average();

      logger.debug("average.RESULT = " + result.getAsDouble());

      results.put("RESULT", result.getAsDouble());
      return results;
   }

   /**
    * Do nothing.
    *
    * @param node the node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> doNothing(Node node, Map<String, Object> inputs) {
      logger.debug("doNothing() ");
      Map<String, Object> results = new HashMap<>();
      return results;
   }

   /**
    * Subtract.
    *<p>
    *Assumes that <code>inputs</code> contains:
    *<ul>
    *<li>"floatA" as a <code>Number</code>
    *<li>"floatB" as a <code>Number</code>
    *</ul>
    *<p>
    *formula: <code>RESULT = floatA - floatB</code>
    *
    * @param node the node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> subtract(Node node, Map<String, Object> inputs) {
      Map<String, Object> results = new HashMap<>();

      Float floatA = ((Number) inputs.get(node.getKey() + "." + "floatA")).floatValue();
      Float floatB = ((Number) inputs.get(node.getKey() + "." + "floatB")).floatValue();
      logger.debug("subtract: " + floatA + " - " + floatB);
      logger.debug("   current node: " + node.getKey());

      Float result = floatA - floatB;
      results.put("RESULT", result);
      return results;
   }

   /**
    * add.
    *<p>
    *Assumes that <code>inputs</code> contains:
    *<ul>
    *<li>"floatA" as a <code>Number</code>
    *<li>"floatB" as a <code>Number</code>
    *</ul>
    *<p>
    *formula: <code>RESULT = floatA + floatB</code>
    *
    * @param node the node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> add(Node node, Map<String, Object> inputs) {
      Map<String, Object> results = new HashMap<>();

      Float floatA = ((Number) inputs.get(node.getKey() + "." + "floatA")).floatValue();
      Float floatB = ((Number) inputs.get(node.getKey() + "." + "floatB")).floatValue();
      logger.debug("add: " + floatA + " + " + floatB);
      logger.debug("   current node: " + node.getKey());

      Float result = floatA + floatB;
      results.put("RESULT", result);
      return results;
   }

   /**
    * End.
    *
    * @param node the node
    * @param inputs the inputs
    * @return the map
    */
   public static Map<String, Object> end(Node node, Map<String, Object> inputs) {
      return inputs;
   }

}
