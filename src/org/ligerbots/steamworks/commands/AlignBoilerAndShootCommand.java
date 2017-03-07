package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain.ShiftType;
import org.ligerbots.steamworks.subsystems.Vision.VisionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command automatically gets vision data, begins to spin up shooter while aligning, and then
 * shoots when everything is ready.
 */
public class AlignBoilerAndShootCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(AlignBoilerAndShootCommand.class);

  private static final long WAIT_NANOS = 250_000_000;

  enum State {
    WAIT_FOR_VISION, TURN, SHOOT, DONE, ABORTED, DRIVE_TO_RANGE
  }

  State currentState;
  TurnCommand turnCommand;
  ShooterFeederCommand shooterFeederCommand;
  DriveDistanceCommand driveDistanceCommand;

  boolean justStarted;
  long nanosStartOfWait;
  double currentAngle;

  /**
   * Creates a new AlignBoilerAndShootCommand.
   */
  public AlignBoilerAndShootCommand() {
    requires(Robot.driveTrain);
    requires(Robot.shooter);
    requires(Robot.feeder);
  }

  @Override
  protected void initialize() {
    logger.info("Initialize, state=WAIT_FOR_VISION");
    currentState = State.WAIT_FOR_VISION;

    shooterFeederCommand = new ShooterFeederCommand(0);
    shooterFeederCommand.initialize();
    shooterFeederCommand.setWithholdShooting(true);
    
    justStarted = true;
  }

  @Override
  protected void execute() {
    super.execute();
    shooterFeederCommand.execute();
    if (shooterFeederCommand.isFinished()) {
      if (shooterFeederCommand.aborted) {
        logger.error("ShooterFeederCommand aborted!");
      } else {
        logger.info("ShooterFeederCommand ended");
      }

      currentState = State.DONE;
    }

    switch (currentState) {
      case WAIT_FOR_VISION:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        if ((justStarted || System.nanoTime() - nanosStartOfWait >= WAIT_NANOS)
            && Robot.vision.isBoilerVisionDataValid()) {
          VisionData data = Robot.vision.getBoilerVisionData();
          // phone is rotated, so Y is actually the axis that would be zero if aligned
          double cx = data.getCenterX();
          double cy = data.getCenterY();
          logger.info(String.format("cx %f, cy %f", cx, cy));
          
          double boilerCenterHeight = (FieldPosition.BOILER_LOW_VISION_TARGET_BOTTOM + 5.0)
              - RobotMap.BOILER_CAMERA_HEIGHT;
          // use small angle approximation to turn image position into angle from the camera's
          // center of frame. Nexus 5 half-vertical-FOV is 60deg/2 = 30deg
          // use 0.62 for cx 0.49 for cy
          double angleOnCamera = (cx - 0.5) * 30 / 0.5;
          double angleFromGround = angleOnCamera + RobotMap.VISION_BOILER_CAMERA_ANGLE;
          double distanceToTarget = boilerCenterHeight / Math.tan(Math.toRadians(angleFromGround));
          logger.info(String.format("distance %f", distanceToTarget));
          
          if (Math.abs(cy - 0.5) >= 0.02) {
            currentState = State.TURN;
            Robot.driveTrain.shift(ShiftType.DOWN);
            justStarted = false;
            logger.info("state=TURN");
          } else if (distanceToTarget > RobotMap.SHOOTING_DISTANCE
              + RobotMap.AUTO_DRIVE_ACCEPTABLE_ERROR
              || distanceToTarget < RobotMap.SHOOTING_DISTANCE
                  - RobotMap.AUTO_DRIVE_ACCEPTABLE_ERROR) {
            currentState = State.DRIVE_TO_RANGE;
            justStarted = false;
            double distanceToDrive = distanceToTarget - RobotMap.SHOOTING_DISTANCE;
            logger.info(String.format("state=DRIVE_TO_RANGE dist=%f", distanceToDrive));
            driveDistanceCommand =
                new DriveDistanceCommand(distanceToDrive);
            driveDistanceCommand.initialize();
          } else {
            // calculate shooter rpm
            double calculatedRpm = RobotMap.SHOOTING_RPM;
            logger.info(String.format("Shooter rpm: %f", calculatedRpm));
            shooterFeederCommand.setRpm(calculatedRpm);
            
            shooterFeederCommand.setWithholdShooting(false);
            currentState = State.SHOOT;
            justStarted = false;
            logger.info("state=SHOOT");
          }
        }
        break;
      case TURN:
        if (!Robot.vision.isBoilerVisionDataValid()) {
          Robot.driveTrain.rawThrottleTurnDrive(0, 0);
          logger.info("Lost vision, state=WAIT_FOR_VISION");
          currentState = State.WAIT_FOR_VISION;
        } else {
          VisionData data = Robot.vision.getBoilerVisionData();
          
          if (Math.abs(data.getCenterY() - 0.5) < 0.02) {
            Robot.driveTrain.rawThrottleTurnDrive(0, 0);
            logger.info("Completed turn, state=WAIT_FOR_VISION");
            currentState = State.WAIT_FOR_VISION;
          }
          
          double turn = RobotMap.AUTO_TURN_MIN_SPEED_LOW
              + Math.abs(data.getCenterY() - 0.5) * (1 - RobotMap.AUTO_TURN_MIN_SPEED_LOW);
          // turn clockwise if necessary, otherwise counterclockwise
          if (data.getCenterY() < 0.5) {
            turn = -turn;
          }
          Robot.driveTrain.rawThrottleTurnDrive(0, turn);
        }
        break;
      case DRIVE_TO_RANGE:
        driveDistanceCommand.execute();
        if (driveDistanceCommand.isFinished()) {
          driveDistanceCommand.end();
          
          if (!driveDistanceCommand.succeeded) {
            logger.warn("drive command failed, state=ABORTED");
            currentState = State.ABORTED;
          } else {
            currentState = State.WAIT_FOR_VISION;
            nanosStartOfWait = System.nanoTime();
            logger.info("state=WAIT_FOR_VISION");
          }
        }
        break;
      default:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        break;
    }
  }

  @Override
  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      currentState = State.ABORTED;
      return true;
    }

    return currentState == State.DONE || currentState == State.ABORTED;
  }

  @Override
  protected void end() {
    super.end();

    logger.info("Finish");
    shooterFeederCommand.end();

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }

  @Override
  protected void interrupted() {
    super.interrupted();

    logger.warn("Interrupted");
    shooterFeederCommand.interrupted();

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }

  @Override
  protected String getState() {
    return currentState.toString();
  }
  
  protected boolean isFailedToComplete() {
    return currentState == State.ABORTED;
  }
}
