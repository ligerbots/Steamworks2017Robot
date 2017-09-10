package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class BeltCommand extends Command {

  double speed;
    public BeltCommand(double speed) {
      this.speed = speed;
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
      Robot.feeder.setFeeder(speed);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
      
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return false;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
