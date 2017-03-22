package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.FieldMap.FieldSide;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.RobotPosition;
import org.ligerbots.steamworks.subsystems.GearManipulator;
import org.ligerbots.steamworks.subsystems.Vision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the autonomous gear delivery then fuel shooting mode.
 */
public class AutoGearAndShootCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(AutoGearAndShootCommand.class);

  // @formatter:off
  enum State {
    GEAR_NAVIGATION,
    GEAR_DELIVERY,
    BOILER_NAVIGATION,
    BOILER_ALIGN,
    BOILER_SHOOT,
    DONE,
    ABORTED
  }
  // @formatter:on

  State currentState;
  DriveToGearCommand gearCommand;
  TurnCommand boilerAlign;
  AlignBoilerAndShootCommand boilerCommand;

  DrivePathCommand driveToGear;
  DrivePathCommand driveToBoiler;
  
  boolean doGear;
  boolean doShoot;

  /**
   * Creates a new AutoGearAndShootCommand.
   */
  public AutoGearAndShootCommand(boolean doGear, boolean doShoot) {
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);
    requires(Robot.shooter);
    requires(Robot.feeder);

    gearCommand = new DriveToGearCommand();
    boilerCommand = new AlignBoilerAndShootCommand();
    
    this.doGear = doGear;
    this.doShoot = doShoot;
  }

  protected void initialize() {
    FieldSide startingPosition = Robot.operatorInterface.getStartingPosition();
    if (doGear) {
      FieldSide gearLiftPosition = Robot.operatorInterface.getGearLiftPosition();
      
      if (gearLiftPosition == FieldSide.CENTER) {
        logger.info("Initialize center, state=GEAR_DELIVERY");
        currentState = State.GEAR_DELIVERY;
        gearCommand.initialize();
      } else {
        logger.info("Initialize, state=GEAR_NAVIGATION");
        currentState = State.GEAR_NAVIGATION;
        driveToGear = FieldMap.navigateStartToGearLift(startingPosition, gearLiftPosition);
        driveToGear.initialize();
      }
    } else if (doShoot) {
      driveToBoiler = FieldMap.navigateStartToBoiler(startingPosition);
      driveToBoiler.initialize();
      logger.info("state=BOILER_NAVIGATION");
      currentState = State.BOILER_NAVIGATION;
    }
    
    Robot.vision.setLedRingOn(Vision.LedState.ON);
  }

  protected void execute() {
    super.execute();

    switch (currentState) {
      case GEAR_NAVIGATION:
        driveToGear.execute();
        if (driveToGear.isFinished()) {
          driveToGear.end();

          logger.info("state=GEAR_DELIVERY");
          currentState = State.GEAR_DELIVERY;
          gearCommand.initialize();
        }
        break;
      case GEAR_DELIVERY:
        gearCommand.execute();
        if (gearCommand.isFinished()) {
          gearCommand.end();

          if (doShoot) {
            if (Robot.operatorInterface.getGearLiftPosition() == FieldSide.FEEDER) {
              driveToBoiler =
                  FieldMap.navigateFeederSideLiftToBoiler(Robot.driveTrain.getRobotPosition());
              driveToBoiler.initialize();
              logger.info("state=BOILER_NAVIGATION");
              currentState = State.BOILER_NAVIGATION;
            } else {
              generateBoilerAlign();
              boilerAlign.initialize();
              logger.info("state=BOILER_ALIGN");
              currentState = State.BOILER_ALIGN;
            }
          } else {
            logger.info("State=DONE");
            currentState = State.DONE;
          }
        }
        break;
      case BOILER_NAVIGATION:
        driveToBoiler.execute();
        if (driveToBoiler.isFinished()) {
          driveToBoiler.end();

          generateBoilerAlign();
          boilerAlign.initialize();
          logger.info("state=BOILER_ALIGN");
          currentState = State.BOILER_ALIGN;
        }
        break;
      case BOILER_ALIGN:
        boilerAlign.execute();
        if (boilerAlign.isFinished()) {
          boilerAlign.end();

          if (boilerAlign.isFailedToComplete()) {
            logger.warn("Boiler align failed! Recalculating turn");
            generateBoilerAlign();
            boilerAlign.initialize();
          } else {
            logger.info("state=BOILER_SHOOT");
            currentState = State.BOILER_SHOOT;
            boilerCommand.initialize();
          }
        }
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
  
  private TurnCommand generateAlign(FieldPosition targetPosition) {
    RobotPosition pos = Robot.driveTrain.getRobotPosition();
    double angle = 90 - pos.angleTo(targetPosition);
    return new TurnCommand(angle - pos.getDirection(),  RobotMap.AUTO_TURN_ACCEPTABLE_ERROR);
  }
  
  private void generateBoilerAlign() {
    FieldMap map = FieldMap.getAllianceMap();
    boilerAlign = generateAlign(map.boiler);
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
    Robot.gearManipulator.setPosition(GearManipulator.Position.CLOSED);
  }

  protected void interrupted() {
    super.interrupted();
    logger.warn("Interrupted");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    Robot.shooter.setShooterRpm(0);
    Robot.feeder.setFeeder(0);
    Robot.gearManipulator.setPosition(GearManipulator.Position.CLOSED);
  }

  @Override
  protected String getState() {
    return currentState.toString();
  }
}
