package aka.jhttprequest.main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.springframework.http.MediaType;

import aka.jhttprequest.main.common.HTTPRequestData;
import aka.jhttprequest.main.common.HTTPResponseBufferedImage;
import aka.jhttprequest.main.common.HTTPResponseData;
import aka.jhttprequest.main.common.HTTPResponseString;
import aka.jhttprequest.main.exceptions.HTTPException;

/**
 * @author Charlotte
 */
public class HTTPManager {

    @NonNull
    private static final Logger LOGGER = Logger.getLogger(HTTPManager.class.getSimpleName());

    /**
     * Send a "Get" HTTP(s) request.
     *
     * @param httpRequestData
     * @return String if result found, null if not (null means that the request can be resend an other time)
     * @throws HTTPException if bad request, bad gateway, ...
     * @see HTTPRequestData
     * @see HTTPException
     */
    @Nullable
    public HTTPResponseData<@NonNull ?> sendGetRequest(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<@NonNull ?> result = null;

        try {
            result = AccessController.doPrivileged((PrivilegedExceptionAction<HTTPResponseData<@NonNull ?>>) () -> sendGetRequestPrivileged(httpRequestData));
        } catch (final PrivilegedActionException e) {
            handleException(e, "sendGetRequest");
        }

        return result;
    }

    private void handleException(final @NonNull PrivilegedActionException e, @NonNull final String failingMethodName) throws HTTPException {
        final Throwable t = e.getCause();
        if (t instanceof HTTPException) {
            final var httpException = (HTTPException) e.getException();
            throw httpException;
        } else {
            LOGGER.logp(Level.SEVERE, getClass().getName(), failingMethodName, e.getMessage(), e);
            throw new HTTPException(e.getMessage(), e.getCause());
        }
    }

