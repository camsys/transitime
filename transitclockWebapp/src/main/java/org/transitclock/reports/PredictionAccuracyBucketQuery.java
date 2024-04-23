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

package org.transitclock.reports;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.GenericQuery;
import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.utils.Time;
import org.transitclock.utils.TimeV2;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Date;

/**
 * For doing SQL query and generating JSON data for a prediction accuracy chart.
 * This abstract class does the SQL query and puts data into a map. Then a
 * subclass must be used to convert the data to JSON rows and columns for Google
 * chart.
 *
 * TODO: rewrite as hibernate criteria.
 *
 * @author SkiBu Smith
 *
 */
abstract public class PredictionAccuracyBucketQuery {

	private final Connection connection;
	private String dbType = null;

	// Keyed on source (so can show data for multiple sources at
	// once in order to compare prediction accuracy. Contains a array,
	// with an element for each prediction bucket, containing an array
	// of the prediction accuracy values in seconds for that bucket. Each bucket
	// is for
	// a certain prediction range, specified by predictionLengthBucketSize.
	protected final Map<String, List<List<Integer>>> map = new HashMap<String, List<List<Integer>>>();


	private static final Logger logger = LoggerFactory
			.getLogger(PredictionAccuracyBucketQuery.class);

	/********************** Member Functions **************************/

	/**
	 * Creates connection to the database for the specified agency.
	 *
	 * @param agencyId
	 * @throws SQLException
	 */
	public PredictionAccuracyBucketQuery(String agencyId) throws SQLException {
		WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
		this.dbType = agency.getDbType();
		connection = GenericQuery.getConnection(agency.getDbType(),
				agency.getDbHost(), agency.getDbName(), agency.getDbUserName(),
				agency.getDbPassword());
	}

	protected void doQuery(String beginDateStr,
						   String numDaysStr,
						   String beginTimeStr,
						   String endTimeStr,
						   String routeIds[],
						   String stopIds[],
						   String predSource,
						   String predType,
						   Integer[] minHorizon,
						   Integer[] maxHorizon
						  ) throws SQLException,
			ParseException {

		boolean isMySQL = "mysql".equals(dbType);

		// Make sure not trying to get data for too long of a time span since
		// that could bog down the database.
		int numDays = Integer.parseInt(numDaysStr);
		if (numDays > 31) {
			throw new ParseException(
					"Begin date to end date spans more than a month for endDate="
							+ " startDate=" + Time.parseDate(beginDateStr)
							+ " Number of days of " + numDays + " spans more than a month", 0);
		}


		String timeSql = getTimeSQL(beginDateStr, numDays, beginTimeStr, endTimeStr);


		// Determine route portion of SQL
		// Need to examine each route ID twice since doing a
		// routeId='stableId' OR routeShortName='stableId' in
		// order to handle agencies where GTFS route_id is not
		// stable but the GTFS route_short_name is.
		String routeSql = "";
		if (routeIds != null && routeIds.length > 0 && !routeIds[0].trim().isEmpty()) {
			routeSql = " AND (routeId=? OR routeShortName=?";
			for (int i = 1; i < routeIds.length; ++i)
				routeSql += " OR routeId=? OR routeShortName=?";
			routeSql += ")";
		}

		// Determine stop portion of SQL
		String stopSql = "";
		if (stopIds != null && stopIds.length > 0 && !stopIds[0].trim().isEmpty()) {
			stopSql = " AND stopId IN (";
			for (int i = 0; i < stopIds.length; i++) {
				stopSql += stopIds[i];
				if(i != stopIds.length - 1){
					stopSql += ",";
				}
			}
			stopSql += ")";
		}

		// Determine the source portion of the SQL. Default is to provide
		// predictions for all sources
		String sourceSql = "";
		if (StringUtils.isNotBlank(predSource) && !predSource.equalsIgnoreCase("All")) {
			if(predSource.equalsIgnoreCase("Other")) {
				// Anything but hard coded prediction source values "TransitClock, GTFS-RT (Arrival), GTFS-RT (Departure)"
				sourceSql = " AND predictionSource NOT IN ('TransitClock','GTFS-RT (Arrival)','GTFS-RT (Departure)')";
			}
			else {
				// Just match provided prediction source
				sourceSql = " AND predictionSource = '" +  predSource + "'";
			}
		}

		// Determine SQL for prediction type. Can be "" (for
		// all), "AffectedByWaitStop", or "NotAffectedByWaitStop".
		String predTypeSql = "";
		if (predType != null && !predType.isEmpty()) {
			if (predSource.equals("AffectedByWaitStop")) {
				// Only "AffectedByLayover" predictions
				predTypeSql = " AND affectedByWaitStop = true ";
			} else {
				// Only "NotAffectedByLayover" predictions
				predTypeSql = " AND affectedByWaitStop = false ";
			}
		}
		// TODO generate database independent SQL if possible!
		// Put the entire SQL query together
		String postSql = "SELECT "
				+ "     to_char(predictedTime-predictionReadTime, 'SSSS')::integer as predLength, "
				+ "     predictionAccuracyMsecs/1000 as predAccuracy, "
				+ "     predictionSource as source "
				+ " FROM predictionAccuracy "
				+ "WHERE "
				+ timeSql
				+ "  AND predictedTime-predictionReadTime < '00:30:00' "
				+ routeSql
				+ stopSql
				+ sourceSql
				+ predTypeSql;


		String mySql = "SELECT "
				+ "     (TIMESTAMPDIFF(MICROSECOND,predictionReadTime,arrivalDepartureTime)/1000000) AS predLength, "
				+ "     (TIMESTAMPDIFF(MICROSECOND,predictedTime,arrivalDepartureTime) / 1000000) AS  predAccuracy, "
				+ "     predictionSource as source "
				+ "FROM PredictionAccuracy "
				+ "WHERE "
				+ timeSql
				+ " AND (TIMESTAMPDIFF(MICROSECOND,predictionReadTime,arrivalDepartureTime)/1000000) <=1800 " // 30 min
				+ routeSql
				+ stopSql
				+ sourceSql
				+ predTypeSql;


		String sql = postSql;
		if (isMySQL) {
			sql = mySql;
		}

		PreparedStatement statement = null;
		try {
			logger.debug("SQL: {}", sql);
			statement = connection.prepareStatement(sql);

			// Set the parameters for the query
			int i = 1;
			if (routeIds != null && routeIds.length > 0 && !routeIds[0].trim().isEmpty()) {
				for (String routeId : routeIds)
					if (!routeId.trim().isEmpty()) {
						// Need to add the route ID twice since doing a
						// routeId='stableId' OR routeShortName='stableId' in
						// order to handle agencies where GTFS route_id is not
						// stable but the GTFS route_short_name is.
						statement.setString(i++, routeId);
						statement.setString(i++, routeId);

					}
			}

			// Actually execute the query
			ResultSet rs = statement.executeQuery();

			// Process results of query
			while (rs.next()) {
				int predLength = rs.getInt("predLength");
				int predAccuracy = rs.getInt("predAccuracy");
				String sourceResult = rs.getString("source");

				addDataToMap(predLength, predAccuracy, sourceResult, minHorizon, maxHorizon);
				logger.debug("predLength={} predAccuracy={} source={}",
						predLength, predAccuracy, sourceResult);
			}
			rs.close();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (statement != null)
				statement.close();
		}
	}

