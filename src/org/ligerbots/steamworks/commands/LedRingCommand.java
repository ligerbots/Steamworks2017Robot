package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Vision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command turns the LED ring on or off.
 */
public class LedRingCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(LedRingCommand.class);

  Vision.LedState desiredState;

  /**
   * Creates a new LedRingCommand.
   * @param desiredState The state for the led ring to be in
   */
  public LedRingCommand(Vision.LedState desiredState) {
    this.desiredState = desiredState;
    // vision isn't a physical subsystem and we don't need to lock on it. Plus this is a oneshot
    // command. So we don't use requires()
  }

  protected void initialize() {
    logger.info(String.format("Setting led ring to %s", desiredState.toString()));
  }

  protected void execute() {
    Robot.vision.setLedRingOn(desiredState);
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
