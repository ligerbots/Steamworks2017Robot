package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Engages the climber ratchet.
 */
public class ClimberEngageRatchetCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(ClimberEngageRatchetCommand.class);

  public ClimberEngageRatchetCommand() {
    // don't use requires because this is a one-shot command
  }

  protected void initialize() {
    // prevent driver accidents from taking us out for the entire match
    double matchTimeRemaining = DriverStation.getInstance().getMatchTime();
    if (matchTimeRemaining > 30) {
      logger.warn("Bad time to engage ratchet!");
    } else {
      logger.info("Engaging ratchet");
      Robot.driveTrain.engageClimberRatchet();
    }
  }

  protected void execute() {}

  protected boolean isFinished() {
    return true;
  }

  protected void end() {}

  protected void interrupted() {}
}
