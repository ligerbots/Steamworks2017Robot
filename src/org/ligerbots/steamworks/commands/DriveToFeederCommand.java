package org.ligerbots.steamworks.commands;

import java.util.LinkedList;
import java.util.List;
import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.RobotPosition;
import org.ligerbots.steamworks.commands.DriveToGearCommand.State;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.GearManipulator;
import org.ligerbots.steamworks.subsystems.GearManipulator.Position;
import org.ligerbots.steamworks.subsystems.Vision.VisionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lines up to the feeder with vision.
 */
public class DriveToFeederCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(DriveToFeederCommand.class);
  
  private static final long WAIT_VISION_NANOS = 100_000_000;
  private static final long MAX_WAIT_VISION_NANOS = 2_000_000_000;
  
  enum State {
    VISION,
    PATH_DRIVE,
    TURN_BACK_ON_TARGET,
    ULTRASONIC_DRIVE,
    DONE,
    ABORTED
  }

  State currentState = State.VISION;
  
  long nanosAtWaitForVisionStart;
  DrivePathCommand initialDriveCommand;
  DriveUltrasonicCommand driveUltrasonicCommand;
  TurnCommand turnCommand;
  double finalAngle;

  /**
   * Creates a new DriveToFeederCommand.
   */
  public DriveToFeederCommand() {
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);
    
    driveUltrasonicCommand = new DriveUltrasonicCommand(7.5, true);
  }

  protected void initialize() {
    Robot.gearManipulator.setPosition(Position.CLOSED);
    
    logger.info("Initialize, state=VISION");
    currentState = State.VISION;
    nanosAtWaitForVisionStart = System.nanoTime();
  }
  
  protected void execute() {
    super.execute();
    
    switch (currentState) {
      case VISION:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        if (System.nanoTime() - nanosAtWaitForVisionStart >= WAIT_VISION_NANOS
            && Robot.vision.isGearVisionDataValid()) {
          // get current vision data
          VisionData data = Robot.vision.getGearVisionData();
  
          double tx = data.getTvecX();
          double tz = data.getTvecZ() + RobotMap.ROBOT_GEAR_CAM_TURN_CENTER_DIST;
          double ry = data.getRvecYaw();
  
          logger.debug(String.format("tx: %f, tz: %f, ry: %f", tx, tz, ry));
          
          double distanceToFeeder = Math.sqrt(tx * tx + tz * tz);
          
          double distanceBack = 51.75;
          
          if (distanceToFeeder < distanceBack) {
            logger.info("state=ULTRASONIC_DRIVE");
            currentState = State.ULTRASONIC_DRIVE;
            driveUltrasonicCommand.initialize();
          } else {
            // calculate the location that is distanceBack inches back from the target in the robot
            // frame
            double dx = -distanceBack * Math.sin(Math.toRadians(ry));
            double dz = -distanceBack * Math.cos(Math.toRadians(ry));
  
            logger.debug(String.format("dx: %f/dz: %f", dx, dz));
  
            // add target and delta back 48 inches for actual position to go to first (still in
            // robot frame)
            double px = dx + tx;
            double pz = dz + tz;
  
            logger.debug(String.format("px: %f/pz: %f", px, pz));
  
            // calculate turn. Convert conventional counterclockwise positive from +x to our/NavX
            // convention of clockwise positive from +y
  
            RobotPosition currentPosition = Robot.driveTrain.getRobotPosition();
            double oppositeDirectionConventionalAngle =
                Math.toRadians(90 - (currentPosition.getDirection() + 180));
            
            final FieldPosition backFromCurrentPosition =
                currentPosition.add(60 * Math.cos(oppositeDirectionConventionalAngle),
                    60 * Math.sin(oppositeDirectionConventionalAngle));
            
            double deltaAngle = 90 - Math.toDegrees(Math.atan2(pz, px));
            double targetAngle = currentPosition.getDirection() + deltaAngle;
            double targetConventionalAngle = Math.toRadians(90 - targetAngle);
            double targetDistance = Math.sqrt(px * px + pz * pz);
            
            final FieldPosition midDestination =
                currentPosition.add(targetDistance * Math.cos(targetConventionalAngle),
                    targetDistance * Math.sin(targetConventionalAngle));
  
            deltaAngle = 90 - Math.toDegrees(Math.atan2(tz, tx));
            targetAngle = currentPosition.getDirection() + deltaAngle;
            targetConventionalAngle = Math.toRadians(90 - targetAngle);
            targetDistance = Math.sqrt(tx * tx + tz * tz);
            
            double feederConventionalAngle =
                Math.toRadians(90 - (currentPosition.getDirection() + ry));
            
            FieldPosition destination =
                currentPosition.add(targetDistance * Math.cos(targetConventionalAngle),
                    targetDistance * Math.sin(targetConventionalAngle));
            
            List<FieldPosition> ctrlPoints = new LinkedList<>();
            ctrlPoints.add(backFromCurrentPosition);
            ctrlPoints.add(currentPosition);
            ctrlPoints.add(midDestination);
            ctrlPoints.add(destination
                .add(-41.75 * Math.cos(feederConventionalAngle),
                    -41.75 * Math.sin(feederConventionalAngle)));
            ctrlPoints.add(destination);
            
            finalAngle = currentPosition.getDirection() + ry;
            
            logger.info(String.format("Control points: %s", ctrlPoints.toString()));
  
            initialDriveCommand =
                new DrivePathCommand(FieldMap.generateCatmullRomSpline(ctrlPoints));
            initialDriveCommand.initialize();
            
            currentState = State.PATH_DRIVE;
            logger.info("state=PATH_DRIVE");
          }
        } else if (System.nanoTime() - nanosAtWaitForVisionStart >= MAX_WAIT_VISION_NANOS) {
          logger.info("state=ABORTED");
          currentState = State.ABORTED;
        }
        break;
      case PATH_DRIVE:
        initialDriveCommand.execute();
        if (initialDriveCommand.isFinished()) {
          initialDriveCommand.end();
  
          turnCommand = new TurnCommand(DriveTrain
              .fixDegrees(
                  finalAngle - Robot.driveTrain.getRobotPosition().getDirection()));
          turnCommand.initialize();
          logger.info("state=TURN_BACK_ON_TARGET");
          currentState = State.TURN_BACK_ON_TARGET;
        }
        break;
      case TURN_BACK_ON_TARGET:
        turnCommand.execute();
        if (turnCommand.isFinished()) {
          turnCommand.end();
          
          logger.info("state=ULTRASONIC_DRIVE");
          currentState = State.ULTRASONIC_DRIVE;
          driveUltrasonicCommand.initialize();
        }
        break;
      case ULTRASONIC_DRIVE:
        driveUltrasonicCommand.execute();
        
        if (driveUltrasonicCommand.isFinished()) {
          driveUltrasonicCommand.end();
          
          currentState = State.DONE;
        }
        break;
      default:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        currentState = State.DONE;
        break;
    }
  }

  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      currentState = State.ABORTED;
      return true;
    }
  
    return currentState == State.DONE || currentState == State.ABORTED;
  }
  
  protected void end() {
    super.end();
    
    Robot.gearManipulator.setPosition(GearManipulator.Position.RECEIVE_GEAR);
    
    logger.info("Finish");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }
  
  protected void interrupted() {
    super.interrupted();
    
    logger.info("Interrupted");
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
