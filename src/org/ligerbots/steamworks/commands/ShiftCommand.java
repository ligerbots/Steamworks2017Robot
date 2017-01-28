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
  Logger logger = LoggerFactory.getLogger(ShiftCommand.class);
  DriveTrain.ShiftType shiftType;
  
  public ShiftCommand(DriveTrain.ShiftType shiftType) {
    logger.trace("Shift beginning");
    requires(Robot.driveTrain);
    this.shiftType = shiftType;
  }

  protected void initialize() {}

  protected void execute() {
    logger.trace("Shifting");
    Robot.driveTrain.shift(shiftType);
  }

  protected boolean isFinished() {

    return true;
  }

  protected void end() {
    logger.trace("Shift complete");
  }

  protected void interrupted() {
    logger.warn("Shift interrupted");
  }
}
