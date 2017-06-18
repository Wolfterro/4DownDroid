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

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wolfterro on 15/06/2017.
 */

public class GetThreadInfoThread extends Thread {

    // Propriedades privadas
    // =====================
    private String apiURL = "";

    // Propriedades protegidas
    // =======================
    protected String GetThreadInfoStatus = "";

    protected int NumPosts = 0;
    protected int NumFiles = 0;
    protected int NumImages = 0;
    protected int NumVideos = 0;
    protected List<String> Files = new ArrayList<String>();

    protected boolean isArchived = false;

    // Construtor da classe
    // ====================
    public GetThreadInfoThread(String apiURL) {
        this.apiURL = apiURL;
    }

    @Override
    public void run() {
        GetThreadInfo ti = new GetThreadInfo(apiURL);

        ti.init();
        if(ti.GetThreadInfoStatus.equals("OK")) {
            ti.CountFileTypes();
        }

        if (ti.GetThreadInfoStatus.equals("OK")) {
            GetThreadInfoStatus = ti.GetThreadInfoStatus;

            NumPosts = ti.NumPosts;
            NumFiles = ti.NumFiles;
            NumImages = ti.NumImages;
            NumVideos = ti.NumVideos;
            isArchived = ti.isArchived;

            Files = ti.Files;
        }
        else {
            GetThreadInfoStatus = ti.GetThreadInfoStatus;
        }

        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            // =============================
        }
    };
}
