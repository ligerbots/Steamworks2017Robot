package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import org.ligerbots.steamworks.commands.ShiftCommand;
import org.ligerbots.steamworks.subsystems.DriveTrain;

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
    leftBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.DOWN));
    JoystickButton rightBumper = new JoystickButton(xboxController, 6);
    rightBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.UP));
  }

  public double getThrottle() {
    return xboxController.getY(GenericHID.Hand.kLeft);
  }

  public double getTurn() {
    return xboxController.getX(GenericHID.Hand.kRight);
  }
}
