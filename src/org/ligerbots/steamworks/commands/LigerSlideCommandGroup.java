package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.CommandGroup;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class LigerSlideCommandGroup extends CommandGroup {
  float xDisp = 0;
  public LigerSlideCommandGroup(boolean right) {
    if (right) {
        addSequential(new TurnCommand(-45, 2));
    } else {
        addSequential(new TurnCommand(45, 2));
    }
    addParallel(new RecordDistanceCommand());
    addSequential(new DriveForeverCommand(10000000, false));
    
    if (right) {
      addSequential(new TurnCommand(45, 2));
    } 
    else {
      addSequential(new TurnCommand(-45, 2));
    }
    xDisp = Robot.driveTrain.getXDisplacement();
    addSequential(new DriveForeverCommand(xDisp/Math.sqrt(2), true));
 
  }
  
}


