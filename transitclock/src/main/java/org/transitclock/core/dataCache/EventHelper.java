package org.transitclock.core.dataCache;

import org.transitclock.applications.Core;
import org.transitclock.core.dataCache.memcached.scheduled.TinyArrivalDeparture;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Departure;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Static helper class to copy between Struct models and optimized cached versions of said structs.
 */
public class EventHelper {

    public static List<IpcArrivalDeparture> asIpcList(List<TinyArrivalDeparture> events) {
        List<IpcArrivalDeparture> ipcList = new ArrayList<>();
        if (events == null) return ipcList;

        for (TinyArrivalDeparture tad : events) {
            IpcArrivalDeparture ipc = asIpc(tad);
            if (ipc != null) {
                ipcList.add(ipc);
            }
        }
        return ipcList;
    }

    private static IpcArrivalDeparture asIpc(TinyArrivalDeparture tad) {
        ArrivalDeparture ad = null;

        if (tad.isArrival)  {
            ad = new Arrival(null, new Date(tad.time), new Date(tad.avlTime),
                    Core.getInstance().getDbConfig().getBlock(tad.serviceId, tad.blockId), tad.tripIndex, tad.stopPathIndex,
                    null);

        }
        else {
            ad = new Departure(null, new Date(tad.time), new Date(tad.avlTime),
                    Core.getInstance().getDbConfig().getBlock(tad.serviceId, tad.blockId), tad.tripIndex, tad.stopPathIndex,
                    null);
        }
        try {
            return new IpcArrivalDeparture(ad);
        } catch (Exception any) {
            // the constructor throws Exception
            return null;
        }
    }


    public static List<TinyArrivalDeparture> asTinyList(List<IpcArrivalDeparture> events) {
        List<TinyArrivalDeparture> tinyList = new ArrayList<>();
        for (IpcArrivalDeparture iad : events) {
            TinyArrivalDeparture tad = asTiny(iad);
            if (tad != null)
                tinyList.add(tad);
        }
        return tinyList;
    }

    public static TinyArrivalDeparture asTiny(IpcArrivalDeparture iad) {
        TinyArrivalDeparture tad = new TinyArrivalDeparture();
        tad.isArrival = iad.isArrival();
        tad.directionId = iad.getDirectionId();
        tad.stopPathIndex = iad.getStopPathIndex();
        tad.stopId = iad.getStopId();
        tad.serviceId = iad.getServiceId();
        tad.blockId = iad.getBlockId();
        tad.tripIndex = iad.getTripIndex();
        tad.time = iad.getTime().getTime();
        tad.avlTime = iad.getAvlTime().getTime();
        return tad;
    }


}
