package edu.asu.jkhines1.dssr.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.asu.jkhines1.dssr.models.Device;
import edu.asu.jkhines1.dssr.models.InputData;

public class ApiHelper {
    private static final HttpClient mHttpClient = new DefaultHttpClient();
    private static final ResponseHandler<String> mResponseHandler = new BasicResponseHandler();

    public static String getResponse(String apiUrl, String token) throws IOException {
        HttpGet getMethod = new HttpGet(apiUrl);
        getMethod.setHeader("Authorization", "Bearer " + token);
        String response = mHttpClient.execute(getMethod, mResponseHandler);
        return response;
    }

    public static String saveData(String apiUrl, String token, InputData data) throws IOException {
        HttpPost postMethod = new HttpPost(apiUrl);
        postMethod.setHeader("Authorization", "Bearer " + token);
        ObjectMapper mapper = new ObjectMapper();
        HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(data));
        postMethod.setEntity(entity);
        String response = mHttpClient.execute(postMethod, mResponseHandler);
        return response;
    }

    public static String updateDevice(String apiUrl, String token, Device device) throws IOException {
        HttpPut putMethod = new HttpPut(apiUrl);
        putMethod.setHeader("Authorization", "Bearer " + token);
        ObjectMapper mapper = new ObjectMapper();
        HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(device));
        putMethod.setEntity(entity);
        String response = mHttpClient.execute(putMethod, mResponseHandler);
        return response;
    }
}