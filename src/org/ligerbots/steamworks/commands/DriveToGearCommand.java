package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.DriverStation;
import java.util.LinkedList;
import java.util.List;
import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.RobotPosition;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.GearManipulator;
import org.ligerbots.steamworks.subsystems.GearManipulator.Position;
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

  private static final long WAIT_VISION_NANOS = 100_000_000;
  private static final long MAX_WAIT_VISION_NANOS = 2_000_000_000;
  private static final long WAIT_GEAR_NANOS = 1_000_000_000;

  enum State {
    VISION,
    INITIAL_DRIVE,
    DRIVE_BACK,
    TURN_TO_PEG,
    TURN_BACK_ON_TARGET,
    DRIVE_TO_GEAR,
    DELIVER_GEAR,
    RETRY,
    DRIVE_AWAY,
    DONE,
    ABORTED
  }

  State currentState = State.VISION;
  long nanosAtWaitForVisionStart;
  long nanosAtGearDeliverStart;
  DriveDistanceCommand driveBackCommand;
  DriveUltrasonicCommand driveToGearCommand =
      new DriveUltrasonicCommand(RobotMap.GEAR_DELIVERY_DIST, false);
  boolean deliverOnly = false;
  
  double finalAngle;
  
  DrivePathCommand initialDriveCommand;
  DriveDistanceCommand driveAwayCommand;
  TurnCommand turnCommand;
  TankDriveCommand tankDrive;
  
  boolean approachedPegFromRight;

  /**
   * Creates a new DriveToGearCommand.
   */
  public DriveToGearCommand() {
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);

    driveAwayCommand = new DriveDistanceCommand(-12.0, RobotMap.GEAR_BACKOFF_SPEED);
  }
  
  public DriveToGearCommand(boolean deliverOnly) {
    this();
    this.deliverOnly = deliverOnly;
  }

  protected void initialize() {
    if (!deliverOnly) {
      logger.info("Initialize, state=VISION");
      currentState = State.VISION;
      nanosAtWaitForVisionStart = System.nanoTime();
    } else {
      currentState = State.DELIVER_GEAR;
      nanosAtGearDeliverStart = System.nanoTime();
      logger.info("Initialize, state=DELIVER_GEAR");
    }
    
    Robot.gearManipulator.setPosition(Position.CLOSED);
  }

  protected void execute() {
    super.execute();
    
    // procedure
    // completely changed, insert description here

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
          
          double distanceToGearLift = Math.sqrt(tx * tx + tz * tz);
          
          double distanceBack = 51.75;
          // double distanceSide = 6.0;
          
          if (distanceToGearLift < distanceBack) {
            if (Math.abs(tx) < 6.0 && ((ry <= 360 && ry > 340) || (ry >= 0 && ry < 20))) {
              double pegTipOffset = 14.0; // measured in CAD
              double px = -pegTipOffset * Math.sin(Math.toRadians(ry)) + tx;
              double pz = -pegTipOffset * Math.cos(Math.toRadians(ry)) + tz;
              
              double deltaAngle = 90 - Math.toDegrees(Math.atan2(pz, px));
              double angleToGearWedge;
              // choose the wedge to use
              if (deltaAngle > 0) {
                approachedPegFromRight = true;
                angleToGearWedge = Math.toDegrees(Math.atan2(RobotMap.GEAR_ALIGNMENT_OFFSET,
                    RobotMap.ROBOT_GEAR_CAM_TURN_CENTER_DIST));
              } else {
                approachedPegFromRight = false;
                angleToGearWedge = -Math.toDegrees(Math.atan2(RobotMap.GEAR_ALIGNMENT_OFFSET,
                    RobotMap.ROBOT_GEAR_CAM_TURN_CENTER_DIST));
              }
              
              turnCommand = new TurnCommand(deltaAngle - angleToGearWedge);
              turnCommand.initialize();
              logger.info("state=TURN_TO_PEG");
              currentState = State.TURN_TO_PEG;
            } else {
              driveBackCommand = new DriveDistanceCommand(distanceToGearLift - distanceBack - 6);
              driveBackCommand.initialize();
              currentState = State.DRIVE_BACK;
              logger.info("state=DRIVE_BACK");
            }
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
            
            double gearLiftConventionalAngle =
                Math.toRadians(90 - (currentPosition.getDirection() + ry));
            
            FieldPosition destination =
                currentPosition.add(targetDistance * Math.cos(targetConventionalAngle),
                    targetDistance * Math.sin(targetConventionalAngle));
            
            // final FieldPosition splineFinalControl = destination.add(
            // 24 * Math.cos(gearLiftConventionalAngle), 24 * Math.sin(gearLiftConventionalAngle));
            
            List<FieldPosition> ctrlPoints = new LinkedList<>();
            ctrlPoints.add(backFromCurrentPosition);
            ctrlPoints.add(currentPosition);
            ctrlPoints.add(midDestination/*.add(distanceSide * Math.sin(gearLiftConventionalAngle),
                distanceSide * Math.cos(gearLiftConventionalAngle))*/);
            ctrlPoints.add(destination
                // .add(distanceSide * Math.sin(gearLiftConventionalAngle),
                // distanceSide * Math.cos(gearLiftConventionalAngle))
                .add(-41.75 * Math.cos(gearLiftConventionalAngle),
                    -41.75 * Math.sin(gearLiftConventionalAngle)));
            ctrlPoints.add(destination);
            
            finalAngle = currentPosition.getDirection() + ry;
            
            logger.info(String.format("Control points: %s", ctrlPoints.toString()));
  
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
          
          logger.info("Initialize, state=VISION");
          currentState = State.VISION;
          nanosAtWaitForVisionStart = System.nanoTime();
        }
        break;
      case TURN_BACK_ON_TARGET:
      case TURN_TO_PEG:
        turnCommand.execute();
        if (turnCommand.isFinished()) {
          turnCommand.end();
          
          if (currentState == State.TURN_TO_PEG) {
            currentState = State.DRIVE_TO_GEAR;
            driveToGearCommand.initialize();
            logger.info("state=DRIVE_TO_GEAR");
          } else {
            logger.info("state=VISION");
            currentState = State.VISION;
            nanosAtWaitForVisionStart = System.nanoTime();
          }
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

          turnCommand = new TurnCommand(DriveTrain
              .fixDegrees(finalAngle - Robot.driveTrain.getRobotPosition().getDirection()));
          turnCommand.initialize();
          logger.info("state=TURN_BACK_ON_TARGET");
          currentState = State.TURN_BACK_ON_TARGET;
        }
        break;
      case DRIVE_TO_GEAR:
        driveToGearCommand.execute();
        
        // TODO: check for pressure plate pressed
        
        if (driveToGearCommand.isFinished() /* || pressurePlatePressed */) {
          driveToGearCommand.end();
          
          if (driveToGearCommand.aborted /* || !pressurePlatePressed */) {
            logger.warn("Ultrasonic drive aborted, not delivering gear");
            
            if (DriverStation.getInstance().isAutonomous()) {
              tankDrive = new TankDriveCommand(1.0, approachedPegFromRight);
              tankDrive.initialize();
              currentState = State.RETRY;
              logger.info("state=RETRY");
            } else {
              currentState = State.DRIVE_AWAY;
              logger.info("state=DRIVE_AWAY");
              driveAwayCommand.initialize();
            }
          } else {
            currentState = State.DELIVER_GEAR;
            nanosAtGearDeliverStart = System.nanoTime();
            logger.info("state=DELIVER_GEAR");
          }
        }
        break;
      case RETRY:
        tankDrive.execute();
        if (tankDrive.isFinished()) {
          tankDrive.end();
          
          currentState = State.DRIVE_TO_GEAR;
          driveToGearCommand.initialize();
          logger.info("state=DRIVE_TO_GEAR");
        }
        break;
      case DELIVER_GEAR:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        Robot.gearManipulator.setPosition(GearManipulator.Position.DELIVER_GEAR);
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
    
    Robot.gearManipulator.setPosition(GearManipulator.Position.CLOSED);
    
    logger.info("Finish");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }

  protected void interrupted() {
    super.interrupted();
    
    Robot.gearManipulator.setPosition(GearManipulator.Position.CLOSED);
    
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
