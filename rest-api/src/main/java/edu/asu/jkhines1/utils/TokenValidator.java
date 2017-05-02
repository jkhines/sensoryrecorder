/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.utils;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;

/**
 *
 * @author jkhines
 */
@Controller
public class TokenValidator {
    private String authServerUrl;
    
    private String apiClientId;
    
    private String apiClientSecret;
    
    public TokenValidator() {
    }
    
    public String getAuthServerUrl() {
        return authServerUrl;
    }
    
    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }
    
    public String getApiClientId() {
        return apiClientId;
    }
    
    public void setApiClientId(String apiClientId) {
        this.apiClientId = apiClientId;
    }
    
    public String getApiClientSecret() {
        return apiClientSecret;
    }
    
    public void setApiClientSecret(String apiClientSecret) {
        this.apiClientSecret = apiClientSecret;
    }
    
    public TokenInfo validate(String authorizationHeader, Scope requiredScope) throws UnsupportedEncodingException, IOException {
        
        // Validate the contents of the authorization header.
        if (Strings.isNullOrEmpty(authorizationHeader) || 
            !authorizationHeader.toLowerCase().startsWith("bearer ") ||
                authorizationHeader.split(" ").length < 2) {
            throw new IllegalArgumentException("Invalid authorization header.");
        }
        
        // Validate the token by POSTing it against the introspection endpoint.
        String token = authorizationHeader.split(" ")[1];
        
        HttpPost postMethod = new HttpPost(getAuthServerUrl() + "/introspect?token=" + token);
        postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = "client_id=" + getApiClientId() + "&client_secret=" + getApiClientSecret();
        postMethod.setEntity(new StringEntity(body));

        HttpClient httpClient = new DefaultHttpClient();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String response = httpClient.execute(postMethod, responseHandler);
        
        // Validate the response. Possible responses:
        // Empty string (client doesn't have access to introspection endpoint)
        // {"error":"invalid_client","error_description":"Bad client credentials"}
        // {"active":false}
        // {"active":true,"scope":"dssr:data:create offline_access dssr:device:update dssr:apdevice:read dssr:data:read","expires_at":"2017-02-25T21:14:58+0000","exp":1488057298,"sub":"***REMOVED***","client_id":"***REMOVED***","token_type":"Bearer"}
        TokenInfo tokenInfo;
        if (Strings.isNullOrEmpty(response)) {
            throw new InvalidClassException("No introspection response received.");
        } 
        ObjectMapper mapper = new ObjectMapper();
        tokenInfo = mapper.readValue(response, TokenInfo.class);

        if (!Strings.isNullOrEmpty(tokenInfo.getError())) {
            throw new InvalidObjectException(
                tokenInfo.getError() + " " + tokenInfo.getErrorDescription());
        }

        if (tokenInfo.getActive() != null && !tokenInfo.getActive()) {
            throw new InvalidObjectException("Expired or invalid token.");
        }
        
        // Validate  the token scope.
        if (Strings.isNullOrEmpty(tokenInfo.getScope()) ||
            !Arrays.asList(tokenInfo.getScope().split(" ")).contains(requiredScope.toString())) {
            throw new AccessDeniedException("Missing required scope.");
        }
        
        // Return the introspection results.
        return tokenInfo;
    }
}
