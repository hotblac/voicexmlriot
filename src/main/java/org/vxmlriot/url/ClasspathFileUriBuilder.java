package org.vxmlriot.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Build URIs from classpath relative file names
 */
public class ClasspathFileUriBuilder implements UriBuilder {

    @Override
    public URI build(String resource) {
        final URL url = getClass().getClassLoader().getResource(resource);
        try {
            return url.toURI();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Not found: " + resource, e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid resource name: " + resource, e);
        }
    }
}
