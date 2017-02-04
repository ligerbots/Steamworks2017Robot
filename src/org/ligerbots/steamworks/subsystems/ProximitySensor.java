package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ProximitySensor extends Subsystem implements SmartDashboardLogger {
  AnalogInput ai;
  private static final Logger logger = LoggerFactory.getLogger(ProximitySensor.class);

  public ProximitySensor() {
    logger.trace("Initializing proximity sensor");

    ai = new AnalogInput(RobotMap.ANALOG_INPUT_PROXIMITY_SENSOR);
  }

  /**
   * @return inches
   * 
   */
  public double getDistance() {
    double voltage = ai.getVoltage();
    double distanceInMillimeters = voltage / 1000 / RobotMap.MILLIVOLTS_PER_MILLIMETER;
    return distanceInMillimeters / RobotMap.MILLIMETERS_PER_INCH;

  }

  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }

  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Proximity_Sensor_Voltage", ai.getVoltage());
    SmartDashboard.putNumber("Proximity_Sensor_Distance", getDistance());
  }
}

