package org.transitclock.core.dataCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.transitclock.core.ArrivalDepartureGeneratorDefaultImpl;
import org.transitclock.core.VehicleState;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator.VEHICLE;

public class IpcArrivalDepartureGeneratorTest {

  private KalmanDataGenerator dataGenerator;
  private IpcArrivalDepartureGenerator ipcGenerator;
  private ArrivalDepartureGeneratorDefaultImpl adGenerator;
  private long baseTime;

  @Before
  public void setUp() throws Exception {
    baseTime = System.currentTimeMillis();
    dataGenerator = new KalmanDataGenerator(baseTime);
    ipcGenerator = IpcArrivalDepartureGenerator.getInstance();
    adGenerator = new ArrivalDepartureGeneratorDefaultImpl();

  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHash() throws Exception {
    ArrivalDeparture arrivalDepartureList = dataGenerator.getLinkedArrivalDepartures(baseTime+2);
    IpcArrivalDeparture ipcs = ipcGenerator.generate(arrivalDepartureList, true);
    assertEquals(ipcGenerator.hash(arrivalDepartureList),
            ipcGenerator.hash(ipcs));
  }

  @Test
  public void generate() throws Exception {
    ArrivalDeparture arrivalDepartureList = dataGenerator.getLinkedArrivalDepartures(baseTime+2);
    VehicleState vehicleState = new VehicleState(VEHICLE);
    vehicleState.setLastArrivalDeparture(arrivalDepartureList);

    assertEquals(1, arrivalDepartureList.getStopPathIndex());
    ArrivalDeparture ad = arrivalDepartureList.getNext();
    assertNotNull(ad);
    assertEquals(1, ad.getStopPathIndex());
    ad = ad.getNext();
    assertNotNull(ad);
    assertEquals(3, ad.getStopPathIndex());
    ad = ad.getNext();
    assertNotNull(ad);
    assertEquals(3, ad.getStopPathIndex());

    ad = ad.getNext();
    assertNotNull(ad);
    assertEquals(5, ad.getStopPathIndex());
    ad = ad.getNext();
    assertNotNull(ad);
    assertEquals(5, ad.getStopPathIndex());


    IpcArrivalDeparture ipcs = ipcGenerator.generate(arrivalDepartureList, true);
    assertNotNull(ipcs);
    assertNull(ipcs.getPrevious());
    assertNotNull(ipcs.getNext());
    assertEquals(1,ipcs.getStopPathIndex());
    assertNotNull(findElement(ipcs, 1, true));
    assertNotNull(findElement(ipcs, 1, false));
    assertNull(findElement(ipcs, 2, false));
    assertNull(findElement(ipcs, 2, true));
    assertNotNull(findElement(ipcs, 3, true));
    assertNotNull(findElement(ipcs, 3, false));
    assertEquals(1, findElement(ipcs, 3, true).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(ipcs, 1, false).getNext().getStopPathIndex());
    assertNull(findElement(ipcs, 0, false));
    assertNotNull(findElement(ipcs, 9, false));

    // insert element at first of list
    ArrivalDeparture departure0 = dataGenerator.getDeparture(new Date(baseTime),
            new Date(baseTime),
            dataGenerator.getBlock(),
            "sp0",
            0);
    adGenerator.insertArrivalDeparture(vehicleState, departure0);

    IpcArrivalDeparture ipc0 = ipcGenerator.update(departure0);
    // NOTE!!! our list is not updated automatically
    assertNull(findElement(ipcs, 0, false));
    assertNotNull(findElement(ipc0, 0, false));
    assertNotNull(findElement(ipc0, 9, false));

    // add a new element mid list
    ArrivalDeparture departure2 = dataGenerator.getDeparture(new Date(baseTime+2),
            new Date(baseTime+2),
            dataGenerator.getBlock(),
            "sp0",
            2);
    // add departure then arrival mid list
    adGenerator.insertArrivalDeparture(vehicleState, departure2);
    IpcArrivalDeparture ipc2 = ipcGenerator.update(departure2);
    assertNull(findElement(ipc2, 2, true));
    assertNotNull(findElement(ipc2, 2, false));

    assertEquals(1, findElement(ipc2, 2, false).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(ipc2, 2, false).getNext().getStopPathIndex());
    ipcs = ipc2.getPrevious().getPrevious();

    ArrivalDeparture arrival2 = dataGenerator.getArrival(new Date(baseTime+2),
            new Date(baseTime+2),
            dataGenerator.getBlock(),
            "sp0",
            2);
    adGenerator.insertArrivalDeparture(vehicleState, arrival2);
    ipc2 = ipcGenerator.update(arrival2);

    assertEquals(1, findElement(ipc2, 2, true).getPrevious().getStopPathIndex());
    assertEquals(2, findElement(ipc2, 2, true).getNext().getStopPathIndex());
    assertEquals(2, findElement(ipc2, 2, false).getPrevious().getStopPathIndex());
    assertEquals(3, findElement(ipc2, 2, false).getNext().getStopPathIndex());


    // add arrival then departure mid list
    ArrivalDeparture arrival4 = dataGenerator.getArrival(new Date(baseTime+4),
            new Date(baseTime+4),
            dataGenerator.getBlock(),
            "sp0",
            4);
    adGenerator.insertArrivalDeparture(vehicleState, arrival4);
    IpcArrivalDeparture ipc4 = ipcGenerator.update(arrival4);

    assertEquals(3, findElement(ipc4, 4, true).getPrevious().getStopPathIndex());
    assertEquals(5, findElement(ipc4, 4, true).getNext().getStopPathIndex());

    ArrivalDeparture departure4 = dataGenerator.getDeparture(new Date(baseTime+4),
            new Date(baseTime+4),
            dataGenerator.getBlock(),
            "sp0",
            4);
    adGenerator.insertArrivalDeparture(vehicleState, departure4);
    ipc4 = ipcGenerator.update(departure4);
    assertEquals(3, findElement(ipc4, 4, true).getPrevious().getStopPathIndex());
    assertEquals(4, findElement(ipc4, 4, true).getNext().getStopPathIndex());
    assertEquals(4, findElement(ipc4, 4, false).getPrevious().getStopPathIndex());
    assertEquals(5, findElement(ipc4, 4, false).getNext().getStopPathIndex());



    // add last element
    ArrivalDeparture departure10 = dataGenerator.getDeparture(new Date(baseTime+10),
            new Date(baseTime+10),
            dataGenerator.getBlock(),
            "sp0",
            10);

    adGenerator.insertArrivalDeparture(vehicleState, departure10);
    IpcArrivalDeparture ipc10 = ipcGenerator.update(departure10);
    assertNotNull(findElement(ipc10, 10, false));
    assertEquals(9, findElement(ipc10, 10, false).getPrevious().getStopPathIndex());
    assertNull(findElement(ipc10, 10, false).getNext());

  }

  private IpcArrivalDeparture findElement(IpcArrivalDeparture ipcs, int stopPathIndex, boolean isArrival) {
    return findElement(ipcs, stopPathIndex, isArrival, 0);
  }

  private IpcArrivalDeparture findElement(IpcArrivalDeparture ipcs, int stopPathIndex, boolean isArrival, int depth) {
    if (ipcs == null) return null;
    if (depth > 20) return null;
    if (ipcs.getStopPathIndex() == stopPathIndex) {
      if (isArrival == ipcs.isArrival())
        return ipcs;
      if (ipcs.isDeparture())
        return findElement(ipcs.getPrevious(), stopPathIndex, isArrival, depth+1);
      return findElement(ipcs.getNext(), stopPathIndex, isArrival, depth+1);
    }
    if (stopPathIndex > ipcs.getStopPathIndex()) {
      return findElement(ipcs.getNext(), stopPathIndex, isArrival, depth+1);
    }
    if (stopPathIndex < ipcs.getStopPathIndex()) {
      return findElement(ipcs.getPrevious(), stopPathIndex, isArrival, depth+1);
    }
    return null;
   }
  private ArrivalDeparture advanceToDeparture(ArrivalDeparture arrivalDepartureList, int i) {
    if (arrivalDepartureList == null) return null;
    if (arrivalDepartureList.getStopPathIndex() == i && arrivalDepartureList.isDeparture()) {
      return arrivalDepartureList;
    }
    return advanceToDeparture(arrivalDepartureList.getNext(), i);
  }


}