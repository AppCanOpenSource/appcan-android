package org.zywx.wbpalmstar.engine;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;

/**
 * Created by ylt on 2015/4/28.
 */
public class DataHelper {

    public static Gson gson = new Gson();

    /**
     * 转换成适合阅读的Json
     */
    public static String toPrettyJson(String json){
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.toString(4);
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                return jsonArray.toString(4);
            }
        } catch (JSONException e) {
            if (BDebug.DEBUG){
                e.printStackTrace();
            }
        }
        return json;
    }

}

