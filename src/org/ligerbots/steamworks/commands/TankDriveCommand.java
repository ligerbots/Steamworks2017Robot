package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveTrainSide;

/**
 *
 */
public class TankDriveCommand extends Command {

  boolean right;
  double dist; // inches

  public TankDriveCommand(double dist, boolean right) {
    this.right = right;
    this.dist = dist;
    requires(Robot.driveTrain);
  }

  // Called just before this Command runs the first time
  protected void initialize() {}

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    if (right) {
      Robot.driveTrain.rawTankDrive(dist >= 0 ? 0.4 : -0.4, 0.0);
    } else {
      Robot.driveTrain.rawTankDrive(0.0, dist >= 0 ? 0.4 : -0.4);
    }
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    double startRight = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT);
    double startLeft = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT);
    if (right) {
      double distTraveled = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT) - startRight;
      return distTraveled >= Math.abs(dist);
    } else {
      double distTraveled = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT) - startLeft;
      return distTraveled >= Math.abs(dist);
    }
  }

  // Called once after isFinished returns true
  protected void end() {}

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {}
}
