package org.ligerbots.steamworks.triggers;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.Button;

/**
 * Like a JoystickButton, but it detects the POV hat on an xbox controller.
 */
public class JoystickPov extends Button {
  public enum Direction {
    NORTH(0),
    NORTHEAST(45),
    EAST(90),
    SOUTHEAST(135),
    SOUTH(180),
    SOUTHWEST(225),
    WEST(270),
    NORTHWEST(315);
    
    int angle;

    Direction(int angle) {
      this.angle = angle;
    }
  }

  XboxController controller;
  Direction triggerDirection;

  public JoystickPov(XboxController controller, Direction triggerDirection) {
    this.controller = controller;
    this.triggerDirection = triggerDirection;
  }

  public boolean get() {
    return controller.getPOV() == triggerDirection.angle;
  }
}
