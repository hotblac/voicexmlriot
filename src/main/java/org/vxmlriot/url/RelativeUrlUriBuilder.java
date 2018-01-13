package org.vxmlriot.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Builds an HTTP / HTTPS URI from a string, relative to a root context
 */
public class RelativeUrlUriBuilder implements UriBuilder {

    private URL root;

    /**
     * Construct a new builder where URLs are resolved relative to given root context
     * @throws IllegalArgumentException if the root is not a valid URL
     */
    public RelativeUrlUriBuilder(String root) {
        try {
            this.root = new URL(root);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Not a valid URL: " + root);
        }
    }

    @Override
    public URI build(String resource) {
        try {
            URL url = new URL(root, resource);
            return url.toURI();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid relative path: " + resource);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid relative path: " + resource);
        }
    }
}