    @Nullable
    private HTTPResponseData<?> sendGetRequestPrivileged(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<?> result = null;
        final var httpclient = getHttpClient(httpRequestData);
        final var sb = new StringBuilder();
        final var baseUrl = httpRequestData.getUrl().toString();
        sb.append(baseUrl);
        final var params = getParamsToUrl(httpRequestData);
        if (!params.isEmpty()) {
            final var paramString = URLEncodedUtils.format(params, "utf-8");
            if (!baseUrl.endsWith("?")) {
                sb.append('?');
            }
            sb.append(paramString);
        }

        final var url = sb.toString();
        final var httpget = new HttpGet(url);
        try {
            if (httpRequestData.getHeaders().isEmpty()) {
                httpget.addHeader("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
            } else {
                for (final Entry<@NonNull String, String> header : httpRequestData.getHeaders().entrySet()) {
                    httpget.addHeader(header.getKey(), header.getValue());
                }
            }

            // Create a response handler
            final var response = httpclient.execute(httpget);
            if (response == null) {
                throw new HTTPException("No Response from: " + url);
            }
            result = getHTTPResponseData(url, response);
        } catch (final IOException e) {
            LOGGER.logp(Level.SEVERE, getClass().getName(), "sendGetRequestPrivileged", e.getMessage(), e);
            throw new HTTPException("HTTP IOException ERROR  : " + url, e.getCause());
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpget.releaseConnection();
        }

        return result;
    }

    /**
     * Send a "Post" HTTP(s) request.
     *
     * @param httpRequestData
     * @return String if result found, null if connection lost, server busy
     * @throws HTTPException if bad request, bad gateway, ...
     * @see HTTPRequestData
     * @see HTTPException
     */
    @NonNull
    public HTTPResponseData<@NonNull ?> sendPostRequest(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<@NonNull ?> result = null;
        try {
            result = AccessController.doPrivileged((PrivilegedExceptionAction<HTTPResponseData<@NonNull ?>>) () -> sendPostRequestPrivileged(httpRequestData));
        } catch (final PrivilegedActionException e) {
            handleException(e, "sendPostRequest");
        }

        assert result != null;
        return result;
    }

    @Nullable
    private HTTPResponseData<@NonNull ?> sendPostRequestPrivileged(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<@NonNull ?> result = null;
        final var httpclient = getHttpClient(httpRequestData);
        final var url = httpRequestData.getUrl().toString();
        final var httpPost = new HttpPost(url);
        try {

            if (httpRequestData.getHeaders().isEmpty()) {
                httpPost.addHeader("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
                httpPost.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            } else {
                for (final Entry<@NonNull String, String> header : httpRequestData.getHeaders().entrySet()) {
                    httpPost.addHeader(header.getKey(), header.getValue());
                }
            }
            final var typeParametrizedClass = httpRequestData.getTypeParameterClass();
            if (typeParametrizedClass == null) {
                final var params = getParamsToUrl(httpRequestData);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            } else if (typeParametrizedClass instanceof UrlEncodedFormEntity) {
                final var params = getParamsToUrl(httpRequestData);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            } else {
                httpPost.setEntity(typeParametrizedClass);
            }
            // Create a response handler
            final var response = httpclient.execute(httpPost);
            if (response == null) {
                throw new HTTPException("No Response from: " + url);
            }
            result = getHTTPResponseData(url, response);
        } catch (final IOException e) {
            LOGGER.logp(Level.SEVERE, getClass().getName(), "sendPostRequestPrivileged", e.getMessage(), e);
            throw new HTTPException("HTTP IOException ERROR  : " + url, e.getCause());
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpPost.releaseConnection();
        }
        return result;
    }

    @NonNull
    private HTTPResponseData<@NonNull ?> getHTTPResponseData(@NonNull final String url, @NonNull final HttpResponse response) throws HTTPException {
        final var statusLine = response.getStatusLine();
        final var code = statusLine.getStatusCode();
        if (statusLine.getStatusCode() >= 300) {
            throw new HTTPException(code, "HTTP ERROR " + code + " : " + url, new Throwable(statusLine.getReasonPhrase()));
        }
        final var entityResponse = response.getEntity();
        if (entityResponse == null) {
            throw new HTTPException("Response contains no content");
        }
        final var result = getHTTPResponseData(entityResponse, url);
        return result;
    }

    @NonNull
    private HTTPResponseData<@NonNull ?> getHTTPResponseData(@NonNull final HttpEntity entity, @NonNull final String url) throws HTTPException {
        HTTPResponseData<@NonNull ?> httpResponseData = null;
        final var contentType = ContentType.get(entity);

        var charset = contentType.getCharset();
        if (charset == null) {
            charset = Charset.defaultCharset();
            assert charset != null;
        }
        final String mimeType = contentType.getMimeType();

        if (MediaType.APPLICATION_JSON_UTF8_VALUE.equals(mimeType) || MediaType.TEXT_PLAIN_VALUE.equals(mimeType) || MediaType.TEXT_HTML_VALUE.equals(mimeType) || MediaType.TEXT_XML_VALUE.equals(mimeType)) {
            // JSON or text or xml
            final var content = getContentAsString(entity, charset);
            final var httpResponseString = new HTTPResponseString();
            httpResponseString.setContent(content);
            httpResponseData = httpResponseString;
        } else if (MediaType.IMAGE_GIF_VALUE.equals(mimeType) || MediaType.IMAGE_JPEG_VALUE.equals(mimeType) || MediaType.IMAGE_PNG_VALUE.equals(mimeType)) {
            // image
            final var content = getContentAsImage(entity);
            final var httpResponseBufferedImage = new HTTPResponseBufferedImage();
            httpResponseBufferedImage.setContent(content);
            httpResponseData = httpResponseBufferedImage;
        } else {
            // not managed
            throw new HTTPException("HTTP Unknown content type: '" + contentType + "' MimeType: '" + mimeType + "' for url: " + url);
        }

        return httpResponseData;
    }

    @NonNull
    private BufferedImage getContentAsImage(@NonNull final HttpEntity entity) throws HTTPException {
        BufferedImage result = null;
        try {
            result = AccessController.doPrivileged((PrivilegedExceptionAction<BufferedImage>) () -> ImageIO.read(entity.getContent()));
        } catch (final PrivilegedActionException e) {
            handleException(e, "getContentAsImage");
        }

        assert result != null;
        return result;
    }

    @NonNull
    private String getContentAsString(@NonNull final HttpEntity entity, @NonNull final Charset charset) {
        String result = "";
        try (final BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), charset))) {
            var line = "";
            final var sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            result = sb.toString();
        } catch (final IllegalStateException | IOException e) {
            LOGGER.logp(Level.SEVERE, getClass().getName(), "getContent", e.getMessage(), e);
        }

        return result;
    }

    @NonNull
    private HttpClient getHttpClient(@NonNull final HTTPRequestData httpRequestData) {
        final var httpclient = HttpClients.custom();
        if (httpRequestData.isSSl() && httpRequestData.isAllowSelfSignedCertificate()) {
            // this code is to allow self-signed certificates
            try {
                // add ssl if necessary
                // Now put the trust manager into an SSLContext.
                // Supported: SSL, SSLv2, SSLv3, TLS, TLSv1, TLSv1.1
                final SSLContext sslContext = SSLContextBuilder
                        .create()
                        .loadTrustMaterial(new TrustSelfSignedStrategy())
                        .build();

                final HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

                // Register our new socket factory with the typical SSL port and the
                // correct protocol name.
                final SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

                httpclient.setSSLSocketFactory(connectionFactory);
            } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                LOGGER.logp(Level.SEVERE, getClass().getName(), "getHttpClient", e.getMessage(), e);
            }
        }

        return httpclient.build();
    }

    @NonNull
    private List<@NonNull NameValuePair> getParamsToUrl(@NonNull final HTTPRequestData httpRequestData) {
        final var params = new LinkedList<@NonNull NameValuePair>();
        final var requestParams = httpRequestData.getParams();
        for (final Entry<String, Object> entry : requestParams.entrySet()) {
            final var name = entry.getKey();
            final var param = entry.getValue();

            params.add(new BasicNameValuePair(name, param.toString()));
        }

        return params;
    }

    // Create a trust manager that does not validate certificate chains
    private final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // Don't do anything.
            return null;
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) {
            // Don't do anything.
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) {
            // Don't do anything.
        }
    } };
}
