package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveTrainSide;

/**
 * Tank drives the left or right side a certain distance.
 */
public class TankDriveCommand extends Command {

  boolean right;
  double dist; // inches
  
  double startRight;
  double startLeft;

  /**
   * Creates a new TankDriveCommand.
   * @param dist the distance to move the side
   * @param right true for the right side, false for left
   */
  public TankDriveCommand(double dist, boolean right) {
    this.right = right;
    this.dist = dist;
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    startRight = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT);
    startLeft = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT);
  }

  protected void execute() {
    if (right) {
      Robot.driveTrain.rawTankDrive(dist >= 0 ? 0.4 : -0.4, 0.0);
    } else {
      Robot.driveTrain.rawTankDrive(0.0, dist >= 0 ? 0.4 : -0.4);
    }
  }

  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      return true;
    }
    
    if (right) {
      double distTraveled = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT) - startRight;
      return Math.abs(distTraveled) >= Math.abs(dist);
    } else {
      double distTraveled = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT) - startLeft;
      return Math.abs(distTraveled) >= Math.abs(dist);
    }
  }

  protected void end() {
    Robot.driveTrain.rawTankDrive(0, 0);
  }

  protected void interrupted() {
    Robot.driveTrain.rawTankDrive(0, 0);
  }
}
