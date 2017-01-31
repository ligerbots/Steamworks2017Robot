package org.ligerbots.steamworks;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into to a variable name.
 * This provides flexibility changing wiring, makes checking the wiring easier and significantly
 * reduces the number of magic numbers floating around.
 */
public class RobotMap {
  public static final int PCM_CAN_ID = 7;

  public static final int CT_ID_LEFT_1 = 1;
  public static final int CT_ID_LEFT_2 = 3;
  public static final int CT_ID_RIGHT_1 = 2;
  public static final int CT_ID_RIGHT_2 = 4;

  public static final int CT_ID_SHOOTER_MASTER = 5;
  public static final int CT_ID_SHOOTER_SLAVE = 6;

  public static final int CT_ID_INTAKE = 7;
  public static final int CT_ID_FEEDER = 8;

  // Data from http://motors.vex.com/775pro
  public static final int MAX_RPM_775PRO = 18700;
  public static final int STALL_CURRENT_775PRO = 134; // Amps
  // give a margin of error
  public static final int SAFE_CURRENT_775PRO = STALL_CURRENT_775PRO - 20;

  public static final int SOLENOID_SHIFT_UP = 1;
  public static final int SOLENOID_SHIFT_DOWN = 0;

  public static final int RELAY_LED_RING = 0;

  public static final int GEAR_SERVO_CHANNEL = 0;

  public static final int MAG_ENCODER_UNITS_PER_REVOLUTION = 4096;
  public static final float MAG_ENCODER_FREQUENCY = 10;
  public static final double NANOS_PER_MINUTE = 60.0e9;

  public static final int LIMIT_SWITCH_CLIMB_COMPLETE = 0;

  // We don't really know whether or not to multiply or divide by the gear ratio (2.06)
  public static final double ENCODER_TICKS_PER_INCH = 1024 * 2.06 / (4 * Math.PI);
}
