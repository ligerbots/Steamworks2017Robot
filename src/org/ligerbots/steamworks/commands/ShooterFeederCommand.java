package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command spins up the shooter, then starts feeding once the shooter is up to speed.
 */
public class ShooterFeederCommand extends StatefulCommand {
  private static final Logger logger = LoggerFactory.getLogger(ShooterFeederCommand.class);

  double desiredShooterRpm = 0.0;
  boolean readyToStartFeeder = false;

  boolean aborted;

  boolean withholdShooting;

  /**
   * Creates a new ShooterFeederCommand with a not-yet-known rpm.
   */
  public ShooterFeederCommand() {
    this(Double.NaN);
  }

  /**
   * Creates a new ShooterFeederCommand.
   * 
   * @param desiredShooterRpm The rpm we need the shooter at.
   */
  public ShooterFeederCommand(double desiredShooterRpm) {
    requires(Robot.feeder);
    requires(Robot.shooter);
    this.desiredShooterRpm = desiredShooterRpm;
  }

  /**
   * Even if we are currently at the required rpm, this method allows shooting to be withheld
   * anyway, eg if a boiler alignment is still in progress.
   * 
   * @param withholdShooting Whether to withhold shooting
   */
  public void setWithholdShooting(boolean withholdShooting) {
    this.withholdShooting = withholdShooting;
  }

  /**
   * Changes the rpm mid-command to adjust for the distance potentially changing, or distance not
   * known until command is already started.
   * 
   * @param desiredShooterRpm the rpm to set
   */
  public void setRpm(double desiredShooterRpm) {
    this.desiredShooterRpm = desiredShooterRpm;
  }
  
  /**
   * Determines whether shooting was aborted because of a shooter fault.
   * @return true if aborted
   */
  public boolean isAborted() {
    return aborted;
  }

  protected void initialize() {
    logger.info(String.format("Initialize, desired rpm=%f", desiredShooterRpm));
    Robot.feeder.setFeeder(0);
    readyToStartFeeder = false;
    aborted = false;
    withholdShooting = false;
  }

  protected void execute() {
    super.execute();
    
    if (!Double.isNaN(desiredShooterRpm)) {
      Robot.shooter.setShooterRpm(desiredShooterRpm);
      double currentShooterRpm = Robot.shooter.getShooterRpm();
      if (Math.abs((currentShooterRpm - desiredShooterRpm)
          / desiredShooterRpm) < RobotMap.SHOOTER_RPM_PERCENT_TOLERANCE) {
        readyToStartFeeder = true;
        logger.info("Shooter spun up, feeding");
      }
    }

    if (readyToStartFeeder && !withholdShooting) {
      Robot.feeder.setFeeder(1.0);
    }
  }

  protected boolean isFinished() {
    // we want to finish by a JoystickButton.isHeld calling cancel()
    // or a parent command unless there's a fault

    if (Robot.shooter.isShooterFault()) {
      logger.warn("Shooter fault, disabling");
      aborted = true;
      return true;
    }

    return false;
  }

  protected void end() {
    super.end();
    
    logger.info("finish");
    Robot.feeder.setFeeder(0);
    Robot.shooter.setShooterRpm(0);
  }

  protected void interrupted() {
    super.interrupted();
    
    logger.info("Interrupted, spinning down shooter");
    Robot.feeder.setFeeder(0);
    Robot.shooter.setShooterRpm(0);
  }

  @Override
  protected String getState() {
    if (Robot.shooter.isShooterFault()) {
      return "Shooter fault";
    } else if (!readyToStartFeeder) {
      return "Spinning up";
    } else if (withholdShooting) {
      return "Withhold shooting";
    } else {
      return "Shooting";
    }
  }
}
