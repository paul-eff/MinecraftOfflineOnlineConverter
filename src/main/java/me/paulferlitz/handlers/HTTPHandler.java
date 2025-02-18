package me.paulferlitz.handlers;

import me.paulferlitz.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles API connections to external services, such as the Mojang API.
 * Provides a simple way to make GET requests and retrieve response data.
 *
 * @author Paul Ferlitz
 */
public class HTTPHandler {
    private static final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);
    private String url;

    /**
     * Constructs an HTTPHandler with an empty target URL.
     */
    public HTTPHandler() {
        this.url = "";
    }

    /**
     * Sets the target URL for the HTTP request.
     *
     * @param newUrl The new URL to target.
     */
    public void setUrl(String newUrl) {
        this.url = newUrl;
        if (Main.getArgs().hasOption("v")) logger.info("Target URL set to: {}", newUrl);
    }

    /**
     * Performs an HTTP GET request to the set target URL.
     *
     * @return The response content, or null if the request fails.
     * @throws IOException If a connection issue occurs.
     */
    public String httpDoGet() throws IOException {
        if (this.url == null || this.url.isEmpty()) {
            logger.error("No URL set for HTTP request.");
            return null;
        }

        HttpURLConnection con = null;
        StringBuilder content = new StringBuilder();

        try {
            URL urlObj = new URL(this.url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(false);

            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();

            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }
                if (Main.getArgs().hasOption("v")) logger.info("HTTP GET successful: {} {}", responseCode, responseMessage);
                return content.toString();
            } else {
                if (Main.getArgs().hasOption("v")) logger.warn("HTTP GET request failed: {} {}", responseCode, responseMessage);
                return null;
            }
        } catch (IOException e) {
            logger.error("HTTP request failed: {}", e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
