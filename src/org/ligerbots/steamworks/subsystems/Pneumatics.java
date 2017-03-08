package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subsystem handles the compressor.
 */
public class Pneumatics extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Pneumatics.class);

  public enum CompressorState {
    ON, OFF, TOGGLE
  }

  Compressor compressor = null;

  /**
   * Creates a new Pneumatics subsystem.
   */
  public Pneumatics() {
    logger.info("Initialize");
    if (Robot.deviceFinder.isPcmAvailable(RobotMap.PCM_CAN_ID)) {
      compressor = new Compressor(RobotMap.PCM_CAN_ID);
      compressor.setClosedLoopControl(true);
    }
  }

  public void initDefaultCommand() {}

  /**
   * Turns the compressor on or off.
   * 
   * @param state Whether the compressor should be on, off, or toggled
   */
  public void setCompressorOn(CompressorState state) {
    if (compressor != null) {
      if (state == CompressorState.TOGGLE) {
        compressor.setClosedLoopControl(!compressor.getClosedLoopControl());
      } else if (state == CompressorState.ON) {
        compressor.setClosedLoopControl(true);
      } else if (state == CompressorState.OFF) {
        compressor.setClosedLoopControl(false);
      }

      logger.info(String.format("Setting compressor, request=%s, compressor on=%b",
          state.toString(), compressor.getClosedLoopControl()));
    }
  }

  public boolean isCompressorOn() {
    return compressor == null ? false : compressor.getClosedLoopControl();
  }

  @Override
  public void sendDataToSmartDashboard() {
    if (compressor != null) {
      SmartDashboard.putBoolean("Compressor_Closed_Loop", isCompressorOn());
      SmartDashboard.putBoolean("Compressor_Pressure_Switch", compressor.getPressureSwitchValue());
    }
  }
}
