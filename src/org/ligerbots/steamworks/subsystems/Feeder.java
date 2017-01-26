package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import java.util.Arrays;
import org.ligerbots.steamworks.RobotMap;

/**
 * The feeder is the mechanism that delivers fuel consistently to the shooter from the hopper.
 */
public class Feeder extends Subsystem {
  CANTalon feederMaster;
  CANTalon feederSlave;

  /**
   * Creates the Feeder subsystem.
   */
  public Feeder() {
    feederMaster = new CANTalon(RobotMap.CT_ID_FEEDER_MASTER);
    feederMaster.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
    feederSlave = new CANTalon(RobotMap.CT_ID_FEEDER_SLAVE);
    feederSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    feederSlave.set(RobotMap.CT_ID_FEEDER_MASTER);

    // we want the feeder to stop ASAP to avoid shooting extra balls
    Arrays.asList(feederMaster, feederSlave)
        .forEach((CANTalon talon) -> talon.enableBrakeMode(true));
  }

  /**
   * Sets the feeder motors.
   * 
   * @param value A percentvbus value, 0.0 to 1.0
   */
  public void setFeeder(double value) {
    feederMaster.set(value);
  }

  public void initDefaultCommand() {}
}

