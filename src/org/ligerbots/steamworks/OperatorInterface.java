package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Arrays;
import java.util.List;
import org.ligerbots.steamworks.commands.AlignBoilerAndShootCommand;
import org.ligerbots.steamworks.commands.CameraFeedCommand;
import org.ligerbots.steamworks.commands.ClimbCommand;
import org.ligerbots.steamworks.commands.CompressorCommand;
import org.ligerbots.steamworks.commands.DriveDistanceCommand;
import org.ligerbots.steamworks.commands.DrivePathCommand;
import org.ligerbots.steamworks.commands.DriveToGearCommand;
import org.ligerbots.steamworks.commands.DriveUltrasonicCommand;
import org.ligerbots.steamworks.commands.FeederBackOutCommand;
import org.ligerbots.steamworks.commands.GearCommand;
import org.ligerbots.steamworks.commands.IntakeCommand;
import org.ligerbots.steamworks.commands.LedRingCommand;
import org.ligerbots.steamworks.commands.ShiftCommand;
import org.ligerbots.steamworks.commands.ShooterFeederCommand;
import org.ligerbots.steamworks.commands.TurnCommand;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.Pneumatics.CompressorState;
import org.ligerbots.steamworks.subsystems.Vision;
import org.slf4j.LoggerFactory;

/**
 * This class is the glue that binds the controls on the physical operator interface to the commands
 * and command groups that allow control of the robot.
 */
public class OperatorInterface {
  public static final int AUTO_MODE_GEAR_SHOOT = 0;
  public static final int AUTO_MODE_HOPPER_SHOOT = 1;
  public static final int AUTO_MODE_NONE = 2;
  
  XboxController xboxController;
  
  SendableChooser<Integer> autoMode;
  SendableChooser<Integer> startingPosition;
  SendableChooser<Integer> gearLiftPosition;

  /**
   * This is where we set up the operator interface.
   */
  public OperatorInterface() {
    autoMode = new SendableChooser<>();
    autoMode.addDefault("Gear + Shoot", AUTO_MODE_GEAR_SHOOT);
    autoMode.addObject("Hopper + Shoot", AUTO_MODE_HOPPER_SHOOT);
    autoMode.addObject("NO AUTONOMOUS", AUTO_MODE_NONE);
    SmartDashboard.putData("Auto mode", autoMode);
    
    startingPosition = new SendableChooser<>();
    startingPosition.addDefault("Boiler side", FieldMap.FIELD_SIDE_BOILER);
    startingPosition.addObject("Center", FieldMap.FIELD_SIDE_CENTER);
    startingPosition.addObject("Feeder side", FieldMap.FIELD_SIDE_FEEDER);
    SmartDashboard.putData("Auto start position", startingPosition);
    
    gearLiftPosition = new SendableChooser<>();
    gearLiftPosition.addDefault("Boiler side", FieldMap.FIELD_SIDE_BOILER);
    gearLiftPosition.addObject("Center", FieldMap.FIELD_SIDE_CENTER);
    gearLiftPosition.addObject("Feeder side", FieldMap.FIELD_SIDE_FEEDER);
    SmartDashboard.putData("Auto gear lift position", gearLiftPosition);
    
    xboxController = new XboxController(0);

    JoystickButton xboxAButton = new JoystickButton(xboxController, 1);
    xboxAButton.whenPressed(new IntakeCommand());

    JoystickButton xboxBButton = new JoystickButton(xboxController, 2);
    xboxBButton.whenPressed(new ClimbCommand());

    JoystickButton xboxXButton = new JoystickButton(xboxController, 3);
    xboxXButton.whileHeld(new ShooterFeederCommand(true));

    JoystickButton xboxYButton = new JoystickButton(xboxController, 4);
    xboxYButton.whileHeld(new GearCommand(true, true));

    JoystickButton xboxLeftBumper = new JoystickButton(xboxController, 5);
    xboxLeftBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.TOGGLE));

    JoystickButton xboxRightBumper = new JoystickButton(xboxController, 6);
    xboxRightBumper.whenPressed(new DriveToGearCommand());

    JoystickButton xboxMenuButton = new JoystickButton(xboxController, 7);
    xboxMenuButton.whenPressed(new LedRingCommand(Vision.LedState.TOGGLE));

    JoystickButton xboxStartButton = new JoystickButton(xboxController, 8);
    xboxStartButton.whenPressed(new CompressorCommand(CompressorState.TOGGLE));

    SmartDashboard.putData(new TurnCommand(45));
    SmartDashboard.putData(new DriveDistanceCommand(12 * 5));
    SmartDashboard.putData(new DriveToGearCommand());
    
    SmartDashboard.putData(new DriveUltrasonicCommand(3.0));
    
    SmartDashboard.putData(new FeederBackOutCommand());
    
    SmartDashboard.putData(new CameraFeedCommand(Vision.StreamType.TOGGLE));
    
    SmartDashboard.putData(new AlignBoilerAndShootCommand());
    
    FieldPosition startPosition = FieldMap.getRed().startingPositions[0];
    List<FieldPosition> testCtrl =
        Arrays.asList(startPosition.add(-1, 0), startPosition, startPosition.add(60, 0),
            startPosition.add(60, -60), startPosition.add(120, -60), startPosition.add(121, -60));
    List<FieldPosition> testWaypoint = FieldMap.generateCatmullRomSpline(testCtrl);
    SmartDashboard.putData(new DrivePathCommand(testWaypoint));
    
    SmartDashboard.putData(new InstantCommand("ZeroSensors") {
      {
        setRunWhenDisabled(true);
      }
      
      @Override
      public void execute() {
        Robot.driveTrain.zeroSensors();
      }
    });
    
    SmartDashboard.putData(new InstantCommand("Auto Calculations") {
      {
        setRunWhenDisabled(true);
      }
      
      @Override
      public void execute() {
        LoggerFactory.getLogger(OperatorInterface.class).info("Auto calc test");
        FieldMap.navigateStartToGearLift(getStartingPositionId(), getGearLiftPositionId());
      }
    });
  }

  public double getThrottle() {
    return -xboxController.getY(GenericHID.Hand.kLeft);
  }

  public double getTurn() {
    return -xboxController.getX(GenericHID.Hand.kRight);
  }

  /**
   * Every command that implements automatic behavior should check this in isFinished(). This method
   * returns true if
   * <ul>
   * <li>The left (throttle) stick button is pressed, or</li>
   * <li>Throttle is applied beyond 0.5</li>
   * </ul>
   * 
   * @return True if the above conditions are met.
   */
  public boolean isCancelled() {
    // just to be safe. The controller could fall down or something.
    if (DriverStation.getInstance().isAutonomous()) {
      return false;
    }
    return getThrottle() > 0.5 || getThrottle() < -0.5
        || xboxController.getStickButton(GenericHID.Hand.kLeft);
  }
  
  public boolean isQuickTurn() {
    return xboxController.getStickButton(GenericHID.Hand.kRight);
  }
  
  public int getAutoMode() {
    return autoMode.getSelected();
  }
  
  public int getStartingPositionId() {
    return startingPosition.getSelected();
  }
  
  public int getGearLiftPositionId() {
    return gearLiftPosition.getSelected();
  }
}
