package com.example.a3droplets.mycamera;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import static android.media.MediaRecorder.VideoSource.CAMERA;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ORIGINAL = 1;
    // 构造存储图片的文件的路径，文件名为当前时间
    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/" + System.currentTimeMillis() + ".png";
    private ImageView imageView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private View floatWindowView;
    private FloatingActionsMenu float_button;
    private HRBroadcastReceiver mHRBroadcastReceiver;
    private File photoDir;
    private File photo;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.CameraButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initImageFile() ){
                    applyRWPermission();
                }
            }
        });
    }
    private void applyRWPermission(){
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
                return;
            }
            int check = ContextCompat.checkSelfPermission(this, permissions[0]);
            //检查是否已经授权
            if (check == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            }
            createFloatView();//创建悬浮窗口
        }
    }
    public void startCamera() {
        try {
            dispatchTakePictureIntent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        "com.example.a3droplets.mycamera.FileProvider",
                        createImageFile());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_ORIGINAL);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IGM_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    /**
     * 判断是否有SD卡
     * @return 有SD卡返回true， 否则false
     */
    private boolean hasSDCard() {
        // 获取外部存储的状态
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 有SD卡
            return true;
        }
        return false;
    }

    /**
     * 初始化存储图片的文件
     * @return 初始化成功返回true，否则false
     */
    public boolean initImageFile() {
        // 有SD卡时才初始化文件
        if (hasSDCard()) {
            File imageFile = new File(filePath);
            if (!imageFile.exists()) {// 如果文件不存在，就创建文件
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;

    }
    /**
     * 创建悬浮窗体*/
    private void createFloatView() {
        //此wmParams为获取的全局变量，用以保存悬浮窗口的属性
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//得到浮动窗口管理器
        mWindowLayoutParams = new WindowManager.LayoutParams();//设置悬浮窗布局属性
        mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; //设置类型
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//设置行为选项
        mWindowLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER;
        mWindowLayoutParams.format = PixelFormat.RGBA_8888;
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        floatWindowView = LayoutInflater.from(this).inflate(R.layout.float_view, null);//设置悬浮窗的布局
        mWindowManager.addView(floatWindowView, mWindowLayoutParams);//加载显示悬浮窗
        com.getbase.floatingactionbutton.FloatingActionButton fab = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab0);
        com.getbase.floatingactionbutton.FloatingActionButton fab1 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab1);
        com.getbase.floatingactionbutton.FloatingActionButton fab2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab2);
        com.getbase.floatingactionbutton.FloatingActionButton fab3 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab3);
    }
    private final BroadcastReceiver homeListenerReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    // 处理自己的逻辑
                    mWindowManager.removeView(floatWindowView);
                }
            }
        }
    };
    public void onReceive(Context context, Intent intent) {

        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(homeListenerReceiver, homeFilter);
        // 反注册home监听
        if (homeListenerReceiver != null) {
            context.unregisterReceiver(homeListenerReceiver);
        }
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//得到浮动窗口管理器
        floatWindowView = LayoutInflater.from(this).inflate(R.layout.float_view, null);//设置悬浮窗的布局
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                mWindowManager.removeView(floatWindowView);
            case KeyEvent.KEYCODE_MENU:
                mWindowManager.removeView(floatWindowView);
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA &&resultCode == Activity.RESULT_OK && null != data) {
            // Show the thumbnail on ImageView

            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                Bitmap bmp = BitmapFactory.decodeStream(ims);
                Bitmap bm = MyWaterMask.addMarkFlag(bmp);
                imageView = (ImageView)findViewById(R.id.imageView);
                imageView.setImageBitmap(bm);
            } catch (FileNotFoundException e) {
                return;
            }

            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
            mWindowManager.removeView(floatWindowView);//回收悬浮窗口
        }
    }
    public void onDestroy() {
        mWindowManager.removeView(floatWindowView);
        super.onDestroy();
    }

}
