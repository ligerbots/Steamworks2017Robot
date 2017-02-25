package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command runs the drive until it detects the climb limit switch has been pressed.
 */
public class HoldPositionCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(HoldPositionCommand.class);
  
  boolean holdPosition;
  boolean toggle;
  
  /**
   * Creates a new HoldPositionCommand and sets it to toggle holding position.
   */
  public HoldPositionCommand() {
    requires(Robot.driveTrain);
    toggle = true;
  }
  
  /**
   * Creates a new HoldPositionCommand that will enable or disable holding position.
   * @param holdPosition Whether to hold position
   */
  public HoldPositionCommand(boolean holdPosition) {
    requires(Robot.driveTrain);
    toggle = false;
    this.holdPosition = holdPosition;
  }

  protected void initialize() {
    logger.info("Starting");
  }

  protected void execute() {
    logger.trace("Running");
    if (toggle) {
      Robot.driveTrain.setHoldPositionEnabled(!Robot.driveTrain.isHoldPositionEnabled());
    } else {
      Robot.driveTrain.setHoldPositionEnabled(holdPosition);
    }
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
    logger.info("Finished");
  }

  protected void interrupted() {
    logger.info("Interrupted");
  }
}
