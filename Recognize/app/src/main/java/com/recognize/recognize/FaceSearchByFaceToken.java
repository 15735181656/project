package com.recognize.recognize;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.recognize.utils.GsonUtils;
import com.recognize.utils.HttpUtil;

public class FaceSearchByFaceToken {
	public String imgBase64;
	public FaceSearchByFaceToken(String imgBase64) {
		this.imgBase64 = imgBase64;
	}
	@SuppressWarnings("unchecked")
	public String getUserId(String result) {
		Gson gson = new Gson();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1 = gson.fromJson(result, map1.getClass());
        String error_msg = (String) map1.get("error_msg");
        if(error_msg.equals("SUCCESS")) {
        	String sub_result = result.substring(result.indexOf('[')+1,result.indexOf(']'));
        	Map<String, Object> map2 = new HashMap<String, Object>();
        	map2 = gson.fromJson(sub_result, map2.getClass());
        	String user_id = (String) map2.get("user_info");
        	return user_id;
        }else
        	return null;
	}
    public String faceSearch() {
        // ����url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/search";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", imgBase64);
            map.put("liveness_control", "NORMAL");
            map.put("group_id_list", "repository");
            map.put("image_type", "FACE_TOKEN");
            map.put("quality_control", "LOW");

            String param = GsonUtils.toJson(map);

            // ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬 �ͻ��˿����л��棬���ں����»�ȡ��
            String accessToken = "24.4475ae54ab9e707cf7a4fa9d56b89cc7.2592000.1575633574.282335-17710479";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            
            return getUserId(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
