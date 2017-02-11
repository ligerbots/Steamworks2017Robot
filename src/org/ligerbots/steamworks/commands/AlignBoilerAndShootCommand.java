package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Vision.VisionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command automatically gets vision data, begins to spin up shooter while aligning, and then
 * shoots when everything is ready.
 */
public class AlignBoilerAndShootCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(AlignBoilerAndShootCommand.class);

  enum State {
    WAIT_FOR_VISION, TURN, SHOOT, DONE, ABORTED
  }

  State state;
  TurnCommand turnCommand;
  ShooterFeederCommand shooterFeederCommand;

  /**
   * Creates a new AlignBoilerAndShootCommand.
   */
  public AlignBoilerAndShootCommand() {
    super("AlignBoilerAndShootCommand");
    requires(Robot.driveTrain);
    requires(Robot.shooter);
  }

  @Override
  protected void initialize() {
    logger.info("Initialize, state=WAIT_FOR_VISION");
    state = State.WAIT_FOR_VISION;

    shooterFeederCommand = new ShooterFeederCommand(0);
    shooterFeederCommand.initialize();
    shooterFeederCommand.setWithholdShooting(true);
  }

  @Override
  protected void execute() {
    shooterFeederCommand.execute();
    if (shooterFeederCommand.isFinished()) {
      logger.error(
          String.format("ShooterFeederCommand died! aborted=%b", shooterFeederCommand.isAborted()));
      state = State.DONE;
    }

    switch (state) {
      case WAIT_FOR_VISION:
        if (Robot.vision.isBoilerVisionDataValid()) {
          VisionData data = Robot.vision.getBoilerVisionData();
          double tx = data.getTvecX();
          double tz = data.getTvecZ();
          double angle = 90 - Math.toDegrees(Math.atan2(tz, tx));
          logger.info(String.format("Got data: tx=%f, tz=%f, angle=%f, state=TURN", tx, tz, angle));

          // calculate shooter rpm
          double calculatedRpm = 5000;
          logger.info(String.format("Shooter rpm: %f", calculatedRpm));
          shooterFeederCommand.setRpm(calculatedRpm);

          turnCommand = new TurnCommand(angle);
          turnCommand.initialize();
          state = State.TURN;
        }
        break;
      case TURN:
        turnCommand.execute();
        if (turnCommand.isFinished()) {
          turnCommand.end();
          
          if (!turnCommand.succeeded) {
            logger.warn("turn command failed, state=ABORTED");
            state = State.ABORTED;
          } else {
            shooterFeederCommand.setWithholdShooting(false);
            logger.info("state=SHOOT");
            state = State.SHOOT;
          }
        }
        break;
      default:
        break;
    }
  }

  @Override
  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      state = State.ABORTED;
      return true;
    }

    return state == State.DONE || state == State.ABORTED;
  }

  @Override
  protected void end() {
    logger.info("Finish");
    shooterFeederCommand.end();

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }

  @Override
  protected void interrupted() {
    logger.warn("Interrupted");
    shooterFeederCommand.interrupted();

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }
}
