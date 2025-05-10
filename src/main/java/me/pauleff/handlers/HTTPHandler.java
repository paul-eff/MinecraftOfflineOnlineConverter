package me.pauleff.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles HTTP requests to external services, such as the Mojang API.
 *
 * @author Paul Ferlitz
 */
public class HTTPHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPHandler.class);
    private String url;

    /**
     * Constructs an HTTPHandler with an empty target URL.
     */
    public HTTPHandler()
    {
        this.url = "";
    }

    /**
     * Sets the target URL for the HTTP request.
     *
     * @param newUrl The new URL to target.
     */
    public void set(String newUrl)
    {
        this.url = newUrl;
        LOGGER.debug("URL set to: {}", newUrl);
    }

    /**
     * Performs an HTTP GET request to the set target URL.
     *
     * @return The response content, or null if the request fails.
     * @throws IOException If a connection issue occurs.
     */
    public String get() throws IOException
    {
        if (this.url == null || this.url.isEmpty())
        {
            LOGGER.error("No URL set for HTTP request.");
            return null;
        }
        // Setup
        HttpURLConnection con = null;
        StringBuilder content = new StringBuilder();
        try
        {
            // Define request body
            URL urlObj = new URL(this.url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(false);
            // Parse response
            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            // Extract content if response is OK (200)
            if (responseCode == 200)
            {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
                {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                    {
                        content.append(inputLine);
                    }
                }
                LOGGER.debug("HTTP GET successful ({}): {}", responseCode, responseMessage);
                return content.toString();
            } else
            {
                // Handle non-200 responses - e.g. redirects, 404, etc.
                LOGGER.debug("HTTP GET failed ({}): {}", responseCode, responseMessage);
                return null;
            }
        } catch (IOException e)
        {
            // Handle connection issues
            LOGGER.error("HTTP connection failed: {}", e.getMessage());
            throw e;
        } finally
        {
            // Close connection if still open
            if (con != null)
            {
                con.disconnect();
            }
        }
    }
}
