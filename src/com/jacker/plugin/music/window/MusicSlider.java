package com.jacker.plugin.music.window;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

/**
 * 美化滑动条
 * 注意，需要重写setUI()方法。如果在构造方法中设置，工具栏收起再次展开时，idea会调用setUI方法覆盖ui，导致美化时效
 */
public class MusicSlider extends JSlider {
    private final MusicSliderUI musicSliderUI = new MusicSliderUI(this);

    public MusicSlider(int orientation) {
        super(orientation);
    }

    @Override
    public void setUI(SliderUI ui) {
        super.setUI(musicSliderUI);
    }

    public static class MusicSliderUI extends BasicSliderUI {

        public MusicSliderUI(JSlider b) {
            super(b);
        }

        @Override
        public void paintThumb(Graphics g) {
            // 不要滑块
        }

        public void paintTrack(Graphics g) {
            //绘制刻度的轨迹
            int cy, cw;
            Rectangle trackBounds = trackRect;
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                Graphics2D g2 = (Graphics2D) g;
                // 背景色
                g2.setPaint(new JBColor(Gray._220, Gray._80));
                cy = (trackBounds.height / 2) - 2;
                cw = trackBounds.width;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(trackBounds.x, trackBounds.y + cy);
                g2.fillRect(0, -cy + 5, cw, cy - 2);

                int trackLeft = 0;
                int trackRight = 0;
                trackRight = trackRect.width - 1;

                int middleOfThumb = 0;
                int fillLeft = 0;
                int fillRight = 0;
                //换算坐标
                middleOfThumb = thumbRect.x + (thumbRect.width / 2);
                middleOfThumb -= trackRect.x;

                if (!drawInverted()) {
                    fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
                    fillRight = middleOfThumb;
                } else {
                    fillLeft = middleOfThumb;
                    fillRight = !slider.isEnabled() ? trackRight - 1
                            : trackRight - 2;
                }
                //设定渐变,在这里从红色变为红色,则没有渐变,滑块划过的地方自动变成红色
                g2.setPaint(new GradientPaint(0, 0, Gray._125, cw, 0,
                        Gray._125, true));
                g2.fillRect(0, -cy + 5, fillRight - fillLeft, cy - 2);


                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.translate(-trackBounds.x, -(trackBounds.y + cy));
            } else {
                super.paintTrack(g);
            }
        }
    }

}
