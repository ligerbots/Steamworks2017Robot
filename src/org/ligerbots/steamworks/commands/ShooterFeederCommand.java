package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class ShooterFeederCommand extends Command {
    double rpm = 0.0;
    public ShooterFeederCommand(double rpm) {
        requires(Robot.feeder);
        requires(Robot.shooter);
        this.rpm = rpm;
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        Robot.feeder.setFeederRpm(0.0);
        Robot.shooter.setShooterRpm(0.0);
        
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
      double shooterRpm = 
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
