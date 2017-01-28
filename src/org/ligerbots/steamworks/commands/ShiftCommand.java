package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command shifts the drive train using the specified {@link DriveTrain.ShiftType}
 */
public class ShiftCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(ShiftCommand.class);
  DriveTrain.ShiftType shiftType;
  
  /**
   * Creates a new ShiftCommand.
   * @param shiftType Whether to shift up, down, or toggle.
   */
  public ShiftCommand(DriveTrain.ShiftType shiftType) {
    requires(Robot.driveTrain);
    this.shiftType = shiftType;
  }

  protected void initialize() {
    logger.info(String.format("Initialize, type=%s", shiftType.toString()));
  }

  protected void execute() {
    Robot.driveTrain.shift(shiftType);
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
    logger.trace("Finish");
  }

  protected void interrupted() {
    logger.warn("Interrupted");
  }
}
