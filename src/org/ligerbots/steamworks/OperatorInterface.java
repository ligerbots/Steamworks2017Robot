package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import org.ligerbots.steamworks.commands.ShiftCommand;

/**
 * This class is the glue that binds the controls on the physical operator interface to the commands
 * and command groups that allow control of the robot.
 */
public class OperatorInterface {
  XboxController xboxController;
  
  /**
   * This is where we set up the operator interface.
   */
  public OperatorInterface() {
    xboxController = new XboxController(0);
    JoystickButton leftBumper = new JoystickButton(xboxController, 5);
    leftBumper.whenPressed(new ShiftCommand(false));
    JoystickButton rightBumper = new JoystickButton(xboxController, 6);
    rightBumper.whenPressed(new ShiftCommand(true));
  }
  
  public double getThrottle() {
    return xboxController.getY(Hand.kLeft);
  }
  
  public double getTurn() {
    return xboxController.getX(Hand.kRight);
  }
  
}
