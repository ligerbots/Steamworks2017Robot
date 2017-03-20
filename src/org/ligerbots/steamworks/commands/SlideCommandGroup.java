package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.CommandGroup;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;

/**
 * "Slides" the robot over by tank driving one side, then the other, then driving forward.
 */
public class SlideCommandGroup extends CommandGroup {

  /**
   * Creates a new SlideCommandGroup.
   * @param inches the number of inches to move
   * @param right true for right, false for left
   */
  public SlideCommandGroup(double inches, boolean right) {
    double angle = Math.acos((RobotMap.ROBOT_WIDTH - inches) / RobotMap.ROBOT_WIDTH);
    double arc = angle * RobotMap.ROBOT_WIDTH;
    double distBack = Math.sin(angle) * RobotMap.ROBOT_WIDTH;
    if (right) {
      addSequential(new TankDriveCommand(arc, false));
      addSequential(new TankDriveCommand(arc, true));
      addSequential(new DriveDistanceCommand(distBack));
    } else {
      addSequential(new TankDriveCommand(arc, true));
      addSequential(new TankDriveCommand(arc, false));
      addSequential(new DriveDistanceCommand(distBack));
    }
  }
  
  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      return true;
    }
    
    return super.isFinished();
  }
}
