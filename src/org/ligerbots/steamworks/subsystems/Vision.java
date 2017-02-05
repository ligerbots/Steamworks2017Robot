package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.networktables.ConnectionInfo;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.ITable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.ligerbots.steamworks.RobotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The subsystem that handles communication with the android.
 */
public class Vision extends Subsystem implements SmartDashboardLogger {
  private static final Logger logger = LoggerFactory.getLogger(Vision.class);

  /**
   * This is a container for vision data.
   */
  public static class VisionData {
    double rvecPitch;
    double rvecYaw;
    double rvecRoll;
    double tvecX;
    double tvecY;
    double tvecZ;

    public double getRvecPitch() {
      return rvecPitch;
    }

    public double getRvecYaw() {
      return rvecYaw;
    }

    public double getRvecRoll() {
      return rvecRoll;
    }

    public double getTvecX() {
      return tvecX;
    }

    public double getTvecY() {
      return tvecY;
    }

    public double getTvecZ() {
      return tvecZ;
    }

    public String toString() {
      return String.format("%f,%f,%f | %f,%f,%f", tvecX, tvecY, tvecZ, rvecPitch, rvecYaw,
          rvecRoll);
    }
  }

  public static enum LedState {
    ON, OFF, TOGGLE
  }

  private static final int CS_STREAM_PORT = 5810;
  private static final int DATA_PORT = 5808;
  private static final int CS_FEEDBACK_INTERVAL = 1000;
  private static final int CS_MAGIC_NUMBER = 16777216;

  Relay ledRing;
  ITable table = null;
  // buffer vision data for multithreaded access
  VisionData[] visionData = {new VisionData(), new VisionData()};
  long lastPhoneDataTimestamp;
  volatile int currentVisionDataIndex = 0;

  /**
   * Creates the instance of VisionSubsystem.
   */
  public Vision() {
    logger.trace("Initialize");

    ledRing = new Relay(RobotMap.RELAY_LED_RING);

    Thread forwardThread = new Thread(this::packetForwardingThread);
    forwardThread.setDaemon(true);
    forwardThread.setName("Packet Forwarding Thread");
    forwardThread.start();

    Thread dataThread = new Thread(this::dataThread);
    dataThread.setDaemon(true);
    dataThread.setName("Vision Data Thread");
    dataThread.start();
  }

  /**
   * Sends an enable flag to the phone to enable or disable image processing. Helpful for making
   * sure the phone doesn't eat power when it doesn't need to.
   * 
   * @param enabled Whether image processing should be enabled or not
   */
  public void setVisionEnabled(boolean enabled) {
    logger.info(String.format("Setting vision enabled=%b", enabled));

    if (table == null) {
      table = NetworkTable.getTable("Vision");
    }
    table.putBoolean("enabled", enabled);
  }

  /**
   * Turns the LED ring for the retroreflective tape on or off.
   * 
   * @param desiredState Whether the LED ring should be on or not.
   */
  public void setLedRingOn(LedState desiredState) {
    logger.trace(String.format("Setting LED ring %s, current state %b", desiredState.toString(),
        isLedRingOn()));
    boolean on;
    if (desiredState == LedState.TOGGLE) {
      on = !isLedRingOn();
    } else if (desiredState == LedState.ON) {
      on = true;
    } else {
      on = false;
    }
    ledRing.set(on ? Relay.Value.kReverse : Relay.Value.kOff);
  }

  /**
   * Returns whether the LED ring is on or off.
   * 
   * @return True for on, false for off
   */
  public boolean isLedRingOn() {
    return ledRing.get() != Relay.Value.kOff;
  }

  public VisionData getVisionData() {
    return visionData[currentVisionDataIndex];
  }

  /**
   * Checks to make sure the phone is currently active.
   * 
   * @return True if the phone has sent us data in the last 500 ms
   */
  public boolean isVisionDataValid() {
    return System.nanoTime() - lastPhoneDataTimestamp < 500_000_000;
  }

  public void initDefaultCommand() {}

