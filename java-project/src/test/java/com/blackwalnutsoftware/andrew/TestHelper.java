package com.blackwalnutsoftware.andrew;

import com.arangodb.model.TraversalOptions.Direction;
import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.ElementHelper;
import com.blackwalnutsoftware.kgraph.engine.Node;

public class TestHelper {

   private TestHelper() {
      // do nothing.  static class.
   }
   
   
   public static Thought buildModifiedInitialTestThoughtWithNoEnd() {
      String thoughtKey = ElementHelper.generateKey();
      
      //create the goal
      final Node goalNode = new Node(ElementHelper.generateKey(), "goal");
      goalNode.addAttribute("relationType", "LinearDatasetEdge");
      goalNode.addAttribute("direction", Direction.outbound.toString());
      goalNode.addAttribute("name", "goalNode" );
      goalNode.addAttribute("distance", 10);
      goalNode.addAttribute("startingType", "LinearDatasetNode");
      goalNode.addAttribute("targetProperty", "value");
      goalNode.addAttribute("resultClass", "Float");

      
      // create a thought node
      final Node n1 = new Node(thoughtKey, "thought");
      n1.addAttribute("name", "n1" );

      // create steps
      final Node n2 = new Node(ElementHelper.generateKey(), "thought_operation");
      n2.addAttribute("thought_key", n1.getKey());
      n2.addAttribute("name", "n2" );
      n2.addAttribute("operationName", "doNothing");
      
      Edge e1 = new Edge(ElementHelper.generateKey(), n1, n2, "thought_sequence");
      e1.addAttribute("thought_key", n1.getKey());
      e1.addAttribute("name", "e1" );
      e1.addAttribute("input", "targetPropValue" );
      e1.addAttribute("output", "floatA");
      
      // link the thought process using valid sequence relationships
      Edge e0 = new Edge(ElementHelper.generateKey(), goalNode, n1, "approach");
      e0.addAttribute("thought_key", n1.getKey());
      e0.addAttribute("name", "e0" );
      
      App.getGardenGraph().upsert(goalNode, n1, n2);
      App.getGardenGraph().upsert(e0, e1);
      
      return new Thought(thoughtKey);
   }

   public static Thought buildModifiedInitialTestThoughtWithBadNode() {
      String thoughtKey = ElementHelper.generateKey();
      
      //create the goal
      final Node goalNode = new Node(ElementHelper.generateKey(), "goal");
      goalNode.addAttribute("relationType", "LinearDatasetEdge");
      goalNode.addAttribute("direction", Direction.outbound.toString());
      goalNode.addAttribute("name", "goalNode" );
      goalNode.addAttribute("distance", 10);
      goalNode.addAttribute("targetProperty", "value");
      goalNode.addAttribute("resultClass", "Float");

      
      // create a thought node
      final Node n1 = new Node(thoughtKey, "thought");
      n1.addAttribute("name", "n1" );

      // create steps
      final Node n2 = new Node(ElementHelper.generateKey(), "bad_type");
      n2.addAttribute("thought_key", n1.getKey());
      n2.addAttribute("name", "n2" );
      n2.addAttribute("operationName", "multiply");
      
      Edge e1 = new Edge(ElementHelper.generateKey(), n1, n2, "thought_sequence");
      e1.addAttribute("thought_key", n1.getKey());
      e1.addAttribute("name", "e1" );
      e1.addAttribute("input", "targetPropValue" );
      e1.addAttribute("output", "floatA");
      
      // link the thought process using valid sequence relationships
      Edge e0 = new Edge(ElementHelper.generateKey(), goalNode, n1, "approach");
      e0.addAttribute("name", "e0" );
      
      App.getGardenGraph().upsert(goalNode, n1, n2);
      App.getGardenGraph().upsert(e0, e1);
      
      return new Thought(thoughtKey);
   }

