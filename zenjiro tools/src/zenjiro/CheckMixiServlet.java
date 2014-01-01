package zenjiro;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
		final URL url = new URL("http://mixi.jp/new_bbs.pl");
		final HttpURLConnection connection = (HttpURLConnection) url
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setRequestProperty("Cookie", Const.MIXI_COOKIE);
		final Scanner scanner = new Scanner(new InputStreamReader(
				new GZIPInputStream(connection.getInputStream()), "EUC-JP"));
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			Logger.getAnonymousLogger().log(Level.INFO, line);
		}
		scanner.close();
	}
}
