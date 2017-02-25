package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import com.kauailabs.navx.AHRSProtocol.AHRSUpdateBase;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.ITable;
import java.util.Arrays;
import org.ligerbots.steamworks.FieldMap;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.RobotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subsystem handles driving (duh).
 */
public class DriveTrain extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(DriveTrain.class);

  /**
   * This is a list of all shift actions. UP and DOWN shift up and down, and TOGGLE toggles. This is
   * so that you can initialize a ShiftCommand with TOGGLE and it will always toggle.
   */
  public static enum ShiftType {
    UP, DOWN, TOGGLE
  }

  /**
   * This makes calls to getEncoderValue() readable.
   */
  public static enum DriveTrainSide {
    LEFT, RIGHT
  }

  CANTalon leftMaster;
  CANTalon leftSlave;
  CANTalon rightMaster;
  CANTalon rightSlave;
  RobotDrive robotDrive;
  DoubleSolenoid shiftingSolenoid;
  DigitalInput climbLimitSwitch;
  AHRS navX;

  double positionX;
  double positionY;
  double rotation;
  double absoluteDistanceTraveled;

  double prevEncoderLeft;
  double prevEncoderRight;
  DriverStation driverStation;
  double rotationOffset;
  double lastOutputLeft = 0;
  double lastOutputRight = 0;

  long navxUpdateNanos;
  ITable swFieldDisplay;
  boolean pcmPresent;
  
  boolean isHoldingPosition;

  /**
   * Creates a new drive train instance.
   */
  public DriveTrain() {
    logger.info("Initialize");

    swFieldDisplay = NetworkTable.getTable("SmartDashboard/SwField");

    isHoldingPosition = false;
    
    if (Robot.deviceFinder.isTalonAvailable(RobotMap.CT_ID_LEFT_1)) {
      leftMaster = new CANTalon(RobotMap.CT_ID_LEFT_1);
      configureMaster(leftMaster);
      leftMaster.reverseSensor(true);
      leftSlave = new CANTalon(RobotMap.CT_ID_LEFT_2);
      configureSlave(leftSlave, RobotMap.CT_ID_LEFT_1);
    } else {
      logger.warn("Left1 not present, switching master to left2");
      leftMaster = new CANTalon(RobotMap.CT_ID_LEFT_2);
      configureMaster(leftMaster);
      // in case it comes back?
      // also to avoid NPEs
      leftSlave = new CANTalon(RobotMap.CT_ID_LEFT_1);
      configureSlave(leftSlave, RobotMap.CT_ID_LEFT_2);
    }

    if (Robot.deviceFinder.isTalonAvailable(RobotMap.CT_ID_RIGHT_1)) {
      rightMaster = new CANTalon(RobotMap.CT_ID_RIGHT_1);
      configureMaster(rightMaster);
      rightSlave = new CANTalon(RobotMap.CT_ID_RIGHT_2);
      configureSlave(rightSlave, RobotMap.CT_ID_RIGHT_1);
    } else {
      logger.warn("Right1 not present, switching master to right2");
      rightMaster = new CANTalon(RobotMap.CT_ID_RIGHT_2);
      configureMaster(rightMaster);
      rightSlave = new CANTalon(RobotMap.CT_ID_RIGHT_1);
      configureSlave(rightSlave, RobotMap.CT_ID_RIGHT_2);
    }

    robotDrive = new RobotDrive(leftMaster, rightMaster) {
      public void setLeftRightMotorOutputs(double leftOutput, double rightOutput) {
        super.setLeftRightMotorOutputs(leftOutput, rightOutput);
        lastOutputLeft = leftOutput;
        lastOutputRight = rightOutput;
      }
    };
    robotDrive.setMaxOutput(12.0);

    shiftingSolenoid = new DoubleSolenoid(RobotMap.PCM_CAN_ID, RobotMap.SOLENOID_SHIFT_UP,
        RobotMap.SOLENOID_SHIFT_DOWN);
    pcmPresent = Robot.deviceFinder.isPcmAvailable(RobotMap.PCM_CAN_ID);

    climbLimitSwitch = new DigitalInput(RobotMap.LIMIT_SWITCH_CLIMB_COMPLETE);

    // restore X and Y in case of crash
    if (SmartDashboard.containsKey("Robot_x")) {
      positionX = SmartDashboard.getNumber("Robot_x", positionX);
    }
    if (SmartDashboard.containsKey("Robot_y")) {
      positionY = SmartDashboard.getNumber("Robot_y", positionY);
    }

    // new firmware supports 200hz
    navX = new AHRS(SPI.Port.kMXP, (byte) 200);
    navX.registerCallback(
        (long systemTimestamp, long sensorTimestamp, AHRSUpdateBase sensorData, Object context) -> {
          updatePosition(sensorData.yaw);
        }, new Object());

    calibrateYaw();
  }

  private void configureMaster(CANTalon talon) {
    logger.info("init master: " + talon.getDeviceID());
    talon.changeControlMode(CANTalon.TalonControlMode.Voltage);
    talon.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
    talon.configEncoderCodesPerRev(RobotMap.QUAD_ENCODER_TICKS_PER_REV);
    talon.configNominalOutputVoltage(+0.0f, -0.0f);
    talon.configPeakOutputVoltage(+12.0f, -12.0f);
    // PID constants. TODO: find PID constants
    talon.setProfile(0);
    talon.setF(0);
    talon.setP(0.5);
    talon.setI(0.0001);
    talon.setD(1.0);
    talon.setPosition(0);
    talon.enableBrakeMode(true);
  }

  private void configureSlave(CANTalon talon, int masterId) {
    logger.info("init slave: " + talon.getDeviceID() + " to master " + masterId);
    talon.changeControlMode(CANTalon.TalonControlMode.Follower);
    talon.set(masterId);
    talon.enableBrakeMode(true);
  }

  /**
   * Sets the default command to give driver control.
   */
  public void initDefaultCommand() {
    logger.info(String.format("initDefaultCommand called, right now Robot.driveJoystickCommand=%s",
        Robot.driveJoystickCommand.toString()));
    setDefaultCommand(Robot.driveJoystickCommand);
  }

  /**
   * This method drives the robot using joystick values. Squared inputs are enabled, and turning at
   * high speeds is compensated. Do not use this unless actually passing in joystick values. For
   * auto drive, use a raw* function. Flips direction for production robot.
   * 
   * @param throttle is the vertical axis
   * @param turn is the horizontal axis
   * @param quickTurn true to override constant-curvature turning behavior
   */
  public void joystickDrive(double throttle, double turn, boolean quickTurn) {
    if (isHoldingPosition) {
      return;
    }
    
    if (!RobotMap.IS_ROADKILL) {
      throttle = -throttle;
      turn = -turn;
    }
    logger.trace(String.format("Driving with throttle %f and turn %f quickTurn %b", throttle, turn,
        quickTurn));
    if (quickTurn || !RobotMap.JOYSTICK_DRIVE_COMPENSATION_ENABLED) {
      robotDrive.arcadeDrive(throttle, turn);
    } else {
      robotDrive.arcadeDrive(throttle,
          Math.abs(throttle) * turn * RobotMap.JOYSTICK_DRIVE_TURN_SENSITIVITY);
    }
  }

  /**
   * Arcade drive but without squared inputs or high speed turn compensation. Flips direction for
   * production robot
   * 
   * @param throttle is the vertical axis
   * @param turn turn is the horizontal axis
   */
  public void rawThrottleTurnDrive(double throttle, double turn) {
    if (isHoldingPosition) {
      return;
    }
    
    if (!RobotMap.IS_ROADKILL) {
      throttle = -throttle;
      turn = -turn;
    }
    logger.trace(
        String.format("Driving no squared inputs with throttle %f and turn %f", throttle, turn));
    robotDrive.arcadeDrive(throttle, turn, false);
  }

  /**
   * Sets raw left and right motor values. DOES NOT flip direction for production robot.
   * 
   * @param left The left value
   * @param right The right value
   */
  public void rawLeftRightDrive(double left, double right) {
    if (isHoldingPosition) {
      return;
    }
    
    robotDrive.setLeftRightMotorOutputs(left, right);
  }

  public void setBrakeOn(boolean brakeOn) {
    Arrays.asList(leftMaster, leftSlave, rightMaster, rightSlave)
        .forEach((CANTalon talon) -> talon.enableBrakeMode(brakeOn));
  }

  /**
   * Returns the last output value for the motors.
   * 
   * @param side Which side to get the last output value for
   * @return The last known output value for that side
   */
  public double getLastOutput(DriveTrainSide side) {
    if (side == DriveTrainSide.LEFT) {
      return lastOutputLeft;
    } else {
      return lastOutputRight;
    }
  }

  /**
   * Shifts the gearboxes up or down.
   * 
   * @param shiftType whether to shift up or down
   */
  public void shift(ShiftType shiftType) {
    logger.info(String.format("Shifting, type=%s, shifter state=%s", shiftType.toString(),
        shiftingSolenoid.get().toString()));
    if (pcmPresent) {
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

  /**
   * Enables or disables PID position holding for the climb.
   * @param enabled Whether to enable or disable PID position holding.
   */
  public void setHoldPositionEnabled(boolean enabled) {
    setBrakeOn(true);
    
    isHoldingPosition = enabled;
    
    if (!enabled) {
      leftMaster.changeControlMode(CANTalon.TalonControlMode.Voltage);
      rightMaster.changeControlMode(CANTalon.TalonControlMode.Voltage);
      leftMaster.reverseSensor(true);
      robotDrive.setSafetyEnabled(true);
      rawLeftRightDrive(0, 0);
    } else {
      robotDrive.setSafetyEnabled(false);
      
      final double currentEncoderLeft = leftMaster.getPosition();
      final double currentEncoderRight = rightMaster.getPosition();
      
      leftMaster.reverseSensor(false);
      
      leftMaster.changeControlMode(CANTalon.TalonControlMode.Position);
      rightMaster.changeControlMode(CANTalon.TalonControlMode.Position);
      leftMaster.set(-currentEncoderLeft);
      rightMaster.set(currentEncoderRight);
    }
  }
  
  /**
   * Checks whether the drivetrain is currently holding its position.
   * @return True if holding position
   */
  public boolean isHoldPositionEnabled() {
    return isHoldingPosition;
  }

  public boolean isClimbLimitSwitchPressed() {
    return climbLimitSwitch.get();
  }

  /**
   * Gets the encoder value for the specified side.
   * 
   * @param side The side, either LEFT or RIGHT
   * @return The encoder value, in inches
   */
  public double getEncoderDistance(DriveTrainSide side) {
    // getPosition() gives revolutions, since the talons are calibrated for the ticks per
    // revolution. Multiply by wheel circumference and gearing factor to get distance in inches.
    if (side == DriveTrainSide.LEFT) {
      return leftMaster.getPosition() * RobotMap.GEARING_FACTOR * RobotMap.WHEEL_CIRCUMFERENCE;
    } else {
      return rightMaster.getPosition() * RobotMap.GEARING_FACTOR * RobotMap.WHEEL_CIRCUMFERENCE;
    }
  }

  /**
   * Sends all navx data to the dashboard.
   */
  public void dumpNavxData() {
    SmartDashboard.putBoolean("IMU_Connected", navX.isConnected());
    SmartDashboard.putBoolean("IMU_IsCalibrating", navX.isCalibrating());
    SmartDashboard.putNumber("IMU_Yaw", navX.getYaw());
    SmartDashboard.putNumber("IMU_Pitch", navX.getPitch());
    SmartDashboard.putNumber("IMU_Roll", navX.getRoll());

    SmartDashboard.putNumber("IMU_CompassHeading", navX.getCompassHeading());

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
    SmartDashboard.putString("YawAxisDirection", yawAxis.up ? "Up" : "Down");
    SmartDashboard.putNumber("YawAxis", yawAxis.board_axis.getValue());

    SmartDashboard.putString("FirmwareVersion", navX.getFirmwareVersion());


    SmartDashboard.putNumber("QuaternionW", navX.getQuaternionW());
    SmartDashboard.putNumber("QuaternionX", navX.getQuaternionX());
    SmartDashboard.putNumber("QuaternionY", navX.getQuaternionY());
    SmartDashboard.putNumber("QuaternionZ", navX.getQuaternionZ());

    SmartDashboard.putNumber("IMU_Byte_Count", navX.getByteCount());
    SmartDashboard.putNumber("IMU_Update_Count", navX.getUpdateCount());
  }

  /**
   * Sends all navx and talon data to the dashboard.
   */
  public void sendDataToSmartDashboard() {
    dumpNavxData();

    // talon output power
    SmartDashboard.putNumber("Left_Master_Power",
        leftMaster.getOutputCurrent() * leftMaster.getOutputVoltage());
    if (!RobotMap.IS_ROADKILL) {
      SmartDashboard.putNumber("Left_Slave_Power",
          leftSlave.getOutputCurrent() * leftSlave.getOutputVoltage());
    }
    SmartDashboard.putNumber("Right_Master_Power",
        rightMaster.getOutputCurrent() * rightMaster.getOutputVoltage());
    if (!RobotMap.IS_ROADKILL) {
      SmartDashboard.putNumber("Right_Slave_Power",
          rightSlave.getOutputCurrent() * rightSlave.getOutputVoltage());
    }

    // talon fault diagnostics
    // we don't care about under voltage because it's already clear when brownouts happen
    SmartDashboard.putNumber("Left_Master_Failure", leftMaster.getFaultHardwareFailure());
    SmartDashboard.putNumber("Left_Master_OverTemp", leftMaster.getStickyFaultOverTemp());
    if (!RobotMap.IS_ROADKILL) {
      SmartDashboard.putNumber("Left_Slave_Failure", leftSlave.getFaultHardwareFailure());
      SmartDashboard.putNumber("Left_Slave_OverTemp", leftSlave.getStickyFaultOverTemp());
    }
    SmartDashboard.putNumber("Right_Master_Failure", rightMaster.getFaultHardwareFailure());
    SmartDashboard.putNumber("Right_Master_OverTemp", rightMaster.getStickyFaultOverTemp());
    if (!RobotMap.IS_ROADKILL) {
      SmartDashboard.putNumber("Right_Slave_Failure", rightSlave.getFaultHardwareFailure());
      SmartDashboard.putNumber("Right_Slave_OverTemp", rightSlave.getStickyFaultOverTemp());
    }

    SmartDashboard.putNumber("Encoder_Left", getEncoderDistance(DriveTrainSide.LEFT));
    SmartDashboard.putNumber("Encoder_Right", getEncoderDistance(DriveTrainSide.RIGHT));

    SmartDashboard.putBoolean("Climb_Switch", climbLimitSwitch.get());

    // solenoid diagnostics
    if (pcmPresent) {
      SmartDashboard.putString("PCM_Blacklist",
          Integer.toString(shiftingSolenoid.getPCMSolenoidBlackList(), 2));
      SmartDashboard.putBoolean("Shift_Voltage_Fault",
          shiftingSolenoid.getPCMSolenoidVoltageFault());
      SmartDashboard.putBoolean("Shift_Voltage_Sticky_Fault",
          shiftingSolenoid.getPCMSolenoidVoltageStickyFault());
    }

    // dead reckoning field display
    // tell the dashboard what this object is
    swFieldDisplay.putBoolean("_swfield", true); 
    // put in dead reckoning values
    swFieldDisplay.putNumber("x", positionX);
    swFieldDisplay.putNumber("y", positionY);
    swFieldDisplay.putNumber("direction", rotation);
  }

  public RobotPosition getRobotPosition() {
    return new RobotPosition(positionX, positionY, rotation);
  }

  public void setPosition(FieldPosition fieldPos) {
    positionX = fieldPos.getX();
    positionY = fieldPos.getY();
  }

  /**
   * Updates the dead reckoning for our current position.
   */
  public void updatePosition(double navXYaw) {
    rotation = fixDegrees(navXYaw + rotationOffset);

    double encoderLeft = getEncoderDistance(DriveTrainSide.LEFT);
    double encoderRight = getEncoderDistance(DriveTrainSide.RIGHT);

    double deltaEncoderLeft = encoderLeft - prevEncoderLeft;
    double deltaEncoderRight = encoderRight - prevEncoderRight;

    double deltaInches = (deltaEncoderLeft + deltaEncoderRight) / 2;

    absoluteDistanceTraveled += Math.abs(deltaInches);

    positionX = positionX + Math.cos(Math.toRadians(90 - rotation)) * deltaInches;
    positionY = positionY + Math.sin(Math.toRadians(90 - rotation)) * deltaInches;

    prevEncoderLeft = encoderLeft;
    prevEncoderRight = encoderRight;
  }

  public double getAbsoluteDistanceTraveled() {
    return absoluteDistanceTraveled;
  }

  public double getYaw() {
    return rotation;
  }

  /**
   * Sets initial yaw based on where our starting position is.
   */
  public void calibrateYaw() {
    if (DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Blue) {
      rotationOffset = -90.0;
    } else {
      rotationOffset = 90.0;
    }
  }

  /**
   * Zeroes the NavX.
   */
  public void zeroSensors() {
    navX.reset();
    navX.resetDisplacement();

    FieldPosition currentPosition =
        FieldMap.getAllianceMap().startingPositions[Robot.operatorInterface
            .getStartingPositionId()];
    setPosition(currentPosition);

    absoluteDistanceTraveled = 0;

    updatePosition(navX.getYaw());
  }

  public static double fixDegrees(double angle) {
    return ((angle % 360) + 360) % 360;
  }
}

