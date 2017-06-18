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
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Elementos da Activity Principal
    // ===============================
    private EditText editText1;     // URL do tópico
    private EditText editText2;     // Pasta de destino

    private Button button1;         // Verificar Tópico
    private Button button2;         // Download dos Arquivos

    private TextView textView1;     // Posts
    private TextView textView2;     // Arquivos
    private TextView textView3;     // Imagens
    private TextView textView4;     // Vídeos
    private TextView textView5;     // Arquivado

    // Propriedades de classes do aplicativo
    // =====================================
    private List<String> files = null;
    private String id = "";
    private String board = "";
    private List<String> dURLList = null;
    private String chosenDir = "";

    // Confirmação antes de prosseguir com o download dos arquivos
    // -----------------------------------------------------------
    private boolean threadOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = (EditText)findViewById(R.id.editText);
        editText2 = (EditText)findViewById(R.id.editText2);

        button1 = (Button)findViewById(R.id.button);
        button2 = (Button)findViewById(R.id.button2);

        textView1 = (TextView)findViewById(R.id.textView2);
        textView2 = (TextView)findViewById(R.id.textView3);
        textView3 = (TextView)findViewById(R.id.textView4);
        textView4 = (TextView)findViewById(R.id.textView5);
        textView5 = (TextView)findViewById(R.id.textView6);

        // Definindo o local padrão para o salvamento dos arquivos
        // =======================================================
        if(Environment.getExternalStorageState() == null) {
            chosenDir = Environment.getDataDirectory().getAbsolutePath() + "/4DownDroid/";
            editText2.setText(chosenDir);
        }
        else {
            chosenDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/4DownDroid/";
            editText2.setText(chosenDir);
        }

        File mainDir = new File(chosenDir);
        if(mainDir.isDirectory()) {
            System.setProperty("user.dir", chosenDir);
        }
        else {
            mainDir.mkdirs();
            System.setProperty("user.dir", chosenDir);
        }

        // Verificando tópico inserido
        // ===========================
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpando informações sobre o tópico
                // ===================================
                textView1.setText(String.format("%s %s", getString(R.string.posts), ""));
                textView2.setText(String.format("%s %s", getString(R.string.files), ""));
                textView3.setText(String.format("%s %s", getString(R.string.images), ""));
                textView4.setText(String.format("%s %s", getString(R.string.videos), ""));
                textView5.setText(String.format("%s %s", getString(R.string.archived), ""));
                threadOK = false;
                dURLList = null;
                id = "";
                board = "";
                files = null;
                // ====================================

                String apiURL = "";
                FourAPIURL fau = new FourAPIURL(editText1.getText().toString());

                if(fau.FourAPIURLStatus == 0) {
                    if(!fau.getURLHost().equals(GlobalVars.Host)) {
                        String err = String.format("%s", getString(R.string.errorInvalidWebsite));
                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();

                        return;
                    }
                    else {
                        apiURL = fau.getAPIURL();
                    }
                }
                else {
                    String err = String.format("%s %s", getString(R.string.errorRecoveringInformation),
                            fau.FourAPIURLStatus);
                    Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();

                    return;
                }

                // Iniciando resgate de valores do tópico
                // ======================================
                GetThreadInfoThread tit = new GetThreadInfoThread(apiURL);
                tit.start();
                try {
                    tit.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                if(tit.GetThreadInfoStatus.equals("OK")) {
                    textView1.setText(String.format("%s %s", getString(R.string.posts), tit.NumPosts));
                    textView2.setText(String.format("%s %s", getString(R.string.files), tit.NumFiles));
                    textView3.setText(String.format("%s %s", getString(R.string.images), tit.NumImages));
                    textView4.setText(String.format("%s %s", getString(R.string.videos), tit.NumVideos));
                    if(tit.isArchived) {
                        textView5.setText(String.format("%s %s", getString(R.string.archived),
                                getString(R.string.yes)));
                    }
                    else {
                        textView5.setText(String.format("%s %s", getString(R.string.archived),
                                getString(R.string.no)));
                    }

                    files = tit.Files;
                    FourDownloadURL fdu = new FourDownloadURL(apiURL, files);
                    id = fdu.id;
                    board = fdu.board;
                    dURLList = fdu.getDownloadURLList();

                    threadOK = true;
                }
                else {
                    String err = String.format("%s %s",
                            getString(R.string.errorRecoveringInformation),
                            tit.GetThreadInfoStatus);

                    Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // =========================================================================================
        // =========================================================================================

        // Iniciando processo de download após verificar tópico e escolher pasta de destino
        // ================================================================================
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(threadOK) {
                    // Criando ou acessando pasta do tópico
                    // ------------------------------------
                    String threadDirName = String.format("Thread-[%s-%s]", board, id);
                    String tDir = String.format("%s%s", chosenDir, threadDirName);
                    File threadDir = new File(tDir);

                    if(threadDir.isDirectory()) {
                        System.setProperty("user.dir", tDir);
                    }
                    else {
                        threadDir.mkdirs();
                        System.setProperty("user.dir", tDir);
                    }

                    // Iniciando resgate de valores do tópico
                    // ======================================
                    ProgressDialog down = new ProgressDialog(MainActivity.this);

                    down.setTitle(getString(R.string.downloadingFiles));
                    down.setMessage(String.format("%s\n\n%s",
                            getString(R.string.standByWhileFilesAreBeingDownloaded),
                            getString(R.string.thisCanTakeAWhile)));

                    down.setCancelable(false);
                    down.show();

                    DownloadFilesThread dft = new DownloadFilesThread(dURLList, tDir, down,
                            MainActivity.this);
                    dft.start();
                }
                else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.errorThreadNeedsToBeInserted),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // =========================================================================================
        // =========================================================================================
    }
}