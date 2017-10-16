package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that lets you use the joysticks to drive.
 */
public class DriveJoystickCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(DriveJoystickCommand.class);
  
  /**
   * Creates a DriveJoystickCommand.
   */
  public DriveJoystickCommand() {
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    logger.info("Initialize");
  }

  protected void execute() {
	  DriveType ActiveType = Robot.operatorInterface.getDriveType();
      if (ActiveType == DriveType.ARCADE_DRIVE){
    	  Robot.driveTrain.joystickDrive(
    			  Robot.operatorInterface.getThrottle(),
    			  Robot.operatorInterface.getTurn(), 
    			  Robot.operatorInterface.isQuickTurn());
      }else if (ActiveType == DriveType.TANK_DRIVE){
    	  XboxController XBX = Robot.operatorInterface.xboxController;
    	  Robot.driveTrain.rawTankDrive(XBX.getY(GenericHID.Hand.kLeft),XBX.getY(GenericHID.Hand.kRight));
      }
      
	  Robot.driveTrain.setClimberSpeed(Robot.operatorInterface.getTriggerValue());
  }

  protected boolean isFinished() {
    return false;
  }

  protected void end() {
    // never called
    logger.error("end() called, Scheduler probably screwed up");
  }

  protected void interrupted() {
    // not warn because we expect this to be interrupted by auto driving later
    logger.info("Interrupted");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }
}
