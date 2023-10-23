package com.hidarisoft.hidari.srcs.pt.goAnimes;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hidarisoft.hidari.srcs.IDownloadVideo;
import com.hidarisoft.hidari.utils.Constants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoAnimes implements IDownloadVideo {
    @Override
    public void execute(String url) throws IOException {
        try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45)) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            HtmlPage htmlPage = webClient.getPage(url);
            var titleAnime = getTitle(htmlPage.getTitleText()).replaceAll("Assistir ", "");
            webClient.waitForBackgroundJavaScript(5000);

            var updatedPageContent = webClient.getCurrentWindow().getEnclosedPage();

            System.out.println(updatedPageContent.getWebResponse().getContentAsString());
            Document doc = Jsoup.parse(updatedPageContent.getWebResponse().getContentAsString());

            Element anchor = doc.select("a[target=_blank]").first();

            String urlTeste = anchor.attr("href");

            System.out.println("URL extraída: " + urlTeste);
            webClient.getPage(urlTeste);

            webClient.waitForBackgroundJavaScript(5000);

            var testPageContent = webClient.getCurrentWindow().getEnclosedPage();

            var scriptString = testPageContent.getWebResponse().getContentAsString();

            String regex = "file:\\s*\"(https?://[^\"]+)\"";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(scriptString);

            while (matcher.find()) {
                String fileValue = matcher.group(1);
                System.out.println("Valor da propriedade 'file': " + fileValue);
                downloadVideo(titleAnime, fileValue, doc);
            }
        }
    }

    @Override
    public String getTitle(Object document) {
        if (document instanceof String input) {
            String regex = "^(.*?-.*?)-.*$";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(input);

            if (matcher.matches()) {
                return matcher.group(1).trim();
            }
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
}
