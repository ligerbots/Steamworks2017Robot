package org.ligerbots.steamworks;

/**
 * A map of the field, complete with useful locations and obstacles.
 */
public enum FieldMap {
  RED_ALLIANCE_MAP(),
  BLUE_ALLIANCE_MAP();
  
  FieldPosition boiler;
  FieldPosition loadingStationInner;
  FieldPosition loadingStationOuter;
  FieldPosition loadingStationOverflow;
  FieldPosition hopperBoilerRed;
  FieldPosition hopperBoilerCenter;
  FieldPosition hopperBoilerBlue;
  FieldPosition hopperLoadingRed;
  FieldPosition hopperLoadingBlue;
  FieldPosition gearLiftStation1;
  FieldPosition gearLiftStation2;
  FieldPosition gearLiftStation3;
  FieldLine dividerLift12;
  FieldLine dividerLift23;
  FieldPosition ropeStation1;
  FieldPosition ropeStation2;
  FieldPosition ropeStation3;
}
