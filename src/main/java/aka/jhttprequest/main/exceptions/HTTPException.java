package aka.jhttprequest.main.exceptions;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The super class for all exceptions in HTTP request.
 *
 * @since 3.0
 */
public final class HTTPException extends Exception {

    private static final long serialVersionUID = 4810324642979855330L;
    private int code;

    /**
     * Create exception with text/message.
     *
     * @param text text/message
     */
    public HTTPException(@Nullable final String text) {
        super(text);
    }

    /**
     * Create exception with text/message and the cause.
     *
     * @param text text/message
     * @param cause the Throwable that caused the exception
     */
    public HTTPException(@Nullable final String text, @Nullable final Throwable cause) {
        super(text, cause);
    }

    /**
     * Create exception with text/message and the cause.
     *
     * @param code error code HTTP
     * @param text text/message
     * @param cause the Throwable that caused the exception
     */
    public HTTPException(final int code, @Nullable final String text, @Nullable final Throwable cause) {
        super(text, cause);
        this.code = code;
    }

    /**
     * Get the error code of HTTP.
     *
     * @return the code
     */
    public int getCode() {
        return this.code;
    }
}
