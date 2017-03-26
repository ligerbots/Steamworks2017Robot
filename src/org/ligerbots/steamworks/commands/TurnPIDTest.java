package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.RobotMap;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class TurnPIDTest extends Command {

    TurnPIDCommand turnCommand;
    
    public TurnPIDTest() {
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
	turnCommand = new TurnPIDCommand(1, 0.3); // fill in actual angle from dashboard
	
    }

    // Called just before this Command runs the first time
    protected void initialize() {
	turnCommand.setParameters(RobotMap.TURN_TEST_ANGLE, RobotMap.TURN_TEST_ERROR);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
	turnCommand.execute();
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return turnCommand.isFinished();
    }

    // Called once after isFinished returns true
    protected void end() {
	turnCommand.end();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
	turnCommand.interrupted();
    }
}
