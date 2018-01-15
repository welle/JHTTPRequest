package aka.jhttprequest.main.constants;

import org.eclipse.jdt.annotation.NonNull;

/**
 * MimeType Constants for HTTP request.
 *
 * @author Charlotte
 */
public final class MimeTypeConstants {
    // Application MimeTypes

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String APPLICATION_JSON = "application/json";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    // Text MimeTypes
    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String TEXT_HTML = "text/html";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String TEXT_XML = "text/xml";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String TEXT_JAVASCRIPT = "text/javascript";

    // Image MimeTypes
    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String IMAGE_GIF = "image/gif";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String IMAGE_JPEG = "image/jpeg";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String IMAGE_BMP = "image/bmp";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String IMAGE_TIFF = "image/tiff";

    /**
     * MimeType {@value}
     */
    @NonNull
    public static final String IMAGE_PNG = "image/png";

    private MimeTypeConstants() {
        // nothing to do
    }
}
