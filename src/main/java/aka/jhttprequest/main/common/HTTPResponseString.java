package aka.jhttprequest.main.common;

import org.eclipse.jdt.annotation.NonNull;

/**
 * HTTP request response of type String.
 *
 * @author Charlotte
 */
public final class HTTPResponseString implements HTTPResponseData<@NonNull String> {

    private String content;

    @Override
    @NonNull
    public String getContent() {
        final var result = this.content;
        assert result != null;
        return result;
    }

    @Override
    public void setContent(@NonNull final String content) {
        this.content = content;
    }
}
