package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveTrainSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command shifts the drive train using the specified {@link DriveTrain.ShiftType}. Protects
 * the shifter by coasting if shifting while driving.
 */
public class ShiftCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(ShiftCommand.class);
  private static final long NANOS_START_TO_SHIFT = 250000000;
  private static final long NANOS_TOTAL = 500000000;

  DriveTrain.ShiftType shiftType;
  long startTime;
  double lastOutputLeft;
  double lastOutputRight;
  boolean didShift;
  boolean doWait;

  /**
   * Creates a new ShiftCommand.
   * 
   * @param shiftType Whether to shift up, down, or toggle.
   */
  public ShiftCommand(DriveTrain.ShiftType shiftType) {
    requires(Robot.driveTrain);
    this.shiftType = shiftType;
  }

  protected void initialize() {
    logger.info(String.format("Initialize, type=%s", shiftType.toString()));
    startTime = System.nanoTime();
    doWait = true;

    lastOutputLeft = Robot.driveTrain.getLastOutput(DriveTrainSide.LEFT);
    lastOutputRight = Robot.driveTrain.getLastOutput(DriveTrainSide.RIGHT);

    if (Math.abs(lastOutputLeft) < 0.02 && Math.abs(lastOutputRight) < 0.02) {
      doWait = false;
    } else {
      Robot.driveTrain.setBrakeOn(false);
      Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    }

    logger.info(String.format("Coasting/waiting=%b", doWait));

    didShift = false;
  }

  protected void execute() {
    if (doWait) {
      if (System.nanoTime() - startTime > NANOS_START_TO_SHIFT) {
        if (!didShift) {
          Robot.driveTrain.shift(shiftType);
          didShift = true;
        }
      }
    } else {
      if (!didShift) {
        Robot.driveTrain.shift(shiftType);
        didShift = true;
      }
    }
  }

  protected boolean isFinished() {
    if (doWait) {
      return System.nanoTime() - startTime > NANOS_TOTAL;
    } else {
      return true;
    }
  }

  protected void end() {
    logger.trace("Finish");

    if (doWait) {
      Robot.driveTrain.rawLeftRightDrive(lastOutputLeft, lastOutputRight);
      Robot.driveTrain.setBrakeOn(true);
    }
  }

  protected void interrupted() {
    logger.warn("Interrupted");
  }
}
