package com.when0matters.nytimesarticlesearch.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.when0matters.nytimesarticlesearch.R;
import com.when0matters.nytimesarticlesearch.models.Article;

import java.util.List;

/**
 * Created by dongdong on 2/25/2017.
 */

public class ArticleArrayAdapter extends ArrayAdapter<Article> {

    public ArticleArrayAdapter(Context context, List<Article> articles){
        super(context, R.layout.item_article_result, articles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the data item for position
        Article article = this.getItem(position);

        //check to see if existing view being reused
        //not using a recyled view --> inflate the layout.
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_article_result, parent,false);
        }

        //find the imageview
        ImageView iv_thumbnail = (ImageView)convertView.findViewById(R.id.iv_thumbnail);
        iv_thumbnail.setImageResource(0);
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        tv_title.setText(article.getHeadline());

        String thumbnail = article.getThumbnail();

        if (!TextUtils.isEmpty(thumbnail)){
            Picasso.with(getContext()).load(thumbnail).into(iv_thumbnail);
        }
        return convertView;
    }
}
