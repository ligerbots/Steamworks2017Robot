package org.ligerbots.steamworks.commands;

import java.util.LinkedList;
import java.util.List;
import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotPosition;
import org.ligerbots.steamworks.subsystems.Vision.VisionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically drives to the gear target. Since the robot is a tank drive, it drives 48 inches
 * back from the target first, then drives up. This ensures the robot is square to the lift when
 * delivering gears.
 */
public class DriveToGearCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(DriveToGearCommand.class);

  private static final long WAIT_VISION_NANOS = 500_000_000;
  private static final long MAX_WAIT_VISION_NANOS = 2_000_000_000;
  private static final long WAIT_GEAR_NANOS = 500_000_000;

  enum State {
    INITIAL_WAIT_FOR_VISION,
    INITIAL_DRIVE,
    DRIVE_BACK,
    DELIVER_GEAR,
    DRIVE_AWAY,
    DONE,
    ABORTED
  }

  State currentState = State.INITIAL_WAIT_FOR_VISION;
  long nanosAtWaitForVisionStart;
  long nanosAtGearDeliverStart;
  DriveDistanceCommand driveBackCommand;

  DrivePathCommand initialDriveCommand;
  DriveDistanceCommand driveAwayCommand;

  /**
   * Creates a new DriveToGearCommand.
   */
  public DriveToGearCommand() {
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);

    driveAwayCommand = new DriveDistanceCommand(-36.0);
  }

  protected void initialize() {
    logger.info("Initialize, state=INITIAL_WAIT_FOR_VISION");
    currentState = State.INITIAL_WAIT_FOR_VISION;
    nanosAtWaitForVisionStart = System.nanoTime();
  }

  protected void execute() {
    super.execute();
    
    // procedure
    // 1. Find where the target is (initialize)
    // 2. Turn to the place 48 inches back from target
    // 3. Drive to above place
    // 4. Turn to target based on initialize() calculation
    // 5. Take new vision frame and finely align
    // 6. Drive up

    switch (currentState) {
      case INITIAL_WAIT_FOR_VISION:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        if (System.nanoTime() - nanosAtWaitForVisionStart >= WAIT_VISION_NANOS
            && Robot.vision.isGearVisionDataValid()) {
          // get current vision data
          VisionData data = Robot.vision.getGearVisionData();

          double tx = data.getTvecX();
          double tz = data.getTvecZ();
          double ry = data.getRvecYaw();

          logger.debug(String.format("tx: %f, tz: %f, ry: %f", tx, tz, ry));
          
          double distanceToGearLift = Math.sqrt(tx * tx + tz * tz);
          
          double distanceBack = 48.0;
          
          if (distanceToGearLift < 60.0) {
            driveBackCommand = new DriveDistanceCommand(distanceToGearLift - 66);
            driveBackCommand.initialize();
            currentState = State.DRIVE_BACK;
            logger.info("state=DRIVE_BACK");
          } else {
            // calculate the location that is 48 inches back from the target in the robot frame
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
            
            double gearLiftConventionalAngle =
                Math.toRadians(90 - (currentPosition.getDirection() + ry));
            
            FieldPosition destination =
                currentPosition.add(targetDistance * Math.cos(targetConventionalAngle),
                    targetDistance * Math.sin(targetConventionalAngle));
            
            final FieldPosition splineFinalControl = destination.add(
                24 * Math.cos(gearLiftConventionalAngle), 24 * Math.sin(gearLiftConventionalAngle));
            
            List<FieldPosition> ctrlPoints = new LinkedList<>();
            ctrlPoints.add(backFromCurrentPosition);
            ctrlPoints.add(currentPosition);
            ctrlPoints.add(midDestination);
            ctrlPoints.add(destination.add(-24 * Math.cos(gearLiftConventionalAngle),
                -24 * Math.sin(gearLiftConventionalAngle)));
            ctrlPoints.add(splineFinalControl);
  
            initialDriveCommand =
                new DrivePathCommand(FieldMap.generateCatmullRomSpline(ctrlPoints));
            initialDriveCommand.initialize();
  
            currentState = State.INITIAL_DRIVE;
            logger.info("state=INITIAL_DRIVE");
          }
        } else if (System.nanoTime() - nanosAtWaitForVisionStart >= MAX_WAIT_VISION_NANOS) {
          logger.info("state=ABORTED");
          currentState = State.ABORTED;
        }
        break;
      case DRIVE_BACK:
        driveBackCommand.execute();
        if (driveBackCommand.isFinished()) {
          driveBackCommand.end();
          
          logger.info("Initialize, state=INITIAL_WAIT_FOR_VISION");
          currentState = State.INITIAL_WAIT_FOR_VISION;
          nanosAtWaitForVisionStart = System.nanoTime();
        }
        break;
      case INITIAL_DRIVE:
        initialDriveCommand.execute();
        if (initialDriveCommand.isFinished()) {
          initialDriveCommand.end();

          // if (!initialDriveCommand.succeeded) {
          // logger.warn("Initial drive command aborted!");
          // currentState = State.ABORTED;
          // return;
          // }

          currentState = State.DELIVER_GEAR;
          nanosAtGearDeliverStart = System.nanoTime();
          logger.info("state=DELIVER_GEAR");
        }
        break;
      case DELIVER_GEAR:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        Robot.gearManipulator.setOpen(true);
        if (System.nanoTime() - nanosAtGearDeliverStart > WAIT_GEAR_NANOS) {
          currentState = State.DRIVE_AWAY;
          logger.info("state=DRIVE_AWAY");
          driveAwayCommand.initialize();
        }
        break;
      case DRIVE_AWAY:
        driveAwayCommand.execute();
        if (driveAwayCommand.isFinished()) {
          driveAwayCommand.end();
          logger.info("DONE");

          if (!driveAwayCommand.succeeded) {
            logger.warn("Drive away command aborted!");
            // we'll count this as "gear delivered" and not set state to ABORTED
          }

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
