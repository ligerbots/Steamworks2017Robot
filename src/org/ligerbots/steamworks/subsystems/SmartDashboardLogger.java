package org.ligerbots.steamworks.subsystems;

/**
 * An interface that subsystems implement so Robot can easily send all data to smartdashboard.
 * 
 * @author Erik Uhlmann
 *
 */
public interface SmartDashboardLogger {
  public void sendDataToSmartDashboard();
}
