package com.recognize.recognize;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.recognize.adapter.InfoListAdapter;
import com.recognize.clipImage.ClipImageActivity;
import com.recognize.entity.Information;
import com.recognize.entity.UpLoadFileInfo;
import com.recognize.login.MainActivity;
import com.recognize.utils.BitMapUntil;
import com.recognize.utils.StatusBarUtil;
import com.recognize.utils.TimeGetUntil;
import com.recognize.utils.Uri_PathUtil;
import com.recognize.utils.UrlJudgeUntil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImageActivity extends AppCompatActivity {

    private Button takeBtn; //拍照按钮
    private Button choBtn;  //选择相册按钮
    private TextView ejectTakeBtn;//弹出框拍照
    private TextView ejectchoBtn;   //弹出框相册
    private TextView ejectCancelBtn;//弹出框取消
    private CircleImageView userHeadView;//用户头像
    private TextView funcTextView;//功能提示框


    //recyclerview及其adpter
    private RecyclerView recyclerView;
    private InfoListAdapter adapter;
    //好友列表
    private ArrayList<Information> infoList = new ArrayList<Information>();

    //Activity回调函数的判断
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_GALLERY = 2;
    public static final int Clip_Image = 3;

    private File outputImage; //上传百度文件


    private String uploadUrl = "http://182.92.165.130:3000/upload";         //上传头像url
    private String searchUrl = "http://182.92.165.130:3000/search";         //搜索头像url
    private String friendListUrl = "http://182.92.165.130:3000/friendlist"; //拉取关注列表url

    private static final int COMPLETED = 0;//搜索完成标志
    final boolean isRegister = MainActivity.isRegister; //登录/注册标志

//    private TextView searchNameText;
//    private TextView searchImageText;
//    private CardView searchCardView;
//    private String uploadFileName;//上传服务器文件
//    private byte[] fileBuf;
//    private CircleImageView friendHeadImage;
//    private CircleImageView pictureView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadimage);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        init();
    }

    private void init() {
        //初始化button和view
        takeBtn = findViewById(R.id.takePicture);
        takeBtn.setOnClickListener(takeBtnListener());
        choBtn = findViewById(R.id.chooseGallery);
        choBtn.setOnClickListener(chooseBtnChoose());
        userHeadView = findViewById(R.id.userHeadImage);
        userHeadView.setImageResource(R.drawable.logo);
        TextView us = findViewById(R.id.usernameTextView);
        us.setText("用户：" + MainActivity.loginUsername);


        friendsFromServer(MainActivity.loginUsername);
        //初始化recyclerview
        recyclerView = findViewById(R.id.base_swipe_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new InfoListAdapter(this, infoList);


        recyclerView.setAdapter(adapter);


        // 更换头像
        userHeadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomEject(v);
            }
        });

        //功能提示
        funcTextView = findViewById(R.id.funcChoTextView);
        if (MainActivity.isRegister)
            funcTextView.setText("请上传本人照片，以完成注册");
        else {
            funcTextView.setText("通过照片搜索朋友");
            showUrlImage(MainActivity.UserHeadUrL, userHeadView);
        }


