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
    public final double fieldWidth = 654.0;//In inches
    public final double boilerWidth = 42.0;//In inches
    public final double boilerHeight = 97.0;//In inches
    public final double boilerHighVisionTargetTop = 88.0;//In inches
    public final double boilerHighVisionTargetBottom = 84.0;//In inches
    public final double boilerLowVisionTargetTop = 80.0;//In inches
    public final double boilerLowVisionTargetBottom = 78.0;//In inches
    
    public final double boilerCornerOffset = 30;//The approximate distance from the field corner to the boiler corner in inches
    public final double distanceToBaseline = 93.25;//Distance to baseline from alliance wall in inches
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
}

