package org.vxmlriot.jvoicexml.override;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.jvoicexml.documentserver.SchemeStrategy;
import org.jvoicexml.documentserver.schemestrategy.SessionIdentifierFactory;
import org.jvoicexml.documentserver.schemestrategy.SessionStorage;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.event.error.SemanticError;
import org.jvoicexml.interpreter.datamodel.KeyValuePair;
import org.jvoicexml.xml.vxml.RequestMethod;

/**
 * Overrides JVoiceXML 0.7.7 implementation of HttpSchemeStrategy.
 * This contains a defect fix to properly escape + in URL parameters.
 * A pull request has been submitted for this fix in JVoiceXML and it
 * should be in 0.7.8 and above.
 *
 * Note that the whole implementation is copied and pasted as the original
 * is marked as final.
 */
public class UrlEncodingHttpSchemeStrategy implements SchemeStrategy {
    /** Logger for this class. */
    private static final Logger LOGGER = Logger
            .getLogger(UrlEncodingHttpSchemeStrategy.class);

    /** Scheme for which this scheme strategy is responsible. */
    public static final String HTTP_SCHEME_NAME = "http";

    /** the storage of session identifiers. */
    private static final SessionStorage<HttpClient> SESSION_STORAGE;

    /** Encoding that should be used to encode/decode URLs. */
    private static String encoding = System.getProperty(
            "jvoicexml.xml.encoding", "UTF-8");

    /** Scheme name for this strategy. */
    private String scheme;

    /** The default fetch timeout. */
    private int defaultFetchTimeout;

    static {
        final SessionIdentifierFactory<HttpClient> factory = new HttpClientSessionIdentifierFactory();
        SESSION_STORAGE = new SessionStorage<HttpClient>(factory);
    }

    /**
     * Construct a new object.
     */
    public UrlEncodingHttpSchemeStrategy() {
        // Initialize with HTTP as default.
        scheme = HTTP_SCHEME_NAME;
    }

    /**
     * Sets the scheme for this strategy.
     *
     * @param value
     *            the new scheme
     * @since 0.7.4
     */
    public void setScheme(final String value) {
        scheme = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the default fetch timeout.
     *
     * @param timeout
     *            the default fetch timeout.
     * @since 0.7
     */
    public void setFetchTimeout(final int timeout) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("default fetch timeout: " + timeout);
        }
        defaultFetchTimeout = timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(final String sessionId, final URI uri,
                                      final RequestMethod method, final long timeout,
                                      final Collection<KeyValuePair> parameters) throws BadFetchError {
        final HttpClient client = SESSION_STORAGE
                .getSessionIdentifier(sessionId);
        final URI fullUri;
        try {

            /*
             * The following two lines override behaviour of HttpSchemeStrategy
             */
            final String fragmentLessUriString = StringUtils.substringBeforeLast(uri.toString(), "#");
            final URI fragmentLessUri = new URI(fragmentLessUriString);

            fullUri = addParameters(parameters, fragmentLessUri);
        } catch (URISyntaxException e) {
            throw new BadFetchError(e.getMessage(), e);
        } catch (SemanticError e) {
            throw new BadFetchError(e.getMessage(), e);
        }
        final String url = fullUri.toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("connecting to '" + url + "'...");
        }

        final HttpUriRequest request;
        if (method == RequestMethod.GET) {
            request = new HttpGet(url);
        } else {
            request = new HttpPost(url);
        }
        attachFiles(request, parameters);
        try {
            final HttpParams params = client.getParams();
            setTimeout(timeout, params);
            final HttpResponse response = client.execute(request);
            final StatusLine statusLine = response.getStatusLine();
            final int status = statusLine.getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new BadFetchError(statusLine.getReasonPhrase()
                        + " (HTTP error code " + status + ")");
            }
            final HttpEntity entity = response.getEntity();
            return entity.getContent();
        } catch (IOException e) {
            throw new BadFetchError(e.getMessage(), e);
        }
    }

    /**
     * Sets the timeout for the current connection.
     *
     * @param timeout
     *            timeout as it is declared in the document.
     * @param params
     *            connection parameters.
     * @since 0.7
     */
    private void setTimeout(final long timeout, final HttpParams params) {
        if (timeout != 0) {
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                    new Integer((int) timeout));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("timeout set to '" + timeout + "'");
            }
        } else if (defaultFetchTimeout != 0) {
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                    defaultFetchTimeout);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("timeout set to default '" + defaultFetchTimeout
                        + "'");
            }
        }
    }

    /**
     * Adds the parameters to the HTTP method.
     *
     * @param parameters
     *            parameters to add
     * @param uri
     *            the given URI
     * @return URI with the given parameters
     * @throws URISyntaxException
     *             error creating a URI
     * @throws SemanticError
     *             error evaluating a parameter
     */
    private URI addParameters(final Collection<KeyValuePair> parameters,
                              final URI uri) throws URISyntaxException, SemanticError {
        if ((parameters == null) || parameters.isEmpty()) {
            return uri;
        }
        final ArrayList<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
        for (KeyValuePair current : parameters) {
            final Object value = current.getValue();
            if (!(value instanceof File)) {
                final String name = current.getKey();
                final NameValuePair pair = new BasicNameValuePair(name,
                        value.toString());
                queryParameters.add(pair);
            }
        }

        final Collection<NameValuePair> parameterList = URLEncodedUtils.parse(
                uri, encoding);
        queryParameters.addAll(parameterList);

        final String query = URLEncodedUtils.format(queryParameters, encoding);
        final URIBuilder builder = new URIBuilder(uri);
        builder.setQuery(query);
        return builder.build();
    }

    /**
     * Attach the files given in the parameters.
     *
     * @param request
     *            the current request
     * @param parameters
     *            the parameters
     * @since 0.7.3
     */
    private void attachFiles(final HttpUriRequest request,
                             final Collection<KeyValuePair> parameters) {
        if (!(request instanceof HttpPost)) {
            return;
        }
        final HttpPost post = (HttpPost) request;
        for (KeyValuePair current : parameters) {
            final Object value = current.getValue();
            if (value instanceof File) {
                final File file = (File) value;
                final FileEntity fileEntity = new FileEntity(file,
                        ContentType.create("binary/octet-stream"));
                post.setEntity(fileEntity);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(final String sessionId) {
        SESSION_STORAGE.releaseSession(sessionId);
    }
}
