package cc.colorcat.vangogh;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by cxx on 2017/7/10.
 * xx.ch@outlook.com
 */
public class ImageViewTarget implements Target {
    @Override
    public View getView() {
        return null;
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

    @Override
    public void onBitmapLoaded(@NonNull Bitmap bitmap, String url, LoadedFrom from) {

    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }
}
