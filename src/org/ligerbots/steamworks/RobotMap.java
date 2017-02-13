package org.ligerbots.steamworks;

import java.io.File;

public class RobotMap {
  public static final boolean IS_ROADKILL = new File("/home/lvuser/roadkill").exists();
  
  public static final int PCM_CAN_ID = 7;

  public static final int CT_ID_LEFT_1 = 1;
  public static final int CT_ID_LEFT_2 = 3;
  public static final int CT_ID_RIGHT_1 = 2;
  public static final int CT_ID_RIGHT_2 = 4;

  public static final int CT_ID_SHOOTER_MASTER = 5;
  public static final int CT_ID_SHOOTER_SLAVE = 6;

  public static final int CT_ID_INTAKE = 7;
  public static final int CT_ID_FEEDER = 8;
  
  public static final int ANALOG_INPUT_PROXIMITY_SENSOR = 0;

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
  public static final double NANOS_PER_SECOND = 1.0e9;
  public static final double MILLIMETERS_PER_INCH = 25.4;
  public static final double MILLIVOLTS_PER_MILLIMETER = 0.977;

  public static final int LIMIT_SWITCH_CLIMB_COMPLETE = 0;
  public static final int ULTRASONIC_TRIGGER = 1;
  public static final int ULTRASONIC_ECHO = 2;
  
  public static final double GEARMECH_POSITION_CLOSED = 0;
  public static final double GEARMECH_POSITION_OPEN = 1;

  public static final int QUAD_ENCODER_TICKS_PER_REV = 256;
  public static final double WHEEL_RADIUS = IS_ROADKILL ? 3 : 2; // in
  public static final double WHEEL_DIAMETER = WHEEL_RADIUS * 2;
  public static final double WHEEL_CIRCUMFERENCE = WHEEL_DIAMETER * Math.PI;
  public static final double GEARING_FACTOR = 1;

  public static final double AUTO_TURN_MAX_SPEED = 0.3;
  public static final double AUTO_TURN_MIN_SPEED = 0.1;
  public static final double AUTO_TURN_RAMP_ZONE = 80.0;
  public static final double AUTO_TURN_ACCEPTABLE_ERROR = 1.5; // Degrees
  
  public static final double AUTO_DRIVE_ACCEPTABLE_ERROR = 2.0; // in
  public static final double AUTO_FINE_DRIVE_ACCEPTABLE_ERROR = 0.5;
  
  public static final double AUTO_DRIVE_TURN_P = 0.02;
  
  public static final double AUTO_DRIVE_MIN_SPEED = 0.1; // percent voltage from 12.5V
  public static final double AUTO_DRIVE_START_SPEED = 0.3;
  public static final double AUTO_DRIVE_MAX_SPEED = 0.5;
  
  public static final double AUTO_DRIVE_RAMP_UP_DIST = 24.0; // in
  public static final double AUTO_DRIVE_RAMP_DOWN_DIST = 60.0;

  public static final double JOYSTICK_DRIVE_TURN_SENSITIVITY = 1.0;

  public static final double SHOOTER_RPM_PERCENT_TOLERANCE = 0.05;
  public static final long AUTO_SHOOTER_WAIT_NANOS = 5_000_000_000L;
}
