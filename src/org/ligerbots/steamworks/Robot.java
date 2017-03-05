
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
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tInstances;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.hal.HAL;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.ligerbots.steamworks.commands.AutoGearAndShootCommand;
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
import org.ligerbots.steamworks.subsystems.Stirrer;
import org.ligerbots.steamworks.subsystems.Vision;
import org.ligerbots.steamworks.subsystems.Vision.LedState;
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
  public static Stirrer stirrer;
  public static Intake intake;
  public static GearManipulator gearManipulator;
  public static ProximitySensor proximitySensor;
  public static PdpSubsystem pdpSubsystem;

  public static List<SmartDashboardLogger> allSubsystems;

  public static DriveJoystickCommand driveJoystickCommand;
  public static OperatorInterface operatorInterface;

  public static CanDeviceFinder deviceFinder;

  private static final Logger logger = LoggerFactory.getLogger(Robot.class);
  private static long nanosAtLastUpdate = 0;
  
  long prevNanos = System.nanoTime();

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    try {
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
      stirrer = new Stirrer();
      gearManipulator = new GearManipulator();
      intake = new Intake();
      pneumatics = new Pneumatics();
      proximitySensor = new ProximitySensor();
      pdpSubsystem = new PdpSubsystem();
      allSubsystems = Arrays.asList(driveTrain, vision, shooter, feeder, stirrer, gearManipulator,
          intake, pneumatics, proximitySensor, pdpSubsystem);
  
      driveJoystickCommand = new DriveJoystickCommand();
      operatorInterface = new OperatorInterface();
      
      SmartDashboard.putData(Scheduler.getInstance());
  
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
    } catch (Throwable ex) {
      logger.error("robotInit error", ex);
      ex.printStackTrace();
    }
  }

  @Override
  public void robotPeriodic() {
    try {
      // measure total cycle time, time we take during robotPeriodic, and WPIlib overhead
      final long start = System.nanoTime();
      logger.trace("robotPeriodic()");
      Scheduler.getInstance().run();
  
      vision.setVisionEnabled(true);
  
      long currentNanos = System.nanoTime();
      
      if (currentNanos - nanosAtLastUpdate > RobotMap.SMARTDASHBOARD_UPDATE_RATE * 1000000000) {
        allSubsystems.forEach(this::tryToSendDataToSmartDashboard);
        nanosAtLastUpdate = currentNanos;
      }
      
      SmartDashboard.putNumber("cycleMillis", (currentNanos - prevNanos) / 1000000.0);
      SmartDashboard.putNumber("ourTime", (currentNanos - start) / 1000000.0);
      prevNanos = currentNanos;
    } catch (Throwable ex) {
      logger.error("robotPeriodic error", ex);
      ex.printStackTrace();
    }
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
      ex.printStackTrace();
    }
  }

  /**
   * This function is called once each time the robot enters Disabled mode. You can use it to reset
   * any subsystem information you want to clear when the robot is disabled.
   */
  @Override
  public void disabledInit() {
    try {
      logger.trace("disabledInit()");
    } catch (Throwable ex) {
      logger.error("disabledInit error", ex);
      ex.printStackTrace();
    }
  }

  @Override
  public void disabledPeriodic() {
    try {
      SmartDashboard.putNumber("wpilibOverhead", (System.nanoTime() - prevNanos) / 1000000.0);
      logger.trace("disabledPeriodic()");
    } catch (Throwable ex) {
      logger.error("disabledPeriodic error", ex);
      ex.printStackTrace();
    }
  }

  @Override
  public void autonomousInit() {
    try {
      logger.trace("autonomousInit()");
  
      /*
       * String autoSelected = SmartDashboard.getString("Auto Selector", "Default");
       * switch(autoSelected) { case "My Auto": autonomousCommand = new MyAutoCommand(); break; case
       * "Default Auto": default: autonomousCommand = new ExampleCommand(); break; }
       */
  
      // zero the navX for our starting position. Having the call here instead of in robotInit() or
      // the DriveTrain constructor makes sure it is zeroed when the robot is actually physically
      // aligned, but not reset again if the robot code crashes and restarts
      driveTrain.calibrateYaw();
      driveTrain.zeroSensors();
      
      vision.setLedRingOn(LedState.ON);
  
      // schedule the autonomous command (example)
      // if (autonomousCommand != null) {
      // autonomousCommand.start();
      // }
      
      AutoGearAndShootCommand autoGearAndShootCommand = new AutoGearAndShootCommand();
      autoGearAndShootCommand.start();
    } catch (Throwable ex) {
      logger.error("autonomousInit error", ex);
      ex.printStackTrace();
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    try {
      // measure wpilib overhead between robotPeriodic and specific *perodic methods
      // calling order is
      // 1. m_ds.waitForData()
      // 2. (auto|teleop|disabled)Periodic
      // 3. robotPeriodic()
      SmartDashboard.putNumber("wpilibOverhead", (System.nanoTime() - prevNanos) / 1000000.0);
      logger.trace("autonomousPeriodic()");
    } catch (Throwable ex) {
      logger.error("autonomousPeriodic error", ex);
      ex.printStackTrace();
    }
  }

  @Override
  public void teleopInit() {
    try {
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
      // if (autonomousCommand != null) {
      // autonomousCommand.cancel();
      // }
      
      vision.setLedRingOn(LedState.ON);
  
      driveJoystickCommand.start();
    } catch (Throwable ex) {
      logger.error("teleopInit error", ex);
      ex.printStackTrace();
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    try {
      SmartDashboard.putNumber("wpilibOverhead", (System.nanoTime() - prevNanos) / 1000000.0);
      logger.trace("teleopPeriodic()");
    } catch (Throwable ex) {
      logger.error("teleopPeriodic error", ex);
      ex.printStackTrace();
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    try {
      logger.trace("testPeriodic()");
      LiveWindow.run();
    } catch (Throwable ex) {
      logger.error("testPeriodic error", ex);
      ex.printStackTrace();
    }
  }
  
  private boolean disabledInitialized = false;
  private boolean autonomousInitialized = false;
  private boolean teleopInitialized = false;
  private boolean testInitialized = false;
  
  /**
   * {@link IterativeRobot#startCompetition()}.
   */
  public void startCompetition() {
    HAL.report(tResourceType.kResourceType_Framework,
                                   tInstances.kFramework_Iterative);

    robotInit();

    // Tell the DS that the robot is ready to be enabled
    HAL.observeUserProgramStarting();

    // loop forever, calling the appropriate mode-dependent function
    LiveWindow.setEnabled(false);
    long start;
    while (true) {
      // Wait for new data to arrive
      start = System.nanoTime();
      // m_ds.waitForData();
      // Call the appropriate function depending upon the current robot mode
      if (isDisabled()) {
        // call DisabledInit() if we are now just entering disabled mode from
        // either a different mode or from power-on
        if (!disabledInitialized) {
          LiveWindow.setEnabled(false);
          disabledInit();
          disabledInitialized = true;
          // reset the initialization flags for the other modes
          autonomousInitialized = false;
          teleopInitialized = false;
          testInitialized = false;
        }
        HAL.observeUserProgramDisabled();
        disabledPeriodic();
      } else if (isTest()) {
        // call TestInit() if we are now just entering test mode from either
        // a different mode or from power-on
        if (!testInitialized) {
          LiveWindow.setEnabled(true);
          testInit();
          testInitialized = true;
          autonomousInitialized = false;
          teleopInitialized = false;
          disabledInitialized = false;
        }
        HAL.observeUserProgramTest();
        testPeriodic();
      } else if (isAutonomous()) {
        // call Autonomous_Init() if this is the first time
        // we've entered autonomous_mode
        if (!autonomousInitialized) {
          LiveWindow.setEnabled(false);
          // KBS NOTE: old code reset all PWMs and relays to "safe values"
          // whenever entering autonomous mode, before calling
          // "Autonomous_Init()"
          autonomousInit();
          autonomousInitialized = true;
          testInitialized = false;
          teleopInitialized = false;
          disabledInitialized = false;
        }
        HAL.observeUserProgramAutonomous();
        autonomousPeriodic();
      } else {
        // call Teleop_Init() if this is the first time
        // we've entered teleop_mode
        if (!teleopInitialized) {
          LiveWindow.setEnabled(false);
          teleopInit();
          teleopInitialized = true;
          testInitialized = false;
          autonomousInitialized = false;
          disabledInitialized = false;
        }
        HAL.observeUserProgramTeleop();
        teleopPeriodic();
      }
      robotPeriodic();
      
      long dt = 20_000_000 - (System.nanoTime() - start);
      if (dt > 0) {
        try {
          Thread.sleep(dt / 1_000_000, (int) (dt % 1_000_000));
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }
  }
}
