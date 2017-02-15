package org.ligerbots.steamworks.commands;

/**
 * Waits for a specified amount of time.
 */
public class WaitCommand extends AccessibleCommand {
  long timeout;
  long startTime;
  
  public WaitCommand(long timeout) {
    this.timeout = timeout;
  }

  protected void initialize() {
    startTime = System.nanoTime();
  }

  protected void execute() {
  }

  protected boolean isFinished() {
    return (System.nanoTime() - startTime) >= timeout;
  }

  protected void end() {
  }

  protected void interrupted() {
  }
}
