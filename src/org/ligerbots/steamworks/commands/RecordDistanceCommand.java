package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 *
 */
public class RecordDistanceCommand extends Command {

    public RecordDistanceCommand() {        
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        Robot.driveTrain.resetDisplacement();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        Robot.driveTrain.addDisplacement(Robot.driveTrain.getNavXDisplacement());
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return Robot.operatorInterface.isCancelled();
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
