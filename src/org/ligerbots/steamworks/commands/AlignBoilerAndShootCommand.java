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
    WAIT_FOR_VISION, START_TURN, TURNING, SHOOT, DONE, ABORTED, DRIVE_TO_RANGE
  }

  State currentState;
  TurnPIDCommand turnCommand;
  ShooterFeederCommand shooterFeederCommand;

  boolean justStarted;
  long nanosStartOfWait;
  double currentAngle;
  double angleToBoiler;
  long startTime;
  boolean correctDistance;

  /**
   * Creates a new AlignBoilerAndShootCommand.
   */
  public AlignBoilerAndShootCommand() {
    requires(Robot.driveTrain);
    requires(Robot.shooter);
    requires(Robot.feeder);
    //turnCommand = new TurnCommand(0.0); // fill in actual angle from Vision
    turnCommand = new TurnPIDCommand(0.0, RobotMap.AUTO_TURN_ACCEPTABLE_ERROR); // fill in actual angle from Vision
  }

  @Override
  protected void initialize() {
    logger.info("Initialize, state=WAIT_FOR_VISION");
    currentState = State.WAIT_FOR_VISION;

    shooterFeederCommand = new ShooterFeederCommand(0);
    shooterFeederCommand.initialize();
    shooterFeederCommand.setWithholdShooting(true);
    
    Robot.driveTrain.shift(ShiftType.DOWN);
    
    justStarted = true;
    startTime = System.nanoTime();
    
    correctDistance = false;
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
          angleToBoiler = - ((cy - 0.5) * 24 / 0.5);
          
          logger.info(String.format("distance %f", distanceToTarget));
          
          if (Math.abs(cy - 0.5) >= 0.02) {
            currentState = State.START_TURN;
            Robot.driveTrain.shift(ShiftType.DOWN);
            justStarted = false;
            logger.info(String.format("state=START_TURN, cy=%5.2f, angleToBoiler=%5.2f", cy,
                angleToBoiler));
          } else if (cx < RobotMap.BOILER_MIN_DIST || cx > RobotMap.BOILER_MAX_DIST) {
            currentState = State.DRIVE_TO_RANGE;
            justStarted = false;
            logger.info(String.format("state=DRIVE_TO_RANGE, cx=%5.2f, distance=%5.2f", cx,
                distanceToTarget));
          } else {
            // calculate shooter rpm
            double calculatedRpm = RobotMap.SHOOTING_RPM;
            logger.info(String.format("Shooter rpm: %f", calculatedRpm));
            shooterFeederCommand.setRpm(calculatedRpm);
            
            shooterFeederCommand.setWithholdShooting(false);
            currentState = State.SHOOT;
            justStarted = false;
            logger.info(String.format("state=SHOOT, time to prepare=%5.2f seconds", 
                                      (System.nanoTime() - startTime) / RobotMap.NANOS_PER_SECOND));
          }
        }
        break;
      case START_TURN:
        if (!Robot.vision.isBoilerVisionDataValid()) {
          Robot.driveTrain.rawThrottleTurnDrive(0, 0);
          logger.info("Lost vision, state=WAIT_FOR_VISION");
          nanosStartOfWait = System.nanoTime();
          currentState = State.WAIT_FOR_VISION;
        } else {
          // we got data from WAIT_FOR_VISION

          turnCommand.setParameters(angleToBoiler, RobotMap.AUTO_TURN_ACCEPTABLE_ERROR, 0.25);
          turnCommand.initialize();
          
          currentState = State.TURNING;
        }
        // fall through
      case TURNING:
        turnCommand.execute();
        if (turnCommand.isFinished()) {
          turnCommand.end();
          if (correctDistance) {
            // calculate shooter rpm
            double calculatedRpm = RobotMap.SHOOTING_RPM;
            logger.info(String.format("Shooter rpm: %f", calculatedRpm));
            shooterFeederCommand.setRpm(calculatedRpm);
            
            shooterFeederCommand.setWithholdShooting(false);
            currentState = State.SHOOT;
            justStarted = false;
            logger.info(String.format("state=SHOOT, time to prepare=%5.2f seconds", 
                                      (System.nanoTime() - startTime) / RobotMap.NANOS_PER_SECOND));
          } else {
            currentState = State.WAIT_FOR_VISION;
          }
        }

         
        /*
         * old way based on constant feedback from Vision
        // VisionData data = Robot.vision.getBoilerVisionData();
          if (Math.abs(data.getCenterY() - 0.5) < 0.01) {
            Robot.driveTrain.rawThrottleTurnDrive(0, 0);
            logger.info("Completed turn, state=WAIT_FOR_VISION");
            nanosStartOfWait = System.nanoTime();
            currentState = State.WAIT_FOR_VISION;
          }
          
          double turn = RobotMap.AUTO_BOILER_TURN_SPEED
              + Math.abs(data.getCenterY() - 0.5) * (1 - RobotMap.AUTO_BOILER_TURN_SPEED);
          if (Math.abs(data.getCenterY() - 0.5) < 0.2) {
            turn = RobotMap.AUTO_BOILER_TURN_SPEED;
          }
          // turn clockwise if necessary, otherwise counterclockwise
          if (data.getCenterY() < 0.5) {
            turn = -turn;
          }
          Robot.driveTrain.rawThrottleTurnDrive(0, turn);
        } */
        break;
      case DRIVE_TO_RANGE:
        if (!Robot.vision.isBoilerVisionDataValid()) {
          Robot.driveTrain.rawThrottleTurnDrive(0, 0);
          logger.info("Lost vision, state=WAIT_FOR_VISION");
          nanosStartOfWait = System.nanoTime();
          currentState = State.WAIT_FOR_VISION;
          correctDistance = true;
        } else {
          VisionData data = Robot.vision.getBoilerVisionData();
          double cx = data.getCenterX();
          
          if (cx > RobotMap.BOILER_MAX_DIST) {
            Robot.driveTrain.rawThrottleTurnDrive(RobotMap.AUTO_DRIVE_MIN_SPEED_LOW, 0);
          } else if (cx < RobotMap.BOILER_MIN_DIST) {
            Robot.driveTrain.rawThrottleTurnDrive(-RobotMap.AUTO_DRIVE_MIN_SPEED_LOW, 0);
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