//        pictureView = findViewById(R.id.picture);
//        searchNameText = findViewById(R.id.searchName);
//        friendHeadImage = findViewById(R.id.friendHeadImage);
//        searchCardView = findViewById(R.id.searchCardView);
//        searchImageText = findViewById(R.id.textShowImageTv);
//        searchImageText.setVisibility(View.INVISIBLE);
//        searchCardView.setVisibility(View.INVISIBLE);
    }

    public void showUrlImage(final String url, final ImageView v) {
        final BitMapUntil bm = new BitMapUntil();
        new Thread() {
            @Override
            public void run() {
                if (url != null && UrlJudgeUntil.isHttpUrl(url)) {
                    final Bitmap img = bm.urlToBitMap(url);
                    UploadImageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.setImageBitmap(img);
                        }
                    });
                } else {
                    UploadImageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.setImageResource(R.drawable.logo);
                        }
                    });
                }
            }
        }.start();
    }

    protected Uri takePhotoUri = null;//拍照的文件uri

    protected View.OnClickListener takeBtnListener() {
        View.OnClickListener cl = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                //创建File对象，用于存储拍照后的图片
                //getExternalCacheDir()方法将图片存放在应用关联缓存目录下,也就是/sdcard/Android/data/应用的包名/cache中
                outputImage = new File(getExternalCacheDir(), TimeGetUntil.getTime());
                try {
                    //如果文件存在则删除，从新创建一个新的文件
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    takePhotoUri = FileProvider.getUriForFile(UploadImageActivity.this,
                            "com.recognize.recognize.provider", outputImage);
                } else {
                    takePhotoUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                //指定图片的输出地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        };
        return cl;
    }

    protected View.OnClickListener chooseBtnChoose() {

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, CHOOSE_GALLERY);
            }
        };
        return cl;
    }

    //回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imgPath;
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
                //拍照回调
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    imgPath = outputImage.getPath();
                    clip(imgPath);
                }
                break;
                //相册回调
            case CHOOSE_GALLERY:
                if (data != null) {
                    imgPath = Uri_PathUtil.getImageAbsolutePath(this, data.getData());
                    clip(imgPath);
                }
                break;
                //裁剪回调
            case Clip_Image:
                if (resultCode == RESULT_OK) {
                    String clipResultPath = data.getStringExtra("path");
                    Bitmap bm = BitmapFactory.decodeFile(clipResultPath);
                    Dialog da = setSearchEject(bm);
                    da.show();
//                    if (Build.VERSION.SDK_INT >= 24) {
//                        imageUri = FileProvider.getUriForFile(UploadImageActivity.this,
//                                "com.example.recognize.provider", new File(clipResultPath));
//                    } else {
//                        imageUri = Uri.fromFile(new File(clipResultPath));
//                    }
//                    searchImageText.setVisibility(View.VISIBLE);
//                    pictureView.setVisibility(View.VISIBLE);
//                    pictureView.setImageBitmap(bm);
                    if (clipResultPath != null)
                        upload(clipResultPath, MainActivity.loginUsername, da);
                    else {
                        Toast.makeText(UploadImageActivity.this, "上传失败，请重新上传", Toast.LENGTH_SHORT).show();
                        funcTextView.setText("请重新选择图片上传");
                    }
                }
                break;
            default:
                break;
        }
        if (bottomDialog != null)
            bottomDialog.dismiss();
    }

    //截图
    public void clip(final String path) {
        Intent intent = new Intent(UploadImageActivity.this, ClipImageActivity.class);
        intent.putExtra("path", path); // 传字符串
        startActivityForResult(intent, Clip_Image);
    }

    //上传头像
    public void upload(final String path, final String username, final Dialog da) {

        new Thread() {
            @Override
            public void run() {
                //注册
                if (MainActivity.isRegister) {
                    funcTextView.setText("上传中，请等待...");
                    if (FaceAddBase64.addFace(path, username) == 0) {
                        UpLoadFileInfo info = handleSelect(Uri_PathUtil.getUri(UploadImageActivity.this, path));
                        uploadToServer(da, info.getFileBuf(), info.getUploadFileName(), path);
                    } else {
                        da.dismiss();
                        Looper.prepare();
                        Toast.makeText(UploadImageActivity.this, "上传失败，请重新上传", Toast.LENGTH_SHORT).show();
                        funcTextView.setText("请重新选择图片上传");
                        Looper.loop();
                    }
                } else {
                    //搜索
                    funcTextView.setText("识别中，请等待...");
                    final List<String> schName = FaceDetectBase64.searchFace(path);
                    List<String> userId = new ArrayList<>();
                    if (schName != null) {
                        Log.d("12312312312431423", "" + schName.size());
                    } else
                        Log.d("12312312312431423", "null");
                    if (schName != null && schName.size() != 0) {
                        searchFromServerUrl(schName, da);
                    } else {
                        Looper.prepare();
                        Toast.makeText(UploadImageActivity.this, "上传失败，请重新上传", Toast.LENGTH_SHORT).show();
                        funcTextView.setText("选择方式通过照片搜索朋友");
                        da.dismiss();
//                        searchImageText.setVisibility(View.INVISIBLE);
//                        pictureView.setVisibility(View.INVISIBLE);
                        Looper.loop();
                    }
                }
            }
        }.start();
    }

    //选择后照片的读取工作
    private UpLoadFileInfo handleSelect(Uri uri) {
        Cursor cursor = null;
        String uploadFileName = null;
        UpLoadFileInfo info = null;
        byte[] fileBuf;
        cursor = UploadImageActivity.this.getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            uploadFileName = cursor.getString(columnIndex);
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            fileBuf = convertToBytes(inputStream);
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
            info = new UpLoadFileInfo(fileBuf, uploadFileName);
            Dialog da = setSearchEject(bitmap);
            da.show();
//            searchImageText.setVisibility(View.VISIBLE);
//            pictureView.setVisibility(View.VISIBLE);
//            pictureView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
        return info;
    }

    //头像上传
    public void uploadToServer(final Dialog da, final byte[] fileBuf, final String uploadFileName, final String path) {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
                //读取超时
                clientBuilder.readTimeout(100, TimeUnit.SECONDS);
                //连接超时
                clientBuilder.connectTimeout(100, TimeUnit.SECONDS);
                //写入超时
                clientBuilder.writeTimeout(100, TimeUnit.SECONDS);
                //自定义连接池最大空闲连接数和等待时间大小，否则默认最大5个空闲连接
                clientBuilder.connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES));
                OkHttpClient client = clientBuilder.build();
                //上传文件域的请求体部分
                RequestBody formBody = RequestBody
                        .create(fileBuf, MediaType.parse("image/jpeg"));
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("username", MainActivity.loginUsername)
                        .addFormDataPart("image", uploadFileName, formBody)
                        .build();
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String registerRes = response.body().string();
                    //注册照片上传数据库是否成功
                    if (registerRes.equals("1")) {
                        //修改注册状态
                        MainActivity.isRegister = false;
                        UploadImageActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                pictureView.setVisibility(View.INVISIBLE);
//                                searchImageText.setVisibility(View.INVISIBLE);
                                da.dismiss();
                                Bitmap bm = BitmapFactory.decodeFile(path);
                                userHeadView.setImageBitmap(bm);
                                funcTextView.setText("选择方式通过照片搜索朋友");
                            }
                        });
                        Looper.prepare();
                        Toast.makeText(UploadImageActivity.this, "上传照片成功", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        UploadImageActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                funcTextView.setText("请重新选择图片上传，网络错误");
                                da.dismiss();
                            }
                        });
                        Toast.makeText(UploadImageActivity.this, "上传失败，请重新选择", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //从服务器拉取好友列表
    public void friendsFromServer(final String username) {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
                //读取超时
                clientBuilder.readTimeout(100, TimeUnit.SECONDS);
                //连接超时
                clientBuilder.connectTimeout(100, TimeUnit.SECONDS);
                //写入超时
                clientBuilder.writeTimeout(100, TimeUnit.SECONDS);
                //自定义连接池最大空闲连接数和等待时间大小，否则默认最大5个空闲连接
                clientBuilder.connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES));
                OkHttpClient client = clientBuilder.build();
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("username", username)
                        .build();
                Request request = new Request.Builder()
                        .url(friendListUrl)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    final String friend = response.body().string();
                    //将服务器传回的String分割
                    SplitFriends sp = new SplitFriends();
                    String[] friends = sp.split(friend);
                    //根据好友列表查找好友信息
                    if (!friend.equals("0")) {
                        if (!friend.isEmpty()) {
                            for (int i = 0; i < friends.length; i++) {
                                searchFromServer(friends[i], 1);
                            }
                            UploadImageActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //服务器搜索好友
    public void searchFromServerUrl(final List<String> searchName, final Dialog da) {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < searchName.size(); i++)
                    if(searchName.get(i) != null &&
                            !searchName.get(i).isEmpty() &&
                            !searchName.get(i).equals("null"))
                    searchFromServer(searchName.get(i), 0);
                UploadImageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                                pictureView.setVisibility(View.INVISIBLE);
//                                searchImageText.setVisibility(View.INVISIBLE);
//                                searchCardView.setVisibility(View.VISIBLE);
//                                searchNameText.setText("联系邮箱：" + ua);
                        funcTextView.setText("选择方式通过照片搜索朋友");
                        da.dismiss();
                        adapter.notifyDataSetChanged();

                    }
                });
            }
        }.start();
    }

    //服务器取图
    protected void searchFromServer(final String searchName, final int type) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        //读取超时
        clientBuilder.readTimeout(100, TimeUnit.SECONDS);
        //连接超时
        clientBuilder.connectTimeout(100, TimeUnit.SECONDS);
        //写入超时
        clientBuilder.writeTimeout(100, TimeUnit.SECONDS);
        //自定义连接池最大空闲连接数和等待时间大小，否则默认最大5个空闲连接
        clientBuilder.connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES));
        OkHttpClient client = clientBuilder.build();
        //整个上传的请求体部分（普通表单+文件上传域）
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", searchName)
                .build();
        Request request = new Request.Builder()
                .url(searchUrl)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            final String searchPath = response.body().string();
            //将取到的图片加入list
            if (type == 0) {
                infoList.add(0, new Information(searchName, searchPath, false));
            } else if (type == 1) {
                infoList.add(0, new Information(searchName, searchPath, true));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] convertToBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        return out.toByteArray();
    }

    //更改头像弹出框
    protected Dialog bottomDialog = null;

    public void showBottomEject(View view) {
        MainActivity.isRegister = true;
        bottomDialog = new Dialog(this, R.style.DialogTheme);
        bottomDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                MainActivity.isRegister = isRegister;
            }
        });
        //填充对话框的布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.layout_bottom_eject, null);
        //初始化控件
        ejectTakeBtn = inflate.findViewById(R.id.camera);
        ejectchoBtn = inflate.findViewById(R.id.pic);
        ejectCancelBtn = inflate.findViewById(R.id.cancel);
        ejectTakeBtn.setOnClickListener(takeBtnListener());
        ejectchoBtn.setOnClickListener(chooseBtnChoose());
        ejectCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        //将布局设置给Dialog
        bottomDialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = bottomDialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);
        bottomDialog.show();//显示对话框
    }

    //搜索弹出框
    public Dialog setSearchEject(Bitmap bm) {
        Dialog searchDialog = new Dialog(this, R.style.DialogSearchTheme);
        //填充对话框的布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.layout_search_eject, null);
        //初始化控件
        CircleImageView searchImageView = inflate.findViewById(R.id.searchImage);
        searchImageView.setImageResource(R.drawable.logo);
        if (bm != null) {
            searchImageView.setImageBitmap(bm);
        }
        //将布局设置给Dialog
        searchDialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = searchDialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.CENTER);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);
        return searchDialog;
    }
}
