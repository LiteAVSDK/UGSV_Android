package com.tencent.qcloud.ugckit.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.component.circlebmp.TCGlideCircleTransform;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片工具类
 */
public class BitmapUtils {
    private static final String TAG            = "BitmapUtils";
    // 默认图片宽高
    public static final  int    DEFAULT_WIDTH  = 720;
    public static final  int    DEFAULT_HEIGHT = 1280;

    @NonNull
    public static ArrayList<Bitmap> decodeFileToBitmap(@NonNull List<String> picPathList) {
        ArrayList<Bitmap> arrayList = new ArrayList<>();
        for (int i = 0; i < picPathList.size(); i++) {
            String filePath = picPathList.get(i);
            Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(filePath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            arrayList.add(bitmap);
            VideoEditerSDK.getInstance().addThumbnailBitmap(0, bitmap);
        }
        return arrayList;
    }

    @NonNull
    public static int[] getSize(String picturePath) {
        if (TextUtils.isEmpty(picturePath)) {
            return new int[2];
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int[] size = new int[2];
        size[0] = width;
        size[1] = height;
        return size;
    }

    public static Bitmap decodeSampledBitmapFromFile(String picPath, int reqWidth, int reqHeight) {
        boolean isContentUri = picPath != null && picPath.startsWith("content://");
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        ParcelFileDescriptor pdf = null;
        if (isContentUri) {
            try {
                pdf = UGCKit.getAppContext().getContentResolver().openFileDescriptor(Uri.parse(picPath), "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                pdf = null;
            }
            BitmapFactory.decodeFileDescriptor(pdf.getFileDescriptor(), null, options);
        } else {
            BitmapFactory.decodeFile(picPath, options);
        }
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        if (pdf != null) {
            bitmap = BitmapFactory.decodeFileDescriptor(pdf.getFileDescriptor(), null, options);

            int orientation = -1;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                orientation = getImageOrientation(pdf.getFileDescriptor());
                Log.d(TAG, "getImageOrientation from fileDescriptor，orientation = " + orientation);
            }
            if (orientation == -1) {
                orientation = getImageOrientation(Uri.parse(picPath));
                Log.d(TAG, "getImageOrientation from uri，orientation = " + orientation);
            }

            bitmap = rotateBitmap(bitmap, orientation);
            try {
                pdf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(picPath, options);
            int orientation = getImageOrientation(picPath);
            bitmap = rotateBitmap(bitmap, orientation);
        }
        return bitmap;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (bitmap == null) {
            return null;
        }

        Log.d(TAG, "getImageOrientation = " + orientation);
        if (orientation == -1 || orientation == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotatedBitmap;
    }

    /**
     * getImageOrientation
     *
     * @param uri
     * @return -1,0,90,180,270
     */
    private static int getImageOrientation(Uri uri) {
        Cursor cursor = UGCKit.getAppContext().getContentResolver().query(uri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor == null) {
            return -1;
        }
        if (cursor.getCount() != 1) {
            cursor.close();
            return -1;
        }

        cursor.moveToFirst();
        //orientation here can be 90, 180, 270!
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }

    private static int getImageOrientation(String filePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return getDegree(orientation);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getImageOrientation-filePath,e=" + e.toString());
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static int getImageOrientation(FileDescriptor fileDescriptor) {
        try {
            ExifInterface exifInterface = new ExifInterface(fileDescriptor);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return getDegree(orientation);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getImageOrientation-fileDescriptor,e=" + e.toString());
            return -1;
        }
    }

    private static int getDegree(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 处理图片
     *
     * @param bm        所要转换的bitmap
     * @param newWidth  新的宽
     * @param newHeight 新的高
     * @return 指定宽高的bitmap
     */
    public static Bitmap zoomImg(@NonNull Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    public static void blurBgPic(@Nullable final Context context, @Nullable final ImageView view, final String url, int defResId) {
        if (context == null || view == null) {
            return;
        }

        if (TextUtils.isEmpty(url)) {
            view.setImageResource(defResId);
        } else {
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            if (resource == null) {
                                return;
                            }

                            final Bitmap bitmap = blurBitmap(resource, context.getApplicationContext());
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    view.setImageBitmap(bitmap);
                                }
                            });
                        }
                    });
        }
    }

    private static Bitmap blurBitmap(@NonNull Bitmap resource, @NonNull Context context) {
        Bitmap bitmap = Bitmap.createBitmap(resource.getWidth(), resource.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        PorterDuffColorFilter filter =
                new PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
        paint.setColorFilter(filter);
        canvas.drawBitmap(resource, 0, 0, paint);

        RenderScript rs = RenderScript.create(context.getApplicationContext());
        Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        blur.setInput(input);
        blur.setRadius(10);
        blur.forEach(output);
        output.copyTo(bitmap);
        rs.destroy();

        return bitmap;
    }

    /**
     * 圆角显示图片
     *
     * @param context  一般为activtiy
     * @param view     图片显示类
     * @param url      图片url
     * @param defResId 默认图 id
     */
    public static void showPicWithUrl(@Nullable Context context, @Nullable ImageView view, String url, int defResId) {
        if (context == null || view == null) {
            return;
        }
        try {
            if (TextUtils.isEmpty(url)) {
                view.setImageResource(defResId);
            } else {
                RequestManager req = Glide.with(context);
                req.load(url).placeholder(defResId).transform(new TCGlideCircleTransform(context)).into(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Android10兼容，图片String类型地址先要转换为Uri，再加载
     *
     * @param context
     * @param path
     * @return
     */
    public static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    /**
     * 通过uri加载图片
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
