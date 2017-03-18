package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.CommandGroup;
import org.ligerbots.steamworks.RobotMap;
// import org.ligerbots.steamworks.commands.TankDriveCommand;

/**
 *
 */
public class SlideCommandGroup extends CommandGroup {

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

    // Add Commands here:
    // e.g. addSequential(new Command1());
    // addSequential(new Command2());
    // these will run in order.

    // To run multiple commands at the same time,
    // use addParallel()
    // e.g. addParallel(new Command1());
    // addSequential(new Command2());
    // Command1 and Command2 will run in parallel.

    // A command group will require all of the subsystems that each member
    // would require.
    // e.g. if Command1 requires chassis, and Command2 requires arm,
    // a CommandGroup containing them would require both the chassis and the
    // arm.
  }
}
