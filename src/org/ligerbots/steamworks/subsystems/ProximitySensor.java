package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Arrays;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subsystem for the ultrasonic sensor that will be used to align to precise distances from the
 * feeder station.
 */
public class ProximitySensor extends Subsystem implements SmartDashboardLogger {
  Ultrasonic pulseWidthUltrasonic;

  double[] buffer;
  double[] bufferCopy;
  int bufferIndex;
  
  double adjustedDistance;

  private static final Logger logger = LoggerFactory.getLogger(ProximitySensor.class);

  /**
   * Creates the ProximitySensor.
   */
  public ProximitySensor() {
    logger.trace("Initializing proximity sensor");

    pulseWidthUltrasonic = new Ultrasonic(RobotMap.ULTRASONIC_TRIGGER, RobotMap.ULTRASONIC_ECHO);
    pulseWidthUltrasonic.setAutomaticMode(true);

    buffer = new double[12];
    bufferCopy = new double[12];
    bufferIndex = 0;

    Thread averagingThread = new Thread(this::averagingThread);
    averagingThread.setName("Ultrasonic Averaging Thread");
    averagingThread.setDaemon(true);
    averagingThread.start();
  }

  private void averagingThread() {
    while (true) {
      double distance = pulseWidthUltrasonic.getRangeInches();
      buffer[bufferIndex] = distance;
      bufferIndex++;
      if (bufferIndex >= buffer.length) {
        bufferIndex = 0;
      }
      
      // sort the historical values and average the middle 50%
      // this should reasonably avoid random glitch values like we typically see from ping-echo
      // sensors
      System.arraycopy(buffer, 0, bufferCopy, 0, buffer.length);
      Arrays.sort(bufferCopy);

      double sum = 0;
      for (int i = bufferCopy.length / 4; i < bufferCopy.length * 3 / 4; i++) {
        sum += bufferCopy[i];
      }
      adjustedDistance = sum / (bufferCopy.length / 2);
      
      try {
        Thread.sleep(20);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Returns the distance detected by the ultrasonic sensor.
   * 
   * @return Distance in inches
   */
  public double getDistance() {
    return adjustedDistance;
  }

  public void initDefaultCommand() {}

  /**
   * Sends sensor data to the dashboard.
   */
  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Proximity_Sensor_Distance", adjustedDistance);
  }
}

