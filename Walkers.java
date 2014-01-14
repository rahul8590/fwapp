/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.gnslocationclient;

/**
 * Creates a bunch of {@link Walker} instances.
 * 
 * @author westy
 */
public class Walkers {

  /**
   * Walking speed in Meters per second
   */
  public static final double NOMINAL_SPEED = 1.4;
  public static final int NUMBER_OF_WALKERS = 20;
  private int walkerCount = 0;
  
  // Complements of Key and Peele
  private static final String[] walkerNames = {"D'Marcus", "T.J.", "T'varisuness", "Tyroil", "D'Squarius", "Ibrahim", "Jackmerius", "D'Isiah", 
  "D'Jasper", "Leoz", "Javaris", "Hingle", "L'Carpetron", "J'Dinkalage", "Xmus", "Saggitariutt", "D'Glester", "Swirvithan", "Quatro","Ozamataz",
  "Beezer", "Shakiraquan", "X-Wing", "Sequester", "Scoish", "R.J.", "A.J.", "Eeeee", "Torque", "Mousecop", "Dan", "Sam", "Joe", "Bill", "Sarah",
  "Sally", "Henry", "Billy-Bob", "Frank", "Oscar", "Wallace", "Flipper", "Squid", "Binkworth", "Spanky", "Scooter", "Emily", "Randy"};


  // make it a singleton class
  public static Walkers getInstance() {
    return WalkersHolder.INSTANCE;
  }

  private static class WalkersHolder {
    private static final Walkers INSTANCE = new Walkers();
  }

  private Walkers() {
  }

  public void initWalkers() {
    for (int x = 0; x < NUMBER_OF_WALKERS; x++) {
      newWalker();
    }
  }

  public void newWalker() {
    new Walker(GNSLocationClient.getInstance().getGuid(), walkerNames[walkerCount++ % walkerNames.length]);
  }
}
