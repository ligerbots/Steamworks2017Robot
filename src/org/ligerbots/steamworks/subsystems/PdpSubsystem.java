package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.Robot;

/**
 * Contains the PDP and sends diagnostics to the dashboard.
 */
public class PdpSubsystem extends Subsystem implements SmartDashboardLogger {
  PowerDistributionPanel pdp;

  /**
   * Creates the PdpSubsystem.
   */
  public PdpSubsystem() {
    if (Robot.deviceFinder.isPdpAvailable()) {
      pdp = new PowerDistributionPanel();
    }
  }

  public void initDefaultCommand() {}

  @Override
  public void sendDataToSmartDashboard() {
    if (pdp != null) {
      SmartDashboard.putNumber("Temperature", pdp.getTemperature());
      //SmartDashboard.putNumber("TotalPower", pdp.getTotalPower());

      // in the past, this has tended to screw up the CAN bus
      // for (int i = 0; i < 16; i++) {
      // SmartDashboard.putNumber("Current_" + i, pdp.getCurrent(i));
      // }
    }
  }
}

