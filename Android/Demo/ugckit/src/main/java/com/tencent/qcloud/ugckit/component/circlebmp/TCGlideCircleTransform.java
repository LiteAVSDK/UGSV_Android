package com.tencent.qcloud.ugckit.component.circlebmp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;
import java.util.Objects;

/**
 * Glide图像裁剪
 */
public class TCGlideCircleTransform extends BitmapTransformation {

    private final String id = getClass().getName();

    public TCGlideCircleTransform(Context context) {
    }

    @Override
    protected Bitmap transform(BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return createCircleImage(toTransform, 0);
    }

    // 根据原图绘制圆形图片
    public static Bitmap createCircleImage(@NonNull Bitmap source, int min) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (0 == min) {
            min = source.getHeight() > source.getWidth() ? source.getWidth() : source.getHeight();
        }
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        // 创建画布
        Canvas canvas = new Canvas(target);
        // 绘圆
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        // 设置交叉模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 绘制图片
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TCGlideCircleTransform;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((id).getBytes());
    }
}
