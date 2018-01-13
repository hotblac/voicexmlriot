package org.vxmlriot.url;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RelativeUrlUriBuilderTest {

    private static final String VALID_ROOT = "http://example.com";
    private static final String VALID_ROOT_WITH_CONTEXT_PATH = "http://example.com/contextPath/";
    private static final String INVALID_ROOT = "example.com";
    private static final String VALID_RELATIVE = "resource.vxml";
    private static final String INVALID_RELATIVE = null;

    @Test
    public void validRoot_createsBuilder() {
        new RelativeUrlUriBuilder(VALID_ROOT);
    }

    @Test
    public void validRootWithContextPath_createsBuilder() {
        new RelativeUrlUriBuilder(VALID_ROOT_WITH_CONTEXT_PATH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRoot_throwsException() {
        new RelativeUrlUriBuilder(INVALID_ROOT);
    }

    @Test
    public void buildRelativeToRoot_buildsURI() {
        UriBuilder builder = new RelativeUrlUriBuilder(VALID_ROOT);
        URI uri = builder.build(VALID_RELATIVE);
        assertNotNull(uri);
        assertEquals(VALID_ROOT + "/" + VALID_RELATIVE, uri.toString());
    }

    @Test
    public void buildRelativeToContextPath_buildsURI() {
        UriBuilder builder = new RelativeUrlUriBuilder(VALID_ROOT_WITH_CONTEXT_PATH);
        URI uri = builder.build(VALID_RELATIVE);
        assertNotNull(uri);
        assertEquals(VALID_ROOT_WITH_CONTEXT_PATH + VALID_RELATIVE, uri.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildInvalidPath_throwsException() {
        UriBuilder builder = new RelativeUrlUriBuilder(VALID_ROOT);
        builder.build(INVALID_RELATIVE);
    }
}