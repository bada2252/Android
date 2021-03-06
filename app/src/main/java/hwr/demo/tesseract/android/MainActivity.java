package hwr.demo.tesseract.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hwr.demo.tesseract.android.sample.CropActivity;

public class MainActivity extends AppCompatActivity {
    TessBaseAPI tessBaseAPI;
    private static final String CAPTURE_PATH = "/OCR_doc";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private String absolutePath;
    private Uri mImageCaptureUri;
    Button button;
    ImageView imageView;
    TextView textView;
    String dataPath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPicClick();
            }
        });
    }


    boolean checkLanguageFile(String dir, String lang)
    {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/" +lang+ ".traineddata";
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
            Log.d("msg", "error");
        }
        return true;
    }
    // ?????? ?????? ??????


    private void createFiles(String dir)
    {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("tessdata/eng.traineddata");

            String destFile = dir + "/tessdata/eng.traineddata";

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPicClick() {


        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {
                doTakePhotoAction();
            }

        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {
                doTakeAlbumAction();
            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        };

        new AlertDialog.Builder(this,R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("???????????? ????????? ??????")
                .setPositiveButton("????????????", cameraListener)
                .setNeutralButton("??????", cancelListener)
                .setNegativeButton("????????????", albumListener)
                .show();
    }



    public void doTakePhotoAction() // ????????? ?????? ??? ????????? ????????????
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // ????????? ????????? ????????? ????????? ??????
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    public void doTakeAlbumAction() // ???????????? ????????? ????????????

    {
        // ?????? ??????
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("imageUri", resultUri);
                startActivity(intent);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


        if(resultCode != RESULT_OK)
            return;


        switch(requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                // ????????? ????????? ???????????? ???????????? ??????  break?????? ???????????????.
                // ?????? ??????????????? ?????? ???????????? ????????? ??????????????? ????????????.
                mImageCaptureUri = data.getData();
                Log.d("????????? ??????",mImageCaptureUri.getPath().toString());
            }


            case PICK_FROM_CAMERA:
            {
                // ???????????? ????????? ????????? ??????????????? ????????? ????????? ???????????????.
                CropImage.activity(mImageCaptureUri)
                        .start(this);
                break;
            }
        }
    }

    private void storeCropImage(Bitmap bitmap, String filePath) {

        // SmartWheel ????????? ???????????? ???????????? ???????????? ????????????.

        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CAPTURE_PATH;

        File directory_SmartWheel = new File(dirPath);


        if(!directory_SmartWheel.exists()) // SmartWheel ??????????????? ????????? ????????? (?????? ???????????? ????????? ????????? ?????????.)

            directory_SmartWheel.mkdir();
        File copyFile = new File(filePath);

        BufferedOutputStream out = null;


        try {


            copyFile.createNewFile();

            out = new BufferedOutputStream(new FileOutputStream(copyFile));

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);


            // sendBroadcast??? ?????? Crop??? ????????? ????????? ???????????? ????????????.

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,

                    Uri.fromFile(copyFile)));

            MediaScannerConnection.scanFile( getApplicationContext(),

                    new String[]{copyFile.getAbsolutePath()},

                    null,

                    new MediaScannerConnection.OnScanCompletedListener(){

                        @Override

                        public void onScanCompleted(String path, Uri uri) {

                            Log.v("File scan", "file:" + path + "was scanned seccessfully");

                        }

                    });



            out.flush();

            out.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        Log.d("file path", filePath);
        Uri imageUri = Uri.fromFile(copyFile);
//        Glide.with(getApplicationContext()).load(imageUri)
//                .centerCrop()
//                //.placeholder(R.drawable.alimi_sample)
//                //.error(R.drawable.alimi_sample)
//                .into(ivImage);


    }
}

