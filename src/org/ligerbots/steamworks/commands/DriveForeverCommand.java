package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 *
 */
public class DriveForeverCommand extends Command {

    boolean forward;
    double distance;
    public DriveForeverCommand(double distance, boolean forward) {
        this.distance = distance;
        this.forward = forward;
        requires(Robot.driveTrain);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        Robot.driveTrain.resetDisplacement();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
      Robot.driveTrain.rawThrottleTurnDrive(forward ? 1 : -1, 0);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return Math.abs(Robot.driveTrain.getXDisplacement()) >= distance || Robot.operatorInterface.isSlideCancelled();
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
