package aka.jhttprequest.main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jdt.annotation.NonNull;

import aka.jhttprequest.main.common.HTTPRequestData;
import aka.jhttprequest.main.common.HTTPResponseBufferedImage;
import aka.jhttprequest.main.common.HTTPResponseData;
import aka.jhttprequest.main.common.HTTPResponseString;
import aka.jhttprequest.main.constants.MimeTypeConstants;
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
    @NonNull
    public HTTPResponseData<@NonNull ?> sendGetRequest(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<@NonNull ?> result = null;

        try {
            result = AccessController.doPrivileged((PrivilegedExceptionAction<HTTPResponseData<@NonNull ?>>) () -> sendGetRequestPrivileged(httpRequestData));
        } catch (final PrivilegedActionException e) {
            final Throwable t = e.getCause();
            if (t instanceof HTTPException) {
                final HTTPException httpException = (HTTPException) e.getException();
                throw httpException;
            } else {
                LOGGER.logp(Level.SEVERE, "HTTPManager", "sendGetRequest", e.getMessage(), e);
                throw new HTTPException(e.getMessage(), e.getCause());
            }
        }

        assert result != null;
        return result;
    }

    @NonNull
    private HTTPResponseData<?> sendGetRequestPrivileged(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<?> result = null;
        final DefaultHttpClient httpclient = getHttpClient(httpRequestData);
        final StringBuilder sb = new StringBuilder();
        final String baseUrl = httpRequestData.getUrl().toString();
        sb.append(baseUrl);
        final List<NameValuePair> params = getParamsToUrl(httpRequestData);
        if (!params.isEmpty()) {
            final String paramString = URLEncodedUtils.format(params, "utf-8");
            if (!baseUrl.endsWith("?")) {
                sb.append('?');
            }
            sb.append(paramString);
        }

        final String url = sb.toString();
        try {
            final HttpGet httpget = new HttpGet(url);

            if (httpRequestData.getHeaders().isEmpty()) {
                httpget.addHeader("Accept", MimeTypeConstants.APPLICATION_JSON);
            } else {
                for (final Entry<@NonNull String, String> header : httpRequestData.getHeaders().entrySet()) {
                    httpget.addHeader(header.getKey(), header.getValue());
                }
            }

            // Create a response handler
            final HttpResponse response = httpclient.execute(httpget);
            if (response == null) {
                throw new HTTPException("No Response from: " + url);
            }
            result = getHTTPResponseData(url, response);
        } catch (final IOException e) {
            LOGGER.logp(Level.SEVERE, "HTTPManager", "sendGetRequestPrivileged", e.getMessage(), e);
            throw new HTTPException("HTTP IOException ERROR  : " + url, e.getCause());
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
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
            final Throwable t = e.getCause();
            if (t instanceof HTTPException) {
                final HTTPException httpException = (HTTPException) e.getException();
                throw httpException;
            } else {
                LOGGER.logp(Level.SEVERE, "HTTPManager", "sendPostRequest", e.getMessage(), e);
                throw new HTTPException(e.getMessage(), e.getCause());
            }
        }

        assert result != null;
        return result;
    }

    @NonNull
    private HTTPResponseData<@NonNull ?> sendPostRequestPrivileged(@NonNull final HTTPRequestData httpRequestData) throws HTTPException {
        HTTPResponseData<@NonNull ?> result = null;
        final DefaultHttpClient httpclient = getHttpClient(httpRequestData);
        final String url = httpRequestData.getUrl().toString();
        try {
            final HttpPost httpPost = new HttpPost(url);

            if (httpRequestData.getHeaders().isEmpty()) {
                httpPost.addHeader("Accept", MimeTypeConstants.APPLICATION_JSON);
                httpPost.addHeader("Content-Type", MimeTypeConstants.APPLICATION_X_WWW_FORM_URLENCODED);
            } else {
                for (final Entry<@NonNull String, String> header : httpRequestData.getHeaders().entrySet()) {
                    httpPost.addHeader(header.getKey(), header.getValue());
                }
            }
            final HttpEntity typeParametrizedClass = httpRequestData.getTypeParameterClass();
            if (typeParametrizedClass == null) {
                final List<NameValuePair> params = getParamsToUrl(httpRequestData);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            } else if (typeParametrizedClass instanceof UrlEncodedFormEntity) {
                final List<NameValuePair> params = getParamsToUrl(httpRequestData);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            } else {
                httpPost.setEntity(typeParametrizedClass);
            }
            // Create a response handler
            final HttpResponse response = httpclient.execute(httpPost);
            if (response == null) {
                throw new HTTPException("No Response from: " + url);
            }
            result = getHTTPResponseData(url, response);
        } catch (final IOException e) {
            LOGGER.logp(Level.SEVERE, "HTTPManager", "sendPostRequestPrivileged", e.getMessage(), e);
            throw new HTTPException("HTTP IOException ERROR  : " + url, e.getCause());
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }

        return result;
    }

    @NonNull
    private HTTPResponseData<@NonNull ?> getHTTPResponseData(@NonNull final String url, @NonNull final HttpResponse response) throws HTTPException {
        final StatusLine statusLine = response.getStatusLine();
        final int code = statusLine.getStatusCode();
        if (statusLine.getStatusCode() >= 300) {
            throw new HTTPException(code, "HTTP ERROR " + code + " : " + url, new Throwable(statusLine.getReasonPhrase()));
        }
        final HttpEntity entityResponse = response.getEntity();
        if (entityResponse == null) {
            throw new HTTPException("Response contains no content");
        }
        final HTTPResponseData<@NonNull ?> result = getHTTPResponseData(entityResponse, url);
        return result;
    }

    @NonNull
    private HTTPResponseData<@NonNull ?> getHTTPResponseData(@NonNull final HttpEntity entity, @NonNull final String url) throws HTTPException {
        HTTPResponseData<@NonNull ?> httpResponseData = null;
        final ContentType contentType = ContentType.get(entity);

        Charset charset = contentType.getCharset();
        if (charset == null) {
            charset = Charset.defaultCharset();
            assert charset != null;
        }
        final String mimeType = contentType.getMimeType();

        if (MimeTypeConstants.APPLICATION_JSON.equals(mimeType) || MimeTypeConstants.TEXT_PLAIN.equals(mimeType) || MimeTypeConstants.TEXT_HTML.equals(mimeType) || MimeTypeConstants.TEXT_JAVASCRIPT.equals(mimeType) || MimeTypeConstants.TEXT_XML.equals(mimeType)) {
            // JSON or text or xml
            final String content = getContentAsString(entity, charset);
            final HTTPResponseString httpResponseString = new HTTPResponseString();
            httpResponseString.setContent(content);
            httpResponseData = httpResponseString;
        } else if (MimeTypeConstants.IMAGE_BMP.equals(mimeType) || MimeTypeConstants.IMAGE_GIF.equals(mimeType) || MimeTypeConstants.IMAGE_JPEG.equals(mimeType) || MimeTypeConstants.IMAGE_PNG.equals(mimeType) || MimeTypeConstants.IMAGE_TIFF.equals(mimeType)) {
            // image
            final BufferedImage content = getContentAsImage(entity);
            final HTTPResponseBufferedImage httpResponseBufferedImage = new HTTPResponseBufferedImage();
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
            final Throwable t = e.getCause();
            if (t instanceof HTTPException) {
                final HTTPException httpException = (HTTPException) e.getException();
                throw httpException;
            } else {
                LOGGER.logp(Level.SEVERE, "HTTPManager", "getContentAsImage", e.getMessage(), e);
                throw new HTTPException(e.getMessage(), e.getCause());
            }
        }

        assert result != null;
        return result;
    }

    @NonNull
    private String getContentAsString(@NonNull final HttpEntity entity, @NonNull final Charset charset) {
        String result = "";
        try (final BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), charset))) {

            String line = "";
            final StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            result = sb.toString();

        } catch (final IllegalStateException e) {
            LOGGER.logp(Level.SEVERE, "HTTPManager", "getContent", e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.logp(Level.SEVERE, "HTTPManager", "getContent", e.getMessage(), e);
        }

        return result;
    }

    @NonNull
    private DefaultHttpClient getHttpClient(@NonNull final HTTPRequestData httpRequestData) {
        final DefaultHttpClient httpclient = new DefaultHttpClient();

        if (httpRequestData.isSSl() && httpRequestData.isAllowSelfSignedCertificate()) {
            // this code is to allow self-signed certificates
            try {
                // add ssl if necessary
                // Now put the trust manager into an SSLContext.
                // Supported: SSL, SSLv2, SSLv3, TLS, TLSv1, TLSv1.1
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, this.trustAllCerts, new SecureRandom());
                final SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                // Register our new socket factory with the typical SSL port and the
                // correct protocol name.
                final Scheme httpsScheme = new Scheme("https", 443, sf);
                httpclient.getConnectionManager().getSchemeRegistry().register(httpsScheme);

            } catch (final KeyManagementException e) {
                LOGGER.logp(Level.SEVERE, "HTTPManager", "getHttpClient", e.getMessage(), e);
            } catch (final NoSuchAlgorithmException e) {
                LOGGER.logp(Level.SEVERE, "HTTPManager", "getHttpClient", e.getMessage(), e);
            }
        }

        return httpclient;
    }

    @NonNull
    private List<@NonNull NameValuePair> getParamsToUrl(@NonNull final HTTPRequestData httpRequestData) {
        final List<@NonNull NameValuePair> params = new LinkedList<>();
        final Map<String, Object> requestParams = httpRequestData.getParams();
        for (final Entry<String, Object> entry : requestParams.entrySet()) {
            final String name = entry.getKey();
            final Object param = entry.getValue();

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
