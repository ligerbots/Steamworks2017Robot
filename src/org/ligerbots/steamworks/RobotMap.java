package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotMap {
  private static final Logger logger = LoggerFactory.getLogger(RobotMap.class);
  
  @Retention(RetentionPolicy.RUNTIME)
  protected static @interface Preference {
  }
  
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
  
  public static final int QUAD_ENCODER_TICKS_PER_REV = 256;
  public static final double WHEEL_RADIUS = IS_ROADKILL ? 3 : 2; // in
  public static final double WHEEL_DIAMETER = WHEEL_RADIUS * 2;
  public static final double WHEEL_CIRCUMFERENCE = WHEEL_DIAMETER * Math.PI;
  public static final double GEARING_FACTOR = IS_ROADKILL ? 1 : 1; // TODO: add
  
  public static final double VISION_BOILER_CAMERA_ANGLE = 50.8; // degrees
  
  @Preference
  public static double GEARMECH_POSITION_CLOSED = 0;
  @Preference
  public static double GEARMECH_POSITION_OPEN = 1;

  @Preference
  public static double AUTO_TURN_MAX_SPEED = 0.3;
  @Preference
  public static double AUTO_TURN_MIN_SPEED = 0.1;
  @Preference
  public static double AUTO_TURN_RAMP_ZONE = 80.0;
  @Preference
  public static double AUTO_TURN_ACCEPTABLE_ERROR = 1.5; // Degrees
  
  @Preference
  public static double AUTO_DRIVE_ACCEPTABLE_ERROR = 2.0; // in
  @Preference
  public static double AUTO_FINE_DRIVE_ACCEPTABLE_ERROR = 0.5;
  
  @Preference
  public static double AUTO_DRIVE_TURN_P = 0.02;
  
  @Preference
  public static double AUTO_DRIVE_MIN_SPEED = 0.1; // percent voltage from 12.5V
  @Preference
  public static double AUTO_DRIVE_START_SPEED = 0.3;
  @Preference
  public static double AUTO_DRIVE_MAX_SPEED = 0.5;
  
  @Preference
  public static double AUTO_DRIVE_RAMP_UP_DIST = 24.0; // in
  @Preference
  public static double AUTO_DRIVE_RAMP_DOWN_DIST = 60.0;

  @Preference
  public static double JOYSTICK_DRIVE_TURN_SENSITIVITY = 1.0;

  @Preference
  public static double SHOOTER_RPM_PERCENT_TOLERANCE = 0.05;
  @Preference
  public static long AUTO_SHOOTER_WAIT_NANOS = 5_000_000_000L;
  
  @Preference
  public static boolean VISION_BOILER_AUTO_CORRECT = false;
  
  /**
   * Sets up Preferences variables. Must be called first in robotInit().
   */
  public static void initialize() {
    // initialize Preferences if not initialized already
    Class<RobotMap> thisClass = RobotMap.class;
    for (Field field: thisClass.getFields()) {
      if (field.isAnnotationPresent(Preference.class)) {
        String name = field.getName();
        Class<?> type = field.getType();
        // assuming field are numbers for now
        Preferences prefs = Preferences.getInstance();
        
        if (!prefs.containsKey(name)) {
          try {
            if (type == Integer.TYPE) {
              prefs.putInt(name, field.getInt(null));
            } else if (type == Long.TYPE) {
              prefs.putLong(name, field.getLong(null));
            } else if (type == Float.TYPE) {
              prefs.putFloat(name, field.getFloat(null));
            } else if (type == Double.TYPE) {
              prefs.putDouble(name, field.getDouble(null));
            } else if (type == Boolean.TYPE) {
              prefs.putBoolean(name, field.getBoolean(null));
            } else if (type == String.class) {
              prefs.putString(name, field.get(null).toString());
            } else {
              throw new IllegalArgumentException("Invalid field type: " + type);
            }
          } catch (Exception ex) {
            logger.error("RobotMap initialize error", ex);
          }
        }
      }
    }
    
    // update when user changes Preferences
    ITable prefsTable = NetworkTable.getTable("Preferences");
    prefsTable.addTableListener(new ITableListener() {
      @Override
      public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        try {
          Field field = RobotMap.class.getDeclaredField(key);
          if (!field.isAnnotationPresent(Preference.class)) {
            throw new IllegalArgumentException("Field does not have @Preference");
          }
          Preferences prefs = Preferences.getInstance();
          Class<?> type = field.getType();
          if (type == Integer.TYPE) {
            field.setInt(null, prefs.getInt(key, 0));
          } else if (type == Long.TYPE) {
            field.setLong(null, prefs.getLong(key, 0));
          } else if (type == Float.TYPE) {
            field.setFloat(null, prefs.getFloat(key, 0));
          } else if (type == Double.TYPE) {
            field.setDouble(null, prefs.getDouble(key, 0));
          } else if (type == Boolean.TYPE) {
            field.setBoolean(null, prefs.getBoolean(key, false));
          } else if (type == String.class) {
            field.set(null, prefs.getString(key, ""));
          } else {
            throw new IllegalArgumentException("Invalid field type: " + type);
          }
        } catch (Exception ex) {
          logger.warn("Error on key: " + key, ex);
        }
      }
    }, true);
  }
}
