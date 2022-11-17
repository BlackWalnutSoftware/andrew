package com.blackwalnutsoftware.andrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

import com.blackwalnutsoftware.kgraph.engine.Edge;
import com.blackwalnutsoftware.kgraph.engine.Node;

/**
 * The Class Goal.
 */
public class Goal {

   /** The goal. */
   private Node _goal;

   /**
    * Instantiates a new goal.
    *
    * @param goal the goal
    */
   public Goal(Node goal) {
      _goal = goal;
   }

   /**
    * Gets the node.
    *
    * @return the node
    */
   public Node getNode() {
      return _goal;
   }

   /**
    * Gets the thoughts.
    *
    * @return the thoughts
    */
   public List<Thought> getThoughts() {
      List<Thought> results = new ArrayList<>();
      List<Triple<Node, Edge, Node>> triples = App.getGardenGraph().expandRight(_goal, "approach", null, null);
      for(Triple<Node, Edge, Node> triple : triples) {
         Thought thought = new Thought(triple.getRight());
         results.add(thought);
      }

      return results;
   }

   /**
    * Sets the training parameters.
    *
    * @param trainingParameters the training parameters
    * @return the map
    */
   public Map<String, Object> setTrainingParameters(TrainingParameters trainingParameters) {
      Map<String, Object> workingMemory = new HashMap<>();
      for(String key : trainingParameters.getGoalAttributes() ) {
         Object value = trainingParameters.getRandomValue(key);
         workingMemory.put("GOAL." + key, value);
         _goal.addAttribute(key, value);
      }
      return workingMemory;
   }

   /**
    * Sets the property.
    *
    * @param key the key
    * @param value the value
    */
   public void setProperty(String key, Object value) {
      _goal.addAttribute(key, value);     
   }

}
