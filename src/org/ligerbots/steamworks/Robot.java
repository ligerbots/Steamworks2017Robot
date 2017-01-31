
package org.ligerbots.steamworks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.net.server.ServerSocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.ligerbots.steamworks.commands.DriveJoystickCommand;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.ligerbots.steamworks.subsystems.Feeder;
import org.ligerbots.steamworks.subsystems.GearManipulator;
import org.ligerbots.steamworks.subsystems.Intake;
import org.ligerbots.steamworks.subsystems.Pneumatics;
import org.ligerbots.steamworks.subsystems.Shooter;
import org.ligerbots.steamworks.subsystems.SmartDashboardLogger;
import org.ligerbots.steamworks.subsystems.Vision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the IterativeRobot documentation. If you change the name of this class
 * or the package after creating this project, you must also update the manifest file in the
 * resource directory.
 */
public class Robot extends IterativeRobot {
  static {
    // set up log server that the DS laptop can read and save
    ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.ALL);
    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
    
    LevelFilter levelFilter = new LevelFilter();
    levelFilter.setContext(ctx);
    levelFilter.setLevel(Level.DEBUG);
    levelFilter.setOnMatch(FilterReply.ACCEPT);
    levelFilter.setOnMismatch(FilterReply.DENY);
    levelFilter.start();
    
    Iterator<Appender<ILoggingEvent>> apps = root.iteratorForAppenders();
    while (apps.hasNext()) {
      apps.next().addFilter(levelFilter);
    }
    
    ServerSocketAppender socketAppender = new ServerSocketAppender();
    socketAppender.setContext(ctx);
    socketAppender.setPort(5801);
    socketAppender.start();
    root.addAppender(socketAppender);
  }

  public static DriveTrain driveTrain;
  public static Pneumatics pneumatics;
  public static Vision vision;
  public static Shooter shooter;
  public static Feeder feeder;
  public static Intake intake;
  public static GearManipulator gearManipulator;
  public static List<SmartDashboardLogger> allSubsystems;
  public static DriveJoystickCommand driveJoystickCommand;
  public static OperatorInterface operatorInterface;

  private static final Logger logger = LoggerFactory.getLogger(Robot.class);

  long prevNanos = System.nanoTime();

  Command autonomousCommand;
  SendableChooser<Command> chooser;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    logger.info("robotInit()");

    driveTrain = new DriveTrain();
    vision = new Vision();
    shooter = new Shooter();
    feeder = new Feeder();
    gearManipulator = new GearManipulator();
    intake = new Intake();
    pneumatics = new Pneumatics();
    allSubsystems =
        Arrays.asList(driveTrain, vision, shooter, feeder, gearManipulator, intake, pneumatics);
    
    driveJoystickCommand = new DriveJoystickCommand();
    operatorInterface = new OperatorInterface();

    chooser = new SendableChooser<>();
    // chooser.addDefault("Default Auto", new ExampleCommand());
    // chooser.addObject("My Auto", new MyAutoCommand());
    SmartDashboard.putData("Auto mode", chooser);
  }

  @Override
  public void robotPeriodic() {
    logger.trace("robotPeriodic()");
    driveTrain.updatePosition();
    Scheduler.getInstance().run();
    allSubsystems.forEach((SmartDashboardLogger logger) -> logger.sendDataToSmartDashboard());
    long currentNanos = System.nanoTime();
    SmartDashboard.putNumber("cycleMillis", (currentNanos - prevNanos) / 1000.0);
    prevNanos = currentNanos;
  }

  /**
   * This function is called once each time the robot enters Disabled mode. You can use it to reset
   * any subsystem information you want to clear when the robot is disabled.
   */
  @Override
  public void disabledInit() {
    logger.trace("disabledInit()");
  }

  @Override
  public void disabledPeriodic() {
    logger.trace("disabledPeriodic()");
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString code to get the auto name from the text box below the Gyro You can add
   * additional auto modes by adding additional commands to the chooser code above (like the
   * commented example) or additional comparisons to the switch structure below with additional
   * strings & commands.
   */
  @Override
  public void autonomousInit() {
    logger.trace("autonomousInit()");
    autonomousCommand = chooser.getSelected();

    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector", "Default");
     * switch(autoSelected) { case "My Auto": autonomousCommand = new MyAutoCommand(); break; case
     * "Default Auto": default: autonomousCommand = new ExampleCommand(); break; }
     */

    // schedule the autonomous command (example)
    if (autonomousCommand != null) {
      autonomousCommand.start();
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    logger.trace("autonomousPeriodic()");
  }

  @Override
  public void teleopInit() {
    logger.trace("teleopInit()");
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }

    driveJoystickCommand.start();
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    logger.trace("teleopPeriodic()");
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    logger.trace("testPeriodic()");
    LiveWindow.run();
  }
}
