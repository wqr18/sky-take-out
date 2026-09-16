package com.sky.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 高德地图工具类
 */
public class AmapUtil {

    private static final String GEO_CODE_URL = "https://restapi.amap.com/v3/geocode/geo";

    private static final double EARTH_RADIUS = 6371000;

    /**
     * 地理编码：将地址文本转换为经纬度
     *
     * @param address 地址文本
     * @param key     高德地图 key
     * @return [经度, 纬度]，失败返回 null
     */
    public static double[] geocode(String address, String key) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("key", key);
        paramMap.put("address", address);

        String result = HttpClientUtil.doGet(GEO_CODE_URL, paramMap);
        if (result == null || result.isEmpty()) {
            return null;
        }

        JSONObject jsonObject = JSONObject.parseObject(result);
        if (!"1".equals(jsonObject.getString("status"))) {
            return null;
        }

        JSONArray geocodes = jsonObject.getJSONArray("geocodes");
        if (geocodes == null || geocodes.isEmpty()) {
            return null;
        }

        String location = geocodes.getJSONObject(0).getString("location");
        if (location == null || location.isEmpty()) {
            return null;
        }

        String[] lngLat = location.split(",");
        return new double[]{Double.parseDouble(lngLat[0]), Double.parseDouble(lngLat[1])};
    }

    /**
     * 计算两个经纬度之间的直线距离（Haversine 公式）
     *
     * @param lng1 点1经度
     * @param lat1 点1纬度
     * @param lng2 点2经度
     * @param lat2 点2纬度
     * @return 距离（单位：米）
     */
    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1 - lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s;
    }
}