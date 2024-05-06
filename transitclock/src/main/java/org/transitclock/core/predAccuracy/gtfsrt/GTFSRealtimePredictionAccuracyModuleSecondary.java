/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.core.predAccuracy.gtfsrt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;


/**
 * Reads in external prediction data from a GTFS realtime trip updates feed and
 * stores the data in memory. Then when arrivals/departures occur the prediction
 * accuracy can be determined and stored.
 *
 * @author Sean Og Crudden
 *
 */
public class GTFSRealtimePredictionAccuracyModuleSecondary extends GTFSRealtimePredictionAccuracyModule {

	private static final Logger logger = LoggerFactory.getLogger(GTFSRealtimePredictionAccuracyModuleSecondary.class);

	private static final String ARRIVAL_SOURCE_NAME = "GTFS-RT 2nd (Arrival)";

	private static final String DEPARTURE_SOURCE_NAME = "GTFS-RT 2nd (Departure)";

	/**
	 * @param agencyId
	 */
	public GTFSRealtimePredictionAccuracyModuleSecondary(String agencyId) {
		super(agencyId);
	}

	/********************** Config Params **************************/


	@Override
	protected Logger getLogger(){
		return logger;
	}

	private static final StringConfigValue gtfsTripUpdateUrl = new StringConfigValue(
			"transitclock.predAccuracy.secondary.gtfsTripUpdateUrl", "http://127.0.0.1:8091/trip-updates",
			"URL to access secondary gtfs-rt trip updates.");

	private static StringConfigValue gtfsRealtimeHeaderKey =
			new StringConfigValue("transitclock.predictionAccuracy.secondary.apiKeyHeader",
					null,
					"api key header value if necessary for secondary feed, null if not needed");

	private static StringConfigValue gtfsRealtimeHeaderValue =
			new StringConfigValue("transitclock.predictionAccuracy.secondary.apiKeyValue",
					null,
					"api key value if necessary for secondary feed, null if not needed");


  	private static ClassConfigValue translatorConfig =
		  new ClassConfigValue("transitclock.predAccuracy.secondary.RtTranslator", null,
			  "Implementation of GTFSRealtimeTranslator to perform " +
		  "the translation of stopIds and other rt quirks");


	@Override
	public StringConfigValue getGtfstripupdateurl() {
		return gtfsTripUpdateUrl;
	}

	@Override
	public StringConfigValue getGtfsRealtimeHeaderKey(){
		return gtfsRealtimeHeaderKey;
	}

	@Override
	public StringConfigValue getGtfsRealtimeHeaderValue(){
		return gtfsRealtimeHeaderValue;
	}

	@Override
	public ClassConfigValue getTranslatorConfig(){
		return translatorConfig;
	}

    @Override
	public String getArrivalSourceName() {
		return ARRIVAL_SOURCE_NAME;
	}

	@Override
	public String getDepartureSourceName() {
		return DEPARTURE_SOURCE_NAME;
	}

}
