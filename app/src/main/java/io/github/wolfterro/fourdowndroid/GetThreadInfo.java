/*
MIT License

Copyright (c) 2017 Wolfgang Almeida

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package io.github.wolfterro.fourdowndroid;

import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetThreadInfo {

    // Propriedades privadas
    // =====================
    private URL url = null;
    private HttpURLConnection conn = null;
    private InputStream stream = null;
    private String apiURL = "";

    // Propriedades protegidas
    // =======================
    protected String GetThreadInfoStatus = "OK";

    protected int NumPosts = 0;
    protected int NumFiles = 0;
    protected int NumImages = 0;
    protected int NumVideos = 0;
    protected List<String> Files = new ArrayList<String>();

    protected boolean isArchived = false;

    // Construtor da classe
    // ====================
    public GetThreadInfo(String apiURL) {
        this.apiURL = apiURL;
    }

    // Métodos privados
    // ================

    // Resgatando o nome dos arquivos, número de posts
    // e se o tópico está arquivado.
    // ===============================================
    public void init() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(this.apiURL).build();
        String res = "";
        try {
            Response response = client.newCall(request).execute();
            res = response.body().string();
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
            GetThreadInfoStatus = "NOT_FOUND_INNACESSIBLE_OR_UNAVAILABLE";
            return;
        }

        JSONObject obj = null;
        JSONArray arr = null;
        try {
            obj = new JSONObject(res);
            arr = obj.getJSONArray("posts");
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            GetThreadInfoStatus = "JSON_EXCEPTION_1";
            return;
        }

        for(int i = 0; i < arr.length(); i++) {
            try {
                if(arr.getJSONObject(i).has("ext")) {
                    String filename = arr.getJSONObject(i).get("tim").toString();
                    String ext = arr.getJSONObject(i).getString("ext");

                    Files.add(filename + ext);
                    NumPosts += 1;
                    NumFiles += 1;
                }
                else {
                    NumPosts += 1;
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
                GetThreadInfoStatus = "JSON_EXCEPTION_2";
                return;
            }
        }

        try {
            if(arr.getJSONObject(0).has("archived")) {
                isArchived = true;
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            GetThreadInfoStatus = "JSON_EXCEPTION_3";
            return;
        }

    }

    // Contando os tipos de arquivos
    // =============================
    public void CountFileTypes() {
        for(int i = 0; i < Files.size(); i++) {
            String ext = "";
            int ii = Files.get(i).lastIndexOf('.');
            if(ii > 0) {
                ext = Files.get(i).substring(ii + 1);
            }

            if(Arrays.asList(GlobalVars.ImageTypes).contains(ext)) {
                NumImages += 1;
            }
            else if(Arrays.asList(GlobalVars.VideoTypes).contains(ext)) {
                NumVideos += 1;
            }

        }
    }
}
