package com.when0matters.nytimesarticlesearch.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by dongdong on 2/25/2017.
 */

public class Article implements Serializable {


    String webUrl;
    String headline;
    String thumbnail;


    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Article(JSONObject jsonObject){
        try{
            this.webUrl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");

            JSONArray multimedia = jsonObject.getJSONArray("multimedia");

            if (multimedia.length() > 0){
                JSONObject multimediaJson = multimedia.getJSONObject(0);
                this.thumbnail = "http://www.nytimes.com/" + multimediaJson.getString("url");
            }
            else{
                this.thumbnail = "";
            }

        }catch(JSONException ex){

        }

    }

    public static ArrayList<Article> fromJSONArray(JSONArray jsonArray){
        ArrayList<Article> results = new ArrayList<>();
        for (int x = 0; x < jsonArray.length(); x++){
            try{
                results.add(new Article(jsonArray.getJSONObject(x)));
            }catch(JSONException ex){

            }
        }
        return results;
    }

}
