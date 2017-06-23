package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls LED lights on the robot.
 */
public class Lights extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Lights.class);
  
  public enum Type {
    SIGN_GEAR(0x01),
    SIGN_FUEL(0x00);
    
    byte code;
    Type(int code) {
      this.code = (byte) code;
    }
  }
  
  public enum Pulse {
    SOLID(0x00),
    SLOW(0x01),
    MEDIUM(0x02),
    FAST(0x03);
    
    byte code;
    Pulse(int code) {
      this.code = (byte) code;
    }
  }
  
  // we probably don't have java.awt.Color on the robot
  public static class Color {
    public static final Color OFF = new Color(0, 0, 0);
    
    byte red;
    byte green;
    byte blue;
    
    /**
     * Creates a new RGB color.
     * @param red The red component
     * @param green The green component
     * @param blue The blue component
     */
    public Color(int red, int green, int blue) {
      this.red = (byte) red;
      this.green = (byte) green;
      this.blue = (byte) blue;
    }
    
    public String toString() {
      return String.format("[%d, %d, %d]", red, green, blue);
    }
  }
  
  SerialPort teensyCommunication;
  
  /**
   * Creates a new Lights subsystem.
   */
  public Lights() {
//    teensyCommunication = new SerialPort(9600, SerialPort.Port.kMXP);
//    teensyCommunication.enableTermination();
//    
//    Thread serialThread = new Thread(this::serialThread);
//    serialThread.setName("LED UART Thread");
//    serialThread.setDaemon(true);
//    serialThread.start();
//    
//    setLedLight(Type.SIGN_FUEL, Pulse.SOLID, Color.OFF);
//    setLedLight(Type.SIGN_GEAR, Pulse.SOLID, Color.OFF);
  }
  
  private void serialThread() {
//    while (true) {
//      //logger.debug(teensyCommunication.readString());
//      teensyCommunication.readString();
//      try {
//        Thread.sleep(100);
//      } catch (InterruptedException ex) {
//        ex.printStackTrace();
//      }
//    }
  }
  
  /**
   * Sets a light strip configuration.
   * @param type Which light stripghhh
   * @param pulse How fast to pulse
   * @param color The color
   */
  public void setLedLight(Type type, Pulse pulse, Color color) {
//    logger.debug(String.format("Setting lights %s %s %s", type.toString(), pulse.toString(),
//        color.toString()));
//    byte[] command = new byte[] {type.code, pulse.code, color.red, color.green, color.blue};
//    int written = teensyCommunication.write(command, command.length);
//    if (written != command.length) {
//      logger.warn("Serial write failed!");
//    }
//    teensyCommunication.flush();
  }

  @Override
  public void initDefaultCommand() {
  }

  @Override
  public void sendDataToSmartDashboard() {
  }
}

