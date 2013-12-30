package zenjiro;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

/**
 * 受け取ったメールの本文と添付ファイルをTwitterに投稿し、メールをFlickrに転送するサーブレット
 */
public class TweetServlet extends HttpServlet {
	@Override
	protected void doPost(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		final String applicationMailAddress = "tweet-xxxxxxxx";
		final String twitterOAuthConsumerKey = "xxxxxxxx";
		final String twitterOAuthConsumerSecret = "xxxxxxxx";
		final String twitterOAuthAccessToken = "xxxxxxxx";
		final String twitterOAuthAccessTokenSecret = "xxxxxxxx";
		final String fromMailAddress = "xxxxxxxx@gmail.com";
		final String flickrMailAddress = "xxxxxxxx@photos.flickr.com";
		final String mixiMailAddress = "xxxxxxxx@pv.mixi.jp";
		final String facebookMailAddress = "xxxxxxxx@m.facebook.com";
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
			for (final Address address : message
					.getRecipients(RecipientType.TO)) {
				if (address.toString().startsWith(applicationMailAddress + "@")
						|| address.toString().replaceFirst("^.+<", "")
								.startsWith(applicationMailAddress + "@")) {
					isValid = true;
				}
			}
			if (!isValid) {
				Logger.getAnonymousLogger().log(Level.INFO, "宛先が違うので無視します。");
				return;
			}
			if (message.getContent() instanceof MimeMultipart) {
				final MimeMultipart content = (MimeMultipart) message
						.getContent();
				// FIXME 本文が先に来たときにしか本文を投稿できない。
				String text = null;
				for (int i = 0; i < content.getCount(); i++) {
					final BodyPart body = content.getBodyPart(i);
					if (body.getContent() instanceof String) {
						text = (String) body.getContent();
					} else if (body.getContent() instanceof InputStream) {
						final Twitter twitter = new TwitterFactory()
								.getInstance();
						twitter.setOAuthConsumer(twitterOAuthConsumerKey,
								twitterOAuthConsumerSecret);
						twitter.setOAuthAccessToken(new AccessToken(
								twitterOAuthAccessToken,
								twitterOAuthAccessTokenSecret));
						final StatusUpdate status = new StatusUpdate(
								text == null ? body.getFileName() : text);
						status.media(body.getFileName(),
								(InputStream) body.getContent());
						final Metadata metadata = ImageMetadataReader
								.readMetadata(new BufferedInputStream(
										(InputStream) body.getContent()), true);
						final Directory directory = metadata
								.getDirectory(GpsDirectory.class);
						if (directory instanceof GpsDirectory) {
							final GpsDirectory gps = (GpsDirectory) directory;
							// FIXME 東経、北緯決め打ちで処理している。
							final Rational[] lat = gps
									.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
							final double latitude = lat[0].doubleValue()
									+ lat[1].doubleValue() / 60
									+ lat[2].doubleValue() / 3600;
							final Rational[] lng = gps
									.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
							final double longitude = lng[0].doubleValue()
									+ lng[1].doubleValue() / 60
									+ lng[2].doubleValue() / 3600;
							status.setLocation(new GeoLocation(latitude,
									longitude));
						}
						twitter.updateStatus(status);
					}
				}
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(flickrMailAddress, "Flickr"));
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(mixiMailAddress, "mixi"));
				message.setFrom(new InternetAddress(fromMailAddress));
				Transport.send(message);
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(facebookMailAddress, "Facebook"));
				message.setSubject(text, "ISO-2022-JP");
				Transport.send(message);
			}
		} catch (final MessagingException exception) {
			Logger.getAnonymousLogger().log(Level.WARNING, "メールの処理に失敗しました：{0}",
					exception.toString());
		} catch (final ImageProcessingException exception) {
			Logger.getAnonymousLogger().log(Level.WARNING, "画像の処理に失敗しました：{0}",
					exception.toString());
		} catch (final TwitterException exception) {
			Logger.getAnonymousLogger().log(Level.WARNING,
					"Twitterへの投稿に失敗しました：{0}", exception.toString());
		}
	}
}
