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
 * Created by Wolfterro on 16/06/2017.
 */

import java.util.*;
import java.net.*;

public class FourDownloadURL {

    // Propriedades protegidas
    // =======================
    protected List<String> dURLList = new ArrayList<String>();
    protected String id = "";
    protected String board = "";

    // Propriedades privadas
    // =====================
    private String tURL = "";
    private List<String> files = null;

    // Construtor da classe
    // ====================
    public FourDownloadURL(String tURL, List<String> files) {
        this.tURL = tURL;
        this.files = files;

        getBoard();
        if(!board.equals("")) {
            assembleDownloadURL();
        }
    }

    // Métodos privados
    // ================
    private void getBoard() {
        URL u = null;
        try {
            u = new URL(tURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String path = u.getPath();
        String[] pathSlice = path.split("/");

        if(pathSlice.length > 3) {
            board = pathSlice[1];
            id = pathSlice[3].replace(".json", "");
        }
    }

    // Montando as URLs de download
    // ============================
    private void assembleDownloadURL() {
        for(int i = 0; i < files.size(); i++) {
            dURLList.add(String.format("%s/%s/%s", GlobalVars.dURL, board, files.get(i)));
        }
    }

    // Métodos públicos
    // ================

    // Resgatando a lista de URLs para download
    // ========================================
    public List<String> getDownloadURLList() {
        return dURLList;
    }

}
