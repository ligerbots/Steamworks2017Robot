package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.can.CANJNI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class checks for present CAN devices on the CAN bus.
 * 
 * <p>
 * This helps us to
 * <ol>
 * <li>Replace the default console spamming error behavior with better custom error handling</li>
 * <li>Reconfigure drive if Talons are missing</li>
 * </ol>
 * </p>
 */
public class CanDeviceFinder {
  private static final Logger logger = LoggerFactory.getLogger(CanDeviceFinder.class);

  private ByteBuffer targetId = ByteBuffer.allocateDirect(4);
  private ByteBuffer timestamp = ByteBuffer.allocateDirect(4);
  private boolean[] presentPcms;
  private boolean[] presentTalons;
  private boolean presentPdp;

  public CanDeviceFinder() {
    presentPcms = new boolean[64];
    presentTalons = new boolean[64];
  }

  /**
   * Checks if there's a PCM at a certain CAN ID.
   * 
   * @param canId The CAN ID to check
   * @return True if that PCM is present
   */
  public boolean isPcmAvailable(int canId) {
    return presentPcms[canId];
  }

  /**
   * Checks if there's a Talon SRX at a certain CAN ID.
   * 
   * @param canId The CAN ID to check
   * @return True if that Talon SRX is present
   */
  public boolean isTalonAvailable(int canId) {
    return presentTalons[canId];
  }

  /**
   * Checks if there's a PDP at CAN ID 0.
   * 
   * @return True if the PDP is present
   */
  public boolean isPdpAvailable() {
    return presentPdp;
  }

  /**
   * Helper routine to get last received message for a given ID.
   */
  private long checkMessage(int fullId, int deviceId) {
    try {
      targetId.clear();
      targetId.order(ByteOrder.LITTLE_ENDIAN);
      targetId.asIntBuffer().put(0, fullId | deviceId);

      timestamp.clear();
      timestamp.order(ByteOrder.LITTLE_ENDIAN);
      timestamp.asIntBuffer().put(0, 0x00000000);

      CANJNI.FRCNetCommCANSessionMuxReceiveMessage(targetId.asIntBuffer(), 0x1fffffff, timestamp);

      long retval = timestamp.getInt();
      retval &= 0xFFFFFFFF; /* undo sign-extension */
      return retval;
    } catch (Exception ex) {
      return -1;
    }
  }

  /**
   * Polls for received framing to determine if a device is present. This is meant to be used once
   * initially (and not periodically) since this steals cached messages from the robot API.
   * 
   * <p>
   * This checks the PDP at ID 0, and Talon SRXs and PCMs at 0-63. For each device at each possible
   * ID, it checks the timestamp of the last recieved CAN message, then waits and checks again. It
   * determines whether the device is online by making sure it is still actively sending CAN
   * messages.
   * </p>
   */
  public void findDevices() {
    logger.info("Beginning CAN device scan");
    /* get timestamp0 for each device */
    long pdp0TimeStamp0; // only look for PDP at '0'
    long[] pcmTimeStamp0 = new long[63];
    long[] talonTimeStamp0 = new long[63];

    pdp0TimeStamp0 = checkMessage(0x08041400, 0);
    for (int i = 0; i < 63; ++i) {
      pcmTimeStamp0[i] = checkMessage(0x09041400, i);
      talonTimeStamp0[i] = checkMessage(0x02041400, i);
    }

    /* wait 200ms */
    try {
      Thread.sleep(200);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    /* get timestamp1 for each device */
    long pdp0TimeStamp1; // only look for PDP at '0'
    long[] pcmTimeStamp1 = new long[63];
    long[] talonTimeStamp1 = new long[63];

    pdp0TimeStamp1 = checkMessage(0x08041400, 0);
    for (int i = 0; i < 63; ++i) {
      pcmTimeStamp1[i] = checkMessage(0x09041400, i);
      talonTimeStamp1[i] = checkMessage(0x02041400, i);
    }

    /*
     * compare, if timestamp0 is good and timestamp1 is good, and they are different, device is
     * healthy
     */
    if (pdp0TimeStamp0 >= 0 && pdp0TimeStamp1 >= 0 && pdp0TimeStamp0 != pdp0TimeStamp1) {
      presentPdp = true;
      logger.info("Found PDP");
    }
    for (int i = 0; i < 63; ++i) {
      if (pcmTimeStamp0[i] >= 0 && pcmTimeStamp1[i] >= 0 && pcmTimeStamp0[i] != pcmTimeStamp1[i]) {
        presentPcms[i] = true;
        logger.info(String.format("Found PCM %d", i));
      }
      if (talonTimeStamp0[i] >= 0 && talonTimeStamp1[i] >= 0
          && talonTimeStamp0[i] != talonTimeStamp1[i]) {
        presentTalons[i] = true;
        logger.info(String.format("Found Talon %d", i));
      }
    }
  }
}
