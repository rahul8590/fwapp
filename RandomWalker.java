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
import edu.umass.cs.gns.utils.Utils;
import java.util.Arrays;
import java.util.Random;
import org.json.JSONArray;

/**
 * Propels the walker on a random journey. The location is maintained
 * in the "location" field of the associated guid in the GNS.
 * 
 * @author westy
 */
/**
 * Generates a random walk 
 */
public class RandomWalker implements Runnable {
  
  private final static int SLEEP_MEAN = 8000;
  private final static int SLEEP_SD = 1000;

  private GuidEntry guid;
  private DesktopGnsClient gnsClient;
  //
  private GlobalCoordinate currentCoord;
  private double speed = Walkers.NOMINAL_SPEED; // in meters per second
  private double bearing = 0.0; // in radians
  private Random random = new Random();
  private long lastTime = 0;

  public RandomWalker(GuidEntry masterGuid, GuidEntry guid) {
    this.guid = guid;
    this.gnsClient = new DesktopGnsClient(GNSLocationClient.gnsHost, GNSLocationClient.gnsPort);
    initCoords();
    new Thread(this, guid.getEntityName()).start();
  }

  private void initCoords() {
    setCurrentCoord(generateFirstCoordinate(speed * 60 * 5)); // randomly place within x minutes
    bearing = random.nextDouble() * 2d * Math.PI;
  }

  @Override
  public void run() {
    GNSLocationClient.getLogger().info("Starting Random " + guid.getEntityName());
    while (true) {
      try {
        update();
        Utils.sleep(SLEEP_MEAN + Math.round(random.nextGaussian() * SLEEP_SD));
      } catch (Exception e) {
        GNSLocationClient.getLogger().warning("Exception in Random Walker for "
                + guid.getEntityName() + ": " + e + ". Sleeping for 4 seconds and trying again.");
        Utils.sleep(4000);
      }
    }
  }

  private void update() {
    generateNextCoord();
    updateMyLocationInGns();
  }

  private void updateMyLocationInGns() {
    JSONArray array = new JSONArray(Arrays.asList(currentCoord.getLong(), currentCoord.getLat()));
    try {
      gnsClient.replaceOrCreateUsingList(guid, GnsProtocol.LOCATION_FIELD_NAME, array);
    } catch (Exception e) {
      GNSLocationClient.getLogger().warning("Unable to update location field: " + e);
    }
  }

  private GlobalCoordinate generateNextCoord() {
    if (lastTime == 0) {
      lastTime = System.currentTimeMillis();
      return currentCoord;
    } else {
      double accel = 0;
      speed = speed + accel;
      double dist = (System.currentTimeMillis() - lastTime) * speed / 1000.0; // dived by 1000 converts from millesconds to seconds
      lastTime = System.currentTimeMillis();
      if (random.nextDouble() < 0.2d) {
        bearing = bearing + random.nextGaussian() * Math.PI;
      }

      setCurrentCoord(GeodeticCalculator.calculateEndingGlobalCoordinates(currentCoord, Math.toDegrees(bearing), dist));

      return currentCoord;
    }
  }

  /**
   * Returns a uniformly distributed point within a circle from start point.
   * 
   * @param range
   * @return 
   */
  private GlobalCoordinate generateFirstCoordinate(double range) {
    double bearing = random.nextDouble() * 2d * Math.PI;
    double dist = random.nextDouble() * range;

    return GeodeticCalculator.calculateEndingGlobalCoordinates(GNSLocationClient.STARTING_POINT, Math.toDegrees(bearing), dist);

  }

  private synchronized void setCurrentCoord(GlobalCoordinate currentCoord) {
    this.currentCoord = currentCoord;
  }

  public synchronized GlobalCoordinate getCurrentCoord() {
    return currentCoord;
  }
}
