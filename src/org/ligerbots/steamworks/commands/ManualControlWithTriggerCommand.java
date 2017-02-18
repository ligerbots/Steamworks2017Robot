package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operates motors on manual control with the trigger buttons.
 */
public class ManualControlWithTriggerCommand extends Command {
  private static final Logger logger =
      LoggerFactory.getLogger(ManualControlWithTriggerCommand.class);
  
  public enum ManualControlType {
    DRIVE_THROTTLE,
    DRIVE_TURN,
    SHOOTER_RPM,
    INTAKE,
    FEEDER,
    GEAR_SERVO
  }
  
  ManualControlType type;
  
  public ManualControlWithTriggerCommand(ManualControlType type) {
    this.type = type;
  }

  protected void initialize() {
    logger.info(String.format("Init, type=%s", type));
    
    switch (type) {
      case DRIVE_THROTTLE:
        requires(Robot.driveTrain);
        break;
      case DRIVE_TURN:
        requires(Robot.driveTrain);
        break;
      case SHOOTER_RPM:
        requires(Robot.shooter);
        break;
      case INTAKE:
        requires(Robot.intake);
        break;
      case FEEDER:
        requires(Robot.feeder);
        break;
      case GEAR_SERVO:
        requires(Robot.gearManipulator);
        break;
      default:
        break;
    }
  }

  protected void execute() {
    double value = Robot.operatorInterface.xboxController.getTriggerAxis(Hand.kRight)
        - Robot.operatorInterface.xboxController.getTriggerAxis(Hand.kLeft);
    SmartDashboard.putNumber("ManualControl_value", value);
    switch (type) {
      case DRIVE_THROTTLE:
        Robot.driveTrain.rawThrottleTurnDrive(value, 0);
        break;
      case DRIVE_TURN:
        Robot.driveTrain.rawThrottleTurnDrive(0, value);
        break;
      case SHOOTER_RPM:
        Robot.shooter.setShooterRpm(value * RobotMap.SHOOTER_MAX_RPM);
        break;
      case INTAKE:
        Robot.intake.setIntakeRaw(value);
        break;
      case FEEDER:
        Robot.feeder.setFeeder(value);
        break;
      case GEAR_SERVO:
        Robot.gearManipulator.setServoRaw(Math.abs(value));
        break;
      default:
        break;
    }
  }

  protected boolean isFinished() {
    return false;
  }

  protected void end() {
    switch (type) {
      case DRIVE_THROTTLE:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        break;
      case DRIVE_TURN:
        Robot.driveTrain.rawThrottleTurnDrive(0, 0);
        break;
      case SHOOTER_RPM:
        Robot.shooter.setShooterRpm(0);
        break;
      case INTAKE:
        Robot.intake.setIntakeRaw(0);
        break;
      case FEEDER:
        Robot.feeder.setFeeder(0);
        break;
      case GEAR_SERVO:
        break;
      default:
        break;
    }
  }

  protected void interrupted() {
    end();
  }
}
