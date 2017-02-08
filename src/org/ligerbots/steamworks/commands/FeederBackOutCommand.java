package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command backs out and turns away from the feeder.
 */
public class FeederBackOutCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(FeederBackOutCommand.class);

  double startAngle;
  double targetAngle;
  boolean clockwise;

  /**
   * Creates a new FeederBackOutCommand.
   */
  public FeederBackOutCommand() {
    super("FeederBackOutCommand");
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    logger.info("Initialize");

    startAngle = Robot.driveTrain.getYaw();
    
    if (DriverStation.getInstance().getAlliance() == Alliance.Red) {
      targetAngle = DriveTrain.fixDegrees(startAngle + 120);
      clockwise = true;
    } else {
      targetAngle = DriveTrain.fixDegrees(startAngle - 120);
      clockwise = false;
    }
  }

  protected void execute() {
    Robot.driveTrain.rawThrottleTurnDrive(-0.7, clockwise ? 0.3 : -0.3);
  }

  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      logger.warn("Aborted");
      return true;
    }
    
    double error1 = Math.abs(Robot.driveTrain.getYaw() - targetAngle) % 360.0;
    double error2 = 360 - error1;
    return error1 < 10 || error2 < 10;
  }

  protected void end() {
    logger.info("Finish");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }

  protected void interrupted() {
    logger.warn("End");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }
}
