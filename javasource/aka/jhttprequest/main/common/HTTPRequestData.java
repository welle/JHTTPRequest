package aka.jhttprequest.main.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author Charlotte
 */
public final class HTTPRequestData {

    @NonNull
    private final URL url;
    @NonNull
    private final Map<@NonNull String, Object> params = new HashMap<>();
    private boolean allowSelfSignedCertificate = false;

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
     * Add parameter to the request.
     *
     * @param name
     * @param param
     */
    public final void addParams(@NonNull final String name, @NonNull final Object param) {
        this.params.put(name, param);
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

}
