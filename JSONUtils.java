/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.utils;

import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author westy
 */
public class JSONUtils {

  /**
   * Returns a JSONArray of elements of array1 that do not appear in array2.
   * 
   * @param array1
   * @param array2
   * @return
   * @throws JSONException 
   */
  public static JSONArray JSONArraySetDifference(JSONArray array1, JSONArray array2) throws JSONException {
    HashSet<Object> set2 = new HashSet<Object>();
    for (int i = 0; i < array2.length(); i++) {
      set2.add(array2.get(i));
    }
    HashSet<Object> resultSet = new HashSet<Object>();
    for (int i = 0; i < array1.length(); i++) {
      Object element = array1.get(i);
      if (!set2.contains(element)) {
        resultSet.add(element);
      }
    }
    return new JSONArray(resultSet);
  }
}
