/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.gnslocationclient;

//import edu.umass.cs.gns.client.DesktopGnsClient;
//import edu.umass.cs.gns.client.GuidEntry;
//import edu.umass.cs.gns.client.util.ServerSelectDialog;
import edu.umass.cs.gns.geodesy.GlobalCoordinate;
import edu.umass.cs.gns.utils.GuidUtils;
import edu.umass.cs.gns.utils.Logging;
import java.util.logging.Logger;

/**
 * Contains the main routine of this demonstration GNS client which
 * simulates mobile devices wondering around an area and exchanging messages 
 * using the GNS.
 * 
 * @author westy
 */
public class GNSLocationClient {

  public static final GlobalCoordinate STARTING_POINT = new GlobalCoordinate(42.39352, -72.530837); // On UMass campus
  public static String gnsHost = null; // set below using a dialog
  public static int gnsPort = 8080;
  private static final String CLIENT_ACCOUNT_NAME = "david@westy.org";
  //
  private DesktopGnsClient gnsClient;
  private GuidEntry guid;

  public GuidEntry getGuid() {
    return guid;
  }
  
  public static String getGNSHostPortString() {
    return gnsHost + ":" + gnsPort;
  }

  // make it a singleton class
  public static GNSLocationClient getInstance() {
    return GNSLocationClientHolder.INSTANCE;
  }

  private static class GNSLocationClientHolder {

    private static final GNSLocationClient INSTANCE = new GNSLocationClient();
  }

  private GNSLocationClient() {
    gnsHost = ServerSelectDialog.selectServer();
    gnsClient = new DesktopGnsClient(GNSLocationClient.gnsHost, GNSLocationClient.gnsPort);
    try {
      guid = GuidUtils.lookupOrCreateAccountGuid(gnsClient, CLIENT_ACCOUNT_NAME);
    } catch (Exception e) {
      GNSLocationClient.getLogger().warning("Unable to create GUID for " + CLIENT_ACCOUNT_NAME + ": " + e);
    }
  }

  public GuidEntry getGuidEntry() {
    return guid;
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // Make some walkers
    Walkers.getInstance().initWalkers();
    // Make the observer and it's map
    ObserverWithMap.getInstance().init();
  }
  /**
   * Logging level for main logger
   */
  public static String fileLoggingLevel = "FINE";
  /**
   * Console output level for main logger
   */
  public static String consoleOutputLevel = "FINE";
  private final static Logger LOGGER = Logger.getLogger(GNSLocationClient.class.getName());
  public static boolean initRun = false;

  public static Logger getLogger() {
    if (!initRun) {
      //Logging.setupLogger(LOGGER, consoleOutputLevel, fileLoggingLevel, "log" + "/gns.xml");
      initRun = true;
    }
    return LOGGER;
  }
}
