package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;

/**
 *
 */
public class Intake extends Subsystem implements SmartDashboardLogger {

  // Put methods for controlling this subsystem
  // here. Call these from Commands.

  boolean isOn;

  CANTalon masterIntake;
  CANTalon slaveIntake;

  public Intake() {
    masterIntake = new CANTalon(RobotMap.CT_ID_INTAKE_MASTER);
    slaveIntake = new CANTalon(RobotMap.CT_ID_INTAKE_SLAVE);

    masterIntake.changeControlMode(CANTalon.TalonControlMode.PercentVbus);

    slaveIntake.changeControlMode(CANTalon.TalonControlMode.Follower);
    slaveIntake.set(RobotMap.CT_ID_INTAKE_MASTER);

    intakeOff();
  }

  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }

  public void intakeOn() {
    masterIntake.set(0.7);
    isOn = true;
  }

  public void intakeOff() {
    masterIntake.set(0.0);
    isOn = false;
  }

  public boolean isOn() {
    return isOn;
  }

  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Intake_Master_Talon_Power",
        masterIntake.getOutputCurrent() * masterIntake.getOutputVoltage());
    SmartDashboard.putNumber("Intake_Slave_Talon_Power",
        slaveIntake.getOutputCurrent() * slaveIntake.getOutputVoltage());
    SmartDashboard.putBoolean("Intake_On", isOn);
  }
}

