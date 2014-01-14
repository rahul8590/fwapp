/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.gnslocationclient;

import edu.umass.cs.gns.client.DesktopGnsClient;
import edu.umass.cs.gns.client.GnsProtocol;
import edu.umass.cs.gns.client.GuidEntry;
import edu.umass.cs.gns.geodesy.GeodeticCalculator;
import edu.umass.cs.gns.geodesy.GlobalCoordinate;
import edu.umass.cs.gns.utils.GuidUtils;
import edu.umass.cs.gns.utils.JSONUtils;
import edu.umass.cs.gns.utils.Utils;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This guy pops up a map frame on which it plots the location of all the walkers.
 * It finds walkers by doing a select near on the "location" field. It also maintains a
 * group of the walkers that it finds in it's own GUID.
 * 
 * @author westy
 */
/**
 * Generates a random walk 
 */
public class ObserverWithMap implements Runnable {

  private final static int DELAY = 15000;
  private static final double CHECK_RADIUS = Walkers.NOMINAL_SPEED * 10 * 60; // in meters. = N minutes of straight line walking
  private DesktopGnsClient gnsClient;
  // remember that the GNS uses LONG, LAT (X,Y)
  private JSONArray startingPointJson = new JSONArray(Arrays.asList(GNSLocationClient.STARTING_POINT.getLong(),
          GNSLocationClient.STARTING_POINT.getLat()));
  private GuidEntry guid;
  private GuidEntry masterGuid;
  // maps from GUID -> HRN
  private ConcurrentMap<String, String> guidToNameTable;

  // make it a singleton class
  public static ObserverWithMap getInstance() {
    return ObserverHolder.INSTANCE;
  }

  private static class ObserverHolder {

    private static final ObserverWithMap INSTANCE = new ObserverWithMap();
  }

  private ObserverWithMap() {
    guidToNameTable = new ConcurrentHashMap<String, String>();
    gnsClient = new DesktopGnsClient(GNSLocationClient.gnsHost, GNSLocationClient.gnsPort);
  }

  public void init() {
    this.masterGuid = GNSLocationClient.getInstance().getGuid();
    try {
      guid = GuidUtils.lookupOrAddGuid(gnsClient, masterGuid, "Observer");
      new Thread(this, "Observer").start();
    } catch (Exception e) {
      GNSLocationClient.getLogger().warning("Unable to create GUID for Observer: " + e);
    }
  }

  @Override
  public void run() {
    GNSLocationClient.getLogger().info("Starting Map Observer");
    MapFrame mapFrame = new MapFrame();
    mapFrame.setVisible(true);
    mapFrame.setDisplayPosition(GNSLocationClient.STARTING_POINT.getLat(), GNSLocationClient.STARTING_POINT.getLong(), 17);
    Utils.sleep(500); // let everyone else start
    while (true) {
      try {
        update();
        // wait a bit for the GNS to settle
        Utils.sleep(1000);
        mapFrame.update(masterGuid, guid);
        Utils.sleep(DELAY);
      } catch (Exception e) {
        GNSLocationClient.getLogger().warning("Exception Map Observer for "
                + guid.getEntityName() + ": " + e + ". Sleeping for 4 seconds and trying again.");
        Utils.sleep(4000);
      }
    }
  }

  public void update() throws Exception {
     // First we extract all the guids that are nears us from the GNS
    JSONArray result = gnsClient.selectNear(GnsProtocol.LOCATION_FIELD_NAME, startingPointJson, CHECK_RADIUS);
    JSONArray guids = new JSONArray();
    for (int i = 0; i < result.length(); i++) {
      try {
        JSONObject record = result.getJSONObject(i);
        //JSONArray jsonLoc = record.getJSONArray(GnsProtocol.LOCATION_FIELD_NAME);
        //NOTE: database is LONG, LAT and GlobalCoordinates is LAT, LONG
        //GlobalCoordinate location = new GlobalCoordinate(jsonLoc.getDouble(1), jsonLoc.getDouble(0));
        //double distance = GeodeticCalculator.calculateGeodeticCurve(GNSLocationClient.STARTING_POINT, location).getEllipsoidalDistance();
        String guidString = record.getString("GUID");
        //String name = getHumanReadableName(guidString);
//        if (name != null) {
//          System.out.println(String.format("%s is at %s, %5.2f meters from target ", name, location, distance));
//        }
        guids.put(guidString);
      } catch (JSONException e) {
        GNSLocationClient.getLogger().warning("Problem parsing JSON from selected record: " + e);
      }
    }
    // Then we update our group member ship
    JSONArray oldMembers = gnsClient.getGroupMembers(guid.getGuid(), guid);
    JSONArray newMembers = JSONUtils.JSONArraySetDifference(guids, oldMembers);
    if (newMembers.length() > 0) {
      System.out.println(newMembers.length() + " new members");
      gnsClient.addToGroup(guid.getGuid(), newMembers, guid);
    }
    JSONArray membersToRemove = JSONUtils.JSONArraySetDifference(oldMembers, guids);
    if (membersToRemove.length() > 0) {
      System.out.println(membersToRemove.length() + " fewer members");
      gnsClient.removeFromGroup(guid.getGuid(), membersToRemove, guid);
      // We also make sure that if the number of walkers in the area drops to low we make some more
      if (oldMembers.length() < Walkers.getInstance().NUMBER_OF_WALKERS) {
        Walkers.getInstance().newWalker();
        System.out.println("add one more member");
      }
    }
  }

  /**
   * Retrieves and caches the alias of the guid
   * @param guid
   * @return 
   */
  public String getHumanReadableName(String guid) {
    String name;
    if ((name = guidToNameTable.get(guid)) != null) {
      return name;
    } else {
      try {
        JSONObject guidRecord = gnsClient.lookupGuidRecord(guid);
        name = guidRecord.getString(GnsProtocol.GUID_RECORD_NAME);
        guidToNameTable.put(guid, name);
        return name;
      } catch (Exception e) {
        GNSLocationClient.getLogger().warning("Problem parsing JSON from selected record: " + e);
        e.printStackTrace();
        return null;
      }
    }
  }
}
