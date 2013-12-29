package zenjiro;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * メール受信のテスト
 */
public class TweetServlet extends HttpServlet {
	@Override
	protected void doPost(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		try {
			final MimeMessage message = new MimeMessage(
					Session.getDefaultInstance(new Properties(), null),
					req.getInputStream());
			Logger.getAnonymousLogger()
					.log(Level.INFO,
							"メールを受信しました：from: {0}, to: {1}, subject: {2}, content: {3}",
							new Object[] {
									Arrays.toString(message.getFrom()),
									Arrays.toString(message
											.getRecipients(RecipientType.TO)),
									message.getSubject(), message.getContent() });
			boolean isValid = false;
			final String secretAddress = "tweet-xxxxxxxx";
			for (final Address address : message
					.getRecipients(RecipientType.TO)) {
				if (address.toString().startsWith(secretAddress + "@")
						|| address.toString().replaceFirst("^.+<", "")
								.startsWith(secretAddress + "@")) {
					isValid = true;
				}
			}
			if (!isValid) {
				Logger.getAnonymousLogger().log(Level.INFO, "宛先が違うので無視します。");
				return;
			}
			if (message.getContent() instanceof MimeMultipart) {
				Logger.getAnonymousLogger().log(Level.INFO, "マルチパートでした。");
			}
		} catch (final MessagingException exception) {
			Logger.getAnonymousLogger().log(Level.WARNING, "メールの処理に失敗しました：{0}",
					exception.toString());
		}
	}
}
