package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command spins up the shooter, then starts feeding once the shooter is up to speed.
 */
public class ShooterFeederCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(ShooterFeederCommand.class);
  
  static final double RPM_PERCENT_TOLERANCE = 0.05;

  double desiredShooterRpm = 0.0;
  boolean readyToStartFeeder = false;

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

  protected void initialize() {
    logger.info(String.format("Initialize, desired rpm=%f", desiredShooterRpm));
    Robot.feeder.setFeeder(0);
    Robot.shooter.setShooterRpm(desiredShooterRpm);
    readyToStartFeeder = false;
  }

  protected void execute() {
    double currentShooterRpm = Robot.shooter.getShooterRpm();
    if (Math.abs((currentShooterRpm - desiredShooterRpm)
        / desiredShooterRpm) < RPM_PERCENT_TOLERANCE) {
      readyToStartFeeder = true;
      logger.info("Shooter spun up, feeding");
    }

    if (readyToStartFeeder) {
      Robot.feeder.setFeeder(1.0);
    }
  }

  protected boolean isFinished() {
    // we want to finish by a JoystickButton.isHeld calling cancel()
    return false;
  }

  protected void end() {
    // shouldn't ever be called
    logger.error("end() called");
    Robot.feeder.setFeeder(0);
    Robot.shooter.setShooterRpm(0);
  }

  protected void interrupted() {
    logger.info("Interrupted, spinning down shooter");
    Robot.feeder.setFeeder(0);
    Robot.shooter.setShooterRpm(0);
  }
}
