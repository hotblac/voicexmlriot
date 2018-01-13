package org.vxmlriot.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Builds a HTTP / HTTPS URI from a string
 */
public class AbsoluteUrlUriBuilder implements UriBuilder {
    @Override
    public URI build(String resource) {
        try {
            URL url = new URL(resource);
            return url.toURI();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Not a valid URL: " + resource);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Not a valid URL: " + resource);
        }
    }
}
