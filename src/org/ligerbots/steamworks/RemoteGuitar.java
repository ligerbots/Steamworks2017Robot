package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.GenericHID;

public class RemoteGuitar extends GenericHID {

  private RemoteInputDevice remoteDevice;
  
  public RemoteGuitar(int port) {
    super(port);
    // TODO Auto-generated constructor stub
  }

  @Override
  public double getX(Hand hand) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getY(Hand hand) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getRawAxis(int which) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean getRawButton(int button) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int getPOV(int pov) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getPOVCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public HIDType getType() {
    // TODO Auto-generated method stub
    return GenericHID.HIDType.kXInputGuitar;
  }

  @Override
  public String getName() {
    return "Remote Guitar";
  }

  @Override
  public void setOutput(int outputNumber, boolean value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setOutputs(int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setRumble(RumbleType type, double value) {
    // TODO Auto-generated method stub

  }

}
