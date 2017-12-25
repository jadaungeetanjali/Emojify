package com.silive.pc.emojify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by PC on 12/25/2017.
 */

public class BitmapUtils {
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    static File createTempImageFile(Context context) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    static boolean deleteImageFile(Context context, String imagePath) {
        // get the file
        File imageFile = new File(imagePath);
        // delete the file
        Boolean deleted = imageFile.delete();

        //if there is an error detecting the file show the error
        if (!deleted){
            Toast.makeText(context, "Error finding the image", Toast.LENGTH_SHORT).show();
        }
        return deleted;
    }
    //Resamples the captured photo to fit the screen for better memory usage.
    public static Bitmap resamplePic(Context context, String imagePath) {

        // get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // get the dimensions of original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // decode the imageFile into Bitmap size to fit the view
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }
    //Helper method for adding the photo to the system photo gallery so it can be accessed from other apps.
    private static void galleryAddPic(Context context, String imagePath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
    static String saveImage(Context context, Bitmap image) {

        String savedImagePath = null;

        // Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/Emojify");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        // save the new bitmap
        if (success){
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fout = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // add the image to system gallery
            galleryAddPic(context, savedImagePath);
            // Show a Toast with the save location
            String savedMessage = context.getString(R.string.saved_message, savedImagePath);
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show();
        }
        return savedImagePath;
    }

    public static void shareImage(Context context, String imagePath) {
        // create the share intent and start the share activity
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoURI = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        context.startActivity(shareIntent);
    }
}
