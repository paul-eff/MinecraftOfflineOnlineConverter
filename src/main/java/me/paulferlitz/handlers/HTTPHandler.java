package me.paulferlitz.handlers;

import me.paulferlitz.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class for handling the API connections to the Mojang API.
 *
 * @author Paul Ferlitz
 */
public class HTTPHandler
{
    // Class variables
    private String url;

    /**
     * Main constructor.
     */
    public HTTPHandler()
    {
        this.url = "";
    }

    /**
     * Method to set the target URL.
     *
     * @param newUrl The new URL to target.
     */
    public void setUrl(String newUrl)
    {
        this.url = newUrl;
    }

    /**
     * Method for handling GET requests to target URL.
     *
     * @return Returns the response content.
     * @throws IOException When there are problems with connection etc.
     */
    public String httpDoGet() throws IOException
    {
        // Create URL and connect
        URL url = new URL(this.url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        // Set method and parameters
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(false);
        // Check response
        if (con.getResponseCode() == 200)
        {
            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
            {
                content.append(inputLine);
            }
            // Close and return results
            in.close();
            con.disconnect();
            if(Main.getArgs().hasOption("v")) System.out.println("HttpHandler: " + con.getResponseCode() + " " + con.getResponseMessage() + " " + content);
            return content.toString();
        }
        if(Main.getArgs().hasOption("v")) System.out.println("HttpHandler: " + con.getResponseCode() + " " + con.getResponseMessage());
        return null;
    }
}
