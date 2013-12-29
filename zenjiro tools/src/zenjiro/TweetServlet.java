package zenjiro;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
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
							"メールを受信しました：subject: {0}, content: {1}",
							new Object[] { message.getSubject(),
									message.getContent() });
			if (message.getContent() instanceof MimeMultipart) {
				Logger.getAnonymousLogger().log(Level.INFO, "マルチパートでした。");
			}
		} catch (final MessagingException exception) {
			Logger.getAnonymousLogger().log(Level.WARNING, "メールの処理に失敗しました：{0}",
					exception.toString());
		}
	}
}
