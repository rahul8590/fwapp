/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.gnslocationclient;

import edu.umass.cs.gns.client.DesktopGnsClient;
import edu.umass.cs.gns.client.GuidEntry;
import edu.umass.cs.gns.utils.GuidUtils;

/**
 * Each walker has a two component classes: one that does the walking {@link RandomWalker} and one that
 * looks around to see who is near and exchanges messages ({@link WalkingObserver}. 
 * This is meant to simulate a mobile device that is being carried around by a person.
 * 
 * @author westy
 */
public class Walker {

  private GuidEntry guid;
  private DesktopGnsClient gnsClient;
  
  public Walker(GuidEntry masterGuid, String name) {
    gnsClient = new DesktopGnsClient(GNSLocationClient.gnsHost, GNSLocationClient.gnsPort);
    try {
      guid = GuidUtils.lookupOrAddGuid(gnsClient, masterGuid, name);
      // This class does the walking around
      RandomWalker walker = new RandomWalker(GNSLocationClient.getInstance().getGuid(), guid);
      // This class looks around for friends nearby
      WalkingObserver observer = new WalkingObserver(GNSLocationClient.getInstance().getGuid(), guid, walker);
    } catch (Exception e) {
      GNSLocationClient.getLogger().warning("Unable to create GUID: " + e);
    }
  }
}
