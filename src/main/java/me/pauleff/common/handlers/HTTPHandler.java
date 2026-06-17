package me.pauleff.common.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPHandler.class);
    private String url;

        public HTTPHandler()
    {
        this.url = "";
    }

        public void set(String newUrl)
    {
        this.url = newUrl;
        LOGGER.debug("URL set to: {}", newUrl);
    }

        public String get() throws IOException
    {
        if (this.url == null || this.url.isEmpty())
        {
            LOGGER.error("No URL set for HTTP request.");
            return null;
        }
        HttpURLConnection con = null;
        StringBuilder content = new StringBuilder();
        try
        {
            URL urlObj = new URL(this.url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(false);
            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
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
                LOGGER.debug("HTTP GET failed ({}): {}", responseCode, responseMessage);
                return null;
            }
        } catch (IOException e)
        {
            LOGGER.error("HTTP connection failed: {}", e.getMessage());
            throw e;
        } finally
        {
            if (con != null)
            {
                con.disconnect();
            }
        }
    }
}
