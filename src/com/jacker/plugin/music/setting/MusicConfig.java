package com.jacker.plugin.music.setting;

import java.util.Objects;

/**
 * 保存用户配置(会使用xml持久化存储，密码会使用隐私存储)
 */
public class MusicConfig {

    /**
     * 手机号
     */
    private String cellphone;

    /**
     * 歌曲存储路径
     */
    private String filePath;

    /**
     * 是否展示导航栏
     */
    private Boolean showNavigateBar = true;

    /**
     * 是否展示状态插件
     */
    private Boolean showStatusWidget = true;


    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Boolean getShowNavigateBar() {
        return showNavigateBar;
    }

    public void setShowNavigateBar(Boolean showNavigateBar) {
        this.showNavigateBar = showNavigateBar;
    }

    public Boolean getShowStatusWidget() {
        return showStatusWidget;
    }

    public void setShowStatusWidget(Boolean showStatusWidget) {
        this.showStatusWidget = showStatusWidget;
    }

    public boolean equals(MusicConfig musicConfig){
        if (musicConfig == null) {
            return false;
        }
        return Objects.equals(cellphone, musicConfig.cellphone)
                && Objects.equals(filePath, musicConfig.filePath)
                && Objects.equals(showNavigateBar, musicConfig.showNavigateBar)
                && Objects.equals(showStatusWidget, musicConfig.showStatusWidget);
    }
}
