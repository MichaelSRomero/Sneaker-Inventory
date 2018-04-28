package com.example.android.sneakerinventory.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Mrome on 2/26/2018.
 */

public class ImageUtils {

    /** Private Constructor to avoid creating an instance of it*/
    private ImageUtils() {}

    public static byte[] convertToByte (int drawableResourceID, Context context) {
        //Convert to bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                drawableResourceID);
        //Convert to byte to store
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageByte = bos.toByteArray();
        return imageByte;
    }
}
