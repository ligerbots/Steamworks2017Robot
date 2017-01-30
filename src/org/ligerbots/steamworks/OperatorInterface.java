package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import org.ligerbots.steamworks.commands.ClimbCommand;
import org.ligerbots.steamworks.commands.CompressorCommand;
import org.ligerbots.steamworks.commands.GearCommand;
import org.ligerbots.steamworks.commands.IntakeCommand;
import org.ligerbots.steamworks.commands.LedRingCommand;
import org.ligerbots.steamworks.commands.ShiftCommand;
import org.ligerbots.steamworks.commands.ShooterFeederCommand;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.Pneumatics.CompressorState;
import org.ligerbots.steamworks.subsystems.Vision;

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

    JoystickButton xboxAButton = new JoystickButton(xboxController, 1);
    xboxAButton.whenPressed(new IntakeCommand(!Robot.intake.isIntakeOn()));

    JoystickButton xboxBButton = new JoystickButton(xboxController, 2);
    xboxBButton.whenPressed(new ClimbCommand());

    JoystickButton xboxXButton = new JoystickButton(xboxController, 3);
    xboxXButton.whileHeld(new ShooterFeederCommand(4000));

    JoystickButton xboxYButton = new JoystickButton(xboxController, 4);
    xboxYButton.whileHeld(new GearCommand(true, true));

    JoystickButton xboxLeftBumper = new JoystickButton(xboxController, 5);
    xboxLeftBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.DOWN));

    JoystickButton xboxRightBumper = new JoystickButton(xboxController, 6);
    xboxRightBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.UP));

    JoystickButton menuButton = new JoystickButton(xboxController, 7);
    menuButton.whenPressed(new LedRingCommand(Vision.LedState.TOGGLE));

    JoystickButton startButton = new JoystickButton(xboxController, 8);
    startButton.whenPressed(new CompressorCommand(CompressorState.TOGGLE));
  }

  public double getThrottle() {
    return xboxController.getY(GenericHID.Hand.kLeft);
  }

  public double getTurn() {
    return xboxController.getX(GenericHID.Hand.kRight);
  }
}
