package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.XboxController;

/**
 * This class is the glue that binds the controls on the physical operator interface to the commands
 * and command groups that allow control of the robot.
 */
public class OperatorInterface {
  XboxController xboxController;
  
  public OperatorInterface() {
    xboxController = new XboxController(0);
  }
  
  public double getThrottle() {
    return xboxController.getY(Hand.kLeft);
  }
  
  public double getTurn() {
    return xboxController.getX(Hand.kRight);
  }
}
