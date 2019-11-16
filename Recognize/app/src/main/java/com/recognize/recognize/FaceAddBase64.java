package com.recognize.recognize;

import android.util.Log;

import com.google.gson.Gson;
import com.recognize.utils.Base64Util;
import com.recognize.utils.FileUtil;
import com.recognize.utils.GsonUtils;
import com.recognize.utils.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FaceAddBase64 {
    public String imgBase64;
    public String userId;

    public String userInfo;

    public FaceAddBase64(String imgBase64, String userId, String userInfo) {
        this.imgBase64 = imgBase64;
        this.userId = userId;

        this.userInfo = userInfo;

    }
    @SuppressWarnings("unchecked")
    static public double getError_code(String result) {
        //String sub_result = result.substring(result.indexOf('[')+1,result.indexOf(']'));
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<String, Object>();
        map = gson.fromJson(result, map.getClass());
        double error_code = (double) map.get("error_code");
        return error_code;
    }
    public double add() {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", imgBase64);
            map.put("image_type", "BASE64");
            map.put("group_id", "repository");
            map.put("user_id", userId);
            map.put("user_info", userInfo);
            map.put("liveness_control", "NORMAL");
            map.put("quality_control", "LOW");
            map.put("action_type", "REPLACE");

            String param = GsonUtils.toJson(map);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.4475ae54ab9e707cf7a4fa9d56b89cc7.2592000.1575633574.282335-17710479";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            return getError_code(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int groupGetusers() {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/getusers";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("group_id", "repository");

            String param = GsonUtils.toJson(map);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.4475ae54ab9e707cf7a4fa9d56b89cc7.2592000.1575633574.282335-17710479";

            String r = HttpUtil.post(url, accessToken, "application/json", param);

            String sub = r.substring(r.indexOf('[')+1, r.indexOf(']'));
            if(sub.isEmpty()) {
                return 1;
            }else {
                String[] str = sub.split(",");

                return str.length + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static double addFace(String path, String username) {
        int usernum =  groupGetusers();
        String userId = String.valueOf(usernum);
		byte[] imageDate = {};
		try {
			imageDate = FileUtil.readFileByBytes(path);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        //���ֽ�ת��base64��ʽ
        String imageBase64 = Base64Util.encode(imageDate);
        Log.d("12312312312",imageBase64);
        FaceAddBase64 test = new FaceAddBase64(imageBase64, userId, username);
//        test.add();
        Log.d("12312312312"," " + test.add());
        return test.add();
	}
}
