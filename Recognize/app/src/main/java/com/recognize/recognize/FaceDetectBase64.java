package com.recognize.recognize;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.recognize.utils.Base64Util;
import com.recognize.utils.FileUtil;
import com.recognize.utils.GsonUtils;
import com.recognize.utils.HttpUtil;

public class FaceDetectBase64 {

    public String imgBase64;

    public FaceDetectBase64(String imgFile) {
        this.imgBase64 = imgFile;
    }


    @SuppressWarnings("unchecked")
    public List<String> faceDetect() {

        // ����url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", imgBase64);
//            map.put("face_field", "faceshape,facetype");
            map.put("image_type", "BASE64");
            map.put("liveness_control", "NORMAL");
            map.put("max_face_num", 10);
            String param = GsonUtils.toJson(map);

            // ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬 �ͻ��˿����л��棬���ں����»�ȡ��
            String accessToken = "24.4475ae54ab9e707cf7a4fa9d56b89cc7.2592000.1575633574.282335-17710479";

            String result = HttpUtil.post(url, accessToken, "application/json", param);


            List<String> userId = new ArrayList<>();
            Log.d("12312312312431423:fa1", userId.size() + "");
            Gson gson = new Gson();
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1 = gson.fromJson(result, map1.getClass());

            String error_msg = (String) map1.get("error_msg");
            if (error_msg.equals("SUCCESS")) {
                Map<String, Object> link = (HashMap<String, Object>) map1.get("result");
                List<Map<String, Object>> list = (List<Map<String, Object>>) link.get("face_list");
                for (Map<String, Object> l : list) {
                    FaceSearchByFaceToken search = new FaceSearchByFaceToken((String) l.get("face_token"));
                    if (!search.faceSearch().isEmpty()){
                        userId.add(search.faceSearch());
                    }
                    Thread.sleep(1000);
                }
            }
            return userId;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> searchFace(String path) {
        byte[] imageDate = {};
        try {
            imageDate = FileUtil.readFileByBytes(path);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        //���ֽ�ת��base64��ʽ
        String imageBase64 = Base64Util.encode(imageDate);
        FaceDetectBase64 test = new FaceDetectBase64(imageBase64);
        return test.faceDetect();
    }
}
