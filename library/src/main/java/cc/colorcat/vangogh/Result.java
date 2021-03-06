package cc.colorcat.vangogh;

import android.graphics.Bitmap;

import java.io.InputStream;

/**
 * Created by cxx on 2017/7/10.
 * xx.ch@outlook.com
 */
public final class Result {
    private final Bitmap bitmap;
    private final InputStream stream;
    private final long contentLength;
    private final From from;

    public Result(Bitmap bitmap, From from) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap == null");
        }
        if (from == null) {
            throw new NullPointerException("reqFrom == null");
        }
        this.bitmap = bitmap;
        this.stream = null;
        this.contentLength = bitmap.getByteCount();
        this.from = from;
    }

    public Result(InputStream is, From from) {
        this(is, -1L, from);
    }

    public Result(InputStream is, long contentLength, From from) {
        if (is == null) {
            throw new NullPointerException("is == null");
        }
        if (from == null) {
            throw new NullPointerException("reqFrom == null");
        }
        this.bitmap = null;
        this.stream = is;
        this.contentLength = contentLength;
        this.from = from;
    }

    Bitmap bitmap() {
        return bitmap;
    }

    InputStream stream() {
        return stream;
    }

    long contentLength() {
        return contentLength;
    }

    From from() {
        return from;
    }
}
