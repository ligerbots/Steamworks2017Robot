
package org.ligerbots.steamworks.subsystems;

import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PWM.PeriodMultiplier;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This subsystem opens or closes the gear manipulator.
 */
public class GearManipulator extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(GearManipulator.class);

  public enum Position {
    DELIVER_GEAR, RECEIVE_GEAR, CLOSED
  }
  
  public enum GearOrientation {
    WEDGE_DOWN,
    SPOKE_DOWN,
    NO_GEAR
  }

  Servo gearServo;
  Position position = Position.CLOSED;
  
  AnalogInput lsSpokeDown;
  AnalogInput lsWedgeDown;
  
  AnalogInput pressurePlate;
  
  DoubleSolenoid gearHolder;

  /**
   * Creates the GearManipulator, and sets servo to closed position.
   */
  public GearManipulator() {
    logger.info("Initialize");

    gearServo = new Servo(RobotMap.GEAR_SERVO_CHANNEL);
    gearServo.setBounds(2.4, 0, 0, 0, 0.8);
    gearServo.setPeriodMultiplier(PeriodMultiplier.k4X);
    
    setPosition(Position.CLOSED);
    
    lsSpokeDown = new AnalogInput(RobotMap.AI_LS_SPOKE_DOWN);
    lsWedgeDown = new AnalogInput(RobotMap.AI_LS_WEDGE_DOWN);
    
    pressurePlate = new AnalogInput(RobotMap.AI_PRESSURE_PLATE);
    
    gearHolder = new DoubleSolenoid(RobotMap.PCM_CAN_ID, RobotMap.GEAR_HOLDER_FORWARD,
        RobotMap.GEAR_HOLDER_REVERSE);
  }

  public void initDefaultCommand() {}
  
  /**
   * Returns the detected orientation of the gear.
   * @return {@link GearOrientation} representing what the gear is at
   */
  public GearOrientation getGearOrientation() {
    double spokeDownV = lsSpokeDown.getAverageVoltage();
    double wedgeDownV = lsWedgeDown.getAverageVoltage();
    if (Math.abs(wedgeDownV - spokeDownV) < 0.2) {
      return GearOrientation.NO_GEAR;
    } else if (wedgeDownV < spokeDownV) {
      return GearOrientation.WEDGE_DOWN;
    } else {
      return GearOrientation.SPOKE_DOWN;
    }
  }
  
  /**
   * Sets the position of the pneumatic gear holder.
   * @param open Whether it should be open (release) or not
   */
  public void setGearHolder(boolean open) {
    gearHolder.set(open ? DoubleSolenoid.Value.kForward : DoubleSolenoid.Value.kReverse);
  }
  
  public boolean getGearHolder() {
    return gearHolder.get() == DoubleSolenoid.Value.kForward;
  }

  /**
   * Sets the gear mechanism position.
   * 
   * @param position The position to set it to
   */
  public void setPosition(Position position) {
    this.position = position;

    
    double servoValue;

    if (position == Position.DELIVER_GEAR) {
      setGearHolder(true);
      servoValue = RobotMap.GEARMECH_DELIVER;
    } else if (position == Position.RECEIVE_GEAR) {
      setGearHolder(true);
      servoValue = RobotMap.GEARMECH_RECEIVE;
    } else if (position == Position.CLOSED) {
      servoValue = RobotMap.GEARMECH_CLOSED;
    } else {
      throw new IllegalArgumentException("position is not valid");
    }

    logger.info(String.format("pos=%s value=%f", position.toString(), servoValue));

    setServoRaw(servoValue);
  }

  public void setServoRaw(double value) {
    gearServo.set(value);
  }

  /**
   * @return Whether the gear mechanism is open or closed.
   */
  public Position getPosition() {
    return position;
  }
  
  public boolean isPressurePlatePressed() {
    return pressurePlate.getAverageVoltage() < 4;
  }

  /**
   * Sends all diagnostics.
   */
  public void sendDataToSmartDashboard() {
    SmartDashboard.putString("Gear_Mechanism_Position", position.toString());
    
    SmartDashboard.putNumber("Light_Spoke_Down", lsSpokeDown.getAverageVoltage());
    SmartDashboard.putNumber("Light_Wedge_Down", lsWedgeDown.getAverageVoltage());
    SmartDashboard.putString("Gear_Orientation", getGearOrientation().toString());
    SmartDashboard.putBoolean("Pressure_Plate", isPressurePlatePressed());
  }
}
