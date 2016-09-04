package aka.jhttprequest.main.common;

import java.awt.image.BufferedImage;

import org.eclipse.jdt.annotation.NonNull;

/**
 * HTTP response of type BufferedImage.
 *
 * @author Charlotte
 */
public final class HTTPResponseBufferedImage implements HTTPResponseData<@NonNull BufferedImage> {

    private BufferedImage content;

    @Override
    @NonNull
    public BufferedImage getContent() {
        final BufferedImage result = this.content;
        assert result != null;
        return result;
    }

    @Override
    public void setContent(@NonNull final BufferedImage content) {
        this.content = content;
    }

}
