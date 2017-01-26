package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.Trigger;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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
  DigitalInput limitSwitch;
  AHRS navX;
  
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
    
    limitSwitch = new DigitalInput(RobotMap.LIMIT_SWITCH_DIO_PORT);
    
    navX = new AHRS(SPI.Port.kMXP);
  }

  /**
   * Sets the default command to give driver control.
   */
  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    setDefaultCommand(Robot.driveJoystickCommand);
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
  
  /**
   * Makes the robot drive until the limitSwitch is pressed.
   */
  public void climb() {
    shift(ShiftType.DOWN);
    joystickDrive(1,0); 
  }
  
  public boolean limitSwitch() {
    return limitSwitch.get();
  }
  
  public void dumpNavX() {
    SmartDashboard.putBoolean("IMU_Connected", navX.isConnected());
    SmartDashboard.putBoolean("IMU_IsCalibrating", navX.isCalibrating());
    SmartDashboard.putNumber("IMU_Yaw", navX.getYaw());
    SmartDashboard.putNumber("IMU_Pitch", navX.getPitch());
    SmartDashboard.putNumber("IMU_Roll", navX.getRoll());

    SmartDashboard.putNumber("IMU_CompassHeading",
            navX.getCompassHeading());

    SmartDashboard.putNumber("IMU_FusedHeading", navX.getFusedHeading());

    SmartDashboard.putNumber("IMU_TotalYaw", navX.getAngle());
    SmartDashboard.putNumber("IMU_YawRateDPS", navX.getRate());

    SmartDashboard.putNumber("IMU_Accel_X", navX.getWorldLinearAccelX());
    SmartDashboard.putNumber("IMU_Accel_Y", navX.getWorldLinearAccelY());
    SmartDashboard.putBoolean("IMU_IsMoving", navX.isMoving());
    SmartDashboard.putBoolean("IMU_IsRotating", navX.isRotating());

    SmartDashboard.putNumber("Velocity_X", navX.getVelocityX());
    SmartDashboard.putNumber("Velocity_Y", navX.getVelocityY());
    SmartDashboard.putNumber("Displacement_X", navX.getDisplacementX());
    SmartDashboard.putNumber("Displacement_Y", navX.getDisplacementY());

    SmartDashboard.putNumber("RawGyro_X", navX.getRawGyroX());
    SmartDashboard.putNumber("RawGyro_Y", navX.getRawGyroY());
    SmartDashboard.putNumber("RawGyro_Z", navX.getRawGyroZ());
    SmartDashboard.putNumber("RawAccel_X", navX.getRawAccelX());
    SmartDashboard.putNumber("RawAccel_Y", navX.getRawAccelY());
    SmartDashboard.putNumber("RawAccel_Z", navX.getRawAccelZ());
    SmartDashboard.putNumber("RawMag_X", navX.getRawMagX());
    SmartDashboard.putNumber("RawMag_Y", navX.getRawMagY());
    SmartDashboard.putNumber("RawMag_Z", navX.getRawMagZ());
    SmartDashboard.putNumber("IMU_Temp_C", navX.getTempC());

    AHRS.BoardYawAxis yawAxis = navX.getBoardYawAxis();
    SmartDashboard.putString("YawAxisDirection",
            yawAxis.up ? "Up" : "Down");
    SmartDashboard.putNumber("YawAxis", yawAxis.board_axis.getValue());

    SmartDashboard.putString("FirmwareVersion", navX.getFirmwareVersion());

    
    SmartDashboard.putNumber("QuaternionW", navX.getQuaternionW());
    SmartDashboard.putNumber("QuaternionX", navX.getQuaternionX());
    SmartDashboard.putNumber("QuaternionY", navX.getQuaternionY());
    SmartDashboard.putNumber("QuaternionZ", navX.getQuaternionZ());

    SmartDashboard.putNumber("IMU_Byte_Count", navX.getByteCount());
    SmartDashboard.putNumber("IMU_Update_Count", navX.getUpdateCount());
    
  }
}

