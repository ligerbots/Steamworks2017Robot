package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.DriveTrainPID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class TurnPIDCommand extends Command {
  double offsetDegrees;
  double acceptableError;
  int ticks;
  boolean finished;
  private static final Logger logger = LoggerFactory.getLogger(TurnPIDCommand.class);

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
    offsetDegrees = DriveTrain.fixDegrees(offsetDegrees);
    if (offsetDegrees > 180) {
      offsetDegrees = offsetDegrees - 360;
    }
    this.offsetDegrees = offsetDegrees;
    this.acceptableError = acceptableError;
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    logger.info(String.format("TurnPID started for %5.2f degrees", offsetDegrees));
    Robot.driveTrain.enableTurningControl(offsetDegrees, acceptableError);
    ticks = 5;
    finished = false;
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    // tell controlTurning to output log message every 5 ticks
    finished = Robot.driveTrain.controlTurning(ticks-- == 0);
    if (ticks <= 0) ticks = 5;
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    return finished;
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    Robot.driveTrain.disableTurningControl();
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    end();
  }
}
