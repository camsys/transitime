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

package org.transitclock.monitoring;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.dao.MonitoringEventDAO;
import org.transitclock.db.structs.MonitoringEvent;
import org.transitclock.utils.EmailSender;
import org.transitclock.utils.Time;

/**
 * Base class for doing monitoring. If event is first triggered then an ERROR
 * email is sent out. If not triggered anymore then an OK email is sent.
 *
 * @author SkiBu Smith
 *
 */
public abstract class MonitorBase {

	private final EmailSender emailSender;
	
	protected final String agencyId;
	
	// wasTriggered indicates if last time monitor was checked whether the  
	// system was triggered and the acceptableIfTriggered wasn't true. For 
	// determining when state changes.
	private boolean wasTriggered = false;
	
	// Message to be logged when monitor is triggered. Set by subclass.
	private String message;
	
	// To be set when monitor is triggered but it is actually ok. Example is 
	// when not getting AVL data but it is OK since there are no active blocks.
	private String acceptableEvenIfTriggeredMessage;
	
	// A value for the monitor that can be logged into database.
	private double value;
	
	private static StringConfigValue emailRecipients =
			new StringConfigValue(
					"transitclock.monitoring.emailRecipients", 
					"Comma separated list of e-mail addresses indicating who "
					+ "should be e-mailed when monitor state changes.");

	private static IntegerConfigValue retryTimeoutSecs =
			new IntegerConfigValue(
					"transitclock.monitoring.retryTimeoutSecs", 
					5, 
					"How long in seconds system should wait before rexamining "
					+ "monitor. This way a short lived outage can be ignored. "
					+ "0 seconds means do not retry.");
	
	private static final Logger logger = LoggerFactory
			.getLogger(MonitorBase.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor.
	 * 
	 * @param emailSender
	 *            For sending out e-mails when monitor triggered or untriggered
	 * @param agencyId
	 *            Identifies agency
	 */
	public MonitorBase(EmailSender emailSender, String agencyId) {
		this.emailSender = emailSender;
		this.agencyId = agencyId;
	}
	
	/**
	 * When triggered() is called the subclass should set the message. This
	 * needs to be done whether or not monitor is triggered since monitoring also 
	 * sends an OK message when was triggered but isn't anymore.
	 * 
	 * @param message
	 */
	protected void setMessage(String message, double value) {
		// Log the message for debugging
		logger.debug(message);
		
		// Save the message so can be retrieved later for notifying users
		this.message = message;
		
		// Save the new value so can log it to database
		this.value = value;
	}
	
	/**
	 * For when acceptableEvenIfTriggered() is called. Sets the
	 * acceptableEvenIfTriggeredMessage so that it can be included in logging.
	 * 
	 * @param acceptableEvenIfTriggeredMessage
	 */
	protected void setAcceptableEvenIfTriggeredMessage(
			String acceptableEvenIfTriggeredMessage) {
		logger.debug(acceptableEvenIfTriggeredMessage);
		
		this.acceptableEvenIfTriggeredMessage = acceptableEvenIfTriggeredMessage;
	}
	
	/**
	 * For when there is no value associated with the monitoring, such
	 * as for monitoring db when it is either up or down. The value
	 * is set to Double.NaN.
	 * 
	 * @param message
	 */
	protected void setMessage(String message) {
		setMessage(message, Double.NaN);
	}
	
	/**
	 * Returns any message from the last time monitoring was checked. If secondary
	 * message is set (not null) then it is added to the message.
	 * 
	 * @return Message describing situation. Should be set whether or not
	 *         monitor is currently triggered so can also provide an OK message.
	 */
	public String getMessage() {
		if (acceptableEvenIfTriggeredMessage == null)
			return message;
		else
			return message + " " + acceptableEvenIfTriggeredMessage;
	}
	
	/**
	 * Abstract method. Returns whether or not a monitor is triggered, meaning
	 * it has a problem. The subclass method should also set the message using
	 * setMessage().
	 * 
	 * @return True if the monitor is currently triggered
	 */
	protected abstract boolean triggered();
	
	/**
	 * Also have a secondary monitor check. The idea is that there can be a
	 * primary check (e.g. no AVL data) but then also a secondary check (e.g.
	 * active blocks so not expecting AVL data). The monitor subclass can
	 * override this method and return true when the secondary monitor indicates
	 * that system is is actually ok even if triggered. The subclass should also
	 * call setAcceptableEvenIfTriggeredMessage() when the monitor is OK so that
	 * the message can be included when logging events.
	 * <p>
	 * This method is only to be called if the monitor is triggered. It is
	 * intended for determining if the monitor being triggered is actually
	 * acceptable.
	 * 
	 * @return True if secondary monitor indicates that system is ok and that
	 *         e-mail message indicating problem should not be sent out.
	 */
	protected boolean acceptableEvenIfTriggered() {
		return false;
	}
	
	/**
	 * A short description of the monitoring type that is displayed as part of
	 * the subject line for notification e-mails.
	 * 
	 * @return short description
	 */
	protected abstract String type();
	
	/**
	 * Returns comma separated list of who should be notified via e-mail when
	 * trigger state changes for the monitor. Specified by the Java property
	 * transitclock.monitoring.emailRecipients . Can be overwritten by an
	 * implementation of a monitor if want different list for a monitor.
	 * 
	 * @return E-mail addresses of who to notify
	 */
	protected String recipients() {		
		return emailRecipients.getValue();
	}
	
	/**
	 * Returns comma separated list of who should be notified via e-mail when
	 * trigger state changes for the monitor. Specified by the Java property
	 * transitclock.monitoring.emailRecipients . A static class so cannot be
	 * overwritten.
	 * 
	 * @return E-mail addresses of who to notify
	 */
	public static String recipientsGlobal() {
		return emailRecipients.getValue();
	}
	
	/**
	 * Returns true if the monitor previously was triggered.
	 * 
	 * @return true if was triggered
	 */
	protected boolean wasTriggered() {
		return wasTriggered;
	}
}
