package aka.jhttprequest.main.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Charlotte
 */
public final class HTTPRequestData {

    @NonNull
    private final URL url;
    @NonNull
    private final Map<@NonNull String, Object> params = new HashMap<>();
    @NonNull
    private final Map<@NonNull String, String> headers = new HashMap<>();
    private boolean allowSelfSignedCertificate = false;
    @Nullable
    private HttpEntity typeParameterClass = null;

    /**
     * Constructor.
     *
     * @param url URL to request
     * @throws MalformedURLException
     */
    public HTTPRequestData(@NonNull final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * Constructor.
     *
     * @param url URL to request
     * @param typeParameterClass type of HTTPEntity to use.
     * @throws MalformedURLException
     */
    public HTTPRequestData(@NonNull final String url, @NonNull final HttpEntity typeParameterClass) throws MalformedURLException {
        this(url);
        this.typeParameterClass = typeParameterClass;
    }

    /**
     * Add parameter to the request.
     *
     * @param name
     * @param param
     */
    public final void addParams(@NonNull final String name, @NonNull final Object param) {
        this.params.put(name, param);
    }

    /**
     * Add header to the request.
     *
     * @param name
     * @param header
     */
    public final void addHeader(@NonNull final String name, @NonNull final String header) {
        this.headers.put(name, header);
    }

    /**
     * Get URL.
     *
     * @return url String
     */
    @NonNull
    public final URL getUrl() {
        final URL result = this.url;
        return result;
    }

    /**
     * Is the current URL in SSL ?
     *
     * @return <code>true</code> if URL is using SSL
     */
    public final boolean isSSl() {
        return this.url.getProtocol().equalsIgnoreCase("https");
    }

    /**
     * Get parameters for the request.
     *
     * @return the params
     */
    @NonNull
    public Map<@NonNull String, Object> getParams() {
        final Map<@NonNull String, Object> result = Collections.unmodifiableMap(this.params);
        return result;
    }

    /**
     * Get headers for the request.
     *
     * @return the headers
     */
    @NonNull
    public Map<@NonNull String, String> getHeaders() {
        final Map<@NonNull String, String> result = Collections.unmodifiableMap(this.headers);
        return result;
    }

    /**
     * Allow self certificate SSL for this request.
     *
     * @param allowSelfSignedCertificate
     */
    public void setAllowSelfSignedCertificate(final boolean allowSelfSignedCertificate) {
        this.allowSelfSignedCertificate = allowSelfSignedCertificate;
    }

    /**
     * Is self signed certificate ?
     *
     * @return <code>true</code> if request permit self signed SSL certificate
     */
    public boolean isAllowSelfSignedCertificate() {
        return this.allowSelfSignedCertificate;
    }

    /**
     * Get HTTPEntity type.
     *
     * @return the typeParameterClass
     */
    @Nullable
    public final HttpEntity getTypeParameterClass() {
        return this.typeParameterClass;
    }

}
