package org.ligerbots.steamworks.subsystems;

import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.networktables.ConnectionInfo;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.ligerbots.steamworks.RobotMap;

/**
 * The subsystem that handles communication with the android.
 */
public class Vision extends Subsystem {
  private static final int CS_STREAM_PORT = 5810;
  private static final int DATA_PORT = 5808;
  private static final int CS_FEEDBACK_INTERVAL = 1000;
  private static final int CS_MAGIC_NUMBER = 16777216;

  Relay ledRing;
  ITable table = null;

  /**
   * Creates the instance of VisionSubsystem.
   */
  public Vision() {
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
    if (table == null) {
      table = NetworkTable.getTable("Vision");
    }
    table.putBoolean("enabled", enabled);
  }

  /**
   * Turns the LED ring for the retroreflective tape on or off.
   * @param on Whether the LED ring should be on or not.
   */
  public void setLedRingOn(boolean on) {
    ledRing.set(on ? Relay.Value.kForward : Relay.Value.kReverse);
  }

  public void initDefaultCommand() {}

  /**
   * This method runs in a separate thread and receives data from the phone.
   */
  // yes this will be removed once vision data is used
  @SuppressWarnings({"unused", "localvariablename"})
  public void dataThread() {
    DatagramChannel udpChannel = null;
    ByteBuffer dataPacket = ByteBuffer.allocateDirect(Double.SIZE / 8 * 6);

    try {
      udpChannel = DatagramChannel.open();
      udpChannel.socket().setReuseAddress(true);
      udpChannel.socket().bind(new InetSocketAddress(DATA_PORT));
      udpChannel.configureBlocking(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    // the phone sends processing data over UDP faster than NetworkTables
    // 10fps refresh rate, so here we set up a receiver for the data

    while (true) {
      try {
        dataPacket.position(0);
        SocketAddress from = udpChannel.receive(dataPacket);
        if (from == null) {
          continue;
        }

        double rvec_0 = dataPacket.getDouble();
        double rvec_1 = dataPacket.getDouble();
        double rvec_2 = dataPacket.getDouble();
        double tvec_0 = dataPacket.getDouble();
        double tvec_1 = dataPacket.getDouble();
        double tvec_2 = dataPacket.getDouble();

        // now do something with the data
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * This runs in a separate thread and forwards vision frames from the phone to the DS.
   */
  public void packetForwardingThread() {
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
      ex.printStackTrace();
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
        ex.printStackTrace();
      }
    }
  }
}
