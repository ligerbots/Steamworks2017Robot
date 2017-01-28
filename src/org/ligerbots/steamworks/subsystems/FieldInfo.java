package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class FieldInfo extends Subsystem {

    // Put methods for controlling this subsystem
    // here. Call these from Commands.
    public enum StartingPositions {
      //Which driver station we are starting from. The first driver station is toward the 
      POS_STATION_1,
      POS_STATION_2,
      POS_STATION_3
    }
    
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
}

