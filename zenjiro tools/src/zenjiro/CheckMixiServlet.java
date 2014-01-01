package zenjiro;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckMixiServlet extends HttpServlet {
	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		final HttpURLConnection connection = (HttpURLConnection) new URL(
				"http://mixi.jp/new_bbs.pl").openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setRequestProperty("Cookie", Const.MIXI_COOKIE);
		final Scanner scanner = new Scanner(new InputStreamReader(
				new GZIPInputStream(connection.getInputStream()), "EUC-JP"));
		while (scanner.hasNextLine()) {
			final Matcher matcher = Pattern
					.compile(
							"<p class=\"title iconEvent\"><a href=\"(http://mixi.jp/view_event.pl\\?comment_count=[0-9]+&comm_id=[0-9]+&id=[0-9]+)\">(.+)</a></p>")
					.matcher(scanner.nextLine().trim());
			if (matcher.matches()) {
				final String url = matcher.group(1);
				final String title = matcher.group(2);
				Logger.getAnonymousLogger().log(Level.INFO,
						"title: {0}, url: {1}", new String[] { title, url });
				final HttpURLConnection connection2 = (HttpURLConnection) new URL(
						url).openConnection();
				connection2.setRequestMethod("GET");
				connection2.setRequestProperty("Accept-Encoding", "gzip");
				connection2.setRequestProperty("Cookie", Const.MIXI_COOKIE);
				final Scanner scanner2 = new Scanner(new InputStreamReader(
						new GZIPInputStream(connection2.getInputStream()),
						"EUC-JP"));
				boolean isPrinting = false;
				while (scanner2.hasNextLine()) {
					final String line = scanner2.nextLine().trim();
					if (isPrinting && line.equals("</dd>")) {
						isPrinting = false;
						break;
					}
					if (isPrinting) {
						Logger.getAnonymousLogger().info(
								line.replaceAll("<[^>]+>|-->", "").trim());
					}
					if (line.equals("<dd>")) {
						isPrinting = true;
					}
				}
				scanner2.close();
			}
		}
		scanner.close();
	}
}
