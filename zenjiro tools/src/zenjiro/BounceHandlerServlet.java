package zenjiro;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.mail.BounceNotification;
import com.google.appengine.api.mail.BounceNotificationParser;

/**
 * エラーメールを処理するサーブレット
 */
public class BounceHandlerServlet extends HttpServlet {
	@Override
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		try {
			final BounceNotification bounce = BounceNotificationParser.parse(req);
			Logger.getAnonymousLogger()
					.log(Level.WARNING,
							"from: {0}, to: {1}, subject: {2}, text: {3}, from: {4}, to: {5}, subject: {6}, text: {7}",
							new Object[] { bounce.getOriginal().getFrom(),
									bounce.getOriginal().getTo(),
									bounce.getOriginal().getSubject(),
									bounce.getOriginal().getText(),
									bounce.getNotification().getFrom(),
									bounce.getNotification().getTo(),
									bounce.getNotification().getSubject(),
									bounce.getNotification().getText() });
		} catch (final MessagingException e) {
			throw new IOException(e);
		}
	}
}