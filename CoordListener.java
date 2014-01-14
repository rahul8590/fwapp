/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.gnslocationclient;

import edu.umass.cs.gns.client.GuidEntry;
import edu.umass.cs.gns.geodesy.GlobalCoordinate;

/**
 *
 * @author westy
 */
public interface CoordListener {

    public void onCoord(GuidEntry guid, GlobalCoordinate coord);
}
