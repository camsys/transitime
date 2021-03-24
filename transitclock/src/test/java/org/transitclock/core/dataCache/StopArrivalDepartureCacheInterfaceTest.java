package org.transitclock.core.dataCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanDataGenerator;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class StopArrivalDepartureCacheInterfaceTest {

  long baseTime;
  KalmanDataGenerator generator;

  @Before
  public void setUp() throws Exception {
    baseTime = System.currentTimeMillis();
    generator = new KalmanDataGenerator(baseTime);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void linkResults() {
    ArrayList<ArrivalDeparture> input = new ArrayList<>();
    StopArrivalDepartureCacheInterface.linkResults(input);
    assertEquals(0, input.size());

    input.add(createArrival(0));
    input.add(createDeparture(0));
    assertNull(input.get(0).getNext());
    assertNull(input.get(0).getPrevious());
    assertNull(input.get(1).getNext());
    assertNull(input.get(1).getPrevious());

    StopArrivalDepartureCacheInterface.linkResults(input);

    assertNotNull(input.get(0).getNext());
    assertEquals(input.get(1), input.get(0).getNext());
    assertNull(input.get(0).getPrevious());
    assertNull(input.get(1).getNext());
    assertNotNull(input.get(1).getPrevious());
    assertEquals(input.get(0), input.get(1).getPrevious());

    input.add(createArrival(1));
    input.add(createDeparture(1));

    assertEquals(0, input.get(0).getStopPathIndex());
    assertEquals(0, input.get(1).getStopPathIndex());
    assertEquals(1, input.get(2).getStopPathIndex());
    assertEquals(1, input.get(3).getStopPathIndex());


    StopArrivalDepartureCacheInterface.linkResults(input);

    assertNotNull(input.get(0).getNext());
    assertEquals(input.get(1), input.get(0).getNext());
    assertNull(input.get(0).getPrevious());
    assertNotNull(input.get(1).getNext());
    assertNotNull(input.get(1).getPrevious());
    assertEquals(input.get(0), input.get(1).getPrevious());

    assertNotNull(input.get(2).getNext());
    assertEquals(input.get(3), input.get(2).getNext());
    assertNotNull(input.get(2).getPrevious());
    assertNull(input.get(3).getNext());
    assertNotNull(input.get(3).getPrevious());
    assertEquals(input.get(2), input.get(3).getPrevious());

  }

  private ArrivalDeparture createArrival(int i) {
    return generator.getArrival(getTime(i),
            getAvlTime(i),
            getBlock(),
            String.valueOf(i),
            i);
  }

  private Date getTime(int i) {
    return new Date(getBaseTime() + i);
  }

  private Date getAvlTime(int i) {
    return new Date(getBaseTime() + i);
  }

  private long getBaseTime() {
    return baseTime;
  }

  private ArrivalDeparture createDeparture(int i) {
    return generator.getDeparture(getTime(i),
            getAvlTime(i),
            getBlock(),
            String.valueOf(i),
            i);
  }

  private Block getBlock() {
    return generator.getBlock();
  }

}