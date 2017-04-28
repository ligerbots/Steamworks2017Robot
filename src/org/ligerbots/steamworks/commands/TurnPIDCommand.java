package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class TurnPIDCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(TurnPIDCommand.class);
  double offsetDegrees;
  double acceptableError;
  double startDegrees;
  int ticks = 0;

  public TurnPIDCommand(double offsetDegrees, double acceptableError) {
    super("TurnCommand_" + offsetDegrees + "_" + acceptableError);
    requires(Robot.driveTrain);
    this.offsetDegrees = offsetDegrees;
    this.acceptableError = acceptableError;
    startDegrees = Robot.driveTrain.getYaw();

  }

  /**
   * Set parameters for turn command
   * 
   * @param offsetDegrees The number of degrees to turn by. Clockwise is positive
   * @param acceptableError How many degrees off the turn is allowed to be.
   */
  public void setParameters(double offsetDegrees, double acceptableError) {
    offsetDegrees = DriveTrain.fixDegrees(offsetDegrees);
    if (offsetDegrees > 180) {
      offsetDegrees = offsetDegrees - 360;
    }
    this.offsetDegrees = offsetDegrees;
    this.acceptableError = acceptableError;
    ticks = 2;
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    double startingAngle = Robot.driveTrain.getYaw();
    if (startingAngle > 180) {
      startingAngle = startingAngle - 360;
    }
    logger.info(String.format("TurnPID for %5.2f, startingAngle %5.2f, acceptableError %5.2f",
        offsetDegrees, startingAngle, acceptableError));
    Robot.driveTrain.enableTurningControl(offsetDegrees, acceptableError);
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    Robot.driveTrain.controlTurning();
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    double currentAngle = Robot.driveTrain.getYawRotation();
    if (currentAngle > 180) {
      currentAngle = currentAngle - 360;
    }
    double amountTurned = Math.abs((startDegrees - currentAngle));
    if (amountTurned > 180) {
      amountTurned = amountTurned - 360;
    }
    boolean finished = Math.abs(offsetDegrees - amountTurned)< acceptableError;
    if (ticks-- == 0 || finished) {
      logger.info(String.format("Current Angle: %5.2f, Amount Turned: %5.2f, Difference: %5.2f ",
          currentAngle, amountTurned, Math.abs(offsetDegrees - amountTurned)));
      ticks = 2;
    }
    return finished;
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
    end();
  }
}
