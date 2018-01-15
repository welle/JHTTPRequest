package aka.jhttprequest.main.common;

import org.eclipse.jdt.annotation.NonNull;

/**
 * HTTP request response.
 *
 * @author Charlotte
 * @param <T> java type of content (String, BufferedImage)
 */
public interface HTTPResponseData<@NonNull T> {

    /**
     * Get the response content.
     *
     * @return response content
     */
    @NonNull
    public T getContent();

    /**
     * Set the response content.
     *
     * @param content
     */
    public void setContent(@NonNull final T content);
}
