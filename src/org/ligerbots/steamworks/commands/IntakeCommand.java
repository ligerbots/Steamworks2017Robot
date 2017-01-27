package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 * This command toggles the intake on and off.
 */
public class IntakeCommand extends Command {

  boolean setIntakeOn;

  public IntakeCommand(boolean setIntakeOn) {
    requires(Robot.intake);
    this.setIntakeOn = setIntakeOn;
  }

  protected void initialize() {}

  protected void execute() {
    Robot.intake.setIntakeOn(setIntakeOn);
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {}

  protected void interrupted() {}
}
