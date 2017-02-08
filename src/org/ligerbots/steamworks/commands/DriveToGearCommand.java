package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Vision.VisionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically drives to the gear target. Since the robot is a tank drive, it drives 48 inches
 * back from the target first, then drives up. This ensures the robot is square to the lift when
 * delivering gears.
 */
public class DriveToGearCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(DriveToGearCommand.class);

  private static final long WAIT_VISION_NANOS = 500_000_000;
  private static final long MAX_WAIT_VISION_NANOS = 2_000_000_000;
  
  private static final long WAIT_GEAR_NANOS = 500_000_000;

  enum State {
    INITIAL_TURN,
    INITIAL_DRIVE,
    ALIGNMENT_TURN,
    WAIT_FOR_VISION,
    FINE_ALIGNMENT_TURN,
    FINAL_DRIVE,
    DELIVER_GEAR,
    DRIVE_AWAY
  }

  State currentState = State.INITIAL_TURN;
  boolean validData;
  boolean done;
  long nanosAtWaitForVisionStart;
  long nanosAtGearDeliverStart;

  TurnCommand initialTurnCommand;
  DriveDistanceCommand initialDriveCommand;
  TurnCommand alignmentTurnCommand;
  TurnCommand fineAlignmentTurnCommand;
  DriveDistanceCommand finalDriveCommand;
  DriveDistanceCommand driveAwayCommand;

  /**
   * Creates a new DriveToGearCommand.
   */
  public DriveToGearCommand() {
    super("DriveToGearCommand");
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);
    
    driveAwayCommand = new DriveDistanceCommand(-36.0);
  }

  protected void initialize() {
    logger.info("Initialize, state=INITIAL_TURN");
    currentState = State.INITIAL_TURN;

    // do we have recent data? if not, the command ends immediately
    if (!Robot.vision.isGearVisionDataValid()) {
      validData = false;
      done = true;
      return;
    }

    // get current vision data
    VisionData data = Robot.vision.getGearVisionData();

    double tx = data.getTvecX();
    double tz = data.getTvecZ();
    double ry = data.getRvecYaw();

    validData = true;
    done = false;

    logger.debug(String.format("tx: %f, tz: %f, ry: %f", tx, tz, ry));

    // calculate the location that is 48 inches back from the target in the robot frame
    double dx = -48.0 * Math.sin(Math.toRadians(ry));
    double dz = -48.0 * Math.cos(Math.toRadians(ry));

    logger.debug(String.format("dx: %f/dz: %f", dx, dz));

    // add target and delta back 48 inches for actual position to go to first (still in robot frame)
    double px = dx + tx;
    double pz = dz + tz;

    logger.debug(String.format("px: %f/pz: %f", px, pz));

    // calculate turn. Convert conventional counterclockwise positive from +x to our/NavX convention
    // of clockwise positive from +y
    double initialTurn = 90 - Math.toDegrees(Math.atan2(pz, px));
    double initialDist = Math.sqrt(px * px + pz * pz);

    if (Math.abs(initialTurn) > 90) {
      initialTurn = Math.signum(initialTurn) * (Math.abs(initialTurn) - 180);
      initialDist = -initialDist;
    }

    // calculate turn back toward target after we drive to target - 48
    double finalTurn = -initialTurn + ry;

    logger.debug(String.format("initialTurn: %f / initialDist: %f / finalTurn: %f", initialTurn,
        initialDist, finalTurn));

    initialTurnCommand = new TurnCommand(initialTurn);
    initialDriveCommand = new DriveDistanceCommand(initialDist);
    alignmentTurnCommand = new TurnCommand(finalTurn);

    initialTurnCommand.initialize();
  }

  protected void execute() {
    if (!validData) {
      return;
    }

    // procedure
    // 1. Find where the target is (initialize)
    // 2. Turn to the place 48 inches back from target
    // 3. Drive to above place
    // 4. Turn to target based on initialize() calculation
    // 5. Take new vision frame and finely align
    // 6. Drive up

    switch (currentState) {
      case INITIAL_TURN:
        initialTurnCommand.execute();
        if (initialTurnCommand.isFinished()) {
          initialTurnCommand.end();

          if (!initialTurnCommand.succeeded) {
            logger.warn("Initial turn command aborted!");
            done = true;
            return;
          }

          currentState = State.INITIAL_DRIVE;
          initialDriveCommand.initialize();
          logger.info("state=INITIAL_DRIVE");
        }
        break;
      case INITIAL_DRIVE:
        initialDriveCommand.execute();
        if (initialDriveCommand.isFinished()) {
          initialDriveCommand.end();

          if (!initialDriveCommand.succeeded) {
            logger.warn("Initial drive command aborted!");
            done = true;
            return;
          }

          currentState = State.ALIGNMENT_TURN;
          alignmentTurnCommand.initialize();
          logger.info("state=ALIGNMENT_TURN");
        }
        break;
      case ALIGNMENT_TURN:
        alignmentTurnCommand.execute();
        if (alignmentTurnCommand.isFinished()) {
          alignmentTurnCommand.end();

          if (!alignmentTurnCommand.succeeded) {
            logger.warn("Alignment turn command aborted!");
            done = true;
            return;
          }

          currentState = State.WAIT_FOR_VISION;
          logger.info("state=WAIT_FOR_VISION");
          nanosAtWaitForVisionStart = System.nanoTime();
        }
        break;
      case WAIT_FOR_VISION:
        // wait a bit for the robot to settle down then get a new vision frame
        if (System.nanoTime() - nanosAtWaitForVisionStart >= WAIT_VISION_NANOS) {
          if (System.nanoTime() - nanosAtWaitForVisionStart >= MAX_WAIT_VISION_NANOS) {
            logger.warn("Timeout while waiting for vision");
            validData = false;
            done = true;
          }

          if (!Robot.vision.isGearVisionDataValid()) {
            return;
          }

          VisionData data = Robot.vision.getGearVisionData();
          double tx = data.getTvecX();
          double tz = data.getTvecZ();
          double ry = data.getRvecYaw();

          logger.debug(String.format("tx: %f, tz: %f, ry: %f", tx, tz, ry));

          // find where the target actually is now
          double fineAngle = 90 - Math.toDegrees(Math.atan2(tz, tx));
          double fineDist = Math.sqrt(tx * tx + tz * tz) - 24.0;
          logger.info(String.format("fineAngle: %f, fineDist: %f", fineAngle, fineDist));
          fineAlignmentTurnCommand = new TurnCommand(fineAngle);
          finalDriveCommand = new DriveDistanceCommand(fineDist);
          currentState = State.FINE_ALIGNMENT_TURN;
          fineAlignmentTurnCommand.initialize();
          logger.debug("state=FINE_ALIGNMENT_TURN");
        }
        break;
      case FINE_ALIGNMENT_TURN:
        fineAlignmentTurnCommand.execute();
        if (fineAlignmentTurnCommand.isFinished()) {
          fineAlignmentTurnCommand.end();

          if (!fineAlignmentTurnCommand.succeeded) {
            logger.warn("Fine alignment turn command aborted!");
            done = true;
            return;
          }

          currentState = State.FINAL_DRIVE;
          finalDriveCommand.initialize();
          logger.info("state=FINAL_DRIVE");
        }
        break;
      case FINAL_DRIVE:
        finalDriveCommand.execute();
        if (finalDriveCommand.isFinished()) {
          finalDriveCommand.end();

          if (!finalDriveCommand.succeeded) {
            logger.warn("Final drive command aborted!");
            done = true;
            return;
          }

          currentState = State.DELIVER_GEAR;
          nanosAtGearDeliverStart = System.nanoTime();
          logger.info("state=DELIVER_GEAR");
        }
        break;
      case DELIVER_GEAR:
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
          
          if (!finalDriveCommand.succeeded) {
            logger.warn("Drive away command aborted!");
          }
          
          done = true;
        }
        break;
      default:
        done = true;
        break;
    }
  }

  protected boolean isFinished() {
    if (!validData) {
      // we don't have valid vision data
      logger.warn("Vision data not valid");
      return true;
    }

    if (Robot.operatorInterface.isCancelled()) {
      return true;
    }

    return done;
  }

  protected void end() {
    logger.info("Finish");
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void interrupted() {
    logger.info("Interrupted");
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
