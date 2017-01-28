package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Pneumatics.CompressorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns the compressor on or off.
 */
public class CompressorCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(CompressorCommand.class);
  
  CompressorState state;
  
  /**
   * Creates a new CompressorCommand.s
   * @param state What to set the compressor to
   */
  public CompressorCommand(CompressorState state) {
    requires(Robot.pneumatics);
    this.state = state;
  }

  protected void initialize() {
    logger.info("Set %s", state.toString());
  }

  protected void execute() {
    Robot.pneumatics.setCompressorOn(state);
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
    logger.trace("Finished %s", state.toString());
  }

  protected void interrupted() {
    logger.warn("Interrupted %s", state.toString());
  }
}
