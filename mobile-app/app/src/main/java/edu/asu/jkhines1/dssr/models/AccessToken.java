package edu.asu.jkhines1.dssr.models;

import android.util.Base64;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * Created by jkhines on 3/2/17.
 */
// {"sub":"john.k.hines@asu.edu",
// "azp":"client_id",
// "iss":"HTTPS:\/\/auth.jkhines-dev.xyz\/openid-connect-server-webapp\/",
// "exp":1488504488,
// "iat":1488500888,
// "jti":"5adb78ba-aafd-4b51-a3c2-ab16c77c94ae"}
public class AccessToken {
    private String authorizedPresenter;
    private Long expiresAt;
    private Long issuedAt;
    private String issuer;
    private String jwtId;
    private String subject;

    public AccessToken() {
    }

    @JsonProperty("sub")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonProperty("azp")
    public String getAuthorizedPresenter() {
        return authorizedPresenter;
    }

    public void setAuthorizedPresenter(String authorizedPresenter) {
        this.authorizedPresenter = authorizedPresenter;
    }

    @JsonProperty("exp")
    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @JsonIgnore
    public static AccessToken getInstance(String encodedAccessToken) throws IOException {
        // Decode the string.
        String tokenBody = encodedAccessToken.split("\\.")[1];
        tokenBody = new String(Base64.decode(tokenBody, Base64.DEFAULT), "UTF-8");
        // Deserialize the string.
        ObjectMapper mapper = new ObjectMapper();
        AccessToken accessToken = mapper.readValue(tokenBody, AccessToken.class);
        return accessToken;
    }

    @JsonProperty("iat")
    public Long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    @JsonProperty("iss")
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @JsonProperty("jti")
    public String getJwtId() {
        return jwtId;
    }

    public void setJwtId(String jwtId) {
        this.jwtId = jwtId;
    }

    public static Boolean needsRefresh(String encodedAccessToken) {
        Boolean needsRefresh = true;

        try {
            AccessToken accessToken = AccessToken.getInstance(encodedAccessToken);
            Long currentEpochTime = (Calendar.getInstance().getTimeInMillis() / 1000);
            needsRefresh = (currentEpochTime >= accessToken.getExpiresAt());
        } catch (Exception ex) {
        }

        return needsRefresh;
    }
}