	private String getTimeSQL(String startDateStr, int numDays, String beginTimeStr, String endTimeStr) throws DateTimeParseException {

		try {
			LocalDate currentDate = TimeV2.parseDate(startDateStr);
			LocalTime beginTime = TimeV2.parseTime(beginTimeStr, "00:00:00");
			LocalTime endTime = TimeV2.parseTime(endTimeStr, "23:59:59");

			LocalDateTime currentBeginDateTime = currentDate.atTime(beginTime);
			LocalDateTime currentEndDateTime = currentDate.atTime(endTime);

			StringBuilder sql = new StringBuilder();

			// Loop through each date based on the number of days
			for (int i = 0; i < numDays; i++) {
				String currentDateBeginTimeStr = TimeV2.dateTimeStrSec(currentBeginDateTime);
				String currentDateEndTimeStr = TimeV2.dateTimeStrSec(currentEndDateTime);

				if(i > 0){
					sql.append(" OR ");
				}
				sql.append(" arrivalDepartureTime BETWEEN ");
				sql.append("'").append(currentDateBeginTimeStr).append("'");
				sql.append(" AND ");
				sql.append("'").append(currentDateEndTimeStr).append("' ");

				// Move to the next date
				currentBeginDateTime.plusDays(1);
				currentEndDateTime.plusDays(1);
			}

			String timeSql = sql.toString();
			logger.info(timeSql);
			return timeSql;

		} catch (DateTimeParseException e) {
			logger.error("Error parsing date string {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Puts the data from the query into the map so it can be further processed
	 * later.
	 *
	 * @param predLength
	 * @param predAccuracy
	 * @param source
	 */
	private void addDataToMap(int predLength, int predAccuracy, String source, Integer[] minHorizon, Integer[] maxHorizon) {
		// Get the prediction buckets for the specified source
		List<List<Integer>> predictionBuckets = map.get(source);
		if (predictionBuckets == null) {
			predictionBuckets = new ArrayList<>();
			map.put(source, predictionBuckets);
		}

		// Determine the index of the appropriate prediction bucket
		int predictionBucketIndex = fiveBucketIndex(predLength, minHorizon, maxHorizon);
		while (predictionBuckets.size() < predictionBucketIndex + 1)
			predictionBuckets.add(new ArrayList<>());

		if (predictionBucketIndex < predictionBuckets.size() && predictionBucketIndex >= 0) {
			List<Integer> predictionAccuracies = predictionBuckets
					.get(predictionBucketIndex);
			// Add the prediction accuracy to the bucket.
			predictionAccuracies.add(predAccuracy);
		} else {
			// some prediction streams supply predictions in the past -- ignore those
			logger.error("predictionLength {} has illegal index {} for predAccuracy {} and source {}",
					predLength, predictionBucketIndex, predAccuracy, source);
		}

	}

	private static int fiveBucketIndex(int predLength, Integer[] minHorizon, Integer maxHorizon[]) {
		try {
			if(predLength >= minHorizon[0] &&  predLength <= maxHorizon[0]){
				return 0;
			}
			if(predLength > minHorizon[1] &&  predLength <= maxHorizon[1]){
				return 1;
			}
			if(predLength > minHorizon[2] &&  predLength <= maxHorizon[2]){
				return 2;
			}
			if(predLength > minHorizon[3] &&  predLength <= maxHorizon[3]){
				return 3;
			}
			if(predLength > minHorizon[4] &&  predLength <= maxHorizon[4]){
				return 4;
			}
		} catch(Exception e){
			logger.error("Five Bucket Index error {}, {}", minHorizon, maxHorizon, e);
		}
		return -1;

	}

}
