package com.common.stdlib.network;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import kotlin.text.Charsets;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by xzw on 16/9/6.
 */
public class RequestParams extends RequestBody {
    private JsonObject json;
    private HashMap<String, Object> params;
    private static Type TYPE = Type.FORM_URL_ENCODED;

    private RequestParams(@NonNull HashMap<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("params must not be null...");
        }
        this.params = params;
        this.json = fromMap(params);
    }

    enum Type {
        FORM_URL_ENCODED, JSON
    }

    /**
     * @param params
     * @return RequestBody
     */
    public static RequestParams jsonBody(@NonNull HashMap<String, Object> params) {
        TYPE = Type.JSON;
        return new RequestParams(params);
    }

    /**
     * @param params
     * @return RequestBody
     */
    public static RequestParams body(@NonNull HashMap<String, Object> params) {
        return new RequestParams(params);
    }


    public RequestParams append(String name, String value) {
        if (TYPE == Type.JSON) {
            json.addProperty(name, value);
        } else {
            params.put(name, value);
        }
        return this;
    }

    @Override
    public MediaType contentType() {
        return TYPE == Type.JSON ? MediaType.parse("application/json") : MediaType.parse("application/x-www-form-urlencoded");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        StringBuilder sb = encode();
        sink.write(sb.toString().getBytes(Charsets.UTF_8));
    }

    private StringBuilder encode() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            i++;
            if (i == params.size()) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb;
    }

    private static JsonObject fromMap(Map<String, Object> map) {
        Gson gson = new Gson();
        String json = new Gson().toJson(map);
        JsonObject object = gson.fromJson(json, JsonObject.class);
        return object;
    }

    private static String toJson(Map<String, Object> map) {
        String json = new Gson().toJson(map);
        return json;
    }

    //create a RequestBody directly
    private static RequestBody body(String json) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
        return body;
    }
}
