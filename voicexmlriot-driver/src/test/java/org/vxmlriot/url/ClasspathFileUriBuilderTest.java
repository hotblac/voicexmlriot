package org.vxmlriot.url;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;

public class ClasspathFileUriBuilderTest {

    private static final String FILE_EXISTS = "found.vxml";
    private static final String FILE_DOES_NOT_EXIST = "notfound.vxml";
    private static final String FILE_INVALID_NAME = "files should not have spaces.vxml";

    private ClasspathFileUriBuilder builder = new ClasspathFileUriBuilder();

    @Test
    public void build_returnsUri() {
        URI uri = builder.build(FILE_EXISTS);
        assertNotNull(uri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildMissingFile_throwsIllegalArgumentException() {
        builder.build(FILE_DOES_NOT_EXIST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildInvalidFileName_throwsIllegalArgumentException() {
        builder.build(FILE_INVALID_NAME);
    }
}