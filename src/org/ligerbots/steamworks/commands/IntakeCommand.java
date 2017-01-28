package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command toggles the intake on and off.
 */
public class IntakeCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(IntakeCommand.class);
  
  boolean setIntakeOn;

  public IntakeCommand(boolean setIntakeOn) {
    requires(Robot.intake);
    this.setIntakeOn = setIntakeOn;
  }

  protected void initialize() {
    logger.info("Initialize, set intake=%b", setIntakeOn);
  }

  protected void execute() {
    Robot.intake.setIntakeOn(setIntakeOn);
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
    logger.info("Finish");
  }

  protected void interrupted() {
    logger.warn("Interrupted");
  }
}
