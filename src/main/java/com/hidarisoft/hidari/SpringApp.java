package com.hidarisoft.hidari;

import com.hidarisoft.hidari.srcs.IDownloadVideo;
import com.hidarisoft.hidari.srcs.pt.animesOnlineNz.AnimesOnlineNz;
import com.hidarisoft.hidari.srcs.pt.dramaClub.DramaClub;
import com.hidarisoft.hidari.srcs.pt.goAnimes.GoAnimes;

import java.util.List;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringApp {

    public static void main(String[] args) {

        LogManager.getLogManager().reset();
        List<String> urls = List.of("URLS");

        urls.forEach(url -> {
            try {
                if (Objects.requireNonNull(extractDomain(url)).contains("animesonline")) {
                    IDownloadVideo iDownloadVideo = new AnimesOnlineNz();
                    iDownloadVideo.execute(url);
                } else if (Objects.requireNonNull(extractDomain(url)).contains("dramaclub")) {
                    IDownloadVideo iDownloadVideo = new DramaClub();
                    iDownloadVideo.execute(url);
                } else {
                    IDownloadVideo iDownloadVideo = new GoAnimes();
                    iDownloadVideo.execute(url);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private static String extractDomain(String url) {
        String regex = "https?://([^/]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }


}