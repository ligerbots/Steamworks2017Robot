package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.SecondGearManipulator;

/**
 *
 */
public class SecondGearCommand extends Command {

  
  
    public SecondGearCommand() {
      requires(Robot.secondGearManipulator);
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
      Robot.secondGearManipulator
      .setPosition(Robot.secondGearManipulator.getPosition() == SecondGearManipulator.Position.CLOSED
          ? SecondGearManipulator.Position.DELIVER_GEAR : SecondGearManipulator.Position.CLOSED);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return true;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
