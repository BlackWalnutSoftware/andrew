package com.blackwalnutsoftware.andrew.process;

import java.util.Map;

import com.blackwalnutsoftware.andrew.dataset.DateUtils;
import com.blackwalnutsoftware.kgraph.engine.Node;

public class Evaluation {

   private Node _thought;
   private Number _guess;
   private Number _actual;
   private Map<String, Object> _workingMemory;

   private static DateUtils dateUtils = new DateUtils();

   public Evaluation(Node thought, Number guess, Number actual, Map<String, Object> workingMemory) {
      _thought = thought;
      _guess = guess;
      _actual = actual;
      _workingMemory = workingMemory;
   }
   
   public Number getGuess() {
      return _guess;
   }
   
   public Number getActual() {
      return _actual;
   }
   
   public Node getThought() {
      return _thought;
   }
   
   public  Map<String, Object> getWorkingMemory() {
      return _workingMemory;
   }
   
   public String toString(){
      Long startDateLong = (Long)_workingMemory.get("GOAL.startDate");
      String startDateString = dateUtils.getDateFromNumber(startDateLong);
      
      String result =  "\n   { " + 
         "\"guess\" : \"" + getGuess() +
         "\", \"actual\" : \"" + getActual() +
         "\", \"thoughtKey\" : \"" + getThought().getKey() +
         "\", \"startDate\" : \"" + startDateString +
         "\" }";
      return result;
     }
}
