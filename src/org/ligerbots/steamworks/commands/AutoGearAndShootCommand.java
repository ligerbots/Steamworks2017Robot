package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the autonomous gear delivery then fuel shooting mode.
 */
public class AutoGearAndShootCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(AutoGearAndShootCommand.class);

  enum State {
    GEAR_NAVIGATION, GEAR_DELIVERY, BOILER_NAVIGATION, BOILER_SHOOT, DONE
  }

  State currentState;
  FieldMap.Navigation gearNavigation;
  DriveToGearCommand gearCommand;

  /**
   * Creates a new AutoGearAndShootCommand.
   */
  public AutoGearAndShootCommand() {
    requires(Robot.driveTrain);
    requires(Robot.gearManipulator);
    requires(Robot.shooter);
    requires(Robot.feeder);
    
    gearCommand = new DriveToGearCommand();
  }

  protected void initialize() {
    logger.info("Initialize, state=GEAR_NAVIGATION");
    currentState = State.GEAR_NAVIGATION;

    gearNavigation = FieldMap.navigateStartToGearLift(1, 1);
    gearNavigation.commandIndex = 0;
    gearNavigation.commands.get(0).initialize();
  }

  protected void execute() {
    super.execute();

    switch (currentState) {
      case GEAR_NAVIGATION:
        AccessibleCommand currentCommand = gearNavigation.commands.get(gearNavigation.commandIndex);
        currentCommand.execute();
        if (currentCommand.isFinished()) {
          currentCommand.end();
          
          if (currentCommand.isFailedToComplete()) {
            // do something
          }
          
          gearNavigation.commandIndex++;
          logger.info(
              String.format("Executing gear navigation command: %d", gearNavigation.commandIndex));
          if (gearNavigation.commandIndex >= gearNavigation.commands.size()) {
            currentState = State.GEAR_DELIVERY;
            logger.info("state=GEAR_DELIVERY");
            
            gearCommand.initialize();
          }
        }
        break;
      case GEAR_DELIVERY:
        gearCommand.execute();
        if (gearCommand.isFinished()) {
          gearCommand.end();
          
          logger.info("state=BOILER_NAVIGATION");
        }
        break;
      default:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    }
  }

  protected boolean isFinished() {
    return currentState == State.DONE;
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
