package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Vision.StreamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Toggles the camera feed stream to switch between boiler and gear cameras.
 */
public class CameraFeedCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(CameraFeedCommand.class);
  
  StreamType streamType;
  
  /** 
   * Creates a new CameraFeedCommand.
   * @param streamType The {@link StreamType} to switch to
   */
  public CameraFeedCommand(StreamType streamType) {
    super("CameraFeedCommand_" + streamType.toString());
    this.streamType = streamType;
  }

  protected void initialize() {
    logger.info(String.format("Initialize, streamType=%s", streamType));
  }

  protected void execute() {
    Robot.vision.setStreamType(streamType);
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
