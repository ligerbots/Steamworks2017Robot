package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The feeder is the mechanism that delivers fuel consistently to the shooter from the hopper.
 */
public class Stirrer extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Feeder.class);

  Servo stirrerServo;

  /**
   * Creates the Stirrer subsystem.
   */
  public Stirrer() {
    logger.info("Initialize");

    // SR1425CR
    stirrerServo = new Servo(RobotMap.STIRRER_SERVO_CHANNEL);
  }

  /**
   * Sets the stirrer motor speed.
   * 
   * @param value A percentvbus value, 0.0 to 1.0
   */
  public void setStirrer(double speed) {
    if (stirrerServo != null) {
      logger.trace(String.format("Setting feeder, speed", speed));
      // stirrerServo.set(speed);      
      stirrerServo.setSpeed(speed);
    }
  }

  public void initDefaultCommand() {}

  /**
   * Sends diagnostics to SmartDashboard.
   */
  public void sendDataToSmartDashboard() {
    if (stirrerServo != null) {
      // SmartDashboard.putNumber("Stirrer Power",
      //     stirrerServo.getOutputCurrent() * feederTalon.getOutputVoltage());
      
    }
  }
}

