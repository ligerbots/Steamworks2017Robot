
package org.ligerbots.steamworks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.net.server.ServerSocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;
import edu.wpi.first.wpilibj.DriverStation;
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
import org.ligerbots.steamworks.subsystems.PdpSubsystem;
import org.ligerbots.steamworks.subsystems.Pneumatics;
import org.ligerbots.steamworks.subsystems.ProximitySensor;
import org.ligerbots.steamworks.subsystems.Shooter;
import org.ligerbots.steamworks.subsystems.SmartDashboardLogger;
import org.ligerbots.steamworks.subsystems.Vision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main robot class.
 */
public class Robot extends IterativeRobot {
  static {
    // logging levels:
    // TRACE: constant 50hz spam (eg, drive outputs)
    // DEBUG: Frequent events: results of calculations, command progress, subsystem methods called
    // INFO: Infrequent events: command initialize/end, state changes
    // WARN/ERROR: self-explanatory

    // configure logging
    ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    // enable TRACE level
    root.setLevel(Level.ALL);
    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

    // disable TRACE level for console output
    LevelFilter levelFilter = new LevelFilter();
    levelFilter.setContext(ctx);
    levelFilter.setLevel(Level.TRACE);
    levelFilter.setOnMatch(FilterReply.DENY);
    levelFilter.setOnMismatch(FilterReply.ACCEPT);
    levelFilter.start();
    // find the console appender and apply filter
    Iterator<Appender<ILoggingEvent>> apps = root.iteratorForAppenders();
    while (apps.hasNext()) {
      apps.next().addFilter(levelFilter);
    }

    // send full logs (with TRACE) to drive laptop for offline analysis
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
  public static ProximitySensor proximitySensor;
  public static PdpSubsystem pdpSubsystem;

  public static List<SmartDashboardLogger> allSubsystems;

  public static DriveJoystickCommand driveJoystickCommand;
  public static OperatorInterface operatorInterface;

  public static CanDeviceFinder deviceFinder;

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
    if (RobotMap.IS_ROADKILL) {
      logger.info("Running on roadkill");
    } else {
      logger.info("Running on production");
    }
    
    RobotMap.initialize();
    
    deviceFinder = new CanDeviceFinder();
    deviceFinder.findDevices();

    driveTrain = new DriveTrain();
    vision = new Vision();
    shooter = new Shooter();
    feeder = new Feeder();
    gearManipulator = new GearManipulator();
    intake = new Intake();
    pneumatics = new Pneumatics();
    proximitySensor = new ProximitySensor();
    pdpSubsystem = new PdpSubsystem();
    allSubsystems = Arrays.asList(driveTrain, vision, shooter, feeder, gearManipulator, intake,
        pneumatics, proximitySensor, pdpSubsystem);

    driveJoystickCommand = new DriveJoystickCommand();
    operatorInterface = new OperatorInterface();
    
    SmartDashboard.putData(Scheduler.getInstance());

    chooser = new SendableChooser<>();
    // chooser.addDefault("Default Auto", new ExampleCommand());
    // chooser.addObject("My Auto", new MyAutoCommand());
    SmartDashboard.putData("Auto mode", chooser);

    // save phone battery
    // needs to be on a separate thread because robotPeriodic() will stop running when the DS is
    // disconnected
    Thread driverStationDisconnectChecker = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }

        if (!DriverStation.getInstance().isDSAttached()) {
          vision.setVisionEnabled(false);
        }
      }
    });
    driverStationDisconnectChecker.setDaemon(true);
    driverStationDisconnectChecker.setName("DS Disconnect Checker");
    driverStationDisconnectChecker.start();
  }

  @Override
  public void robotPeriodic() {
    // measure total cycle time, time we take during robotPeriodic, and WPIlib overhead
    final long start = System.nanoTime();
    logger.trace("robotPeriodic()");
    Scheduler.getInstance().run();

    vision.setVisionEnabled(true);

    allSubsystems.forEach(this::tryToSendDataToSmartDashboard);
    long currentNanos = System.nanoTime();
    SmartDashboard.putNumber("cycleMillis", (currentNanos - prevNanos) / 1000000.0);
    SmartDashboard.putNumber("ourTime", (currentNanos - start) / 1000000.0);
    prevNanos = currentNanos;
  }

  /**
   * Call {@link SmartDashboardLogger#sendDataToSmartDashboard()} but with exception handling.
   * 
   * @param logger The logger to call the method on
   */
  public void tryToSendDataToSmartDashboard(SmartDashboardLogger logger) {
    try {
      logger.sendDataToSmartDashboard();
    } catch (Throwable ex) {
      Robot.logger.debug("Error in tryToSendDataToSmartDashboard", ex);
    }
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
    SmartDashboard.putNumber("wpilibOverhead", (System.nanoTime() - prevNanos) / 1000000.0);
    logger.trace("disabledPeriodic()");
  }

  @Override
  public void autonomousInit() {
    logger.trace("autonomousInit()");
    autonomousCommand = chooser.getSelected();

    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector", "Default");
     * switch(autoSelected) { case "My Auto": autonomousCommand = new MyAutoCommand(); break; case
     * "Default Auto": default: autonomousCommand = new ExampleCommand(); break; }
     */

    // zero the navX for our starting position. Having the call here instead of in robotInit() or
    // the DriveTrain constructor makes sure it is zeroed when the robot is actually physically
    // aligned, but not reset again if the robot code crashes and restarts
    driveTrain.resetNavX();

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
    // measure wpilib overhead between robotPeriodic and specific *perodic methods
    // calling order is
    // 1. m_ds.waitForData()
    // 2. (auto|teleop|disabled)Periodic
    // 3. robotPeriodic()
    SmartDashboard.putNumber("wpilibOverhead", (System.nanoTime() - prevNanos) / 1000000.0);
    logger.trace("autonomousPeriodic()");
  }

  @Override
  public void teleopInit() {
    logger.trace("teleopInit()");

    if (RobotMap.IS_ROADKILL) {
      logger.info("Running on roadkill");
      logger.info("Wheel radius: " + RobotMap.WHEEL_RADIUS);
    } else {
      logger.info("Running on production");
    }

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
    SmartDashboard.putNumber("wpilibOverhead", (System.nanoTime() - prevNanos) / 1000000.0);
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
