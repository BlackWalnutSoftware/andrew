package com.blackwalnutsoftware.andrew.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackwalnutsoftware.andrew.App;
import com.blackwalnutsoftware.andrew.dataset.CSVLoader;

public class CSVLoaderTest {

   private static Logger logger = LogManager.getLogger(CSVLoaderTest.class);

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
   public void loadStocks_goodFiles_loaded() throws Exception {
      App.getDataGraph().flush();
      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/stock_data_fetcher/data/AACG_2020-05-07.txt"));
      // AAPL contains some bad lines
      files.add(new File("../fetchers/stock_data_fetcher/data/AAPL_2020-05-07.txt"));

      CSVLoader stockLoader = new CSVLoader();

      stockLoader.loadStocks(files);

      Long count = App.getDataGraph().getCount("stockOnDate");

      logger.debug("count = " + count);

      assert (count > 10000);
      assert (count < 11000);
   }

   @Test
   public void load_goodVixAndW5k_loaded() throws Exception {
      App.getDataGraph().flush();
      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/index_fetcher/data/VIX.csv"));
      files.add(new File("../fetchers/index_fetcher/data/w5k.csv"));

      CSVLoader stockLoader = new CSVLoader();

      stockLoader.loadStocks(files);

      Long count = App.getDataGraph().getCount("stockOnDate");

      logger.debug("count = " + count);

      assert (count > 16000);
      assert (count < 17000);
   }

   @Test(expected = FileNotFoundException.class)
   public void loadStocks_badFile_exception() throws FileNotFoundException {
      List<File> files = new ArrayList<>();
      files.add(new File("../fetchers/stock_data_fetcher/data/BAD_FILE.txt"));

      CSVLoader stockLoader = new CSVLoader();

      stockLoader.loadStocks(files);
   }

   @Test
   public void loadQuarterlyPercentData_quarterlyUSGDP_loaded() throws Exception {
      File file = new File("../fetchers/us_gdp_fetcher/data/us_gdp.csv");

      CSVLoader stockLoader = new CSVLoader();

      stockLoader.loadQuarterlyPercentData("US_GDP", file);

      Long count = App.getDataGraph().getCount("deltaOnDate");
      logger.debug("count = " + count);
      assert (count >= 300);
      assert (count < 400);
   }
}
