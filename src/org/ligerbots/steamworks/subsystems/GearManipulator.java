package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Servo;
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
    gearServo.setSpeed(RobotMap.GEAR_SERVO_SPEED);
    setOpen(false);
 }

  public void initDefaultCommand() {
  }
  
  /**
   * Sets the gear mechanism to be open or closed.
   * @param shouldBeOpen Whether it should be open or closed.
   */
  public void setOpen(boolean shouldBeOpen) {
    logger.info(String.format("Set gear manipulator, shouldBeOpen=%b", shouldBeOpen));
    
    isOpen = shouldBeOpen;
    if (shouldBeOpen) {
      double gearmech_open_degrees = RobotMap.GEARMECH_OPEN_DEGREES;
      logger.info(String.format("gearmech open degrees %f", gearmech_open_degrees));
      // Map 0 degrees to 0.0 and 180 degrees to 1.0
      double gearmech_position_open = gearmech_open_degrees / 180.;
      gearServo.set(gearmech_position_open);
      logger.info(String.format("Set gear manipulator to %f (open)", gearmech_position_open));
    } else {
      double gearmech_closed_degrees = RobotMap.GEARMECH_CLOSED_DEGREES;
   // Map 0 degrees to 0.0 and 180 degrees to 1.0
      double gearmech_position_closed = gearmech_closed_degrees / 180.;
      gearServo.set(gearmech_position_closed);
      logger.info(String.format("gearmech closed degrees %f", gearmech_closed_degrees));
      logger.info(String.format("Set gear manipulator to %f (closed)", gearmech_position_closed));
    }
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
