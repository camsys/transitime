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

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.StringConfigValue;

public class ReportsConfig {
	
	private static BooleanConfigValue showPredictionSource =
			new BooleanConfigValue(
					"transitclock.reports.showPredictionSource", 
					true, 
					"Whether prediction source UI element should be visible.");
	
	public static boolean isShowPredictionSource() {
		return showPredictionSource.getValue();
	}

	private static StringConfigValue defaultAllowableLateMinutes =
			new StringConfigValue(
					"transitclock.reports.defaultAllowableLateMinutes",
					"5",
					"Default allowable late minutes for OTP reports");

	public static String getDefaultAllowableLateMinutes() {
		return defaultAllowableLateMinutes.getValue();
	}

	private static StringConfigValue defaultAllowableEarlyMinutes =
			new StringConfigValue(
					"transitclock.reports.defaultAllowableEarlyMinutes",
					"1",
					"Default allowable early minutes for OTP reports");

	public static String getDefaultAllowableEarlyMinutes() {
		return defaultAllowableEarlyMinutes.getValue();
	}

	private static StringConfigValue defaultEarlyMinutesBucketOne =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultEarlyMinutesBucketOne",
					"1.0",
					"Default allowable early minutes for Prediction Accuracy Bucket reports for bucket one (0-3 minutes)");

	public static String getDefaultEarlyMinutesBucketOne() {
		return defaultEarlyMinutesBucketOne.getValue();
	}

	private static StringConfigValue defaultLateMinutesBucketOne =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultLateMinutesBucketOne",
					"1.0",
					"Default allowable late minutes for Prediction Accuracy Bucket reports for bucket one (0-3 minutes)");

	public static String getDefaultLateMinutesBucketOne() {
		return defaultLateMinutesBucketOne.getValue();
	}

	private static StringConfigValue defaultEarlyMinutesBucketTwo =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultEarlyMinutesBucketTwo",
					"1.5",
					"Default allowable early minutes for Prediction Accuracy Bucket reports for bucket two (3-6 minutes)");

	public static String getDefaultEarlyMinutesBucketTwo() {
		return defaultEarlyMinutesBucketTwo.getValue();
	}

	private static StringConfigValue defaultLateMinutesBucketTwo =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultLateMinutesBucketTwo",
					"2.0",
					"Default allowable late minutes for Prediction Accuracy Bucket reports for bucket two (3-6 minutes)");

	public static String getDefaultLateMinutesBucketTwo() {
		return defaultLateMinutesBucketTwo.getValue();
	}

	private static StringConfigValue defaultEarlyMinutesBucketThree =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultEarlyMinutesBucketThree",
					"2.5",
					"Default allowable early minutes for Prediction Accuracy Bucket reports for bucket three (6-12) minutes)");

	public static String getDefaultEarlyMinutesBucketThree() {
		return defaultEarlyMinutesBucketThree.getValue();
	}

	private static StringConfigValue defaultLateMinutesBucketThree =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultLateMinutesBucketThree",
					"3.5",
					"Default allowable late minutes for Prediction Accuracy Bucket reports for bucket three (6-12) minutes)");

	public static String getDefaultLateMinutesBucketThree() {
		return defaultLateMinutesBucketThree.getValue();
	}

	private static StringConfigValue defaultEarlyMinutesBucketFour =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultEarlyMinutesBucketFour",
					"4.0",
					"Default allowable early minutes for Prediction Accuracy Bucket reports for bucket four (12-20) minutes)");

	public static String getDefaultEarlyMinutesBucketFour() {
		return defaultEarlyMinutesBucketFour.getValue();
	}

	private static StringConfigValue defaultLateMinutesBucketFour =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultLateMinutesBucketFour",
					"6.0",
					"Default allowable late minutes for Prediction Accuracy Bucket reports for bucket four (12-20) minutes)");

	public static String getDefaultLateMinutesBucketFour() {
		return defaultLateMinutesBucketFour.getValue();
	}

	private static StringConfigValue defaultEarlyMinutesBucketFive =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultEarlyMinutesBucketFive",
					"4.0",
					"Default allowable early minutes for Prediction Accuracy Bucket reports for bucket five (20-30) minutes)");

	public static String getDefaultEarlyMinutesBucketFive() {
		return defaultEarlyMinutesBucketFive.getValue();
	}

	private static StringConfigValue defaultLateMinutesBucketFive =
			new StringConfigValue(
					"transitclock.reports.predictionAccuracyBuckets.defaultLateMinutesBucketFive",
					"6.0",
					"Default allowable late minutes for Prediction Accuracy Bucket reports for bucket five (20-30) minutes)");

	public static String getDefaultLateMinutesBucketFive() {
		return defaultLateMinutesBucketFive.getValue();
	}
}
