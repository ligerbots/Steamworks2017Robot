package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.Subsystem;
import java.util.Arrays;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;

/**
 * This subsystem handles driving (duh).
 */
public class DriveTrain extends Subsystem {

  /**
   * This is a list of all shift actions. Toggle is there because we will probably need to change
   * shifting to a single controller button in the future (and since there are three types, it can't
   * simply be a boolean as before).
   */
  public static enum ShiftType {
    UP, DOWN, TOGGLE
  }

  CANTalon left1;
  CANTalon left2;
  CANTalon right1;
  CANTalon right2;
  RobotDrive robotDrive;
  DoubleSolenoid shiftingSolenoid;

  /**
   * Creates a new drive train instance.
   */
  public DriveTrain() {
    left1 = new CANTalon(RobotMap.CT_ID_LEFT_1);
    left2 = new CANTalon(RobotMap.CT_ID_LEFT_2);
    right1 = new CANTalon(RobotMap.CT_ID_RIGHT_1);
    right2 = new CANTalon(RobotMap.CT_ID_RIGHT_2);

    left1.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
    right1.changeControlMode(CANTalon.TalonControlMode.PercentVbus);

    left2.changeControlMode(CANTalon.TalonControlMode.Follower);
    left2.set(RobotMap.CT_ID_LEFT_1);

    right2.changeControlMode(CANTalon.TalonControlMode.Follower);
    right2.set(RobotMap.CT_ID_RIGHT_1);


    Arrays.asList(left1, left2, right1, right2)
        .forEach((CANTalon talon) -> talon.enableBrakeMode(true));

    robotDrive = new RobotDrive(left1, right1);

    shiftingSolenoid = new DoubleSolenoid(RobotMap.SOLENOID_SHIFT_UP, RobotMap.SOLENOID_SHIFT_DOWN);
  }

  /**
   * Sets the default command to give driver control.
   */
  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
//    setDefaultCommand(Robot.driveJoystickCommand);
  }

  /**
   * This method drives the robot using joystick values.
   * 
   * @param throttle is the vertical axis
   * @param turn is the horizontal axis
   */
  public void joystickDrive(double throttle, double turn) {
    robotDrive.arcadeDrive(throttle, turn);
  }

  /**
   * Shifts the gearboxes up or down.
   * 
   * @param shiftType whether to shift up or down
   */
  public void shift(ShiftType shiftType) {
    if (shiftType == ShiftType.TOGGLE) {
      if (shiftingSolenoid.get() == DoubleSolenoid.Value.kReverse) {
        shiftingSolenoid.set(DoubleSolenoid.Value.kForward);
      } else {
        shiftingSolenoid.set(DoubleSolenoid.Value.kReverse);
      }
    } else if (shiftType == ShiftType.UP) {
      shiftingSolenoid.set(DoubleSolenoid.Value.kForward);
    } else {
      shiftingSolenoid.set(DoubleSolenoid.Value.kReverse);
    }
  }
}

