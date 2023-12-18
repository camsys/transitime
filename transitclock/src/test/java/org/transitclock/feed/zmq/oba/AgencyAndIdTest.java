package org.transitclock.feed.zmq.oba;

import org.junit.Test;

import static org.junit.Assert.*;

public class AgencyAndIdTest {

  @Test
  public void convertFromString() {

    AgencyAndId id1 = AgencyAndId.convertFromString("MTA_1234");
    assertEquals("MTA", id1.getAgencyId());
    assertEquals("1234", id1.getId());
  }
}