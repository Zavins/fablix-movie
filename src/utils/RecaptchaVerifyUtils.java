package utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import constants.RecaptchaConstants;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class RecaptchaVerifyUtils {

    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static String verify(String gRecaptchaResponse, String platform) throws Exception {
        URL verifyUrl = new URL(SITE_VERIFY_URL);

        // Open Connection to URL
        HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();

        // Add Request Header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("models.User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String secretKey = (platform != null && platform.equals("mobile")) ? RecaptchaConstants.SECRET_KEY_MOBILE : RecaptchaConstants.SECRET_KEY_WEB;
        // Data will be sent to the server.
        String postParams = "secret=" + secretKey + "&response=" + gRecaptchaResponse;

        // Send Request
        conn.setDoOutput(true);

        // Get the output stream of Connection
        // Write data in this stream, which means to send data to Server.
        OutputStream outStream = conn.getOutputStream();
        outStream.write(postParams.getBytes());

        outStream.flush();
        outStream.close();

        // Get the InputStream from Connection to read data sent from the server.
        InputStream inputStream = conn.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);

        inputStreamReader.close();

        if (jsonObject.get("success").getAsBoolean()) {
            // verification succeed
            return "success";
        }

        return "recaptcha verification failed: response is " + jsonObject;
    }

}
