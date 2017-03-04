package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.PWM.PeriodMultiplier;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subsystem opens or closes the gear manipulator.
 */
public class GearManipulator extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(GearManipulator.class);  
  
  Servo gearServo;
  boolean isOpen = false;

  /**
   * Creates the GearManipulator, and sets servo to closed position.
   */
  public GearManipulator() {
    logger.info("Initialize");

    gearServo = new Servo(RobotMap.GEAR_SERVO_CHANNEL);
    gearServo.setBounds(2.4, 0, 0, 0, 0.8);
    gearServo.setPeriodMultiplier(PeriodMultiplier.k1X);
    //setOpen(false);
  }

  public void initDefaultCommand() {
  }
  
  /**
   * Sets the gear mechanism to be open or closed.
   * @param shouldBeOpen Whether it should be open or closed.
   */
  public void setOpen(boolean shouldBeOpen) {
    isOpen = shouldBeOpen;
    
    double servoDegrees =
        shouldBeOpen ? RobotMap.GEARMECH_OPEN_DEGREES : RobotMap.GEARMECH_CLOSED_DEGREES;
    double servoValue = servoDegrees / 180;
    logger.info(String.format("open=%b degrees=%f value=%f", shouldBeOpen, servoDegrees,
        servoValue));
    
    setServoRaw(servoValue);
  }
  
  public void setServoRaw(double value) {
    gearServo.set(value);
  }
  
  /**
   * @return Whether the gear mechanism is open or closed.
   */
  public boolean isOpen() {
    logger.info(String.format("isOpen=%b", isOpen));
    return isOpen;
  }
  
  public void sendDataToSmartDashboard() {
    SmartDashboard.putBoolean("Gear_Mechanism_Open", isOpen);
  }
}
