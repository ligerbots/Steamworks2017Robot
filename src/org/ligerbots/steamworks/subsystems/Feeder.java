package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The feeder is the mechanism that delivers fuel consistently to the shooter from the hopper.
 */
public class Feeder extends Subsystem implements SmartDashboardLogger {
  CANTalon feederTalon;

  /**
   * Creates the Feeder subsystem.
   */

  Logger logger = LoggerFactory.getLogger(Feeder.class);
  public Feeder() {
    logger.trace("Feeder beginning");
    feederTalon = new CANTalon(RobotMap.CT_ID_FEEDER);
    feederTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
    feederTalon.enableBrakeMode(true);
  }

  /**
   * Sets the feeder motors.
   * 
   * @param value A percentvbus value, 0.0 to 1.0
   */
  public void setFeeder(double value) {
    logger.trace("Setting feeder to percentvbus of " + value);
    feederTalon.set(value);
  }

  public void initDefaultCommand() {}

  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Feeder_Talon_Power",
        feederTalon.getOutputCurrent() * feederTalon.getOutputVoltage());
  }
}

