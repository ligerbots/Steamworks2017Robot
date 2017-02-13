package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Logs command state to the dashboard.
 */
public abstract class StatefulCommand extends Command {
  protected abstract String getState();
  
  public StatefulCommand() {
    super();
    SmartDashboard.putString(getName() + "_state", "init");
  }

  public StatefulCommand(double timeout) {
    super(timeout);
    SmartDashboard.putString(getName() + "_state", "init");
  }

  public StatefulCommand(String name, double timeout) {
    super(name, timeout);
    SmartDashboard.putString(getName() + "_state", "init");
  }

  public StatefulCommand(String name) {
    super(name);
    SmartDashboard.putString(getName() + "_state", "init");
  }

  protected void execute() {
    SmartDashboard.putString(getName() + "_state", getState());
  }
  
  protected void end() {
    SmartDashboard.putString(getName() + "_state", "ended");
  }
  
  protected void interrupted() {
    SmartDashboard.putString(getName() + "_state", "interrupted");
  }
}
