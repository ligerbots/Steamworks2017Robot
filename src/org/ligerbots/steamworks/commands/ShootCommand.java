package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;

/**
 *
 */
public class ShootCommand extends Command {

    private static final double SHOOTING_SPEED = 0.1;

    public ShootCommand() {
        // Use requires() here to declare subsystem dependencies
        requires(Robot.shooter);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
      Robot.shooter.setShooterRpm(0.0);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
      if (Robot.operatorInterface.shouldShoot()) {
        Robot.shooter.setShooterRpm(SHOOTING_SPEED);
        SmartDashboard.putNumber("Shooter speed (RPM)", SHOOTING_SPEED);
      }
      else {
        Robot.shooter.setShooterRpm(0.0);
        SmartDashboard.putNumber("Shooter speed (RPM)", 0.0);
      }
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
