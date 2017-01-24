package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.ligerbots.steamworks.RobotMap;

/**
 * This subsystem opens or closes the gear manipulator.
 */
public class GearManipulator extends Subsystem {

  Servo gearServo;
  boolean isOpen;
  
  /**
   * Close the manipulator so that we always start closed.
   */
  public GearManipulator() {
    gearServo = new Servo(RobotMap.GEAR_SERVO_CHANNEL);
    isOpen = false;
    closeManipulator();
  }

  public void initDefaultCommand() {
    
  }
  
  public void closeManipulator() {
    gearServo.set(0);
    isOpen = false;
  }
  
  public void openManipulator() {
    gearServo.set(1);
    isOpen = true;
  }
  
  public boolean isOpen() {
    return isOpen;
  }
  
 
}
