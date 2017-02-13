package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Logs command state to the dashboard.
 */
public abstract class StatefulCommand extends Command {
  protected abstract String getState();
  
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
