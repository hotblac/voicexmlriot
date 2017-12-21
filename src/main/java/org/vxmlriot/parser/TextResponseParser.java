package org.vxmlriot.parser;

import org.apache.commons.lang3.StringUtils;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.Optional;

/**
 * Find the text in the speak element of a SSML document
 */
public class TextResponseParser implements SsmlDocumentParser {
    @Override
    public Optional<String> parse(SsmlDocument document) {
        try {
            String textContent = document.getSpeak().getTextContent();
            return Optional.ofNullable(textContent)
                    .filter(StringUtils::isNotBlank);
        } catch (Exception e) {
            // Provided parse methods on SsmlDocument are prone to NPE.
            // Interpret these as invalid SSML and ignore
            return Optional.empty();
        }
    }
}