   public static Thought buildModifiedInitialTestThought() {
      // Note: see "modified initial example" flow from thought_process_language.html for reference.

      String thoughtKey = ElementHelper.generateKey();
      
      //create the goal
      final Node goalNode = new Node(ElementHelper.generateKey(), "goal");
      goalNode.addAttribute("startingType", "LinearDatasetNode");
      goalNode.addAttribute("relationType", "LinearDatasetEdge");
      goalNode.addAttribute("direction", Direction.outbound.toString());
      goalNode.addAttribute("name", "goalNode" );
      goalNode.addAttribute("distance", 10);
      goalNode.addAttribute("targetProperty", "value");
      goalNode.addAttribute("resultClass", "Float");

      
      // create a thought node
      final Node n1 = new Node(thoughtKey, "thought");
      n1.addAttribute("name", "n1" );

      // create steps
      final Node n2 = new Node(ElementHelper.generateKey(), "thought_operation");
      n2.addAttribute("thought_key", n1.getKey());
      n2.addAttribute("name", "n2" );
      n2.addAttribute("operationName", "multiply");
      
      final Node n3 = new Node(ElementHelper.generateKey(), "thought_operation");
      n3.addAttribute("thought_key", n1.getKey());
      n3.addAttribute("name", "n3" );
      n3.addAttribute("operationName", "traverse");
      
      final Node n4 = new Node(ElementHelper.generateKey(), "thought_operation");
      n4.addAttribute("thought_key", n1.getKey());
      n4.addAttribute("name", "n4" );
      n4.addAttribute("operationName", "subtract");

      final Node n5 = new Node(ElementHelper.generateKey(), "thought_result");
      n5.addAttribute("thought_key", n1.getKey());
      n5.addAttribute("name", "n5" );
      n5.addAttribute("operationName", "end");

      final Node n6 = new Node(ElementHelper.generateKey(), "thought_operation");
      n6.addAttribute("thought_key", n1.getKey());
      n6.addAttribute("name", "n6" );
      n6.addAttribute("operationName", "multiply");
      
      // link the thought process using valid sequence relationships
      Edge e0 = new Edge(ElementHelper.generateKey(), goalNode, n1, "approach");
      e0.addAttribute("name", "e0" );
      e0.addAttribute("thought_key", n1.getKey());
      
      Edge e1 = new Edge(ElementHelper.generateKey(), n1, n2, "thought_sequence");
      e1.addAttribute("thought_key", n1.getKey());
      e1.addAttribute("name", "e1" );
      e1.addAttribute("description", "starting node target property value." );
      e1.addAttribute("input", "targetPropValue" );
      e1.addAttribute("mutation_range", "FLOAT:>0" );
      e1.addAttribute("output", "floatA");
      
      Edge e2 = new Edge(ElementHelper.generateKey(), n1, n2, "thought_sequence");
      e2.addAttribute("thought_key", n1.getKey());
      e2.addAttribute("name", "e2" );
      e2.addAttribute("input", "NUMBER.2" );
      e2.addAttribute("mutation_range", "FLOAT:>0" );
      e2.addAttribute("output", "floatB");

      Edge e3 = new Edge(ElementHelper.generateKey(), n1, n3, "thought_sequence");
      e3.addAttribute("thought_key", n1.getKey());
      e3.addAttribute("name", "e3" );
      e3.addAttribute("input", "startingNode" );
      e3.addAttribute("output", "startingNode");
      
      Edge e4 = new Edge(ElementHelper.generateKey(), n1, n3, "thought_sequence");
      e4.addAttribute("thought_key", n1.getKey());
      e4.addAttribute("name", "e4" );
      e4.addAttribute("input", "relationType" );
      e4.addAttribute("output", "traversalEdgeType");
      
      Edge e5 = new Edge(ElementHelper.generateKey(), n1, n3, "thought_sequence");
      e5.addAttribute("thought_key", n1.getKey());
      e5.addAttribute("name", "e5" );
      e5.addAttribute("input", "direction" );
      e5.addAttribute("output", "direction");

      Edge e6 = new Edge(ElementHelper.generateKey(), n1, n6, "thought_sequence");
      e6.addAttribute("thought_key", n1.getKey());
      e6.addAttribute("name", "e6" );
      e6.addAttribute("mutation_range", "FLOAT:>0" );
      e6.addAttribute("input", "distance" );
      e6.addAttribute("output", "floatA");
      
      Edge e7 = new Edge(ElementHelper.generateKey(), n2, n4, "thought_sequence");
      e7.addAttribute("thought_key", n1.getKey());
      e7.addAttribute("name", "e7" );
      e7.addAttribute("input", "RESULT" );
      e7.addAttribute("output", "floatA");

      Edge e8 = new Edge(ElementHelper.generateKey(), n3, n4, "thought_sequence");
      e8.addAttribute("thought_key", n1.getKey());
      e8.addAttribute("name", "e8" );
      e8.addAttribute("mutation_range", "FLOAT:>0" );
      e8.addAttribute("input", "RESULT.value" );
      e8.addAttribute("output", "floatB");
      
      Edge e9 = new Edge(ElementHelper.generateKey(), n4, n5, "thought_sequence");
      e9.addAttribute("thought_key", n1.getKey());
      e9.addAttribute("name", "e9" );
      e9.addAttribute("mutation_range", "FLOAT:>0" );
      e9.addAttribute("input", "RESULT" );
      e9.addAttribute("output", "output");

      Edge e10 = new Edge(ElementHelper.generateKey(), n1, n6, "thought_sequence");
      e10.addAttribute("thought_key", n1.getKey());
      e10.addAttribute("name", "e10" );
      e10.addAttribute("input", "NUMBER.-1" );
      e10.addAttribute("output", "floatB");
      
      Edge e11 = new Edge(ElementHelper.generateKey(), n6, n3, "thought_sequence");
      e11.addAttribute("thought_key", n1.getKey());
      e11.addAttribute("name", "e11" );
      e11.addAttribute("mutation_range", "FLOAT:>0" );
      e11.addAttribute("input", "RESULT" );
      e11.addAttribute("output", "distance");
      
      
      App.getGardenGraph().upsert(goalNode, n1, n2, n3, n4, n5, n6);
      App.getGardenGraph().upsert(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
      
      return new Thought(thoughtKey);
   }
   
   public static Thought buildMultiplicationThought(Float multiplier) {
      //create the goal
      final Node goalNode = new Node(ElementHelper.generateKey(), "goal");
      goalNode.addAttribute("startingType", "LinearDatasetNode");
      goalNode.addAttribute("relationType", "LinearDatasetEdge");
      goalNode.addAttribute("direction", Direction.outbound.toString());
      goalNode.addAttribute("name", "goalNode" );
      goalNode.addAttribute("distance", 10);
      goalNode.addAttribute("targetProperty", "value");
      goalNode.addAttribute("resultClass", "Float");
      
      App.getGardenGraph().upsert(goalNode);
      
      return buildMultiplicationThought(goalNode, multiplier);
   }

   public static Thought buildMultiplicationThought(Node goal, Float multiplier) {
      String thoughtKey = ElementHelper.generateKey();
      
      // create a thought node
      final Node n1 = new Node(thoughtKey, "thought");
      n1.addAttribute("name", "n1" );
      
      final Node n2 = new Node(ElementHelper.generateKey(), "thought_operation");
      n2.addAttribute("thought_key", n1.getKey());
      n2.addAttribute("name", "n6" );
      n2.addAttribute("operationName", "multiply");
      
      final Node end = new Node(ElementHelper.generateKey(), "thought_result");
      end.addAttribute("thought_key", n1.getKey());
      end.addAttribute("name", "end" );
      end.addAttribute("operationName", "end");
      
      // link the thought process using valid sequence relationships
      Edge e0 = new Edge(ElementHelper.generateKey(), goal, n1, "approach");
      e0.addAttribute("thought_key", n1.getKey());
      e0.addAttribute("name", "e0" );

      Edge e1 = new Edge(ElementHelper.generateKey(), n1, n2, "thought_sequence");
      e1.addAttribute("name", "e1" );
      e1.addAttribute("thought_key", n1.getKey());
      e1.addAttribute("input", "targetPropValue" );
      e1.addAttribute("output", "floatA");
      
      Edge e2 = new Edge(ElementHelper.generateKey(), n1, n2, "thought_sequence");
      String multiplierString = "NUMBER." + multiplier;
      e2.addAttribute("name", "e2" );
      e2.addAttribute("thought_key", n1.getKey());
      e2.addAttribute("input", multiplierString );
      e2.addAttribute("output", "floatB");
      
      Edge e3 = new Edge(ElementHelper.generateKey(), n2, end, "thought_sequence");
      e3.addAttribute("name", "e3" );
      e3.addAttribute("thought_key", n1.getKey());
      e3.addAttribute("input", "RESULT" );
      e3.addAttribute("output", "output");
      
      App.getGardenGraph().upsert(n1, n2, end);
      App.getGardenGraph().upsert(e0, e1, e2, e3);
      
      return new Thought(thoughtKey);
   }
 
}
