package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveTrainSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command shifts the drive train using the specified {@link DriveTrain.ShiftType}
 */
public class ShiftCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(ShiftCommand.class);
  private static final long NANOS_START_TO_SHIFT = 250000000;
  private static final long NANOS_TOTAL =          500000000;
  
  DriveTrain.ShiftType shiftType;
  long startTime;
  double lastOutputLeft;
  double lastOutputRight;
  
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
    startTime = System.nanoTime();
    
    lastOutputLeft = Robot.driveTrain.getLastOutput(DriveTrainSide.LEFT);
    lastOutputRight = Robot.driveTrain.getLastOutput(DriveTrainSide.RIGHT);
    
    Robot.driveTrain.setBrakeOn(false);
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void execute() {
    if (System.nanoTime() - startTime > NANOS_START_TO_SHIFT) {
      Robot.driveTrain.shift(shiftType);
    }
  }

  protected boolean isFinished() {
    return System.nanoTime() - startTime > NANOS_TOTAL;
  }

  protected void end() {
    logger.trace("Finish");
    
    Robot.driveTrain.rawLeftRightDrive(lastOutputLeft, lastOutputRight);
    Robot.driveTrain.setBrakeOn(true);
  }

  protected void interrupted() {
    logger.warn("Interrupted");
  }
}
