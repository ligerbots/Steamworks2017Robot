package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Arrays;
import java.util.List;
import org.ligerbots.steamworks.FieldMap.FieldSide;
import org.ligerbots.steamworks.Robot.AutoMode;
import org.ligerbots.steamworks.commands.AlignBoilerAndShootCommand;
import org.ligerbots.steamworks.commands.CameraFeedCommand;
import org.ligerbots.steamworks.commands.ClimberEngageRatchetCommand;
import org.ligerbots.steamworks.commands.CompressorCommand;
import org.ligerbots.steamworks.commands.DriveDistanceCommand;
import org.ligerbots.steamworks.commands.DrivePathCommand;
import org.ligerbots.steamworks.commands.DriveToGearCommand;
import org.ligerbots.steamworks.commands.DriveUltrasonicCommand;
import org.ligerbots.steamworks.commands.FeederBackOutCommand;
import org.ligerbots.steamworks.commands.GearCommand;
import org.ligerbots.steamworks.commands.HumanPlayerCommunicationCommand;
import org.ligerbots.steamworks.commands.HumanPlayerCommunicationCommand.RequestedFeed;
import org.ligerbots.steamworks.commands.IntakeCommand;
import org.ligerbots.steamworks.commands.ManualControlWithTriggerCommand;
import org.ligerbots.steamworks.commands.ManualControlWithTriggerCommand.ManualControlType;
import org.ligerbots.steamworks.commands.ShiftCommand;
import org.ligerbots.steamworks.commands.ShooterFeederCommand;
import org.ligerbots.steamworks.commands.TurnCommand;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.GearManipulator.Position;
import org.ligerbots.steamworks.subsystems.Pneumatics.CompressorState;
import org.ligerbots.steamworks.subsystems.Vision;
import org.ligerbots.steamworks.subsystems.Vision.StreamType;
import org.slf4j.LoggerFactory;

/**
 * This class is the glue that binds the controls on the physical operator interface to the commands
 * and command groups that allow control of the robot.
 */
public class OperatorInterface {
  public static final int AUTO_MODE_GEAR_SHOOT = 0;
  public static final int AUTO_MODE_HOPPER_SHOOT = 1;
  public static final int AUTO_MODE_NONE = 2;

  public XboxController xboxController;
  public Joystick farmController;

  SendableChooser<AutoMode> autoMode;
  SendableChooser<FieldSide> startingPosition;
  SendableChooser<FieldSide> gearLiftPosition;

