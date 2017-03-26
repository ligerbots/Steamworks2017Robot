package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;


/**
 *
 */
public class TurnPIDCommand extends Command {
    	double offsetDegrees;
    	double acceptableError;
    
	public TurnPIDCommand(double offsetDegrees, double acceptableError) {
	      super("TurnCommand_" + offsetDegrees + "_" + acceptableError);
	      requires(Robot.driveTrain);
	      this.offsetDegrees = offsetDegrees;
	      this.acceptableError = acceptableError;
	}

	// Called just before this Command runs the first time
	@Override
	protected void initialize() {
		Robot.driveTrain.enableTurningControl(offsetDegrees, acceptableError);
	}

	// Called repeatedly when this Command is scheduled to run
	@Override
	protected void execute() {
		Robot.driveTrain.controlTurning();
	}

	// Make this return true when this Command no longer needs to run execute()
	@Override
	protected boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true
	@Override
	protected void end() {
		Robot.driveTrain.disableTurningControl();
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	@Override
	protected void interrupted() {
		end();
	}
}
