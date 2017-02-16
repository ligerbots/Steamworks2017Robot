package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotPosition;
import org.ligerbots.steamworks.subsystems.Vision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the autonomous gear delivery then fuel shooting mode.
 */
public class AutoGearAndShootCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(AutoGearAndShootCommand.class);

  enum State {
    GEAR_NAVIGATION, GEAR_ALIGN, GEAR_DELIVERY, BOILER_NAVIGATION, BOILER_SHOOT, DONE, ABORTED
  }

  State currentState;
  DriveToGearCommand gearCommand;
  TurnCommand gearAlign;
  AlignBoilerAndShootCommand boilerCommand;
  
  DrivePathCommand driveToGear;

  /**
   * Creates a new AutoGearAndShootCommand.
   */
  public AutoGearAndShootCommand() {
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);
    requires(Robot.shooter);
    requires(Robot.feeder);

    gearCommand = new DriveToGearCommand();
    boilerCommand = new AlignBoilerAndShootCommand();
  }

  protected void initialize() {
    logger.info("Initialize, state=GEAR_NAVIGATION");
    currentState = State.GEAR_NAVIGATION;

    driveToGear =
        FieldMap.navigateStartToGearLift(Robot.operatorInterface.getGearLiftPositionId(),
            Robot.operatorInterface.getGearLiftPositionId());
    driveToGear.initialize();
    Robot.vision.setLedRingOn(Vision.LedState.ON);
  }

  protected void execute() {
    super.execute();

    switch (currentState) {
      case GEAR_NAVIGATION:
        driveToGear.execute();
        if (driveToGear.isFinished()) {
          driveToGear.end();

          currentState = State.GEAR_ALIGN;
          logger.info("state=GEAR_ALIGN");

          RobotPosition pos = Robot.driveTrain.getRobotPosition();
          FieldMap map = FieldMap.getAllianceMap();
          FieldPosition gear =
              map.gearLiftPositions[Robot.operatorInterface.getGearLiftPositionId()];
          double angle = 90 - pos.angleTo(gear);
          
          gearAlign = new TurnCommand(angle - pos.getDirection());
          gearAlign.initialize();
        }
        break;
      case GEAR_ALIGN:
        gearAlign.execute();
        if (gearAlign.isFinished()) {
          gearAlign.end();
          
          if (gearAlign.isFailedToComplete()) {
            logger.warn("Gear align failed! Recalculating turn");
            RobotPosition pos = Robot.driveTrain.getRobotPosition();
            FieldMap map = FieldMap.getAllianceMap();
            FieldPosition gear =
                map.gearLiftPositions[Robot.operatorInterface.getGearLiftPositionId()];
            double angle = 90 - pos.angleTo(gear);
            
            gearAlign = new TurnCommand(angle - pos.getDirection());
            gearAlign.initialize();
          } else {
            logger.info("state=GEAR_DELIVERY");
            currentState = State.GEAR_DELIVERY;
            gearCommand.initialize();
          }
        }
        break;
      case GEAR_DELIVERY:
        gearCommand.execute();
        if (gearCommand.isFinished()) {
          gearCommand.end();

          logger.info("state=BOILER_NAVIGATION");
          currentState = State.BOILER_NAVIGATION;
        }
        break;
      case BOILER_NAVIGATION:
        
        break;
      case BOILER_SHOOT:
        boilerCommand.execute();
        if (boilerCommand.isFinished()) {
          boilerCommand.end();
          
          logger.info("State=DONE");
          currentState = State.DONE;
        }
        break;
      default:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    }
  }

  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      currentState = State.ABORTED;
      logger.warn("Operator aborted");
      return true;
    }
    return currentState == State.DONE || currentState == State.ABORTED;
  }

  protected void end() {
    super.end();
    logger.info("Finish");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    Robot.shooter.setShooterRpm(0);
    Robot.feeder.setFeeder(0);
    Robot.gearManipulator.setOpen(false);
  }

  protected void interrupted() {
    super.interrupted();
    logger.warn("Interrupted");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    Robot.shooter.setShooterRpm(0);
    Robot.feeder.setFeeder(0);
    Robot.gearManipulator.setOpen(false);
  }

  @Override
  protected String getState() {
    return currentState.toString();
  }
}
