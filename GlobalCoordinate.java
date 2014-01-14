/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.geodesy;

import edu.umass.cs.gns.utils.Format;
import java.io.Serializable;

/**
 * <p>
 * Encapsulation of latitude and longitude coordinates on a globe. Negative
 * latitude is southern hemisphere. Negative longitude is western hemisphere.
 * </p>
 * <p>
 * Any angle may be specified for longtiude and latitude, but all angles will be
 * canonicalized such that:
 * </p>
 * 
 * <pre>
 * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
 * </pre>
 * 
 * @author Mike Gavaghan
 */
public class GlobalCoordinate implements Comparable<GlobalCoordinate>, Serializable {

  /** Latitude in degrees. Negative latitude is southern hemisphere. */
  private double mLatitude;
  /** Longitude in degrees. Negative longitude is western hemisphere. */
  private double mLongitude;

  /**
   * Canonicalize the current latitude and longitude values such that:
   * 
   * <pre>
   * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
   * </pre>
   */
  private void canonicalize() {
    mLatitude = (mLatitude + 180) % 360;
    if (mLatitude < 0) {
      mLatitude += 360;
    }
    mLatitude -= 180;

    if (mLatitude > 90) {
      mLatitude = 180 - mLatitude;
      mLongitude += 180;
    } else if (mLatitude < -90) {
      mLatitude = -180 - mLatitude;
      mLongitude += 180;
    }

    mLongitude = ((mLongitude + 180) % 360);
    if (mLongitude <= 0) {
      mLongitude += 360;
    }
    mLongitude -= 180;
  }

  /**
   * Construct a new GlobalCoordinates. Angles will be canonicalized.
   * 
   * @param latitude latitude in degrees
   * @param longitude longitude in degrees
   */
  public GlobalCoordinate(double latitude, double longitude) {
    mLatitude = latitude;
    mLongitude = longitude;
    canonicalize();
  }

  /**
   * Get latitude.
   * 
   * @return latitude in degrees
   */
  public double getLatitude() {
    return mLatitude;
  }

  // synonym
  public double getLat() {
    return getLatitude();
  }

  /**
   * Set latitude. The latitude value will be canonicalized (which might result
   * in a change to the longitude). Negative latitude is southern hemisphere.
   * 
   * @param latitude in degrees
   */
  public void setLatitude(double latitude) {
    mLatitude = latitude;
    canonicalize();
  }

  /**
   * Get longitude.
   * 
   * @return longitude in degrees
   */
  public double getLongitude() {
    return mLongitude;
  }

  // synonym
  public double getLong() {
    return getLongitude();
  }

  /**
   * Set longitude. The longitude value will be canonicalized. Negative
   * longitude is western hemisphere.
   * 
   * @param longitude in degrees
   */
  public void setLongitude(double longitude) {
    mLongitude = longitude;
    canonicalize();
  }

  /**
   * Compare these coordinates to another set of coordiates. Western longitudes
   * are less than eastern logitudes. If longitudes are equal, then southern
   * latitudes are less than northern latitudes.
   * 
   * @param other instance to compare to
   * @return -1, 0, or +1 as per Comparable contract
   */
  @Override
  public int compareTo(GlobalCoordinate other) {
    int retval;

    if (mLongitude < other.mLongitude) {
      retval = -1;
    } else if (mLongitude > other.mLongitude) {
      retval = +1;
    } else if (mLatitude < other.mLatitude) {
      retval = -1;
    } else if (mLatitude > other.mLatitude) {
      retval = +1;
    } else {
      retval = 0;
    }

    return retval;
  }

  /**
   * Get a hash code for these coordinates.
   * 
   * @return
   */
  @Override
  public int hashCode() {
    return ((int) (mLongitude * mLatitude * 1000000 + 1021)) * 1000033;
  }

  /**
   * Compare these coordinates to another object for equality.
   * 
   * @param other
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GlobalCoordinate)) {
      return false;
    }

    GlobalCoordinate other = (GlobalCoordinate) obj;

    return (mLongitude == other.mLongitude) && (mLatitude == other.mLatitude);
  }

  /**
   * Get coordinates as a string.
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();

    result.append('[');
    result.append(Format.formatLatLong(Math.abs(mLatitude)));
    result.append((mLatitude >= 0) ? 'N' : 'S');
    result.append(',');
    result.append(Format.formatLatLong(Math.abs(mLongitude)));
    result.append((mLongitude >= 0) ? 'E' : 'W');
    result.append(']');

    return result.toString();
  }
}
