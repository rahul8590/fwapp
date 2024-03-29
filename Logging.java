/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umass.cs.gns.utils;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author westy
 */
public class Logging {

  public static Level DEFAULTCONSOLELEVEL = Level.INFO;
  public static Level DEFAULTFILELEVEL = Level.FINE;
  public static String DEFAULTLOGFILENAME = "log" + System.getProperty("file.separator") + "gns_location_client_log.xml";

  /**
   * Sets up the logger using default values.
   *
   * @param logger
   */
  public static void setupLogger(Logger logger) {
    setupLogger(logger, DEFAULTCONSOLELEVEL.getName(), DEFAULTFILELEVEL.getName(), DEFAULTLOGFILENAME);
  }

  /**
   * Sets up the logger.
   *
   * @param logger
   * @param consoleLevelName
   * @param fileLevelName
   * @param logFilename
   */
  public static void setupLogger(Logger logger, String consoleLevelName, String fileLevelName, String logFilename) {
    File dir = new File(logFilename).getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    Level consoleLevel;
    Level fileLevel;

    try {
      consoleLevel = Level.parse(consoleLevelName);
    } catch (Exception e) {
      consoleLevel = DEFAULTCONSOLELEVEL;
    }
    try {
      fileLevel = Level.parse(fileLevelName);
    } catch (Exception e) {
      fileLevel = DEFAULTFILELEVEL;
    }
    // overall level is ALL
    // Abhigyan: changing it to file level.
    logger.setLevel(fileLevel);
    logger.setUseParentHandlers(false);
    
    try {
      Handler ch = new ConsoleHandler();
      // Use our one-line formatter
      ch.setFormatter(new LogFormatter());
      ch.setLevel(consoleLevel);
      logger.addHandler(ch);
    } catch (Exception e) {
      logger.warning("Unable to attach ConsoleHandler to logger!");
      e.printStackTrace();
    }

    try {
      Handler fh = new FileHandler(logFilename, 40000000, 45);
      fh.setLevel(fileLevel);
      logger.addHandler(fh);
    } catch (Exception e) {
      logger.warning("Unable to attach FileHandler to logger!");
      e.printStackTrace();
    }
  }
}
