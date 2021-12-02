package com.arun.cs225.sixfeettracing.api;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VolleyImpl {

    int httpMethod;
    private String url;
    private Context context;

    public VolleyImpl(Context context, int httpMethod, String url) {
        this.context = context;
        this.httpMethod = httpMethod;
        this.url = url;
    }

    public static void nuke() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }
    public void postJSONArray(String data){

        nuke();
        if(data == null)
        {
            return;
        }
        JSONArray obj = null;
        try {
            obj = new JSONArray(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONArray finalObj = obj;
        StringRequest stringRequest = new StringRequest(httpMethod, url, response -> {
            Toast.makeText(context, "Response = "+response,
                    Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(context, "Error = "+error,
                    Toast.LENGTH_SHORT).show();
        }) {
            public byte[] getBody() {
                try {
                    return finalObj.toString().getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }}
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }
    public void postJSONObject(String data){
        nuke();
        if(data == null)
        {
            return;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONObject finalObj = obj;
        StringRequest stringRequest = new StringRequest(httpMethod, url, response -> {
            Toast.makeText(context, "Response = "+response,
                    Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(context, "Error = "+error,
                    Toast.LENGTH_SHORT).show();
        }) {
            public byte[] getBody() {
                try {
                    return finalObj.toString().getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }}
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }
}
