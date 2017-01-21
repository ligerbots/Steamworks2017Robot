package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;

/**
 *
 */
public class Shooter extends Subsystem {
    CANTalon shooter1;
    CANTalon shooter2;
    public Shooter() {
      shooter1 = new CANTalon(RobotMap.CT_ID_SHOOTER_1);
      shooter2 = new CANTalon(RobotMap.CT_ID_SHOOTER_2);
      
      shooter1.changeControlMode(CANTalon.TalonControlMode.Speed);
      shooter2.changeControlMode(CANTalon.TalonControlMode.Follower);
      shooter2.set(RobotMap.CT_ID_SHOOTER_1);
      
    }
    
    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    public void initDefaultCommand() {
      // Set the default command for a subsystem here.
    }
    public void setShooterRpm(double rpm) {
      shooter1.set(rpm);
    }
}
