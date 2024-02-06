package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.Agency;

import java.util.List;
import java.util.TimeZone;

public class AgencyDAO {


    /**
     * Deletes rev from the Agencies table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev)
            throws HibernateException {
        // Note that hql uses class name, not the table name
        String hql = "DELETE Agency WHERE configRev=" + configRev;
        int numUpdates = session.createQuery(hql).executeUpdate();
        return numUpdates;
    }

    /**
     * Returns List of Agency objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Agency> getAgencies(Session session, int configRev)
            throws HibernateException {
        String hql = "FROM Agency " +
                "    WHERE configRev = :configRev";
        Query query = session.createQuery(hql);
        query.setInteger("configRev", configRev);
        return query.list();
    }

    /**
     * Reads the current timezone for the agency from the agencies database
     *
     * @param agencyId
     * @return The TimeZone, or null if not successful
     */
    public static TimeZone getTimeZoneFromDb(String agencyId) {
        int configRev = ActiveRevisions.get(agencyId).getConfigRev();

        List<Agency> agencies = getAgencies(agencyId, configRev);
        if (agencies.size() != 0)
            return agencies.get(0).getTimeZone();
        else
            return null;
    }

    /**
     * Returns cached TimeZone object for agency. Useful for creating
     * Calendar objects and such.
     *
     * @return The TimeZone object for this agency
     */
    public TimeZone getTimeZone() {
        if (timezone == null)
            timezone = TimeZone.getTimeZone(agencyTimezone);
        return timezone;
    }

}
