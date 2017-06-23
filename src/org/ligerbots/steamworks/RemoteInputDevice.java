package org.ligerbots.steamworks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

// Listening for messages of the following form:
// nameOfChangedInput (one space here) value (line feed)
//  * line feed is 0xA, '\n'
//  * nameOfChangedInput is printable non-whitespace ASCII (no unicode)
//  * value is a valid input to parseDouble()
public class RemoteInputDevice {
  static final int PORT = 8888;

  class SocketListener implements Runnable {
    RemoteInputDevice device;
    
    public SocketListener(RemoteInputDevice device) {
      this.device = device;
      // TODO: maybe some more stuff here...?
    }
    
    public void run() {
      ServerSocket serverSocket = null;
      try {
        serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept(); // FIXME: this will block
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        while(!device.timeToStop) {
          String line = in.readLine();
          String[] parts = line.split(" ");
          device.theMap.put(parts[0], Double.parseDouble(parts[1]));
        }
      } catch(IOException exception) {
        exception.printStackTrace();
      } finally {
        if(serverSocket != null) {
          try {
            serverSocket.close();
          } catch(IOException exception) {
            exception.printStackTrace();
          }
        }
      }
    }
  }

  Thread theThread;
  volatile boolean timeToStop = false;
  ConcurrentHashMap<String, Double> theMap = new ConcurrentHashMap<String, Double>();
  
  public RemoteInputDevice() {
    theThread = new Thread(new SocketListener(this));
    theThread.start();
  }
  
  public void shutdown() {
    timeToStop = true;
  }
}
