package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Vision.VisionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DriveToGearCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(DriveToGearCommand.class);
  
  private static final long WAIT_VISION_NANOS = 500_000_000;

  enum State {
    INITIAL_TURN, INITIAL_DRIVE, ALIGNMENT_TURN, WAIT_FOR_VISION, FINE_ALIGNMENT_TURN, FINAL_DRIVE
  }

  State currentState = State.INITIAL_TURN;
  boolean validResults;
  boolean done;
  long nanosAtWaitForVisionStart;

  TurnCommand initialTurnCommand;
  DriveDistanceCommand initialDriveCommand;
  TurnCommand alignmentTurnCommand;
  TurnCommand fineAlignmentTurnCommand;
  DriveDistanceCommand finalDriveCommand;
  
  public DriveToGearCommand() {
    super("DriveToGearCommand");
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    currentState = State.INITIAL_TURN;

    VisionData data = Robot.vision.getVisionData();

    double tx = data.getTvecX();
    double tz = data.getTvecZ();
    double ry = data.getRvecYaw();

    if (Double.isNaN(tx) || Double.isNaN(tz) || Double.isNaN(ry)) {
      validResults = false;
      done = true;
      return;
    }

    validResults = true;
    done = false;

    // double transformedX = tx * Math.cos(ry / 180.0 * Math.PI) - tz * Math.sin(ry / 180.0 *
    // Math.PI);
    // double transformedZ = tx * Math.sin(ry / 180.0 * Math.PI) + tz * Math.cos(ry / 180.0 *
    // Math.PI);

    logger.debug(String.format("tx: %f, tz: %f, ry: %f", tx, tz, ry));

    double dx = -48.0 * Math.sin(Math.toRadians(ry));
    double dz = -48.0 * Math.cos(Math.toRadians(ry));

    logger.debug(String.format("dx: %f/dz: %f", dx, dz));

    double px = dx + tx;
    double pz = dz + tz;

    logger.debug(String.format("px: %f/pz: %f", px, pz));

    double initialTurn = 90 - Math.toDegrees(Math.atan2(pz, px));
    double initialDist = Math.sqrt(px * px + pz * pz);

    double finalTurn = -initialTurn + ry;

    logger.debug(String.format("initialTurn: %f / initialDist: %f / finalTurn: %f", initialTurn,
        initialDist, finalTurn));

    initialTurnCommand = new TurnCommand(initialTurn);
    initialDriveCommand = new DriveDistanceCommand(initialDist);
    alignmentTurnCommand = new TurnCommand(finalTurn);
    
    initialTurnCommand.initialize();
  }

  protected void execute() {
    if (!validResults) {
      return;
    }

    switch (currentState) {
      case INITIAL_TURN:
        initialTurnCommand.execute();
        if (initialTurnCommand.isFinished()) {
          initialTurnCommand.end();
          if (!initialTurnCommand.succeeded) {
            logger.warn("Turn command failed!");
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
          currentState = State.ALIGNMENT_TURN;
          alignmentTurnCommand.initialize();
          logger.info("state=ALIGNMENT_TURN");
        }
        break;
      case ALIGNMENT_TURN:
        alignmentTurnCommand.execute();
        if (alignmentTurnCommand.isFinished()) {
          alignmentTurnCommand.end();
          currentState = State.WAIT_FOR_VISION;
          logger.info("state=WAIT_FOR_VISION");
          nanosAtWaitForVisionStart = System.nanoTime();
        }
        break;
      case WAIT_FOR_VISION:
        if (System.nanoTime() - nanosAtWaitForVisionStart >= WAIT_VISION_NANOS) {
          VisionData data = Robot.vision.getVisionData();
          double tx = data.getTvecX();
          double tz = data.getTvecZ();
          double ry = data.getRvecYaw();

          if (Double.isNaN(tx) || Double.isNaN(tz) || Double.isNaN(ry)) {
            return;
          }
          
          logger.debug(String.format("tx: %f, tz: %f, ry: %f", tx, tz, ry));
          
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
          currentState = State.FINAL_DRIVE;
          finalDriveCommand.initialize();
          logger.info("state=FINAL_DRIVE");
        }
        break;
      case FINAL_DRIVE:
        finalDriveCommand.execute();
        if (finalDriveCommand.isFinished()) {
          finalDriveCommand.end();
          done = true;
        }
        break;
      default:
        done = true;
        break;
    }
  }

  protected boolean isFinished() {
    if (!validResults) {
      return true;
    }
    if (Robot.operatorInterface.isCancelled()) {
      return true;
    }
    return done;
  }

  protected void end() {
    Robot.driveTrain.joystickDrive(0, 0);
  }

  protected void interrupted() {
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
