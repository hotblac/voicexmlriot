package org.vxmlriot.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.jvoicexml.xml.ssml.Speak;
import org.jvoicexml.xml.ssml.SsmlDocument;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Optional;

/**
 * Find the audio src attribute in a SSML document
 */
public class AudioSrcResponseParser implements SsmlDocumentParser {

    private static final String AUDIO_SRC_XPATH = "/ssml:speak/ssml:audio/@src";
    private final NamespaceContextImpl SSML_NS = new NamespaceContextImpl();

    public AudioSrcResponseParser() {
        SSML_NS.startPrefixMapping("ssml", Speak.DEFAULT_XMLNS);
    }

    @Override
    public Optional<String> parse(SsmlDocument document) {
        try {
            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(SSML_NS);

            String audioSrc =  (String) xpath.evaluate(AUDIO_SRC_XPATH, document.getDocument(), XPathConstants.STRING);
            return Optional.ofNullable(audioSrc)
                    .filter(StringUtils::isNotBlank);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Failed to parse SsmlDocument", e);
        }
    }
}
