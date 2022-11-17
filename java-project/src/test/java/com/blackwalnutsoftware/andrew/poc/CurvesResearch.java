package com.blackwalnutsoftware.andrew.poc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CurvesResearch {

   private static Logger logger = LogManager.getLogger(CurvesResearch.class);

   
   public static void main(String[] args) {

      for(int i = 0; i<10; i++) {
         // most mutations will be close to initial value: y = (2x-1)^3+1
         // even tighter: y = (2x-1)^6+1
         // TODO: should force of mutations be related to member's fit?
         double random = Math.random();
         double clustered = Math.pow((2*random)-1, 3)+1;
         double tightClustered = Math.pow((2*random)-1, 5)+1;
         double looseClustered = Math.pow((2*random)-1, 1.5)+1;
         // TODO: reflect the curve around the midpoint to allow for more exponent values
         logger.debug("random: " + random);
         logger.debug("clustered: " + clustered);
         logger.debug("tightClustered: " + tightClustered);
         logger.debug("looseClustered: " + looseClustered);
         logger.debug("-------------------------------------------");
      }



   }

}
