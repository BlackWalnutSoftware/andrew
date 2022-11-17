package com.blackwalnutsoftware.andrew.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackwalnutsoftware.andrew.App;
import com.blackwalnutsoftware.andrew.dataset.CSVLoader;

public class LoadPerformanceTest {

   private static Logger logger = LogManager.getLogger(LoadPerformanceTest.class);

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      App.initialize("load_performance_tests");
      App.getDataGraph().flush();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      App.getDataGraph().cleanup();
   }

   @Test
   public void loadThreeStocks_goodFiles_loaded() throws Exception {
      // start with a clean DB
      App.getDataGraph().flush();

      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/stock_data_fetcher/data/AA_2020-05-07.txt"));
      files.add(new File("../fetchers/stock_data_fetcher/data/AAAU_2020-05-07.txt"));
      files.add(new File("../fetchers/stock_data_fetcher/data/AACG_2020-05-07.txt"));

      CSVLoader stockLoader = new CSVLoader();

      long startTime = System.currentTimeMillis();
      stockLoader.loadStocks(files);
      long endTime = System.currentTimeMillis();
      long elapsed = (endTime - startTime) / 1000;
      logger.debug("loadStocks_goodFiles_loaded took " + elapsed + " seconds");

      if (elapsed > 10) { // 10 seconds
         logger.warn("loadStocks_goodFiles_loaded took too long!");
      }

      Long count = App.getDataGraph().getCount("stockOnDate");

      assert (count > 10000);
      logger.debug("count = " + count);

   }
   
   // TODO: get this do-able in a reasonable amount of time.
   // @Test
   public void loadAStocks_goodFiles_loaded() throws Exception {

      // start with a clean DB
      App.getDataGraph().flush();

      List<File> files = new ArrayList<>();

      String baseDir = "../fetchers/stock_data_fetcher/data/";
      File f = new File(baseDir);

      String[] baseFileNames = f.list();

      for (String baseFileName : baseFileNames) {
         String filePath = baseDir + baseFileName;
         files.add(new File(filePath));
      }

      CSVLoader stockLoader = new CSVLoader();

      long startTime = System.currentTimeMillis();
      stockLoader.loadStocks(files);
      long endTime = System.currentTimeMillis();
      long elapsed = (endTime - startTime) / 1000;
      logger.debug("loadAStocks_goodFiles_loaded took " + elapsed + " seconds");

      Long count = App.getDataGraph().getCount("stockOnDate");
      logger.debug("count = " + count);
      assert (count > 10000);

      if (elapsed > 10) { // 10 seconds
         logger.warn("loadAStocks_goodFiles_loaded took too long!");
      }

   }
}
