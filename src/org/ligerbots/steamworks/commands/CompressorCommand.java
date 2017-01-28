package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Pneumatics.CompressorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CompressorCommand extends Command {

  CompressorState state;
  Logger logger = LoggerFactory.getLogger(CompressorCommand.class);
  public CompressorCommand(CompressorState state) {
    requires(Robot.pneumatics);
    this.state = state;
  }

  // Called just before this Command runs the first time
  protected void initialize() {}

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    logger.trace("Compressor being set to state " + state);
    Robot.pneumatics.setCompressor(state);
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    return true;
  }

  // Called once after isFinished returns true
  protected void end() {
    logger.trace("Compressor command finished");
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {

    logger.warn("Compressor command interrupted");
  }
}
