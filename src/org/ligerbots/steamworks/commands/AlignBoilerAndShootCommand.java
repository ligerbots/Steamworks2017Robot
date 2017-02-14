package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
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
          double tx = data.getTvecX();
          double tz = data.getTvecZ();
          double ty = data.getTvecY();
          double angle = 90 - Math.toDegrees(Math.atan2(tz, tx));
          currentAngle = angle;
          logger.info(String.format("Got data: tx=%f, tz=%f, angle=%f, state=TURN", tx, tz, angle));

          // calculate shooter rpm
          double calculatedRpm = 5000;
          logger.info(String.format("Shooter rpm: %f", calculatedRpm));
          shooterFeederCommand.setRpm(calculatedRpm);

          if (Math.abs(angle) < RobotMap.AUTO_TURN_ACCEPTABLE_ERROR) {
            double distanceFromCameraToBoilerSquared = tz * tz + ty * ty;
            double deltaHeight = 6 * 12 + 9 - RobotMap.BOILER_CAMERA_HEIGHT;
            double horizontalDistanceFromCameraToBoiler =
                Math.sqrt(distanceFromCameraToBoilerSquared - deltaHeight * deltaHeight);
            if (horizontalDistanceFromCameraToBoiler > RobotMap.MAXIMUM_SHOOTING_DISTANCE
                || horizontalDistanceFromCameraToBoiler < RobotMap.MINIMUM_SHOOTING_DISTANCE) {
              currentState = State.DRIVE_TO_RANGE;
              justStarted = false;
              logger.info("state=DRIVE_TO_RANGE");
              
              if (horizontalDistanceFromCameraToBoiler > RobotMap.MAXIMUM_SHOOTING_DISTANCE) {
                driveDistanceCommand = new DriveDistanceCommand(
                    horizontalDistanceFromCameraToBoiler - RobotMap.MAXIMUM_SHOOTING_DISTANCE + 10);
              } else {
                driveDistanceCommand = new DriveDistanceCommand(
                    horizontalDistanceFromCameraToBoiler - RobotMap.MINIMUM_SHOOTING_DISTANCE - 10);
              }
              driveDistanceCommand.initialize();
            } else {
              shooterFeederCommand.setWithholdShooting(false);
              currentState = State.SHOOT;
              justStarted = false;
              logger.info("state=SHOOT");
            }
          } else {
            turnCommand = new TurnCommand(angle);
            turnCommand.initialize();
            currentState = State.TURN;
            justStarted = false;
            logger.info("state=TURN");
          }
        }
        break;
      case TURN:
        turnCommand.execute();
        if (turnCommand.isFinished()) {
          turnCommand.end();

          if (!turnCommand.succeeded) {
            logger.warn("turn command failed, state=ABORTED");
            currentState = State.ABORTED;
          } else {
            logger.info("state=WAIT_FOR_VISION");
            nanosStartOfWait = System.nanoTime();
            currentState = State.WAIT_FOR_VISION;
          }
        }

        if (!Robot.vision.isBoilerVisionDataValid()) {
          Robot.driveTrain.rawThrottleTurnDrive(0, 0);
          logger.info("Lost vision, state=WAIT_FOR_VISION");
          currentState = State.WAIT_FOR_VISION;
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
