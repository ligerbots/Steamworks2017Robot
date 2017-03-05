package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.PWM.PeriodMultiplier;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subsystem opens or closes the gear manipulator.
 */
public class GearManipulator extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(GearManipulator.class);

  public enum Position {
    DELIVER_GEAR, RECEIVE_GEAR, CLOSED
  }

  Servo gearServo;
  Position position = Position.CLOSED;

  /**
   * Creates the GearManipulator, and sets servo to closed position.
   */
  public GearManipulator() {
    logger.info("Initialize");

    gearServo = new Servo(RobotMap.GEAR_SERVO_CHANNEL);
    gearServo.setBounds(2.4, 0, 0, 0, 0.8);
    gearServo.setPeriodMultiplier(PeriodMultiplier.k1X);
    
    setPosition(Position.CLOSED);
    
    Thread servoThread = new Thread(this::servoShutoffThread);
    servoThread.setName("Servo Shutoff Thread");
    servoThread.setDaemon(true);
    servoThread.start();
  }

  public void initDefaultCommand() {}

  /**
   * Sets the gear mechanism position.
   * 
   * @param position The position to set it to
   */
  public void setPosition(Position position) {
    this.position = position;

    double servoValue;

    if (position == Position.DELIVER_GEAR) {
      servoValue = RobotMap.GEARMECH_DELIVER;
    } else if (position == Position.RECEIVE_GEAR) {
      servoValue = RobotMap.GEARMECH_RECEIVE;
    } else if (position == Position.CLOSED) {
      servoValue = RobotMap.GEARMECH_CLOSED;
    } else {
      throw new IllegalArgumentException("position is not valid");
    }

    logger.info(String.format("pos=%s value=%f", position.toString(), servoValue));

    setServoRaw(servoValue);
  }
  
  private void servoShutoffThread() {
    long timeWhen1 = -1;
    while (true) {
      if (gearServo.get() == 1.0) {
        if (timeWhen1 == -1) {
          timeWhen1 = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - timeWhen1 > 2000) {
          gearServo.setDisabled();
        }
      } else {
        timeWhen1 = -1;
      }
      
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void setServoRaw(double value) {
    gearServo.set(value);
    //if (value == 1.0) {
    //  gearServo.setDisabled();
    //}
  }

  /**
   * @return Whether the gear mechanism is open or closed.
   */
  public Position getPosition() {
    return position;
  }

  public void sendDataToSmartDashboard() {
    SmartDashboard.putString("Gear_Mechanism_Position", position.toString());
  }
}
