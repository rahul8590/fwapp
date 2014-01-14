/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.utils;

import edu.umass.cs.gns.client.DesktopGnsClient;
import edu.umass.cs.gns.client.GnsProtocol;
import edu.umass.cs.gns.client.GuidEntry;
import edu.umass.cs.gns.client.util.KeyPairUtils;
import edu.umass.cs.gns.exceptions.GnsException;
import edu.umass.cs.gns.gnslocationclient.GNSLocationClient;
import java.io.IOException;

/**
 *
 * @author westy
 */
public class GuidUtils {

  public static GuidEntry lookupOrCreateAccountGuid(DesktopGnsClient client, String name) throws Exception {
    GuidEntry guid;
    guid = KeyPairUtils.getGuidEntryFromPreferences(GNSLocationClient.getGNSHostPortString(), name);
    if (guid == null || !guidExists(client, guid)) { // also handle case where it has been deleted from database
      return guid = client.registerNewAccountGuid(name);
    } else {
      return guid;
    }
  }

  public static GuidEntry lookupOrAddGuid(DesktopGnsClient client, GuidEntry masterGuid, String name) throws Exception {
    GuidEntry guid;
    guid = KeyPairUtils.getGuidEntryFromPreferences(GNSLocationClient.getGNSHostPortString(), name);
    if (guid == null || !guidExists(client, guid)) { 
      guid = client.addGuid(masterGuid, name);
      // also give master access to all fields
      client.addToACL(GnsProtocol.AccessType.READ_WHITELIST, guid, GnsProtocol.ALLFIELDS, masterGuid.getGuid());
      client.addToACL(GnsProtocol.AccessType.WRITE_WHITELIST, guid, GnsProtocol.ALLFIELDS, masterGuid.getGuid());
      return guid;
    } else { 
      return guid;
    }
  }

  private static boolean guidExists(DesktopGnsClient client, GuidEntry guid) throws IOException {
    try {
      client.lookupGuidRecord(guid.getGuid());
    } catch (GnsException e) {
      return false;
    }
    return true;
  }
}
