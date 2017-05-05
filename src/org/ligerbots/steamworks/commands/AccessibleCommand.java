package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

/**
 * Makes command functions package-accessible.
 */
public abstract class AccessibleCommand extends Command {
  public AccessibleCommand() {
    super();
  }

  public AccessibleCommand(double timeout) {
    super(timeout);
  }

  public AccessibleCommand(String name, double timeout) {
    super(name, timeout);
  }

  public AccessibleCommand(String name) {
    super(name);
  }

  protected void initialize() {
  }
  
  protected void execute() {
  }

  protected boolean isFinished() {
    return false;
  }
  
  protected void end() {
  }

  protected void interrupted() {
  }
  
  protected boolean isFailedToComplete() {
    return false;
  }
}
