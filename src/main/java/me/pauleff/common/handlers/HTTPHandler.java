package me.pauleff.common.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HTTPHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPHandler.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private HTTPHandler()
    {
    }

    public static String get(String url) throws IOException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try
        {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
            {
                LOGGER.debug("HTTP GET successful ({}): {}", response.statusCode(), url);
                return response.body();
            }
            LOGGER.debug("HTTP GET failed ({}): {}", response.statusCode(), url);
            return null;
        } catch (InterruptedException e)
        {
            throw new IOException("HTTP request interrupted: " + url, e);
        }
    }
}
