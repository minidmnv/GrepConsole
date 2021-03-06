package krasa.grepconsole.plugin;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DonationNagger {
	private static final Logger LOG = Logger.getInstance(DonationNagger.class);

	public static final NotificationGroup NOTIFICATION = new NotificationGroup("Grep Console donation",
			NotificationDisplayType.STICKY_BALLOON, true);

	public static final String TITLE = "Support Grep Console plugin development";
	public static final String DONATE = "If you find this plugin helpful and would like to make a donation via PayPal, <a href=\"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Grep%20Console%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest\">" +
			"click here</a>. Thank you.";

	public static final LocalDate MONTH_AFTER_RELEASE = LocalDate.of(2018, 10, 26);

	private long actionsExecuted;
	private Date firstUsage;
	private Date lastNaggingDate;
	private Date lastDonationDate;
	private String firstUsedVersion;

	public long getActionsExecuted() {
		return actionsExecuted;
	}

	public void setActionsExecuted(long actionsExecuted) {
		this.actionsExecuted = actionsExecuted;
	}

	public Date getFirstUsage() {
		return firstUsage;
	}

	public void setFirstUsage(Date firstUsage) {
		this.firstUsage = firstUsage;
	}

	public Date getLastNaggingDate() {
		return lastNaggingDate;
	}

	public void setLastNaggingDate(Date lastNaggingDate) {
		this.lastNaggingDate = lastNaggingDate;
	}

	public Date getLastDonationDate() {
		return lastDonationDate;
	}

	public void setLastDonationDate(Date lastDonationDate) {
		this.lastDonationDate = lastDonationDate;
	}

	public String getFirstUsedVersion() {
		return firstUsedVersion;
	}

	public void setFirstUsedVersion(String firstUsedVersion) {
		this.firstUsedVersion = firstUsedVersion;
	}

	public void actionExecuted(@Nullable Project project) {
		try {
			actionsExecuted++;

			if (firstUsage == null) {
				firstUsage = new Date();
			}

			if (firstUsedVersion == null) {
				IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("GrepConsole"));
				if (plugin != null) {
					firstUsedVersion = plugin.getVersion();
				}
			}

			if (notDonatedRecently() && notNaggedRecently()) {
				if (actionsExecuted == 10 && probablyNotNewUser()) {
					nag(project);
				} else if (actionsExecuted % 100 == 0) {
					nag(project);
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}


	private void nag(Project project) {
		lastNaggingDate = new Date();

		Notification notification = NOTIFICATION.createNotification(TITLE, DONATE, NotificationType.INFORMATION, new NotificationListener.UrlOpeningListener(true) {
			@Override
			protected void hyperlinkActivated(@NotNull Notification notification1, @NotNull HyperlinkEvent event) {
				super.hyperlinkActivated(notification1, event);
				nagged();
			}
		});

		SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));
	}

	private boolean probablyNotNewUser() {
		LocalDate firstUse = firstUsage.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate today = LocalDate.now();

		return firstUse.isBefore(MONTH_AFTER_RELEASE) //probably upgraded   TODO to be deleted
				|| today.isAfter(firstUse.plusMonths(1));   //have it for more than month

	}

	private boolean notDonatedRecently() {
		if (lastDonationDate == null) {
			return true;
		}
		LocalDate today = LocalDate.now();
		LocalDate lastNag = lastDonationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return lastNag.isBefore(today.minusMonths(12));
	}

	private boolean notNaggedRecently() {
		if (lastNaggingDate == null) {
			return true;
		}
		LocalDate today = LocalDate.now();
		LocalDate lastNag = lastNaggingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return lastNag.isBefore(today.minusMonths(3));
	}

	private void nagged() {
		lastDonationDate = new Date();
	}

}
