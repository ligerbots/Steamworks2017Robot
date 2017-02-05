package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subsystem for the ultrasonic sensor that will be used to align to precise distances from the
 * feeder station.
 */
public class ProximitySensor extends Subsystem implements SmartDashboardLogger {
  AnalogInput ai;
  private static final Logger logger = LoggerFactory.getLogger(ProximitySensor.class);

  /**
   * Creates the ProximitySensor.
   */
  public ProximitySensor() {
    logger.trace("Initializing proximity sensor");

    ai = new AnalogInput(RobotMap.ANALOG_INPUT_PROXIMITY_SENSOR);
  }

  /**
   * Returns the distance detected by the ultrasonic sensor.
   * @return Distance in inches
   */
  public double getDistance() {
    double voltage = ai.getVoltage();
    double distanceInMillimeters = voltage / 1000 / RobotMap.MILLIVOLTS_PER_MILLIMETER;
    return distanceInMillimeters / RobotMap.MILLIMETERS_PER_INCH;
  }

  public void initDefaultCommand() {
  }

  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Proximity_Sensor_Voltage", ai.getVoltage());
    SmartDashboard.putNumber("Proximity_Sensor_Distance", getDistance());
  }
}

