package org.vxmlriot.url;

import java.net.URI;

/**
 * Builds a URI from a String
 */
public interface UriBuilder {
    /**
     * Build a URI from a given resource string
     * @param resource to build the URI
     * @return valid URI
     * @throws IllegalArgumentException if resource does not make a valid URI
     */
    URI build(String resource);
}
