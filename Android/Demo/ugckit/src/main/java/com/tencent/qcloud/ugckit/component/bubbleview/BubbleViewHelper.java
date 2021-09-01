package com.tencent.qcloud.ugckit.component.bubbleview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据外部传入的Params，构建一张合适的气泡Bitmap图
 * <p>
 * 1. 根据默认字体大小、可绘制的字幕区域来计算出 最终绘制的字体大小，以及行数
 * 2. 根据字体大小、行数对字体进行排版
 * 3. 绘制： A.绘制已经排好版的字体  B.绘制气泡字幕背景
 * 4. 返回最终的Bitmap图
 */
public class BubbleViewHelper {
    @Nullable
    private Bitmap           mBubbleBitmap;
    private BubbleViewParams mParams;
    private float            mTextDefaultSize = 36; // 字幕的默认代销
    private String           mText;
    private Paint            mPaint;
    private int              mTextAreaTop;
    private int              mTextAreaLeft;
    private int              mTextAreaRight;
    private int              mTextAreaBottom;
    private int              mTextAreaHeight;
    private int              mTextAreaWidth;
    private int              mTextAreaCenterX;
    private int              mTextAreaCenterY;

    public BubbleViewHelper() {

    }

    public void setBubbleTextParams(@NonNull BubbleViewParams params) {
        mParams = params;
        mTextDefaultSize = params.wordParamsInfo.getBubbleInfo().getDefaultSize();
        mBubbleBitmap = params.bubbleBitmap;
        mText = params.text;

        if (mBubbleBitmap != null && mBubbleBitmap.isRecycled()) {
            return;
        }

        initPaint();

        // 如果气泡字幕背景为空， 那么创建一张刚好可以包裹文字的"空背景"
        if (mBubbleBitmap == null) {
            // 创建一张空的背景
            int height = (int) getFontHeight() * 2;
            int width = (int) mPaint.measureText(mText) + 1;
            mBubbleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        initTextArea(
                params.wordParamsInfo.getBubbleInfo().getTop(),
                params.wordParamsInfo.getBubbleInfo().getLeft(),
                params.wordParamsInfo.getBubbleInfo().getRight(),
                params.wordParamsInfo.getBubbleInfo().getBottom());
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(mParams.wordParamsInfo.getTextColor() != 0 ? mParams.wordParamsInfo.getTextColor() : Color.WHITE);
        mPaint.setTextSize(mTextDefaultSize);
        mPaint.setAntiAlias(true);
    }

    /**
     * 初始化可绘制区域的大小
     *
     * @param top
     * @param left
     * @param right
     * @param bottom
     */
    private void initTextArea(float top, float left, float right, float bottom) {
        mTextAreaTop = (int) (top * mBubbleBitmap.getHeight());
        mTextAreaBottom = (int) (bottom * mBubbleBitmap.getHeight());
        mTextAreaLeft = (int) (left * mBubbleBitmap.getWidth());
        mTextAreaRight = (int) (right * mBubbleBitmap.getWidth());

        mTextAreaWidth = mBubbleBitmap.getWidth() - mTextAreaRight - mTextAreaLeft;
        mTextAreaHeight = mBubbleBitmap.getHeight() - mTextAreaBottom - mTextAreaTop;

        mTextAreaCenterX = mTextAreaWidth / 2 + mTextAreaLeft;
        mTextAreaCenterY = mTextAreaHeight / 2 + mTextAreaTop;
    }


    /**
     * 生成最终bitmap的方法
     *
     * @return
     */
    @Nullable
    public Bitmap createBubbleTextBitmap() {
        if (mBubbleBitmap == null || mBubbleBitmap.isRecycled()) {
            return null;
        }
        float textSize = measureFontSize(mText);
        mPaint.setTextSize(textSize);
        Bitmap bitmap = Bitmap.createBitmap(mBubbleBitmap.getWidth(), mBubbleBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mBubbleBitmap, 0, 0, mPaint);
        drawText(canvas);
        if (!mBubbleBitmap.isRecycled()) {
            mBubbleBitmap.recycle();
            mBubbleBitmap = null;
        }
        return bitmap;
    }

    //------------------------------------------------测量文字相关--------------------------------------------------------------

    /**
     * 测量文字最合适的大小
     *
     * @param text
     * @return
     */
    private float measureFontSize(String text) {
        float trySize = mTextDefaultSize;
        mPaint.setTextSize(trySize);
        int lines = measureTextLines(trySize, text);
        double height = measureTextAreaHeight(trySize, lines);
        if (height > mTextAreaHeight) {
            do {
                trySize -= 1;
                lines = measureTextLines(trySize, text) + 1;// +1是为了留出更多的空间
                height = measureTextAreaHeight(trySize, lines);
            } while (height > mTextAreaHeight);
        }
        return trySize;
    }


    /**
     * 测量文字所占用的高度
     *
     * @param fontSize
     * @param lines
     * @return
     */
    private double measureTextAreaHeight(float fontSize, int lines) {
        mPaint.setTextSize(fontSize);
        double fontHeight = getFontHeight();
        double height = lines * fontHeight;
        return height;
    }

    /**
     * 测量输入的文字 所需要占用的行数
     *
     * @param fontSize
     * @param s
     * @return
     */
    private int measureTextLines(float fontSize, String s) {
        mPaint.setTextSize(fontSize);
        float textWidth = mPaint.measureText(s);
        float lines = textWidth / mTextAreaWidth;
        return (int) Math.ceil(lines);
    }


    //------------------------------------------------渲染文字相关--------------------------------------------------------------

    private void drawText(@NonNull Canvas canvas) {
        List<TextParams> list = locateText();  //定位字幕

        for (TextParams params : list) {
            // 具体绘制字幕
            canvas.drawText(params.text, params.x, params.y, mPaint);
        }

    }

    /**
     * 定位字符
     * <p>
     * 1. 分割字幕
     * 2. 每行字幕定位
     */
    @NonNull
    private List<TextParams> locateText() {
        List<TextParams> list = new ArrayList<>();

        List<String> text = splitText();// 将字幕分割为一行一行

        int middlePos = (text.size() + 1) / 2 - 1;

        float baseX = mTextAreaLeft, baseY;

        float fontHeight = getFontHeight();

        if (text.size() % 2 == 1) {//行数奇数
            baseY = mTextAreaCenterY + fontHeight / 2;
        } else {
            baseY = mTextAreaCenterY;
        }

        list.add(new TextParams(text.get(middlePos), mTextAreaLeft, baseY));


        int loopTime = 0;
        for (int i = middlePos - 1; i >= 0; i--) {
            loopTime += 1;
            list.add(new TextParams(text.get(i), baseX, baseY - fontHeight * loopTime));
        }

        loopTime = 0;
        for (int i = middlePos + 1; i < text.size(); i++) {
            loopTime += 1;
            list.add(new TextParams(text.get(i), baseX, baseY + fontHeight * loopTime));
        }
        return list;
    }


    /**
     * 分割字幕
     * <p>
     * 对文字进行排版，分割成一行一行
     *
     * @return
     */
    @NonNull
    private List<String> splitText() {
        List<String> text = new ArrayList<>();

        char[] chars = mText.toCharArray();

        StringBuilder sb = new StringBuilder();

        int lineWidth = 0;

        int lineNumber = 0;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            float charSize = mPaint.measureText(chars, i, 1);
            lineWidth += charSize;

            if (lineWidth < mTextAreaWidth) {
                sb.append(c);
            } else {
                text.add(sb.toString());
                lineNumber++;

                lineWidth = 0;
                lineWidth += charSize;
                sb = new StringBuilder();
                sb.append(c);
            }

            if (i == chars.length - 1) {
                text.add(sb.toString());
                break;
            }
        }

        return text;
    }


    /**
     * 获取一个字体的高度
     *
     * @return
     */
    private float getFontHeight() {
        Paint.FontMetrics metrics = new Paint.FontMetrics();
        mPaint.getFontMetrics(metrics);
        return metrics.bottom - metrics.ascent - metrics.descent;
    }

    private static class TextParams {
        public float x, y;
        public String text;

        public TextParams(String text, float x, float y) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }
}
