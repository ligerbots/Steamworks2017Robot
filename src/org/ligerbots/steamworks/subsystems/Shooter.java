package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;

/**
 * This subsystem controls the shooter.
 */
public class Shooter extends Subsystem implements SmartDashboardLogger {
  CANTalon shooterMaster;
  CANTalon shooterSlave;

  /**
   * Create the instance of Shooter.
   */
  public Shooter() {
    shooterMaster = new CANTalon(RobotMap.CT_ID_SHOOTER_MASTER);
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
    LiveWindow.addActuator("Shooter", "Talon", shooterMaster);

    shooterSlave = new CANTalon(RobotMap.CT_ID_SHOOTER_SLAVE);
    shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    shooterSlave.enableBrakeMode(false);
    shooterSlave.set(RobotMap.CT_ID_SHOOTER_MASTER);

    Thread shooterWatchdog = new Thread(this::shooterWatchdogThread);
    // allow JVM to exit
    shooterWatchdog.setDaemon(true);
    // in the debugger, we'd like to know what this is
    shooterWatchdog.setName("Shooter Watchdog Thread");
    shooterWatchdog.start();

  }

  /**
   * Sets the rpm of the shooter and changes the talon to speed mode. If set to zero, it changes the
   * talon to percentvbus mode and allows it to spin down.
   * 
   * @param rpm The desired rpm.
   */
  public void setShooterRpm(double rpm) {
    // seriously not sure why this is necessary. Issue #6
    shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    shooterSlave.set(RobotMap.CT_ID_SHOOTER_MASTER);
    shooterSlave.enableControl();
    
    if (rpm == 0) {
      shooterMaster.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
      shooterMaster.set(0);
    } else {
      shooterMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
      shooterMaster.set(rpm);
    }
  }

  /**
   * Sets the shooter using percentvbus control. Useful for manual joystick control during testing.
   * @param percentVbus The percentvbus value, 0.0 to 1.0
   */
  public void setShooterPercentVBus(double percentVbus) {
    shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
    shooterSlave.set(RobotMap.CT_ID_SHOOTER_MASTER);
    shooterSlave.enableControl();
    
    shooterMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
    shooterMaster.set(percentVbus);
  }

  public double getShooterRpm() {
    return shooterMaster.get();
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
    while (true) {
      if (shooterMaster.getOutputCurrent() > RobotMap.SAFE_CURRENT_775PRO
          || shooterSlave.getOutputCurrent() > RobotMap.SAFE_CURRENT_775PRO) {
        System.err.println("Dangerous shooter current detected!");
        setShooterRpm(0);
        shooterMaster.disableControl();
        shooterSlave.disableControl();
        System.exit(-42);
      }

      try {
        Thread.sleep(20);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
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
  }
}
