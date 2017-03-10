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
  Ultrasonic ultrasonicLeft;
  Ultrasonic ultrasonicRight;

  double[] bufferLeft;
  double[] bufferCopyLeft;
  int bufferIndexLeft;
  
  double adjustedDistanceLeft;
  double distanceLeft;
  
  double[] bufferRight;
  double[] bufferCopyRight;
  int bufferIndexRight;
  
  double adjustedDistanceRight;
  double distanceRight;

  private static final Logger logger = LoggerFactory.getLogger(ProximitySensor.class);

  /**
   * Creates the ProximitySensor.
   */
  public ProximitySensor() {
    logger.trace("Initializing proximity sensor");

    ultrasonicLeft =
        new Ultrasonic(RobotMap.ULTRASONIC_LEFT_TRIGGER, RobotMap.ULTRASONIC_LEFT_ECHO);
    
    ultrasonicRight =
        new Ultrasonic(RobotMap.ULTRASONIC_RIGHT_TRIGGER, RobotMap.ULTRASONIC_RIGHT_ECHO);
    ultrasonicRight.setAutomaticMode(true);

    bufferLeft = new double[12];
    bufferCopyLeft = new double[12];
    bufferIndexLeft = 0;
    
    bufferRight = new double[12];
    bufferCopyRight = new double[12];
    bufferIndexRight = 0;

    Thread averagingThread = new Thread(this::averagingThread);
    averagingThread.setName("Ultrasonic Averaging Thread");
    averagingThread.setDaemon(true);
    averagingThread.start();
  }

  private void averagingThread() {
    while (true) {
      distanceLeft = ultrasonicLeft.getRangeInches();
      bufferLeft[bufferIndexLeft] = distanceLeft;
      bufferIndexLeft++;
      if (bufferIndexLeft >= bufferLeft.length) {
        bufferIndexLeft = 0;
      }
      
      // sort the historical values and average the middle 50%
      // this should reasonably avoid random glitch values like we typically see from ping-echo
      // sensors
      System.arraycopy(bufferLeft, 0, bufferCopyLeft, 0, bufferLeft.length);
      Arrays.sort(bufferCopyLeft);

      double sum = 0;
      for (int i = bufferCopyLeft.length / 4; i < bufferCopyLeft.length * 3 / 4; i++) {
        sum += bufferCopyLeft[i];
      }
      adjustedDistanceLeft = sum / (bufferCopyLeft.length / 2);
      
      distanceRight = ultrasonicRight.getRangeInches();
      bufferRight[bufferIndexRight] = distanceRight;
      bufferIndexRight++;
      if (bufferIndexRight >= bufferRight.length) {
        bufferIndexRight = 0;
      }
      
      // sort the historical values and average the middle 50%
      // this should reasonably avoid random glitch values like we typically see from ping-echo
      // sensors
      System.arraycopy(bufferRight, 0, bufferCopyRight, 0, bufferRight.length);
      Arrays.sort(bufferCopyRight);

      sum = 0;
      for (int i = bufferCopyRight.length / 4; i < bufferCopyRight.length * 3 / 4; i++) {
        sum += bufferCopyRight[i];
      }
      adjustedDistanceRight = sum / (bufferCopyRight.length / 2);
      
      try {
        Thread.sleep(100);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Returns the distance detected by the left ultrasonic sensor.
   * 
   * @return Distance in inches
   */
  public double getDistanceLeft() {
    if (Math.abs(distanceLeft - adjustedDistanceLeft) < 15.0) {
      return distanceLeft;
    } else {
      return adjustedDistanceLeft;
    }
  }
  
  /**
   * Returns the distance detected by the right ultrasonic sensor.
   * 
   * @return Distance in inches
   */
  public double getDistanceRight() {
    if (Math.abs(distanceRight - adjustedDistanceRight) < 15.0) {
      return distanceRight;
    } else {
      return adjustedDistanceRight;
    }
  }

  public void initDefaultCommand() {}

  /**
   * Sends sensor data to the dashboard.
   */
  public void sendDataToSmartDashboard() {
    SmartDashboard.putNumber("Ultrasonic_Left", getDistanceLeft());
    SmartDashboard.putNumber("Ultrasonic_Right", getDistanceRight());
  }
}

