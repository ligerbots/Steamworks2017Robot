package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.PWM.PeriodMultiplier;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The feeder is the mechanism that delivers fuel consistently to the shooter from the hopper.
 */
public class Stirrer extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Stirrer.class);

  Servo stirrerServo;

  /**
   * Creates the Stirrer subsystem.
   */
  public Stirrer() {
    logger.info("Initialize");

    // HSR-1425CR
    stirrerServo = new Servo(RobotMap.STIRRER_SERVO_CHANNEL);
    // values for the servo we have. http://hitecrcd.com/faqs/servos/general-servos
    stirrerServo.setBounds(2.1, 0, 0, 0, 0.9);
    stirrerServo.setPeriodMultiplier(PeriodMultiplier.k4X);
    setStirrer(RobotMap.STIRRER_SERVO_VALUE_STOP);
  }

  /**
   * Sets the stirrer motor speed.
   * 
   * @param value The servo value, 0 to 1
   */
  public void setStirrer(double value) {
    if (stirrerServo != null) {
      logger.trace(String.format("Setting stirrer, value %f", value));

      stirrerServo.set(value);
    }
  }

  public void initDefaultCommand() {}

  /**
   * Sends diagnostics to SmartDashboard.
   */
  public void sendDataToSmartDashboard() {
    SmartDashboard.putBoolean("Stirrer_On",
        stirrerServo.get() != RobotMap.STIRRER_SERVO_VALUE_STOP);
  }
}

