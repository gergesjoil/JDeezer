package com.tools;

import org.json.JSONException;
import org.json.JSONObject;

public class track_formats {
    String FLAC = "FLAC";
    String MP3_128 = "MP3_128";
    String MP3_256 = "MP3_256";
    String MP3_320 = "MP3_320";
    String MP4_RA1 = "MP4_RA1";
    String MP4_RA2 = "MP4_RA2";
    String MP4_RA3 = "MP4_RA3";

    String[] FALLBACK_QUALITIES = {MP3_320, MP3_128, FLAC};
    String[] FORMAT_LIST = {MP3_128, MP3_256, MP3_320, FLAC};

    public JSONObject TRACK_FORMAT_MAP() throws JSONException {
        JSONObject TRACK_FORMAT_MAP = new JSONObject();
        JSONObject FLAC = new JSONObject().put("code", 9).put("ext", ".flac");
        JSONObject MP3_128 = new JSONObject().put("code", 1).put("ext", ".mp3");
        JSONObject MP3_256 = new JSONObject().put("code", 5).put("ext", ".mp3");
        JSONObject MP3_320 = new JSONObject().put("code", 3).put("ext", ".mp3");
        JSONObject MP4_RA1 = new JSONObject().put("code", 13).put("ext", ".mp4");
        JSONObject MP4_RA2 = new JSONObject().put("code", 14).put("ext", ".mp4");
        JSONObject MP4_RA3 = new JSONObject().put("code", 15).put("ext", ".mp4");
        TRACK_FORMAT_MAP.put("FLAC", FLAC);
        TRACK_FORMAT_MAP.put("MP3_128", MP3_128);
        TRACK_FORMAT_MAP.put("MP3_256", MP3_256);
        TRACK_FORMAT_MAP.put("MP3_320", MP3_320);
        TRACK_FORMAT_MAP.put("MP4_RA1", MP4_RA1);
        TRACK_FORMAT_MAP.put("MP4_RA2", MP4_RA2);
        TRACK_FORMAT_MAP.put("MP4_RA3", MP4_RA3);
        return TRACK_FORMAT_MAP;
    }
}
