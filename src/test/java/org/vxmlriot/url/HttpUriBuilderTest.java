package org.vxmlriot.url;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpUriBuilderTest {

    private static final String VALID_URI_STRING = "http://example.com/start.vxml";
    private static final String INVALID_URI_STRING = "start.vxml";

    private HttpUriBuilder builder = new HttpUriBuilder();

    @Test
    public void build_returnsUri() {
        URI uri = builder.build(VALID_URI_STRING);
        assertNotNull(uri);
        assertEquals(VALID_URI_STRING, uri.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildInvalid_throwsException() throws Exception {
        builder.build(INVALID_URI_STRING);
    }
}