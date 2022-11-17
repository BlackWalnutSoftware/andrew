package com.blackwalnutsoftware.andrew.dataset;

import java.time.LocalDate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DateUtilsTest {

   private DateUtils dateUtils = new DateUtils();
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Test
   public void getDateLong_valid_correct() {
      LocalDate localDate1 = LocalDate.parse("1990-01-01");
      LocalDate localDate2 = LocalDate.parse("1990-01-31");
      Long localDate1Long = dateUtils.getDateLong(localDate1);
      Long localDate2Long = dateUtils.getDateLong(localDate2);
      assert (7305 == localDate1Long) : localDate1Long;
      assert (30 == localDate2Long - localDate1Long) : localDate2Long + ", " + localDate1Long;
   }

   @Test
   public void getDateFromNumber_valid_correct() {
      LocalDate localDate1 = LocalDate.parse("1990-01-01");
      Long localDate1Long = dateUtils.getDateLong(localDate1);
      String result = dateUtils.getDateFromNumber(localDate1Long);
      assert ("Monday, 01/01/1990".equals(result)) : result;
   }
}
