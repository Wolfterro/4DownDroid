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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.*;

/**
 * Created by Wolfterro on 16/06/2017.
 */

public class DownloadFilesThread extends Thread {

    // Propriedades privadas
    // =====================
    private ProgressDialog down = null;
    private Context c = null;
    private String newMessage = "";
    private String downloadMessage = "";
    private boolean replaceFiles = false;

    // Propriedades protegidas
    // =======================
    protected List<String> dURLList = null;
    protected String tDir = "";

    // Construtor da classe
    // ====================
    public DownloadFilesThread(List<String> dURLList,
                               boolean replaceFiles,
                               String tDir,
                               ProgressDialog down,
                               Context c) {

        this.dURLList = dURLList;
        this.replaceFiles = replaceFiles;
        this.tDir = tDir;
        this.down = down;
        this.c = c;
        this.downloadMessage = c.getString(R.string.standByWhileFilesAreBeingDownloaded);
    }

    @Override
    public void run() {
        System.setProperty("user.dir", tDir);

        for(int i = 0; i < dURLList.size(); i++) {
            String filename = "";

            try {
                URL uf = new URL(dURLList.get(i));
                filename = FilenameUtils.getName(uf.getPath());
                File nf = new File(tDir + "/" + filename);

                newMessage = String.format("%s\n\n[%d/%d] %s",
                        downloadMessage, i, dURLList.size(), filename);

                if(!nf.exists()) {
                    updateMsg.sendEmptyMessage(0);
                    FileUtils.copyURLToFile(uf, nf, 10000, 10000);
                }
                else {
                    if(replaceFiles) {
                        updateMsg.sendEmptyMessage(0);
                        FileUtils.copyURLToFile(uf, nf, 10000, 10000);
                    }
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        handler.sendEmptyMessage(0);
    }

    private Handler updateMsg = new Handler() {
        @Override
        public void handleMessage(Message m) {
            down.setMessage(newMessage);
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            down.dismiss();
            String result = String.format("%s %s", c.getString(R.string.filesSavedIn), tDir);
            Toast.makeText(c, result, Toast.LENGTH_SHORT).show();
        }
    };
}
