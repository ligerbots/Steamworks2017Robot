package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command runs the drive until it detects the climb limit switch has been pressed.
 */
public class ClimbCommand extends Command {
  Logger logger = LoggerFactory.getLogger(ClimbCommand.class);
  public ClimbCommand() {
    requires(Robot.driveTrain);
  }

  protected void initialize() {}

  protected void execute() {
    logger.trace("Starting climb command");
    Robot.driveTrain.climb();
  }

  protected boolean isFinished() {
    return Robot.driveTrain.isClimbLimitSwitchPressed();
  }

  protected void end() {
    logger.trace("Climb Finished");
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void interrupted() {
    logger.warn("Climbing Interrupted");
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
