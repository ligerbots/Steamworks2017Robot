package org.ligerbots.steamworks.subsystems;

/**
 * An interface that subsystems implement so Robot can easily send all data to smartdashboard.
 * 
 * @author Erik Uhlmann
 *
 */
public interface SmartDashboardLogger {
  /**
   * Sends diagnostics to smartdashboard.
   */
  public void sendDataToSmartDashboard();
}
