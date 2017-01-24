package org.ligerbots.steamworks.subsystems;

import org.ligerbots.steamworks.RobotMap;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class Feeder extends Subsystem {
    public static final int MAX_RPM = 1300; //I don't know what an actual reasonable max RPM is.
    // Put methods for controlling this subsystem
    // here. Call these from Commands.
	CANTalon feederMaster;
	CANTalon feederSlave;
	public Feeder() {
		feederMaster = new CANTalon(RobotMap.CT_ID_FEEDER_MASTER);
		feederMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
		feederSlave = new CANTalon(RobotMap.CT_ID_FEEDER_SLAVE);
		feederSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
		feederSlave.set(RobotMap.CT_ID_FEEDER_MASTER);
		
	}
	public void setFeederRpm(double rpm) {
	  if (rpm > MAX_RPM) {
	    rpm = 0.0;
	  }
	  if (rpm < 0.0) {
        rpm = 0.0;
      }
	  feederMaster.set(rpm);
	}
    public void initDefaultCommand() {
        // No default command
    }
    
}

