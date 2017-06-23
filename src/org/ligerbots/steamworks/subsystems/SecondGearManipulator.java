package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SecondGearManipulator extends Subsystem implements SmartDashboardLogger{

    private static final Logger logger = LoggerFactory.getLogger(SecondGearManipulator.class);
    public enum Position {
      DELIVER_GEAR, CLOSED
    }
    
    Position position = Position.CLOSED;
    DoubleSolenoid piston;
    
    public SecondGearManipulator() {
      logger.info("Initialize");
      piston = new DoubleSolenoid(RobotMap.PCM_CAN_ID, RobotMap.GEAR_HOLDER_FORWARD2,
          RobotMap.GEAR_HOLDER_REVERSE2);
    }
    
    public void setPosition(Position position) {
      this.position = position;
      
      if (position == Position.DELIVER_GEAR) {
        setPiston(true);
      }
      else {
        setPiston(false);
      }
    }
    
    public void setPiston(boolean open) {
      piston.set(open ? DoubleSolenoid.Value.kForward : DoubleSolenoid.Value.kReverse);
    }
    
    public Position getPosition() {
      return position;
    }

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }

    @Override
    public void sendDataToSmartDashboard() {
      // TODO Auto-generated method stub
      
    }
    
}

