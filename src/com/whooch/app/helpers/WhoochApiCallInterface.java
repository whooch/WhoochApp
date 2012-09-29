package com.whooch.app.helpers;

import org.apache.http.client.methods.HttpRequestBase;

public interface WhoochApiCallInterface {
    public HttpRequestBase getHttpRequest();
    public void handleResponse(String responseString);
    public void postExecute(int statusCode);
}