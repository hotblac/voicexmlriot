package org.vxmlriot.stubs;

import org.jvoicexml.xml.ssml.SsmlDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class SsmlDocumentBuilder {

    private String filename;

    public static SsmlDocumentBuilder ssmlDocument() {
        return new SsmlDocumentBuilder();
    }

    public SsmlDocumentBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public SsmlDocument build() throws ParserConfigurationException, SAXException, IOException {
        final InputStream vxmlInput = getClass().getClassLoader().getResourceAsStream(filename);
        final InputSource vxmlSource = new InputSource(vxmlInput);
        return new SsmlDocument(vxmlSource);
    }
}
