package org.transitclock.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.model.AgencyInterface;

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
     * Returns the list of agencies for the specified project ID.
     *
     * @param agencyId
     *            Specifies name of database
     * @param configRev
     * @return
     */
    public static List<AgencyInterface> getAgencies(String agencyId, int configRev) {
        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(agencyId);
        try {
            return getAgencies(session, configRev);
        } finally {
            session.close();
        }
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
    public static List<AgencyInterface> getAgencies(Session session, int configRev)
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
        int configRev = ActiveRevisionDAO.get(agencyId).getConfigRev();

        List<AgencyInterface> agencies = getAgencies(agencyId, configRev);
        if (agencies.size() != 0)
            return agencies.get(0).getTimeZone();
        else
            return null;
    }


}
