package org.transitclock.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator;
import org.transitclock.db.structs.ArrivalDeparture;

import java.util.Date;

import static org.junit.Assert.*;
import static org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator.VEHICLE;

public class ArrivalDepartureGeneratorDefaultImplTest {

  private KalmanDataGenerator dataGenerator;
  private ArrivalDepartureGeneratorDefaultImpl adGenerator;
  private long baseTime;

  @Before
  public void setUp() throws Exception {
    baseTime = System.currentTimeMillis();
    dataGenerator = new KalmanDataGenerator(baseTime);
    adGenerator = new ArrivalDepartureGeneratorDefaultImpl();

  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void insertArrivalDeparture() {

    ArrivalDeparture arrivalDepartureList = dataGenerator.getLinkedArrivalDepartures(baseTime+2);
    VehicleState vehicleState = new VehicleState(VEHICLE);
    vehicleState.setLastArrivalDeparture(arrivalDepartureList);

    assertEquals(1, arrivalDepartureList.getStopPathIndex());

    assertNull(findElement(arrivalDepartureList, 0, false));
    assertNotNull(findElement(arrivalDepartureList, 1, true));
    assertEquals(true, findElement(arrivalDepartureList, 1, true).isArrival());
    assertEquals(true, findElement(arrivalDepartureList, 1, false).isDeparture());
    assertNotNull(findElement(arrivalDepartureList, 1, false));
    assertNotNull(findElement(arrivalDepartureList, 3, true));
    assertNotNull(findElement(arrivalDepartureList, 3, false));
    assertNotNull(findElement(arrivalDepartureList, 5, true));
    assertNotNull(findElement(arrivalDepartureList, 7, true));
    assertNotNull(findElement(arrivalDepartureList, 9, true));

    assertEquals(1, findElement(vehicleState.getLastArrivalDeparture(), 3, true).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(vehicleState.getLastArrivalDeparture(), 1, false).getNext().getStopPathIndex());


    // insert element at first of list
    ArrivalDeparture departure0 = dataGenerator.getDeparture(new Date(baseTime),
            new Date(baseTime),
            dataGenerator.getBlock(),
            "sp0",
            0);
    adGenerator.insertArrivalDeparture(vehicleState, departure0);
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 0, false));
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 9, true));

    // add departure then arrival mid list
    ArrivalDeparture departure2 = dataGenerator.getDeparture(new Date(baseTime+2),
            new Date(baseTime+2),
            dataGenerator.getBlock(),
            "sp0",
            2);

    assertNull(findElement(arrivalDepartureList, 2, false));
    assertEquals(1, findElement(vehicleState.getLastArrivalDeparture(), 3, true).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(vehicleState.getLastArrivalDeparture(), 1, false).getNext().getStopPathIndex());

    adGenerator.insertArrivalDeparture(vehicleState, departure2);
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 2, false));
    assertEquals(1, findElement(vehicleState.getLastArrivalDeparture(), 2, false).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(vehicleState.getLastArrivalDeparture(), 2, false).getNext().getStopPathIndex());

    ArrivalDeparture arrival2 = dataGenerator.getArrival(new Date(baseTime+2),
            new Date(baseTime+2),
            dataGenerator.getBlock(),
            "sp0",
            2);
    assertNull(findElement(arrivalDepartureList, 2, true));
    adGenerator.insertArrivalDeparture(vehicleState, arrival2);
    // check for added arrival
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 2, true));
    // previous departure should still be present
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 2, false));

    assertEquals(1, findElement(vehicleState.getLastArrivalDeparture(), 2, true).getPrevious().getStopPathIndex());
    assertEquals(2, findElement(vehicleState.getLastArrivalDeparture(), 2, true).getNext().getStopPathIndex());

    assertEquals(2, findElement(vehicleState.getLastArrivalDeparture(), 2, false).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(vehicleState.getLastArrivalDeparture(), 2, false).getNext().getStopPathIndex());


    // add arrival then departure mid list
    ArrivalDeparture arrival4 = dataGenerator.getArrival(new Date(baseTime+4),
            new Date(baseTime+4),
            dataGenerator.getBlock(),
            "sp0",
            4);
    assertNull(findElement(arrivalDepartureList, 4, true));
    adGenerator.insertArrivalDeparture(vehicleState, arrival4);
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 4, true));
    assertEquals(3, findElement(vehicleState.getLastArrivalDeparture(), 4, true).getPrevious().getStopPathIndex());
    assertEquals(5, findElement(vehicleState.getLastArrivalDeparture(), 4, true).getNext().getStopPathIndex());


    ArrivalDeparture departure4 = dataGenerator.getDeparture(new Date(baseTime+4),
            new Date(baseTime+4),
            dataGenerator.getBlock(),
            "sp0",
            4);

    assertNull(findElement(arrivalDepartureList, 4, false));
    adGenerator.insertArrivalDeparture(vehicleState, departure4);
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 4, true));
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 4, false));


    assertEquals(3, findElement(vehicleState.getLastArrivalDeparture(), 4, true).getPrevious().getStopPathIndex());
    assertEquals(4, findElement(vehicleState.getLastArrivalDeparture(), 4, true).getNext().getStopPathIndex());

    assertEquals(4, findElement(vehicleState.getLastArrivalDeparture(), 4, false).getPrevious().getStopPathIndex());
    assertEquals(5, findElement(vehicleState.getLastArrivalDeparture(), 4, false).getNext().getStopPathIndex());


    // add to end of list
    ArrivalDeparture departure10 = dataGenerator.getDeparture(new Date(baseTime+10),
            new Date(baseTime+10),
            dataGenerator.getBlock(),
            "sp0",
            10);
    assertNull(findElement(vehicleState.getLastArrivalDeparture(), 10, false));
    adGenerator.insertArrivalDeparture(vehicleState, departure10);
    assertNotNull(findElement(vehicleState.getLastArrivalDeparture(), 10, false));

  }

  private ArrivalDeparture findElement(ArrivalDeparture arrivalDepartureList, int stopPathIndex, boolean isArrival) {
    return findElement(arrivalDepartureList, stopPathIndex, isArrival, 0);
  }
  private ArrivalDeparture findElement(ArrivalDeparture arrivalDepartureList, int stopPathIndex, boolean isArrival, int depth) {
    if (arrivalDepartureList == null) return null;
    if (depth > 20) return null;
    if (arrivalDepartureList.getStopPathIndex() == stopPathIndex) {
      if (isArrival == arrivalDepartureList.isArrival())
        return arrivalDepartureList;  // found!
      if (arrivalDepartureList.isDeparture())
        return findElement(arrivalDepartureList.getPrevious(), stopPathIndex, isArrival, depth+1);
      return findElement(arrivalDepartureList.getNext(), stopPathIndex, isArrival, depth+1);
    }
    if (stopPathIndex > arrivalDepartureList.getStopPathIndex()) {
      return findElement(arrivalDepartureList.getNext(), stopPathIndex, isArrival, depth+1);
    }
    if (stopPathIndex < arrivalDepartureList.getStopPathIndex()) {
      return findElement(arrivalDepartureList.getPrevious(), stopPathIndex, isArrival,depth+1);
    }
    return null; // not found!
  }
}