package com.jacker.plugin.music.util;

import java.io.File;
import java.util.Objects;

/**
 * 文件工具
 *
 */
public class FileUtil {
    /**
     * 判断文件是否存在
     *
     * @param fileName 文件名
     * @return true/false 存在/不存在
     */
    public static boolean contain(String fileName) {
        return new File(fileName).exists();
    }

    /**
     * 判断文件夹是否存在,不存在则创建
     *
     * @param file 文件夹路径
     */
    public static boolean dirExists(File file) {
        return file.exists() ? file.isDirectory() : file.mkdirs();
    }

    /**
     * 清除指定目录下的m4a缓存文件
     *
     * @param cacheFileDirPath 目录路径
     */
    public static void cleanM4aCache(String cacheFileDirPath) {
        File dir = new File(cacheFileDirPath);
        if (dir.isDirectory()) {
            //如果是文件夹
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isFile() && file.getName().endsWith("m4a")) {
                    //清理M4A文件
                    file.delete();
                }
            }
        }
    }

    public static String getFileExt(File file) {
        return getFileExt(file.getName());
    }

    public static String getFileExt(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) return "";
        return fileName.substring(pos + 1).toLowerCase();
    }
}
