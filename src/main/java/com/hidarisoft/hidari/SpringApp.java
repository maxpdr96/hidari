package com.hidarisoft.hidari;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringApp {

    public static void main(String[] args) throws IOException {
        LogManager.getLogManager().reset();

        try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45)) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            Document docCustomConn = Jsoup.connect("url")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .get();

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

                // Verifica se há elementos de script
                if (!scriptElements.isEmpty()) {
                    // Obtém o conteúdo do script
                    String scriptContent = Objects.requireNonNull(scriptElements.first()).data();

                    // Extrai o valor do atributo contentUrl do JSON
                    String contentUrl = extractContentUrlFromJson(scriptContent);

                    System.out.println("Value of contentUrl: " + contentUrl);
                    downloadVideo(contentUrl, docCustomConn);
                } else {
                    System.out.println("<script> element not found with type=\"application/ld+json\".");
                }


            } else {
                System.out.println("Src attribute not found in the iframe.");
            }
        }
    }

    private static void downloadVideo(String contentUrl, Document docCustomConn) throws IOException {
        URL url = new URL(contentUrl);
        InputStream inVideo = url.openStream();
        var nameAnime = docCustomConn.select("#info .data h1").text().replaceAll(" - Animes Online", "");
        FileOutputStream out = new FileOutputStream(nameAnime + ".mp4");

        byte[] buffer = new byte[1024];
        int len;
        System.out.println("Episode download start: " + nameAnime);
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