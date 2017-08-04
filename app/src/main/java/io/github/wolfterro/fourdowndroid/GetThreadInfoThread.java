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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wolfterro on 15/06/2017.
 */

public class GetThreadInfoThread extends Thread {

    // Propriedades privadas
    // =====================
    private String apiURL = "";
    private Context c = null;
    private ProgressDialog pd = null;

    // Propriedades protegidas
    // =======================
    protected String GetThreadInfoStatus = "";

    protected int NumPosts = 0;
    protected int NumFiles = 0;
    protected int NumImages = 0;
    protected int NumVideos = 0;
    protected List<String> Files = new ArrayList<String>();

    protected boolean isArchived = false;

    protected String id = "";
    protected String board = "";
    protected List<String> dURLList = null;
    protected boolean threadOK = false;

    // Construtor da classe
    // ====================
    public GetThreadInfoThread(String apiURL, Context c, ProgressDialog pd) {
        this.apiURL = apiURL;
        this.c = c;
        this.pd = pd;
    }

    // Métodos públicos da classe
    // ==========================
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

    // Resgatando a lista de arquivos
    // ------------------------------
    public List<String> getFiles() {
        return Files;
    }

    // Resgatando ID do tópico
    // -----------------------
    public String getID() {
        return id;
    }

    // Resgatando ID do tópico
    // -----------------------
    public String getBoard() {
        return board;
    }

    // Resgatando a lista de arquivos
    // ------------------------------
    public List<String> getDURLList() {
        return dURLList;
    }

    // Resgatando ID do tópico
    // -----------------------
    public boolean getThreadOK() {
        return threadOK;
    }

    // Métodos privados da classe
    // ==========================

    // Atualizando informações sobre o tópico
    // ======================================
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            TextView textView1 = (TextView) ((Activity)c).findViewById(R.id.textViewPostsValue);
            TextView textView2 = (TextView) ((Activity)c).findViewById(R.id.textViewFilesValue);
            TextView textView3 = (TextView) ((Activity)c).findViewById(R.id.textViewImagesValue);
            TextView textView4 = (TextView) ((Activity)c).findViewById(R.id.textViewVideosValue);
            TextView textView5 = (TextView) ((Activity)c).findViewById(R.id.textViewArchivedValue);

            // Atualizando resultado na activity principal
            // ===========================================
            if(GetThreadInfoStatus.equals("OK")) {
                textView1.setText(String.format("%s", NumPosts));
                textView2.setText(String.format("%s", NumFiles));
                textView3.setText(String.format("%s", NumImages));
                textView4.setText(String.format("%s", NumVideos));
                if(isArchived) {
                    textView5.setText(String.format("%s", c.getString(R.string.yes)));
                }
                else {
                    textView5.setText(String.format("%s", c.getString(R.string.no)));
                }

                FourDownloadURL fdu = new FourDownloadURL(apiURL, Files);
                id = fdu.id;
                board = fdu.board;
                dURLList = fdu.getDownloadURLList();

                threadOK = true;
            }
            else {
                String err = String.format("%s %s",
                        c.getString(R.string.errorRecoveringInformation),
                        GetThreadInfoStatus);

                Toast.makeText(c, err, Toast.LENGTH_SHORT).show();
                threadOK = false;
            }

            pd.dismiss();
        }
    };
}
