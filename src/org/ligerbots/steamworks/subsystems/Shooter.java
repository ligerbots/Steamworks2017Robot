package org.ligerbots.steamworks.subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;

/**
 * This subsystem controls the shooter.
 */
public class Shooter extends Subsystem {
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
    shooterMaster.configPeakOutputVoltage(+12.0f, 0.0f);
    // configure PID
    shooterMaster.setProfile(0);
    shooterMaster.setF(0.1097);
    shooterMaster.setP(0.22);
    shooterMaster.setI(0);
    shooterMaster.setD(0);
    // luckily, CANSpeedController does the heavy lifting of dashboard PID configuration for us
    SmartDashboard.putData("Shooter PID", shooterMaster);
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
  
  public void setShooterRpm(double rpm) {
    shooterMaster.set(rpm);
  }
  
  public double getShooterRpm() {
    return shooterMaster.get();
  }

  public void initDefaultCommand() {
    // No default command
  }

  /**
   * Constantly checks 775pro current and kills the shooter if it gets close to stall current.
   */
  private void shooterWatchdogThread() {
    while (true) {
      if (shooterMaster.getOutputCurrent() > RobotMap.SAFE_CURRENT_775PRO
          || shooterSlave.getOutputCurrent() > RobotMap.SAFE_CURRENT_775PRO) {
        System.err.println("Dangerous shooter current detected!");
        setShooterRpm(0);
        shooterMaster.disableControl();
        shooterSlave.disableControl();
      }

      try {
        Thread.sleep(20);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}
