package com.blackwalnutsoftware.andrew.dataset;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

   private static LocalDate epoch = LocalDate.ofEpochDay(0);
   
   public Long getDateLong(LocalDate localDate) {
      Long daysSinceEpoch = ChronoUnit.DAYS.between(epoch, localDate);
      return daysSinceEpoch;
   }

   public String getDateFromNumber(Long dateNumber) {
      LocalDate date = LocalDate.ofEpochDay(dateNumber);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/YYYY"); 
      return formatter.format(date);
   }
}
