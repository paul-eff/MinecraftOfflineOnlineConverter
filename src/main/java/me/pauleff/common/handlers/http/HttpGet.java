package me.pauleff.common.handlers.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * Sends HTTP GET requests and returns successful response bodies.
 * <p>
 * Prefer the static one-shot {@link #body(String)} for simple calls. Use {@link #of(HttpClient)}
 * when injecting a custom client (for example in tests).
 */
public final class HttpGet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpGet.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final HttpGet DEFAULT = new HttpGet(HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build());

    private final HttpClient client;

    private HttpGet(HttpClient client)
    {
        this.client = client;
    }

    /**
     * Returns a handle that uses the shared default {@link HttpClient}.
     *
     * @return the default HTTP GET client
     */
    public static HttpGet ofDefault()
    {
        return DEFAULT;
    }

    /**
     * Returns a handle that uses the given {@link HttpClient}.
     *
     * @param client the HTTP client to send requests with
     * @return an HTTP GET handle
     * @throws NullPointerException if {@code client} is {@code null}
     */
    public static HttpGet of(HttpClient client)
    {
        return new HttpGet(Objects.requireNonNull(client, "client"));
    }

    /**
     * Sends an HTTP GET request with the default client and returns the body on success.
     *
     * @param url the absolute URL to request
     * @return the response body if the status code is {@code 200}; {@code null} otherwise
     * @throws IOException if the request fails or is interrupted
     */
    public static String body(String url) throws IOException
    {
        return ofDefault().getBody(url);
    }

    /**
     * Sends an HTTP GET request to the given URL and returns the response body on success.
     *
     * @param url the absolute URL to request
     * @return the response body if the status code is {@code 200}; {@code null} otherwise
     * @throws NullPointerException if {@code url} is {@code null}
     * @throws IOException          if the request fails or is interrupted
     */
    public String getBody(String url) throws IOException
    {
        Objects.requireNonNull(url, "url");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        try
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
            {
                LOGGER.debug("HTTP GET successful ({}): {}", response.statusCode(), url);
                return response.body();
            }
            LOGGER.debug("HTTP GET failed ({}): {}", response.statusCode(), url);
            return null;
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted: " + url, e);
        }
    }
}
