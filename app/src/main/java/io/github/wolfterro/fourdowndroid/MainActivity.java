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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Elementos da Activity Principal
    // ===============================
    private EditText editText1;     // URL do tópico

    private Button button1;         // Verificar Tópico
    private Button button2;         // Download dos Arquivos

    private CheckBox checkBox;      // Substituir arquivos existentes

    private TextView textView1;     // Posts
    private TextView textView2;     // Arquivos
    private TextView textView3;     // Imagens
    private TextView textView4;     // Vídeos
    private TextView textView5;     // Arquivado
    private TextView textView6;     // Pasta de destino

    // Propriedades de classes do aplicativo
    // =====================================
    protected String chosenDir = "";
    protected final int PERMISSION_GRANTED_VALUE = 0;

    // Confirmação antes de prosseguir com o download dos arquivos
    // -----------------------------------------------------------
    private GetThreadInfoThread tit = null;

    // Menu de opções da activity principal
    // ====================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Selecionando opções no menu da activity principal
    // =================================================
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent about = new Intent(MainActivity.this, AboutActivity.class);
                MainActivity.this.startActivity(about);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Criando activity principal do aplicativo
    // ========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = (EditText)findViewById(R.id.editText);

        button1 = (Button)findViewById(R.id.button);
        button2 = (Button)findViewById(R.id.button2);

        checkBox = (CheckBox)findViewById(R.id.checkBox);

        textView1 = (TextView)findViewById(R.id.textViewPostsValue);
        textView2 = (TextView)findViewById(R.id.textViewFilesValue);
        textView3 = (TextView)findViewById(R.id.textViewImagesValue);
        textView4 = (TextView)findViewById(R.id.textViewVideosValue);
        textView5 = (TextView)findViewById(R.id.textViewArchivedValue);
        textView6 = (TextView)findViewById(R.id.textViewOutputDirValue);

        // Pedindo permissão de acesso ao armazenamento do aparelho para o usuário
        // =======================================================================
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_GRANTED_VALUE);
        }

        // Definindo o local padrão para o salvamento dos arquivos
        // =======================================================
        if(Environment.getExternalStorageState() == null) {
            chosenDir = Environment.getDataDirectory().getAbsolutePath() + "/4DownDroid/";
            textView6.setText(chosenDir);
        }
        else {
            chosenDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/4DownDroid/";
            textView6.setText(chosenDir);
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
                ProgressDialog pd = new ProgressDialog(MainActivity.this);

                pd.setTitle(getString(R.string.obtainingInformations));
                pd.setMessage(getString(R.string.pleaseWait));
                pd.setCancelable(false);
                pd.show();

                // Limpando informações sobre o tópico
                // ===================================
                textView1.setText(getString(R.string.notAvailable));
                textView2.setText(getString(R.string.notAvailable));
                textView3.setText(getString(R.string.notAvailable));
                textView4.setText(getString(R.string.notAvailable));
                textView5.setText(getString(R.string.notAvailable));
                // ====================================

                String apiURL = "";
                FourAPIURL fau = new FourAPIURL(editText1.getText().toString());

                if(fau.FourAPIURLStatus == 0) {
                    if(!fau.getURLHost().equals(GlobalVars.Host)) {
                        String err = String.format("%s", getString(R.string.errorInvalidWebsite));
                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();
                        pd.dismiss();

                        return;
                    }
                    else {
                        apiURL = fau.getAPIURL();
                    }
                }
                else {
                    String err = String.format("%s %s",
                            getString(R.string.errorRecoveringInformation),
                            fau.FourAPIURLStatus);
                    Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                    return;
                }

                // Iniciando resgate de valores do tópico
                // ======================================
                tit = new GetThreadInfoThread(apiURL, MainActivity.this, pd);
                tit.start();
            }
        });

        // =========================================================================================
        // =========================================================================================

        // Iniciando processo de download após verificar tópico e escolher pasta de destino
        // ================================================================================
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tit != null) {
                    if(tit.getThreadOK()) {
                        // Criando ou acessando pasta do tópico
                        // ------------------------------------
                        String threadDirName = String.format("Thread-[%s-%s]",
                                tit.getBoard(),
                                tit.getID());

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

                        DownloadFilesThread dft = new DownloadFilesThread(tit.getDURLList(),
                                checkBox.isChecked(),
                                tDir,
                                down,
                                MainActivity.this);
                        dft.start();
                    }
                    else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.errorThreadNeedsToBeInserted),
                                Toast.LENGTH_SHORT).show();
                    }
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

    // =============================================================================================
    // =============================================================================================

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                            int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_GRANTED_VALUE: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Nada por enquanto
                }
                else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.errorNoPermissionGiven),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}