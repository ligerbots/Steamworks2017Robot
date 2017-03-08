package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The feeder is the mechanism that delivers fuel consistently to the shooter from the hopper.
 */
public class Feeder extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Feeder.class);

  CANTalon feederTalon;

  /**
   * Creates the Feeder subsystem.
   */
  public Feeder() {
    if (Robot.deviceFinder.isTalonAvailable(RobotMap.CT_ID_FEEDER)) {
      logger.trace("Initialize");
      feederTalon = new CANTalon(RobotMap.CT_ID_FEEDER);
      feederTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      feederTalon.enableBrakeMode(true);
      feederTalon.setSafetyEnabled(false);
    } else {
      logger.warn("Feeder unavailable");
    }
  }

  /**
   * Sets the feeder motors.
   * 
   * @param value A percentvbus value, 0.0 to 1.0
   */
  public void setFeeder(double value) {
    if (feederTalon != null) {
      logger.trace(String.format("Setting feeder, percentvbus=%f", value));
      feederTalon.set(value);
    }
  }

  public void initDefaultCommand() {}

  /**
   * Sends diagnostics to SmartDashboard.
   */
  public void sendDataToSmartDashboard() {
    SmartDashboard.putBoolean("Feeder_Present", feederTalon != null && feederTalon.isAlive());
    if (feederTalon != null) {
      SmartDashboard.putNumber("Feeder_Power",
          feederTalon.getOutputCurrent() * feederTalon.getOutputVoltage());
      
      SmartDashboard.putBoolean("Feeder_Ok", feederTalon.getFaultHardwareFailure() == 0);
      SmartDashboard.putBoolean("Feeder_Temp_Ok", feederTalon.getStickyFaultOverTemp() == 0);
    }
  }
}

