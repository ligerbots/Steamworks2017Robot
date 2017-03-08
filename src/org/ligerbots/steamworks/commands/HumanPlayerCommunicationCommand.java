package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.Lights.Color;
import org.ligerbots.steamworks.subsystems.Lights.Pulse;
import org.ligerbots.steamworks.subsystems.Lights.Type;

/**
 * Communicates with the human player by turning on the LED lights.
 */
public class HumanPlayerCommunicationCommand extends Command {
  public enum RequestedFeed {
    GEAR,
    FUEL,
    NONE
  }
  
  RequestedFeed requestedFeed;
  
  public HumanPlayerCommunicationCommand(RequestedFeed requestedFeed) {
    requires(Robot.lights);
    this.requestedFeed = requestedFeed;
  }

  protected void initialize() {
    switch (requestedFeed) {
      case NONE:
        Robot.lights.setLedLight(Type.SIGN_FUEL, Pulse.SOLID, Color.OFF);
        Robot.lights.setLedLight(Type.SIGN_GEAR, Pulse.SOLID, Color.OFF);
        break;
      case GEAR:
        Robot.lights.setLedLight(Type.SIGN_FUEL, Pulse.SOLID, Color.OFF);
        Robot.lights.setLedLight(Type.SIGN_GEAR, Pulse.MEDIUM, new Color(208, 78, 29));
        break;
      case FUEL:
        Robot.lights.setLedLight(Type.SIGN_FUEL, Pulse.MEDIUM, new Color(0, 102, 179));
        Robot.lights.setLedLight(Type.SIGN_GEAR, Pulse.SOLID, Color.OFF);
        break;
      default:
        throw new IllegalStateException("Default in switch encountered");
    }
  }

  protected void execute() {}

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
  }

  protected void interrupted() {
  }
}
