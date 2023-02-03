package com.jacker.plugin.music.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DownloadUtil {

    @SuppressWarnings("all")
    public static String download(String parentPath, String songUrl, String songName) {
        if (StringUtils.isBlank(songUrl) || StringUtils.isBlank(parentPath)) {
            return null;
        }
        if (songUrl.length() > 500) {
            return null;
        }
        Closer closer = Closer.create();
        try {
            File songDir = new File(parentPath);
            if (!songDir.exists()) {
                songDir.mkdirs();
            }
            File songFile = new File(songDir, songName);
            if (songFile.exists()) {
                return songFile.getAbsolutePath();
            }
            InputStream in = closer.register(HttpClientUtil.getStream(songUrl));
            Files.write(ByteStreams.toByteArray(in), songFile);
            return songFile.getAbsolutePath();
        } catch (Exception ex) {
            return null;
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                closer = null;
            }
        }
    }

    public static String replaceHttps(String url) {
        return url.replace("https", "http");
    }

    public static void main(String[] args) {
        String download = download("/Users/jacker/Downloads",
                "http://m10.music.126.net/20210901004157/e2b15b6a035d8be9d2d3cafc922b4956/ymusic/0f0c/040c/045e/8fa8a9c2d63ff46b50dd34ee32ea78c7.mp3",
                "处处吻.mp3");
        System.out.println(download);
    }
}
