package aka.jhttprequest.main;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.eclipse.jdt.annotation.NonNull;

import aka.jhttprequest.main.common.HTTPRequestData;
import aka.jhttprequest.main.common.HTTPResponseBufferedImage;
import aka.jhttprequest.main.common.HTTPResponseData;
import aka.jhttprequest.main.common.HTTPResponseString;
import aka.jhttprequest.main.exceptions.HTTPException;

/**
 * JUnitTest for Manager.
 */
public class Manager_JUnitTest {

    /**
     * Test Malformed URL.
     *
     * @throws MalformedURLException
     */
    @org.junit.Test(expected = MalformedURLException.class)
    public void TestMalformedURLException() throws MalformedURLException {
        new HTTPRequestData("This is not an URL.");
        fail("Expected an exception.");
    }

    /**
     * Test correct URL.
     *
     * @throws MalformedURLException
     */
    @org.junit.Test
    public void TestCorrectURL() throws MalformedURLException {
        new HTTPRequestData("https://www.google.be/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");
    }

    /**
     * Test correct URL Image.
     *
     * @throws MalformedURLException
     * @throws HTTPException
     */
    @org.junit.Test
    public void TestCorrectURLImage() throws MalformedURLException, HTTPException {
        final HTTPRequestData httpRequestData = new HTTPRequestData("https://www.google.be/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");
        final HTTPManager httpManager = new HTTPManager();

        final HTTPResponseData<@NonNull ?> response = httpManager.sendGetRequest(httpRequestData);
        assertThat(response, instanceOf(HTTPResponseBufferedImage.class));
    }

    /**
     * Test incorrect URL Image.
     *
     * @throws MalformedURLException
     * @throws HTTPException
     */
    @org.junit.Test
    public void TestIncorrectURLImage() throws MalformedURLException, HTTPException {
        final HTTPRequestData httpRequestData = new HTTPRequestData("https://www.google.be/#hl=fr");
        final HTTPManager httpManager = new HTTPManager();

        final @NonNull HTTPResponseData<@NonNull ?> response = httpManager.sendGetRequest(httpRequestData);
        assertFalse(response instanceof HTTPResponseBufferedImage);
    }

    /**
     * Test correct URL String.
     *
     * @throws MalformedURLException
     * @throws HTTPException
     */
    @org.junit.Test
    public void TestCorrectURLString() throws MalformedURLException, HTTPException {
        final HTTPRequestData httpRequestData = new HTTPRequestData("https://www.google.be/#hl=fr");
        final HTTPManager httpManager = new HTTPManager();

        final @NonNull HTTPResponseData<@NonNull ?> response = httpManager.sendGetRequest(httpRequestData);
        assertThat(response, instanceOf(HTTPResponseString.class));
    }

    /**
     * Test incorrect URL String.
     *
     * @throws MalformedURLException
     * @throws HTTPException
     */
    @org.junit.Test
    public void TestIncorrectURLString() throws MalformedURLException, HTTPException {
        final HTTPManager httpManager = new HTTPManager();
        final HTTPRequestData httpRequestData = new HTTPRequestData("https://www.google.be/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");

        final @NonNull HTTPResponseData<@NonNull ?> response = httpManager.sendGetRequest(httpRequestData);
        assertFalse(response instanceof HTTPResponseString);
    }
}
