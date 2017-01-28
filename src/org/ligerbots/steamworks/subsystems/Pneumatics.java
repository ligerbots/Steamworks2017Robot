package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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

  Compressor compressor;

  public Pneumatics() {
    logger.info("Initialize");
    compressor = new Compressor();
  }

  public void initDefaultCommand() {}

  /**
   * Turns the compressor on or off.
   * 
   * @param state Whether the compressor should be on, off, or toggled
   */
  public void setCompressorOn(CompressorState state) {
    if (state == CompressorState.TOGGLE) {
      compressor.setClosedLoopControl(!compressor.getClosedLoopControl());
    } else if (state == CompressorState.ON) {
      compressor.setClosedLoopControl(true);
    } else if (state == CompressorState.OFF) {
      compressor.setClosedLoopControl(false);
    }

    logger.info("Setting compressor, request=%s, compressor on=%b", state.toString(),
        compressor.getClosedLoopControl());
  }

  public boolean isCompressorOn() {
    return compressor.getClosedLoopControl();
  }

  @Override
  public void sendDataToSmartDashboard() {
    SmartDashboard.putBoolean("Compressor_Closed_Loop", isCompressorOn());
    SmartDashboard.putBoolean("Compressor_Pressure_Switch", compressor.getPressureSwitchValue());
  }
}
