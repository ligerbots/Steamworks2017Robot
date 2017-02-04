package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveTrainSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DriveDistanceCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(DriveDistanceCommand.class);

  private static final double ACCEPTABLE_ERROR = 2.0; // in
  private static final double TURN_PROPORTIONAL_CONSTANT = 0.05;
  private static final double MIN_SPEED = 0.3;
  private static final double START_SPEED = 0.5;
  private static final double MAX_SPEED = 0.7;
  private static final double RAMP_UP_DIST = 24.0;
  private static final double RAMP_DOWN_DIST = 48.0;

  double offsetInches;

  double startLeftEncoderValue;
  double startRightEncoderValue;
  double startYaw;

  double delta;
  double error;

  public DriveDistanceCommand(double offsetInches) {
    super("DriveDistanceCommand_" + offsetInches);
    this.offsetInches = offsetInches;
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    startLeftEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT);
    startRightEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT);
    startYaw = Robot.driveTrain.getYaw();
  }

  protected void execute() {
    double currentLeftEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT);
    double currentRightEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT);
    delta = ((currentLeftEncoderValue - startLeftEncoderValue)
        + (currentRightEncoderValue - startRightEncoderValue)) / 2;
    error = Math.abs(delta - offsetInches);
    double yawDifference;
    double currentYaw = Robot.driveTrain.getYaw();
    if (Math.abs(currentYaw - startYaw) < 180) {
      yawDifference = currentYaw - startYaw;
    } else {
      yawDifference = 360 - Math.abs(currentYaw - startYaw);
      if (currentYaw > startYaw) {
        yawDifference = -yawDifference;
      }
    }

    logger.info(String.format("left: %f, right: %f, delta: %f, error: %f, yawError: %f",
        currentLeftEncoderValue, currentRightEncoderValue, delta, error, yawDifference));

    double turn = TURN_PROPORTIONAL_CONSTANT * yawDifference;
    if (turn > 1.0) {
      turn = 1.0;
    } else if (turn < -1.0) {
      turn = -1.0;
    }

    if (error < RAMP_DOWN_DIST) {
      double driveSpeed = (error * (MAX_SPEED - MIN_SPEED) / RAMP_DOWN_DIST) + MIN_SPEED;
      Robot.driveTrain.joystickDrive(offsetInches > 0 ? driveSpeed : -driveSpeed, turn);
    } else if (Math.abs(delta) < RAMP_UP_DIST) {
      double driveSpeed =
          (Math.abs(delta) * (MAX_SPEED - START_SPEED) / RAMP_UP_DIST) + START_SPEED;
      Robot.driveTrain.joystickDrive(offsetInches > 0 ? driveSpeed : -driveSpeed, turn);
    } else {
      Robot.driveTrain.joystickDrive(offsetInches > 0 ? MAX_SPEED : -MAX_SPEED, turn);
    }
  }

  protected boolean isFinished() {
    if ((offsetInches < 0 && delta < offsetInches) || (offsetInches > 0 && delta > offsetInches)) {
      return true;
    }

    if (Robot.operatorInterface.isCancelled()) {
      return true;
    }

    return error < ACCEPTABLE_ERROR;
  }

  protected void end() {
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void interrupted() {
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
