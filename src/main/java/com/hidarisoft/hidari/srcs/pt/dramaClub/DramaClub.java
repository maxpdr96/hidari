package com.hidarisoft.hidari.srcs.pt.dramaClub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hidarisoft.hidari.srcs.IDownloadVideo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DramaClub implements IDownloadVideo {
    @Override
    public void execute(String url) throws IOException {
        Document docCustomConn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .get();
        var episodes = docCustomConn.select(".episodios .episodiotitle");
        var titleDorama = getTitle(docCustomConn);

        episodes.forEach(element -> {
            var singleEpisodes = element.select("a").attr("href");
            var nameEpisode = element.select("a").text().replaceAll("ó", "o");
            nameEpisode = titleDorama + " " + nameEpisode;
            try {
                downloadVideo(nameEpisode, singleEpisodes, docCustomConn);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String getTitle(Object url) {
        if (url instanceof Document document) {
            return document.select(".data h1").text();
        }
        return "";

    }

    @Override
    public void downloadVideo(String title, String url, Document document) throws IOException {
        Document docCustomConn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .get();

        var urlVideo = docCustomConn.select(".pframe iframe").get(0).attr("src");

        docCustomConn = Jsoup.connect(urlVideo)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .get();

        Elements scripts = docCustomConn.select("script");

        for (Element script : scripts) {
            String scriptContent = script.html();

            if (scriptContent.contains("file")) {
                scriptContent = scriptContent.replaceAll("var jw =", "");
                ObjectMapper objectMapper = new ObjectMapper();
                var doramaData = objectMapper.readValue(scriptContent, DramaClubDataDTO.class);
                if (!doramaData.file().isBlank()) {
                    System.out.println("Dorama file: " + doramaData.file());
                    URL url1 = new URL(doramaData.file());

                    InputStream inVideo = url1.openStream();
                    FileOutputStream out = new FileOutputStream(title + ".mp4");

                    byte[] buffer = new byte[1024];
                    int len;
                    System.out.println("Start download: " + title);
                    while ((len = inVideo.read(buffer, 0, 1024)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    System.out.println("fim");
                    inVideo.close();
                    out.close();
                    break;
                }
            }
        }
    }
}
