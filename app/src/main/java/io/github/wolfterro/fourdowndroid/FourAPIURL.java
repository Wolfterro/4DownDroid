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

/**
 * Created by Wolfterro on 15/06/2017.
 */

import android.util.Log;

import java.net.*;

public class FourAPIURL {
    // Propriedades protegidas
    // =======================
    protected int FourAPIURLStatus = 0;

    protected String board = "";
    protected String threadNum = "";
    protected String host = "";
    protected String apiURL = "";

    // Construtor da classe
    // ====================
    public FourAPIURL(String board, String threadNum) {
        this.board = board;
        this.threadNum = threadNum;
        convertURL();
    }

    // Métodos privados
    // ================

    // Convertendo a URL do tópico para a URL da API
    // =============================================
    private void convertURL() {
        URL u = null;
        String urlAssembled = assembleUrl();

        try {
            u = new URL(urlAssembled);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        host = u.getHost();

        if((u.getPath().split("/")).length > 4) {
            String path = "";
            String[] sPath = u.getPath().split("/");
            for(int i = 1; i < sPath.length - 1; i++) {
                path += "/" + sPath[i];
            }
            apiURL = String.format("%s%s%s", GlobalVars.apiURL1, path, GlobalVars.jsonExt);
        }
        else {
            apiURL = String.format("%s%s%s", GlobalVars.apiURL1, u.getPath(), GlobalVars.jsonExt);
        }
        Log.println(Log.INFO, "CHECK THIS THING TOO!", apiURL);
    }

    private String assembleUrl() {
        String[] splittedBoard = this.board.split(" - ");
        String selectedBoard = splittedBoard[0];

        String url = String.format("https://boards.4chan.org%sthread/%s", selectedBoard, this.threadNum);
        Log.println(Log.INFO, "CHECK THIS THING OUT!", url);
        return url;
    }

    // Métodos públicos
    // ================

    // Resgatando a URL da API já processada
    // =====================================
    public String getAPIURL() {
        return apiURL;
    }

    // Resgatando o host da URL inserida
    // =================================
    public String getURLHost() {
        return host;
    }
}
