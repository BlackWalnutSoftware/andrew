package com.blackwalnutsoftware.andrew;

import java.time.LocalDate;

import com.blackwalnutsoftware.andrew.dataset.DateUtils;

/**
 * The Class TrainingCriteria.
 * <P>
 * Training criteria specifies specific values that are to be used in
 * the training of thoughts for a given goal.
 */
public class TrainingCriteria {

   private static DateUtils dateUtils = new DateUtils();

   private Integer _numGenerations;
   private Integer _questsPerGeneration;
   private Long _startDateLong;
   private Long _endDateLong;
   private Integer _maturationAge;
   private Integer _maxPopulation;

   public TrainingCriteria(Integer numGenerations, Integer questsPerGeneration, LocalDate startDate, LocalDate endDate, Integer maturationAge,
         Integer maxPopulation) {
      _numGenerations = numGenerations;
      _questsPerGeneration = questsPerGeneration;
      _startDateLong = dateUtils.getDateLong(startDate);
      _endDateLong = dateUtils.getDateLong(endDate);
      _maturationAge = maturationAge;
      _maxPopulation = maxPopulation;
   }

   public Integer getNumGenerations() {
      return _numGenerations;
   }

   public Integer getQuestsPerGeneration() {
      return _questsPerGeneration;
   }

   public Long getStartDateLong() {
      return _startDateLong;
   }

   public Long getEndDateLong() {
      return _endDateLong;
   }
   
   public Integer getMaturationAge() {
      return _maturationAge;
   }
   
   public Integer getMaxPopulation() {
      return _maxPopulation;
   }
   
   public String toString() {
      return "   \"numGenerations\" : " + getNumGenerations() +", \n" +
            "   \"questsPerGeneration\" : " + getQuestsPerGeneration() +", \n" +
            "   \"startDate\" : \"" + dateUtils.getDateFromNumber(getStartDateLong()) +"\", \n" +
            "   \"endDate\" : \"" + dateUtils.getDateFromNumber(getEndDateLong()) +"\", \n" +
            "   \"maturationAge\" : " + getMaturationAge() +", \n" +
            "   \"maxPopulation\" : " + getMaxPopulation() +" \n" ;
   }
}
