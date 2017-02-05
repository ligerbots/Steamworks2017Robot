package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subsystem controls the shooter.
 */
public class Shooter extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Shooter.class);

  int masterId;
  int slaveId;
  CANTalon shooterMaster;
  CANTalon shooterSlave;
  
  boolean shooterFault = false;

  /**
   * Create the instance of Shooter.
   */
  public Shooter() {
    logger.info("Initialize");

    // account for missing Talons
    if (Robot.deviceFinder.isTalonAvailable(RobotMap.CT_ID_SHOOTER_MASTER)) {
      masterId = RobotMap.CT_ID_SHOOTER_MASTER;
      slaveId = RobotMap.CT_ID_SHOOTER_SLAVE;
    } else {
      logger.warn("Master talon is missing");
      masterId = RobotMap.CT_ID_SHOOTER_SLAVE;
      // in case it comes back
      slaveId = RobotMap.CT_ID_SHOOTER_MASTER;
    }
    
    shooterMaster = new CANTalon(masterId);
    // basic setup
    shooterMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
    shooterMaster.enableBrakeMode(false); // probably bad for 775pros
    shooterMaster.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
    shooterMaster.reverseSensor(false);
    // the Talon needs peak and nominal output values
    shooterMaster.configNominalOutputVoltage(+0.0f, -0.0f);
    shooterMaster.configPeakOutputVoltage(+12.0f, -12.0f);
    // configure PID
    shooterMaster.setProfile(0);
    shooterMaster.setF(0);
    shooterMaster.setP(0.05);
    shooterMaster.setI(0.0003);
    shooterMaster.setD(0.1);
    // add to LiveWindow for easy testing
    LiveWindow.addActuator("Shooter", "Master", shooterMaster);

    shooterSlave = new CANTalon(slaveId);
    shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    shooterSlave.enableBrakeMode(false);
    shooterSlave.set(masterId);
    LiveWindow.addActuator("Shooter", "Slave", shooterSlave);

    Thread shooterWatchdog = new Thread(this::shooterWatchdogThread);
    // allow JVM to exit
    shooterWatchdog.setDaemon(true);
    // in the debugger, we'd like to know what this is
    shooterWatchdog.setName("Shooter Watchdog Thread");
    shooterWatchdog.start();
    
    SmartDashboard.putData(new InstantCommand("ForceDisableShooterFault") {
      @Override
      public void execute() {
        shooterFault = false;
      }
    });
  }
  
  public boolean isShooterFault() {
    return shooterFault;
  }
  
  /**
   * Sets the rpm of the shooter and changes the talon to speed mode. If set to zero, it changes the
   * talon to percentvbus mode and allows it to spin down.
   * 
   * @param rpm The desired rpm.
   */
  public void setShooterRpm(double rpm) {
    if (shooterFault) {
      return;
    }
    logger.trace(String.format("Setting rpm=%f", rpm));
    // seriously not sure why this is necessary. Issue #6
    shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    shooterSlave.set(masterId);
    shooterSlave.enableControl();

    if (rpm == 0) {
      // just spin down, don't try to speed control it to zero because it tends to unnecessarily
      // oscillate and waste power
      shooterMaster.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      shooterMaster.set(0);
    } else {
      shooterMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
      shooterMaster.set(rpm);
    }
  }

  /**
   * Sets the shooter using percentvbus control. Useful for manual joystick control during testing.
   * 
   * @param percentVbus The percentvbus value, 0.0 to 1.0
   */
  public void setShooterPercentVBus(double percentVbus) {
    if (shooterFault) {
      return;
    }
    logger.trace(String.format("Setting percentvbus=%f", percentVbus));
    shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    shooterSlave.set(masterId);
    shooterSlave.enableControl();

    shooterMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
    shooterMaster.set(percentVbus);
  }

  public double getShooterRpm() {
    return shooterMaster.getSpeed();
  }

  public void initDefaultCommand() {
    // No default command
  }

  /**
   * Constantly checks 775pro current and kills the shooter if it gets close to stall current.
   * 
   * <p>
   * No Charles, this is not a physical watch dog!
   * </p>
   */
  private void shooterWatchdogThread() {
    logger.info("Initialize watchdog");
    while (true) {
      if (shooterMaster.getOutputCurrent() > RobotMap.SAFE_CURRENT_775PRO
          || shooterSlave.getOutputCurrent() > RobotMap.SAFE_CURRENT_775PRO) {
        logger.error("Dangerous shooter current detected!");
        setShooterRpm(0);
        shooterMaster.disableControl();
        shooterSlave.disableControl();
        shooterFault = true;
      }

      try {
        Thread.sleep(20);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        logger.error("InterruptedException", ex);
      }
    }
  }

  /**
   * Sends shooter data to smart dashboard.
   */
  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Shooter_Master_Talon_Power",
        shooterMaster.getOutputCurrent() * shooterMaster.getOutputVoltage());
    SmartDashboard.putNumber("Shooter_Slave_Talon_Power",
        shooterSlave.getOutputCurrent() * shooterSlave.getOutputVoltage());
    SmartDashboard.putNumber("Shooter_RPM_Real", getShooterRpm());
    SmartDashboard.putNumber("Shooter_PID_error", shooterMaster.getClosedLoopError());
    
    SmartDashboard.putBoolean("Shooter_Fault", shooterFault);
    SmartDashboard.putNumber("Shooter_Master_Failure", shooterMaster.getFaultHardwareFailure());
    SmartDashboard.putNumber("Shooter_Master_OverTemp", shooterMaster.getStickyFaultOverTemp());
    SmartDashboard.putNumber("Shooter_Slave_Failure", shooterSlave.getFaultHardwareFailure());
    SmartDashboard.putNumber("Shooter_Slave_OverTemp", shooterSlave.getStickyFaultOverTemp());
  }
}
