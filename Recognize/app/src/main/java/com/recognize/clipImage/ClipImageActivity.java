package com.recognize.clipImage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.recognize.recognize.R;
import com.recognize.utils.FileUtil;
import com.recognize.views.ClipImageLayout;

public class ClipImageActivity extends AppCompatActivity {

    TextView right_btn;
    ClipImageLayout clipImageLayout;
    protected String path;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        initView();
    }


    protected void initView() {
        // 获取Intent对象
        Intent intent = getIntent();
        // 根据key获取value
        path = intent.getStringExtra("path");

        if(path != null){
            right_btn = findViewById(R.id.select);
            clipImageLayout = findViewById(R.id.clipImageLayout);

            bitmap = BitmapFactory.decodeFile(path);
            clipImageLayout.setImageBitmap(bitmap);

            initEvent();
        }
//        int degreee = readBitmapDegree(path);
//        if (bitmap != null) {
//            if (degreee == 0) {
//                clipImageLayout.setImageBitmap(bitmap);
//            } else {
//                clipImageLayout.setImageBitmap(rotateBitmap(degreee, bitmap));
//            }
//        } else {
//            finish();
//        }
    }


    protected void initEvent() {
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = FileUtil.bitMapToFile(clipImageLayout.clip()).getPath();
                Log.d("12312314", path);

                Intent intent = new Intent();
                intent.putExtra("path", path);
                // 给上一个活动返回数据
                setResult(RESULT_OK, intent);// 回调(FirstActivity.java中)onActivityResult方法
                finish();
            }
        });
    }
//
//    private Bitmap createBitmap(String path) {
//        if (path == null) {
//            return null;
//        }
//
//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        //不在内存中读取图片的宽高
//        opts.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(path, opts);
//        int width = opts.outWidth;
//
//        opts.inSampleSize = width > 1080 ? (int)(width / 1080) : 1 ;//注意此处为了解决1080p手机拍摄图片过大所以做了一定压缩，否则bitmap会不显示
//
//        opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
//        opts.inPurgeable = true;
//        opts.inInputShareable = true;
//        opts.inDither = false;
//        opts.inPurgeable = true;
//        FileInputStream is = null;
//        Bitmap bitmap = null;
//        try {
//            is = new FileInputStream(path);
//            bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                    is = null;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return bitmap;
//    }
//
//    // 读取图像的旋转度
//    private int readBitmapDegree(String path) {
//        int degree = 0;
//        try {
//            ExifInterface exifInterface = new ExifInterface(path);
//            int orientation = exifInterface.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return degree;
//    }

//    // 旋转图片
//    private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
//        // 旋转图片 动作
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        // 创建新的图片
//        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//        return resizedBitmap;
//    }
}
