package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrainPID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class TurnPIDCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(TurnPIDCommand.class);
  double offsetDegrees;
  double acceptableError;
  double startAngle;
  double currentDegrees;
  int ticks = 0;
  int reps = 0;
  double startTime;
  double totalTime;
  double turningOutput;

  public TurnPIDCommand(double offsetDegrees, double acceptableError) {
    super("TurnCommand_" + offsetDegrees + "_" + acceptableError);
    requires(Robot.driveTrain);
    this.offsetDegrees = offsetDegrees;
    this.acceptableError = acceptableError;
  }

  /**
   * Set parameters for turn command
   * 
   * @param offsetDegrees The number of degrees to turn by. Clockwise is positive
   * @param acceptableError How many degrees off the turn is allowed to be.
   */
  public void setParameters(double offsetDegrees, double acceptableError) {
    this.offsetDegrees = offsetDegrees;
    this.acceptableError = acceptableError;
    // ticks = 2;
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    Robot.driveTrain.enableTurningControl(offsetDegrees, acceptableError);
    ticks = 2;
    double currentAngle = Robot.driveTrain.getYawRotation();
    startAngle = currentAngle;
    logger.info(String.format("TurnPID for %5.2f, startingAngle %5.2f, targetAngle %5.2f, acceptableError %5.2f",
            offsetDegrees, currentAngle, currentAngle + offsetDegrees, acceptableError));    
    startTime = Robot.getNanoTime();
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    // tell controlTurning to output log message every N ticks
    turningOutput = Robot.driveTrain.controlTurning(); 
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    totalTime = Robot.getNanoTime() - startTime;
    double currentAngle = Robot.driveTrain.getYawRotation();
    boolean onTarget = Robot.driveTrain.onTarget();
    if (ticks-- == 0 || onTarget) {
      logger.info(String.format("CurAngle: %5.2f, Error: %5.2f Output: %5.2f %s time %5.2f", currentAngle, 
          (startAngle + offsetDegrees) - currentAngle, turningOutput, onTarget ? " ON TARGET!" : "", totalTime));
      ticks = 2;
    }

    if (totalTime > 4.0) {
      logger.info("TurnPID timeout after 4 seconds.");
      return true;
    };
    return onTarget;
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    Robot.driveTrain.disableTurningControl();
    logger.debug("Finished");
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    Robot.driveTrain.disableTurningControl();
  }
}