  /**
   * This method runs in a separate thread and receives data from the phone.
   */
  public void dataThread() {
    // the phone sends processing data over UDP faster than NetworkTables
    // 10fps refresh rate, so here we set up a receiver for the data
    logger.info("Data thread init");
    DatagramChannel udpChannel = null;
    ByteBuffer dataPacket = ByteBuffer.allocateDirect(Double.SIZE / 8 * 6);

    try {
      udpChannel = DatagramChannel.open();
      udpChannel.socket().setReuseAddress(true);
      udpChannel.socket().bind(new InetSocketAddress(DATA_PORT));
      udpChannel.configureBlocking(true);
    } catch (Exception ex) {
      logger.error("Data thread init error", ex);
    }

    while (true) {
      try {
        dataPacket.position(0);
        SocketAddress from = udpChannel.receive(dataPacket);
        if (from == null) {
          continue;
        }

        dataPacket.position(0);

        VisionData notCurrentData = visionData[1 - currentVisionDataIndex];
        double rvecPitch = dataPacket.getDouble();
        double rvecYaw = dataPacket.getDouble();
        double rvecRoll = dataPacket.getDouble();
        double tvecX = dataPacket.getDouble();
        double tvecY = dataPacket.getDouble();
        double tvecZ = dataPacket.getDouble();

        // if the data is garbage or no target was located, keep the old data
        if (Double.isNaN(rvecPitch) || Double.isNaN(rvecYaw) || Double.isNaN(rvecRoll)
            || Double.isNaN(tvecX) || Double.isNaN(tvecY) || Double.isNaN(tvecZ)) {
          continue;
        }
        
        notCurrentData.rvecPitch = rvecPitch;
        notCurrentData.rvecYaw = rvecYaw;
        notCurrentData.rvecRoll = rvecRoll;
        notCurrentData.tvecX = tvecX;
        notCurrentData.tvecY = tvecY;
        notCurrentData.tvecZ = tvecZ;
        
        currentVisionDataIndex = 1 - currentVisionDataIndex;

        lastPhoneDataTimestamp = System.nanoTime();
      } catch (IOException ex) {
        logger.error("Data thread communication error", ex);
      }
    }
  }

  /**
   * This runs in a separate thread and forwards vision frames from the phone to the DS.
   */
  public void packetForwardingThread() {
    logger.info("Stream thread init");

    DatagramChannel udpChannel = null;
    InetSocketAddress sendAddress = null;
    ByteBuffer recvPacket = null;
    byte[] feedbackMessage = "👌".getBytes();
    ByteBuffer feedbackPacket = ByteBuffer.allocateDirect(feedbackMessage.length);
    feedbackPacket.put(feedbackMessage);
    long lastFeedbackTime = System.currentTimeMillis();

    // set up UDP
    try {
      udpChannel = DatagramChannel.open();
      udpChannel.socket().setReuseAddress(true);
      udpChannel.socket().bind(new InetSocketAddress(CS_STREAM_PORT));
      udpChannel.configureBlocking(false);

      recvPacket = ByteBuffer.allocateDirect(udpChannel.socket().getReceiveBufferSize());
    } catch (Exception ex) {
      logger.error("Stream thread init error", ex);
    }

    while (true) {
      try {
        // steal the driver laptop's IP from networktables
        if (sendAddress == null) {
          ConnectionInfo[] connections = NetworkTablesJNI.getConnections();
          for (ConnectionInfo connInfo : connections) {
            // we want the laptop, not the phone
            if (connInfo.remote_id.equals("Android")) {
              continue;
            }
            sendAddress = new InetSocketAddress(connInfo.remote_ip, CS_STREAM_PORT);
            logger.trace(String.format("Got DS IP address %s", sendAddress.toString()));
          }
        }
        // get a packet from the phone
        SocketAddress from = null;
        recvPacket.limit(recvPacket.capacity());
        recvPacket.position(0);
        from = udpChannel.receive(recvPacket);
        boolean gotPacket = from != null;

        // if we have a packet and it's time to tell the phone we're
        // getting packets then tell the phone we're getting packets

        if (from != null && System.currentTimeMillis() - lastFeedbackTime > CS_FEEDBACK_INTERVAL) {
          lastFeedbackTime = System.currentTimeMillis();
          feedbackPacket.position(0);
          udpChannel.send(feedbackPacket, from);
        }

        // if sending packets to the driver laptop turns out to be
        // slower than receiving packets from the phone, then drop
        // everything except the latest packet
        while (from != null) {
          // save the length of what we got last time
          recvPacket.limit(recvPacket.capacity());
          recvPacket.position(0);
          from = udpChannel.receive(recvPacket);
        }

        if (sendAddress != null && gotPacket) {
          // make sure to forward a packet of the same length, by
          // setting the limit on the bytebuffer
          recvPacket.position(0);
          int magic = recvPacket.getInt();
          int length = recvPacket.getInt();
          if (magic == CS_MAGIC_NUMBER) {
            recvPacket.limit(length + 8);
            recvPacket.position(0);
            udpChannel.send(recvPacket, sendAddress);
          }
          // otherwise, it's probably a control packet from the
          // dashboard sending the resolution and fps settings - we
          // don't actually care
        }
      } catch (Exception ex) {
        logger.error("Stream thread communication error", ex);
      }
    }
  }

  @Override
  public void sendDataToSmartDashboard() {
    // phone handles vision data for us
    SmartDashboard.putBoolean("LED_On", ledRing.get() != Relay.Value.kOff);
  }
}