  /**
   * This is where we set up the operator interface.
   */
  public OperatorInterface() {
    autoMode = new SendableChooser<>();
    populateSelect(autoMode, AutoMode.class);
    SmartDashboard.putData("Auto mode", autoMode);

    startingPosition = new SendableChooser<>();
    populateSelect(startingPosition, FieldSide.class);
    SmartDashboard.putData("Auto start position", startingPosition);

    gearLiftPosition = new SendableChooser<>();
    populateSelect(gearLiftPosition, FieldSide.class);
    SmartDashboard.putData("Auto gear lift position", gearLiftPosition);

    xboxController = new XboxController(0);
    farmController = new Joystick(1);
    
    final ShooterFeederCommand manualShootCommand = new ShooterFeederCommand(true);
    final AlignBoilerAndShootCommand autoShootCommand = new AlignBoilerAndShootCommand();
    final GearCommand manualGearCommand = new GearCommand();
    final DriveToGearCommand autoGearCommand = new DriveToGearCommand();

    // Xbox main controls
    JoystickButton xboxAButton = new JoystickButton(xboxController, 1);
    xboxAButton.whenPressed(autoShootCommand);

    JoystickButton xboxBButton = new JoystickButton(xboxController, 2);
    xboxBButton.whenPressed(manualGearCommand);

    JoystickButton xboxXButton = new JoystickButton(xboxController, 3);
    xboxXButton.whileHeld(manualShootCommand);

    JoystickButton xboxYButton = new JoystickButton(xboxController, 4);
    xboxYButton.whenPressed(autoGearCommand);

    JoystickButton xboxLeftBumper = new JoystickButton(xboxController, 5);
    xboxLeftBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.DOWN));

    JoystickButton xboxRightBumper = new JoystickButton(xboxController, 6);
    xboxRightBumper.whenPressed(new ShiftCommand(DriveTrain.ShiftType.UP));

    JoystickButton xboxMenuButton = new JoystickButton(xboxController, 7);
    xboxMenuButton.whenPressed(new IntakeCommand());

    JoystickButton xboxStartButton = new JoystickButton(xboxController, 8);
    xboxStartButton.whenPressed(new ClimberEngageRatchetCommand());

    if (isFarmControllerPresent()) {
      LoggerFactory.getLogger(OperatorInterface.class).info("Farm controller found!");
      
      // camera 1, 2, 3
      // intake on, off
      // gearmech closed, feed, open
      // drive to 3 inches for feeder
      // LEDs gear, fuel, none
      // duplicate manual shoot, auto shoot, manual gear, auto gear
      // manual RPM / feeder fiddling with 28 + stick
      
      // CAMERAS
      JoystickButton gearCam = new JoystickButton(farmController, 22);
      gearCam.whenPressed(new CameraFeedCommand(StreamType.GEAR_CAM));
      
      JoystickButton boilerCam = new JoystickButton(farmController, 23);
      boilerCam.whenPressed(new CameraFeedCommand(StreamType.BOILER_CAM));
      
      JoystickButton boilerFrontCam = new JoystickButton(farmController, 24);
      boilerFrontCam.whenPressed(new CameraFeedCommand(StreamType.BOILER_CAM_FRONT));
      
      // INTAKE
      JoystickButton intakeOn = new JoystickButton(farmController, 18);
      intakeOn.whenPressed(new IntakeCommand(true));
      
      JoystickButton intakeOff = new JoystickButton(farmController, 17);
      intakeOff.whenPressed(new IntakeCommand(false));
      
      // GEARMECH
      JoystickButton gearmechClosed = new JoystickButton(farmController, 1);
      gearmechClosed.whenPressed(new GearCommand(Position.CLOSED));
      
      JoystickButton gearmechReceive = new JoystickButton(farmController, 2);
      gearmechReceive.whenPressed(new GearCommand(Position.RECEIVE_GEAR));
      
      JoystickButton gearmechDeliver = new JoystickButton(farmController, 3);
      gearmechDeliver.whenPressed(new GearCommand(Position.DELIVER_GEAR));
      
      // drive to 3 inches
      JoystickButton driveToGearFeed = new JoystickButton(farmController, 6);
      driveToGearFeed.whenPressed(new DriveUltrasonicCommand(3.0));
      
      // LEDs
      JoystickButton ledsOff = new JoystickButton(farmController, 11);
      ledsOff.whenPressed(new HumanPlayerCommunicationCommand(RequestedFeed.NONE));
      
      JoystickButton ledsGear = new JoystickButton(farmController, 13);
      ledsGear.whenPressed(new HumanPlayerCommunicationCommand(RequestedFeed.GEAR));
      
      JoystickButton ledsFuel = new JoystickButton(farmController, 15);
      ledsFuel.whenPressed(new HumanPlayerCommunicationCommand(RequestedFeed.FUEL));
      
      // duplicated controls
      JoystickButton manualShoot = new JoystickButton(farmController, 4);
      manualShoot.whileHeld(manualShootCommand);
      
      JoystickButton autoShoot = new JoystickButton(farmController, 5);
      autoShoot.whenPressed(autoShootCommand);
      
      JoystickButton manualGear = new JoystickButton(farmController, 9);
      manualGear.whenPressed(manualGearCommand);
      
      JoystickButton autoGear = new JoystickButton(farmController, 10);
      autoGear.whenPressed(autoGearCommand);
    } else {
      LoggerFactory.getLogger(OperatorInterface.class).info("(No farm controller found)");
    }

    SmartDashboard.putData(new InstantCommand("ForceOverrideRatchetEngage") {
      @Override
      public void execute() {
        Robot.driveTrain.engageClimberRatchet();
      }
    });

    SmartDashboard.putData(new CompressorCommand(CompressorState.TOGGLE));
    SmartDashboard.putData(new TurnCommand(45));
    SmartDashboard.putData(new TurnCommand(90));
    SmartDashboard.putData(new TurnCommand(180));
    SmartDashboard.putData(new DriveDistanceCommand(12 * 5));
    SmartDashboard.putData(new DriveDistanceCommand(12 * 10));
    SmartDashboard.putData(new DriveToGearCommand());

    SmartDashboard.putData(new DriveUltrasonicCommand(RobotMap.GEAR_DELIVERY_DIST, true));

    SmartDashboard.putData(new ClimberEngageRatchetCommand());

    SmartDashboard.putData(new FeederBackOutCommand());

    SmartDashboard.putData(new CameraFeedCommand(Vision.StreamType.TOGGLE));

    SmartDashboard.putData(new AlignBoilerAndShootCommand());

    FieldPosition startPosition = FieldMap.getRed().startingPositions[0];
    List<FieldPosition> testCtrl =
        Arrays.asList(startPosition.add(-1, 0), startPosition, startPosition.add(60, 0),
            startPosition.add(60, -60), startPosition.add(120, -60), startPosition.add(121, -60));
    List<FieldPosition> testWaypoint = FieldMap.generateCatmullRomSpline(testCtrl);
    SmartDashboard.putData(new DrivePathCommand(testWaypoint));

    for (ManualControlType type : ManualControlWithTriggerCommand.ManualControlType.values()) {
      SmartDashboard.putData("ManualControl_" + type.toString(),
          new ManualControlWithTriggerCommand(type));
    }

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
        FieldMap.navigateStartToGearLift(getStartingPosition(), getGearLiftPosition());
      }
    });
  }

  private <T extends Enum<?>> void populateSelect(SendableChooser<T> chooser, Class<T> options) {
    boolean first = true;
    for (T value : options.getEnumConstants()) {
      if (first) {
        first = false;
        chooser.addDefault(value.toString(), value);
      } else {
        chooser.addObject(value.toString(), value);
      }
    }
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
        || xboxController.getStickButton(GenericHID.Hand.kLeft) || farmController.getRawButton(21);
  }

  public boolean isQuickTurn() {
    return xboxController.getStickButton(GenericHID.Hand.kRight);
  }

  public AutoMode getAutoMode() {
    return autoMode.getSelected();
  }

  public FieldSide getStartingPosition() {
    return startingPosition.getSelected();
  }

  public FieldSide getGearLiftPosition() {
    return gearLiftPosition.getSelected();
  }
  
  public boolean isFarmControllerOverrideButtonPressed() {
    return farmController.getRawButton(28);
  }

  /**
   * Detects whether we have a farm controller.
   * 
   * @return True if present. Currently, always true
   */
  public boolean isFarmControllerPresent() {
    return true;
    // we had problems with a startup check last year
    // return (farmController.getButtonCount() > 10);
  }

}
