package org.ligerbots.steamworks;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into to a variable name.
 * This provides flexibility changing wiring, makes checking the wiring easier and significantly
 * reduces the number of magic numbers floating around.
 */
public class RobotMap {  
  public static final int CT_ID_LEFT_1 = 1;
  public static final int CT_ID_LEFT_2 = 3;
  public static final int CT_ID_RIGHT_1 = 2;
  public static final int CT_ID_RIGHT_2 = 4;
  
  public static final int CT_ID_SHOOTER_MASTER = 5;
  public static final int CT_ID_SHOOTER_SLAVE = 6;
  // Data from http://motors.vex.com/775pro
  public static final int MAX_RPM_775PRO = 18700;
  public static final int STALL_CURRENT_775PRO = 134; // Amps
  // give a margin of error
  public static final int SAFE_CURRENT_775PRO = STALL_CURRENT_775PRO - 20;
  
  public static final int SOLENOID_SHIFT_UP = 1;
  public static final int SOLENOID_SHIFT_DOWN = 0;
  
  public static final int RELAY_LED_RING = 0;
}
