package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.DriveTrain;

/**
 * This command shifts the drive train using the specified {@link DriveTrain.ShiftType}
 */
public class ShiftCommand extends Command {
  DriveTrain.ShiftType shiftType;
  
  public ShiftCommand(DriveTrain.ShiftType shiftType) {
    this.shiftType = shiftType;
  }

  protected void initialize() {}

  protected void execute() {
    Robot.driveTrain.shift(shiftType);
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {}

  protected void interrupted() {}
}
