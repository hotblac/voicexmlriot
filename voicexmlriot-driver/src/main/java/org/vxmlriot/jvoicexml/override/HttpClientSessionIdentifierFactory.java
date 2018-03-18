package org.vxmlriot.jvoicexml.override;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.jvoicexml.documentserver.schemestrategy.SessionIdentifierFactory;

/**
 * Overrides JVoiceXML 0.7.7 implementation of HttpClientSessionIdentifierFactory.
 * This implementation is identical except for the access modifier.
 * The original is package private, meaning it can't be reused in the overridden
 * UrlEncodingHttpSchemeStrategy implementation.
 */
public class HttpClientSessionIdentifierFactory
        implements SessionIdentifierFactory<HttpClient> {
    /** The default proxy port. */
    private static final int DEFAULT_PROXY_PORT = 80;

    /** The name of the proxy to use. */
    private static final String PROXY_HOST;

    /** The port of the proxy server. */
    private static final int PROXY_PORT;

    static {
        PROXY_HOST = System.getProperty("http.proxyHost");
        final String port = System.getProperty("http.proxyPort");
        if (PROXY_HOST != null && port != null) {
            PROXY_PORT = Integer.parseInt(port);
        } else {
            PROXY_PORT = DEFAULT_PROXY_PORT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpClient createSessionIdentifier(final String sessionId) {
        final HttpClient client = new DefaultHttpClient();
        if (PROXY_HOST != null) {
            HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
            HttpParams params = client.getParams();
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return client;
    }

}
