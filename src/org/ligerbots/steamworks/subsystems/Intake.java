package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subsystem handles the intake, which uses a bag motor to bring fuel from the floor into the
 * fuel tank.
 */
public class Intake extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Intake.class);
  static final double INTAKE_SPEED = 0.7;
  
  boolean intakeOn;
  CANTalon intakeTalon;
  
  /**
   * Creates the intake subsystem.
   */
  public Intake() {
    if (Robot.deviceFinder.isSrxAvailable(RobotMap.CT_ID_INTAKE)) {
      logger.info("Initialize");
    
      intakeTalon = new CANTalon(RobotMap.CT_ID_INTAKE);
      intakeTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);

      setIntakeOn(false);
    }
    else {
      logger.warn("Intake unavailable");
    }
  }
    

  public void initDefaultCommand() {}

  /**
   * Turns the intake on or off.
   * @param intakeOn true for on, false for off
   */
  public void setIntakeOn(boolean intakeOn) {
    if (intakeTalon != null) {
      logger.info(String.format("Setting intake, on=%b", intakeOn));
      intakeTalon.set(intakeOn ? INTAKE_SPEED : 0.0);
      this.intakeOn = intakeOn;
    }
  }
  public boolean isIntakeOn() {
    return intakeOn;
  }

  /**
   * Sends intake data to smart dashboard.
   */
  public void sendDataToSmartDashboard() {
    if (intakeTalon != null) {
      SmartDashboard.putNumber("Intake_Talon_Power",
          intakeTalon.getOutputCurrent() * intakeTalon.getOutputVoltage());
      SmartDashboard.putBoolean("Intake_On", intakeOn);
    }
  }
}

