package edu.umass.cs.gns.utils;

import java.util.Random;

/**
 *
 * @author westy
 */
public class Utils {

  private static Random rnd = new Random(System.currentTimeMillis());
  private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (Exception c) {
      System.out.println("error sleeping :" + c);
    }
  }

  public static String randomString(int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(CHARACTERS.charAt(rnd.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }

}
