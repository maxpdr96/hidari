package com.hidarisoft.hidari.srcs.pt.animesOnlineNz;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.hidarisoft.hidari.srcs.IDownloadVideo;
import com.hidarisoft.hidari.utils.Constants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimesOnlineNz implements IDownloadVideo {
    private static final String NAME = "Animes Online Nz";
    private static final String BASE_URL = "https://animesonline.nz";

    @Override
    public void execute(String url) throws IOException {
        try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45)) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            Document docCustomConn = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .get();

            webClient.getPage(docCustomConn.select("#source-player-1 a").attr("href"));

            webClient.waitForBackgroundJavaScript(5000);

            var updatedPageContent = webClient.getCurrentWindow().getEnclosedPage();

            System.out.println(updatedPageContent.getWebResponse().getContentAsString());

            Pattern pattern = Pattern.compile("<iframe\\s+src=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(updatedPageContent.getWebResponse().getContentAsString());

            if (matcher.find()) {
                String srcValue = matcher.group(1);
                System.out.println("Value of the src attribute: " + srcValue);
                Document sadTest = Jsoup.connect(srcValue)
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                        .get();
                System.out.println(sadTest.html());

                var scriptElements = sadTest.select("script[type=application/ld+json]");

                if (!scriptElements.isEmpty()) {
                    String scriptContent = Objects.requireNonNull(scriptElements.first()).data();

                    String contentUrl = extractContentUrlFromJson(scriptContent);

                    System.out.println("Value of contentUrl: " + contentUrl);
                    downloadVideo(getTitle(docCustomConn), contentUrl, docCustomConn);
                } else {
                    System.out.println("<script> element not found with type=\"application/ld+json\".");
                }
            } else {
                System.out.println("Src attribute not found in the iframe.");
            }
        }
    }

    @Override
    public String getTitle(Object document) {
        if (document instanceof Document document1) {
            return document1.select("#info .data h1").text().replaceAll(" - Animes Online", "");
        }
        return "";
    }

    @Override
    public void downloadVideo(String title, String ContentUrl, Document document) throws IOException {
        URL url = new URL(ContentUrl);
        InputStream inVideo = url.openStream();

        FileOutputStream out = new FileOutputStream(Constants.DOWNLOAD_FOLDER.getDescription() + title + ".mp4");

        byte[] buffer = new byte[1024];
        int len;
        System.out.println("Episode download start: " + title);
        while ((len = inVideo.read(buffer, 0, 1024)) != -1) {
            out.write(buffer, 0, len);
        }
        System.out.println("End");
        inVideo.close();
        out.close();

    }

    private static String extractContentUrlFromJson(String scriptContent) {
        String regex = "\"contentUrl\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(scriptContent);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
