package edu.asu.jkhines1.dssr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.core.deps.guava.base.Strings;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.asu.jkhines1.dssr.models.AccessToken;
import edu.asu.jkhines1.dssr.models.Tokens;

/**
 * Created by jkhines on 2/28/17.
 */

public class TokenHelper {
    // Members.
    private static final String SAVED_TOKEN_KEY = "tokenResponse";
    private static final HttpClient mHttpClient = new DefaultHttpClient();
    private static final ResponseHandler<String> mResponseHandler = new BasicResponseHandler();

    public static void deleteTokens(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            String tokenResponseJson = preferences.getString(SAVED_TOKEN_KEY, "");

            if (!Strings.isNullOrEmpty(tokenResponseJson)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(SAVED_TOKEN_KEY);
                editor.commit();
            }
        }
    }

    // Public methods.
    public static final Tokens clientCredentialsFlow(String authServerUrl, String clientId,
                                                     String clientSecret) throws IOException {
        HttpPost postMethod = new HttpPost(authServerUrl + "/token");
        postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials";
        postMethod.setEntity(new StringEntity(body));
        String response = mHttpClient.execute(postMethod, mResponseHandler);

        ObjectMapper mapper = new ObjectMapper();
        Tokens tokens = mapper.readValue(response, Tokens.class);

        return tokens;
    }

    public static Tokens getTokens(Context context) throws IOException {
        Tokens tokens = TokenHelper.getSavedTokens(context);

        // Check if the token needs to be refreshed.
        if (tokens != null && AccessToken.needsRefresh(tokens.getAccessToken())) {
            tokens = refreshAccessToken(ApplicationConfig.AUTH_SERVER_URL + "/token",
                    ApplicationConfig.CLIENT_ID, ApplicationConfig.CLIENT_SECRET,
                    tokens.getRefreshToken());

            saveTokens(context, tokens);
        }

        return tokens;
    }

    public static final String registerUser(String apiServerUrl, String username, char[] password, String token) throws IOException {
        HttpPost postMethod = new HttpPost(apiServerUrl + "/users");
        postMethod.setHeader("Content-Type", "application/json");
        postMethod.setHeader("Authorization", "Bearer " + token);
        char[] body1 = ("{\"username\":\"" + username + "\",\"password\":\"").toCharArray();
        char[] body2 = ("\",\"enabled\":true}").toCharArray();
        int length = body1.length + password.length + body2.length;
        char[] payload = new char[length];
        for (int i = 0; i != body1.length; ++ i) {
            payload[i] = body1[i];
        }
        for (int i = body1.length, j = 0; j != password.length; ++i, ++j) {
            payload[i] = password[j];
        }
        for (int i = body1.length + password.length, j = 0; i != payload.length; ++i, ++j) {
            payload[i] = body2[j];
        }
        String response = "";
        try {
            postMethod.setEntity(new StringEntity(new String(payload)));
            response = mHttpClient.execute(postMethod, mResponseHandler);
        } catch (IOException ex) {
            throw ex;
        } finally {
            for (int i = 0; i != payload.length; ++i) {
                payload[i] = '\u0000'; // Clear the password.
            }
        }

        return response;
    }

    public static final Tokens resourceOwnerPasswordFlow(Context context, String authServerUrl,
                                                         String clientId, String clientSecret,
                                                         String username, char[] password) throws IOException {
        HttpPost postMethod = new HttpPost(authServerUrl + "/token");
        postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
        char[] body = ("client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=password&username=" + username + "&password=").toCharArray();
        int length = body.length + password.length;
        char[] payload = new char[length];
        for (int i = 0; i != body.length; ++i) {
            payload[i] = body[i];
        }
        for (int i = body.length, j = 0; i != payload.length; ++i, ++j) {
            payload[i] = password[j];
        }
        String response = "";
        try {
            postMethod.setEntity(new StringEntity(new String(payload)));
            response = mHttpClient.execute(postMethod, mResponseHandler);
        } catch (IOException ex) {
            throw ex;
        } finally {
            for (int i = 0; i != payload.length; ++i) { // Clear the password.
                payload[i] = '\u0000';
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Tokens tokens = mapper.readValue(response, Tokens.class);

        saveTokens(context, tokens);

        return tokens;
    }

    // Private methods.
    private static Tokens getSavedTokens(Context context) {
        Tokens tokens = null;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            String tokenResponseJson = preferences.getString(SAVED_TOKEN_KEY, "");

            if (!Strings.isNullOrEmpty(tokenResponseJson)) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    tokens = mapper.readValue(tokenResponseJson, Tokens.class);
                } catch (IOException ex) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(SAVED_TOKEN_KEY);
                    editor.commit();
                }
            }
        }

        return tokens;
    }

    private static final Tokens refreshAccessToken(String apiServerUrl, String clientId, String clientSecret, String token) throws IOException {
        HttpPost postMethod = new HttpPost(apiServerUrl);
        postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=refresh_token&refresh_token=" + token;
        postMethod.setEntity(new StringEntity(body));
        String response = mHttpClient.execute(postMethod, mResponseHandler);

        ObjectMapper mapper = new ObjectMapper();
        Tokens tokens = mapper.readValue(response, Tokens.class);
        return tokens;
    }

    private static void saveTokens(Context context, Tokens tokens) throws IOException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        ObjectMapper mapper = new ObjectMapper();
        editor.putString(SAVED_TOKEN_KEY, mapper.writeValueAsString(tokens));
        editor.commit();
    }
}
