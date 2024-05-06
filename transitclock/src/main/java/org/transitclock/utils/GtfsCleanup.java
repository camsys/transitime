package org.transitclock.utils;

import org.transitclock.config.StringListConfigValue;

import java.util.Collections;


public class GtfsCleanup {

    public static StringListConfigValue agencyIdPrefixesToRemove =
            new StringListConfigValue("transitclock.utils.gtfsCleanup.agencyIdPrefixesToRemove",
                    Collections.EMPTY_LIST,
                    "List of gency Id prefixes to remove for GTFS properties such as Blocks, Trips, Routes, and Vehicles." +
                            "Separate agency Id using semicolon.");


    /**
     * Removes specified agencyId plus one additional character to account for separator after agencyId.
     * If no agencyId to remove is specified, then original value is returned.
     * @param value
     * @return
     */
    public static String stripAgencyIdPrefix(String value){
        if(!agencyIdPrefixesToRemove.getValue().isEmpty()){
            for(String agency : agencyIdPrefixesToRemove.getValue()){
                if(value.startsWith(agency)){
                    return value.substring(agency.length()+1);
                }
            }
        }
        return value;
    }
}
