package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command runs the drive until it detects the climb limit switch has been pressed.
 */
public class ClimbCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(ClimbCommand.class);
  
  /**
   * Creates a new ClimbCommand.
   */
  public ClimbCommand() {
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    logger.info("Starting");
  }

  protected void execute() {
    logger.trace("Running");
    Robot.driveTrain.climb();
  }

  protected boolean isFinished() {
    return Robot.driveTrain.isClimbLimitSwitchPressed();
  }

  protected void end() {
    logger.info("Finished");
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void interrupted() {
    logger.info("Interrupted");
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
