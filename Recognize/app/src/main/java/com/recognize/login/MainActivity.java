package com.recognize.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.recognize.recognize.R;
import com.recognize.recognize.UploadImageActivity;
import com.recognize.utils.KeyboardUtil;
import com.recognize.utils.StatusBarUtil;
import com.recognize.utils.UrlJudgeUntil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private EditText usernameEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private Button registerBtn;
    public static String loginUrl = "http://182.92.165.130:3000/up";

    public boolean accessible = false; //注册/登录是否成功
    public static String loginUsername = "rIc";
    public static String UserHeadUrL = "";
    public static boolean isRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        initData();
    }

    private void initData() {
        usernameEdit = findViewById(R.id.username);
        passwordEdit = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);
//        usernameEdit.setOnFocusChangeListener(edtClear());
//        passwordEdit.setOnFocusChangeListener(edtClear());

        //登录监听
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                测试可关闭
//                isRegister = false;
//                Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
//                startActivity(intent);
                //关闭软键盘
                KeyboardUtil.hideSoftKeyboard(MainActivity.this, usernameEdit);
                KeyboardUtil.hideSoftKeyboard(MainActivity.this, passwordEdit);
                //判断用户名密码
                if (usernameEdit.getText().length() == 0 || passwordEdit.getText().length() == 0) {
                    Toast.makeText(MainActivity.this, "用户名或密码为空，请重新输入", Toast.LENGTH_SHORT).show();
                } else
                    upload(usernameEdit.getText().toString(), passwordEdit.getText().toString(), "1", 1);
            }
        });

        //注册监听
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                测试可关闭
//                isRegister = true;
//                Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
//                startActivity(intent);
                //关闭软键盘
                KeyboardUtil.hideSoftKeyboard(MainActivity.this, usernameEdit);
                KeyboardUtil.hideSoftKeyboard(MainActivity.this, passwordEdit);
                //判断输入是否规范
                if (usernameEdit.getText().length() == 0 || passwordEdit.getText().length() == 0) {
                    Toast.makeText(MainActivity.this, "用户名或密码为空，注册请在上方输入框直接输入后点击注册", Toast.LENGTH_SHORT).show();
                } else {
                    if (isEmail(usernameEdit.getText().toString())) {
                        upload(usernameEdit.getText().toString(), passwordEdit.getText().toString(), "0", 0);
                    } else {
                        Toast.makeText(MainActivity.this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //点击清除editText
//    private View.OnFocusChangeListener edtClear() {
//        View.OnFocusChangeListener ofc = new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    if (v.getId() == passwordEdit.getId())
//                        ((EditText) v).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                    if (((EditText) v).getText().toString().equals("请输入密码") || ((EditText) v).getText().toString().equals("请输入邮箱"))
//                        ((EditText) v).setText("");
//                } else {
//                    if (((EditText) v).length() == 0) {
//                        if (v.getId() == passwordEdit.getId()) {
//                            ((EditText) v).setInputType(InputType.TYPE_CLASS_TEXT);
//                            ((EditText) v).setText("请输入密码");
//                        } else
//                            ((EditText) v).setText("请输入邮箱");
//                    }
//                }
//            }
//        };
//        return ofc;
//    }

    public void upload(final String username, final String password, String isLogin, final int type) {
        final String ua = username;
        final String pa = password;
        final String isLog = isLogin;
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                //整个上传的请求体部分（普通表单+文件上传域）

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", isLog)
                        .addFormDataPart("username", ua)
                        .addFormDataPart("password", pa)
                        .build();
                Request request = new Request.Builder()
                        .url(loginUrl)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String re = response.body().string();
                    Log.d("1231231", re);
                    if (re.equals("0")) {
                        accessible = false;
                    } else {
                        accessible = true;
                    }
                    if (type == 0) {
                        //注册是否成功
                        if (accessible) {
                            accessible = false;
                            loginUsername = ua;
                            isRegister = true;
                            Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else {
                            accessible = false;
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "用户名已存在，请重新输入", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        //登录是否成功
                        if (accessible) {
                            accessible = false;
                            loginUsername = ua;
                            if (UrlJudgeUntil.isHttpUrl(re)) {
                                UserHeadUrL = re;
                                isRegister = false;
                                Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
                                startActivity(intent);
                            } else {
                                isRegister = true;
                                Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
                                startActivity(intent);
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "请重新上传照片以完成注册", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } else {
                            accessible = false;
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //判断是否为邮箱
    public static boolean isEmail(String string) {
        if (string == null)
            return false;
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches())
            return true;
        else
            return false;
    }
}
