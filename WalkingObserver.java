/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
//package edu.umass.cs.gns.gnslocationclient;

import edu.umass.cs.gns.client.DesktopGnsClient;
import edu.umass.cs.gns.client.GnsProtocol;
import edu.umass.cs.gns.client.GuidEntry;
import edu.umass.cs.gns.geodesy.GeodeticCalculator;
import edu.umass.cs.gns.geodesy.GlobalCoordinate;
import edu.umass.cs.gns.utils.Format;
import edu.umass.cs.gns.utils.JSONUtils;
import edu.umass.cs.gns.utils.Utils;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Keep track of all the walkers that are within a radius of it's location.
 * Store them all as group members of it's GUID. It also sends messages
 * (ie., updates the "message" field) of anybody new who comes near.
 * 
 * @author westy
 */

public class WalkingObserver implements Runnable {

  private final static int SLEEP_MEAN = 15000;
  private final static int SLEEP_SD = 2000;
  private static final double CHECK_RADIUS = 75; // in meters.
  public static final String MESSAGES_FIELD = "messages";
  private DesktopGnsClient gnsClient;
  private GuidEntry guid;
  private GuidEntry masterGuid;
  private RandomWalker walker;
  private Random random = new Random();

  public WalkingObserver(GuidEntry masterGuid, GuidEntry guid, RandomWalker walker) {
    this.masterGuid = GNSLocationClient.getInstance().getGuid();
    this.guid = guid;
    this.walker = walker;
    try {
      gnsClient = new DesktopGnsClient(GNSLocationClient.gnsHost, GNSLocationClient.gnsPort);
      new Thread(this, "Observer").start();
    } catch (Exception e) {
      GNSLocationClient.getLogger().severe("Unable to create GUID for Observer: " + e);
    }
  }

  @Override
  public void run() {
    GNSLocationClient.getLogger().info("Starting Observer for " + guid.getEntityName());
    try {
      gnsClient.clearField(guid.getGuid(), MESSAGES_FIELD, masterGuid);
    } catch (Exception e) {
      GNSLocationClient.getLogger().warning("Unable to clear messages field: " + e);
    }
    while (true) {
      try {
        Utils.sleep(SLEEP_MEAN + Math.round(random.nextGaussian() * SLEEP_SD));
        update();
      } catch (Exception e) {
        GNSLocationClient.getLogger().warning("Exception in Observer for "
                + guid.getEntityName() + ": " + e + ". Sleeping for 3 seconds and trying again.");
        Utils.sleep(3000);
      }
    }
  }

  private void update() throws Exception {
    JSONArray guids = collectNearbyGuids();
    System.out.println(String.format("Nearby %s: %s", guid.getEntityName(), toStringGuidList(guids)));
    JSONArray newMembers = updateGroupMembership(guids);
    updateMessages(newMembers);
  }

  private JSONArray collectNearbyGuids() throws Exception {
    JSONArray guids = new JSONArray();
    GlobalCoordinate coord = walker.getCurrentCoord();
    JSONArray coordJson = new JSONArray(Arrays.asList(coord.getLong(), coord.getLat()));
    JSONArray queryResult = gnsClient.selectNear(GnsProtocol.LOCATION_FIELD_NAME, coordJson, CHECK_RADIUS);
    for (int i = 0; i < queryResult.length(); i++) {
      try {
        JSONObject record = queryResult.getJSONObject(i);
        String guidString = record.getString("GUID");
        if (!guidString.equals(guid.getGuid())) { // don't include ourself
          JSONArray jsonLoc = record.getJSONArray(GnsProtocol.LOCATION_FIELD_NAME);
          //NOTE: database is LONG, LAT and GlobalCoordinates is LAT, LONG
          GlobalCoordinate location = new GlobalCoordinate(jsonLoc.getDouble(1), jsonLoc.getDouble(0));
          double distance = GeodeticCalculator.calculateGeodeticCurve(coord, location).getEllipsoidalDistance();
          JSONObject guidRecord = gnsClient.lookupGuidRecord(guidString);
//          System.out.println(String.format("%s is at %s, %5.2f meters from %s",
//                  entity.getString(GnsProtocol.GUID_RECORD_NAME), location, distance, guid.getEntityName()));
          guids.put(guidString);
        }
      } catch (JSONException e) {
        GNSLocationClient.getLogger().warning("Problem parsing JSON from selected record: " + e);
      }
    }
    return guids;
  }

  private JSONArray updateGroupMembership(JSONArray guids) throws Exception {
    JSONArray oldMembers = gnsClient.getGroupMembers(guid.getGuid(), guid);
    JSONArray newMembers = JSONUtils.JSONArraySetDifference(guids, oldMembers);
    if (newMembers.length() > 0) {
      //System.out.println(newMembers.length() + " new members");
      gnsClient.addToGroup(guid.getGuid(), newMembers, guid);
    }
    JSONArray membersToRemove = JSONUtils.JSONArraySetDifference(oldMembers, guids);
    if (membersToRemove.length() > 0) {
      //System.out.println(membersToRemove.length() + " fewer members");
      gnsClient.removeFromGroup(guid.getGuid(), membersToRemove, guid);
    }
    return newMembers;
  }

  private void updateMessages(JSONArray newMembers) throws Exception {
    for (int i = 0; i < newMembers.length(); i++) {
      String memberGuid = newMembers.getString(i);
      String message = String.format("%s %s says " + randomGreeting(), Format.formatDateTimeOnlyUTC(new Date()), guid.getEntityName());
      
      gnsClient.appendOrCreate(memberGuid, MESSAGES_FIELD, message, masterGuid);
    }
  }
  
  private final String[] greetings = {"hi", "hi", "hi", "hi", "howdy", "hello", "hello", "bonjour", "guten tag", "¡Hola!", "Sup"};
  
  private String randomGreeting() {
    return greetings[random.nextInt(greetings.length)];
  }
            

  private String toStringGuidList(JSONArray guids) {
    StringBuilder result = new StringBuilder();
    String prefix = "";
    for (int i = 0; i < guids.length(); i++) {
      try {
        result.append(prefix);
        result.append(ObserverWithMap.getInstance().getHumanReadableName(guids.getString(i)));
        prefix = ", ";
      } catch (JSONException e) {
      }
    }
    return result.toString();
  }
}
