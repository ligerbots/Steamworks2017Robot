package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 * This command runs the drive until it detects the climb limit switch has been pressed.
 */
public class ClimbCommand extends Command {
  public ClimbCommand() {
    requires(Robot.driveTrain);
  }

  protected void initialize() {}

  protected void execute() {
    Robot.driveTrain.climb();
  }

  protected boolean isFinished() {
    return Robot.driveTrain.isClimbLimitSwitchPressed();
  }

  protected void end() {
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void interrupted() {
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
