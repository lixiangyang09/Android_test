package com.example.study.test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.os.BatteryManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.view.*;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Set;

import android.widget.Toast;

import android.util.DisplayMetrics;
import android.view.Display;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;

    int screenWidth, screenHeight;
    AutoFocusCallback autoFocusCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set full screen
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //get the window manager
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        // 获取屏幕的宽和高
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);

        surfaceHolder = surfaceView.getHolder();
        // 为srfaceHolder添加一个回调监听器
        surfaceHolder.addCallback(this);
        // 设置surface不需要自己的维护缓存区
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        jpegCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
                    outStream.write(data);
                    outStream.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                }
                Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_LONG).show();
                refreshCamera();
            }
        };
        autoFocusCallback = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if(success){
                    camera.setOneShotPreviewCallback(null);
                    Toast.makeText(getApplicationContext(),"autofocus", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    public void captureImage(View v) throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        }

        catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch (Exception e) {
        }
        if (camera != null) {
            // 控制摄像头自动对焦后才拍摄
            camera.autoFocus(autoFocusCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
        }

        catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters param;
        param = camera.getParameters();
        param.setPreviewSize(600, 800);
        //rotate 180
        param.setRotation(180);


        camera.setParameters(param);


        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }

        catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}