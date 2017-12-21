package com.studygoal.jisc.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * Circle Transform Class
 * <p>
 * Provides the functionality to crop a bitmap into a circle.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class CircleTransform extends BitmapTransformation {

    public CircleTransform() {
        super();
    }

    /**
     * Transforms the bitmap into a circle by calling the method circlecrop.
     *
     * @param pool        bitmap pool
     * @param toTransform bitmap to be transformed
     * @param outWidth    width of the bitmap
     * @param outHeight   height of the bitmap
     * @return transformed bitmap
     */
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    /**
     * Transforms the bitmap into a circle.
     *
     * @param pool   bitmap pool
     * @param source bitmap to be transformed
     * @return transformed bitmap
     */
    private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // TODO this could be acquired from the pool too
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return result;
    }

    /**
     * Overriden to do nothing.
     *
     * @param messageDigest unused parameter
     */
    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        // no need to implement
    }

}