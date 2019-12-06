package com.tencent.qcloud.ugckit.utils;

public class GifUtil {
    private static final String TAG = "GifUtil";

//    public static String createGifByBitmaps(String filePath, List<Bitmap> bitmapList, int delayMs, int width, int height) throws IOException {
//        if (bitmapList == null || bitmapList.size() == 0) {
//            return null;
//        }
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
//        gifEncoder.start(baos);
//        gifEncoder.setRepeat(0);
//        gifEncoder.setDelay(delayMs);
//        TXCLog.d(TAG, "start make gif");
//        for (Bitmap bitmap : bitmapList) {
//            Bitmap resizeBitmap = BitmapUtil.zoomImg(bitmap, width, height);
//            gifEncoder.addFrame(resizeBitmap);
//        }
//        gifEncoder.finish();
//        TXCLog.i(TAG, "finish make gif");
//        FileOutputStream fos = new FileOutputStream(filePath);
//        baos.writeTo(fos);
//        baos.flush();
//        fos.flush();
//        baos.close();
//        fos.close();
//        return filePath;
//    }
}
