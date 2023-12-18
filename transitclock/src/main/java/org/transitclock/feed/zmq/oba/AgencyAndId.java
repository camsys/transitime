package org.transitclock.feed.zmq.oba;

/**
 * local copy of OneBusAway's AgencyAndId for NYC integration
 */
public class AgencyAndId {

  public static final String ID_SEPARATOR = "_";
  private final String agencyId;
  private final String id;

  public String getAgencyId() {
    return agencyId;
  }

  public String getId() {
    return id;
  }

  public AgencyAndId(String agencyId, String id) {
    this.agencyId = agencyId;
    this.id = id;
  }


  public static AgencyAndId convertFromString(String agencyAndId) {
    if (agencyAndId == null) return null;
    int index = agencyAndId.indexOf(ID_SEPARATOR);
    if (index < 0) throw new IllegalStateException("invalid agency-and-id " + agencyAndId);
    String agency = agencyAndId.substring(0, index);
    String id = agencyAndId.substring(index+1, agencyAndId.length());
    return new AgencyAndId(agency, id);
  }

}
