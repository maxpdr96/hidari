package com.hidarisoft.hidari.srcs;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface IDownloadVideo {

    void execute(String url) throws IOException;
    String getTitle(Object url);
    void downloadVideo(String title, String url, Document document) throws IOException;
}
