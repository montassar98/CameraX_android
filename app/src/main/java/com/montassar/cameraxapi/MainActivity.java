package com.montassar.cameraxapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 999;
    private String[] REQUIRED_PERMISSIONS = {"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};
    private TextureView textureView;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.view_finder);

        if (ifAllPermissionGranted())
        {
            startCamera();
        }else {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
        }

    }

    private boolean ifAllPermissionGranted() {

        for (String permission : REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this,permission)!= PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void startCamera() {

        CameraX.unbindAll();
        Rational aspectRatio = new Rational(textureView.getWidth(),textureView.getHeight());
        Size screen = new Size(textureView.getWidth(),textureView.getHeight());

        PreviewConfig previewConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).
                setTargetResolution(screen).build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView);
                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                }
        );
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);
        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "IMG_"+System.currentTimeMillis()+".jpg");
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        Toast.makeText(MainActivity.this, "saved successfully \n"+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        Log.e(TAG, "onError: "+message, cause);
                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        CameraX.bindToLifecycle(this,preview,imageCapture);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        float width = textureView.getMeasuredWidth();
        float height = textureView.getMeasuredHeight();
        float cX = width /2f;
        float cY = height /2f;
        int rotationDegree;
        int rotation = (int) textureView.getRotation();

        switch (rotation)
        {
            case Surface.ROTATION_0:
                rotationDegree = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegree = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegree = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegree = 270;
                break;
                default:return;
        }

        matrix.postRotate((float) rotationDegree, cX, cY);
        textureView.setTransform(matrix);
    }
}
