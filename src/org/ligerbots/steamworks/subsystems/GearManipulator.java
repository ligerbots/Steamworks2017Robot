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
  static final double GM_POSITION_CLOSED = 0;
  static final double GM_POSITION_OPEN = 1;
  
  Servo gearServo;
  boolean isOpen;

  Logger logger = LoggerFactory.getLogger(GearManipulator.class);  
  /**
   * Creates the GearManipulator, and sets servo to closed position.
   */
  public GearManipulator() {

    gearServo = new Servo(RobotMap.GEAR_SERVO_CHANNEL);
    setOpen(false);
  }

  public void initDefaultCommand() {
  }
  
  /**
   * Sets the gear mechanism to be open or closed.
   * @param shouldBeOpen Whether it should be open or closed.
   */
  public void setOpen(boolean shouldBeOpen) {
    isOpen = shouldBeOpen;
    if (shouldBeOpen) {
      gearServo.set(GM_POSITION_OPEN);
    } else {
      gearServo.set(GM_POSITION_CLOSED);
    }
    logger.trace("shouldBeOpen: " + shouldBeOpen);
    
  }
  
  /**
   * @return Whether the gear mechanism is open or closed.
   */
  public boolean isOpen() {
    return isOpen;
  }
  
  public void sendDataToSmartDashboard() {
    SmartDashboard.putBoolean("Gear_Mechanism_Open", isOpen);
  }
}
