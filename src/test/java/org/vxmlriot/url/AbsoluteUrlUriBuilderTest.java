package org.vxmlriot.url;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbsoluteUrlUriBuilderTest {

    private static final String VALID_URI_STRING = "http://example.com/start.vxml";
    private static final String INVALID_URI_STRING = "start.vxml";

    private AbsoluteUrlUriBuilder builder = new AbsoluteUrlUriBuilder();

    @Test
    public void build_returnsUri() {
        URI uri = builder.build(VALID_URI_STRING);
        assertNotNull(uri);
        assertEquals(VALID_URI_STRING, uri.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildInvalid_throwsException() {
        builder.build(INVALID_URI_STRING);
    }
}