package com.blackwalnutsoftware.andrew.process;

public class ThoughtScore implements Comparable<ThoughtScore> {

   private String _thoughtKey;
   private Float _score;

   public ThoughtScore(String thoughtKey, Float score) {
      _thoughtKey = thoughtKey;
      _score = score;
   }
   
   public String getThoughtKey() {
      return _thoughtKey;
   }
   
   public Float getThoughtScore() {
      return _score;
   }

   @Override
   public int compareTo(ThoughtScore o) {
      return this.getThoughtScore().compareTo(o.getThoughtScore());
   }

   public String toString(){
      return "\n   {\"key\":\"" + _thoughtKey + "\", \"score\":" + _score + "}";
   }
}
