package com.when0matters.nytimesarticlesearch.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.when0matters.nytimesarticlesearch.R;
import com.when0matters.nytimesarticlesearch.activities.ArticleActivity;
import com.when0matters.nytimesarticlesearch.models.Article;

import java.util.List;

/**
 * Created by dongdong on 2/28/2017.
 */

public class ArticleRecyclerAdapter  extends
        RecyclerView.Adapter<ArticleRecyclerAdapter.ViewHolder> implements View.OnClickListener  {

    public List<Article> itemsDataset;
    private Context mContext;

    public ArticleRecyclerAdapter(List<Article> itemsDataset, Context mContext){
        this.itemsDataset = itemsDataset;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_article_result, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article current_item = itemsDataset.get(position);
        holder.tv_title.setText(current_item.getHeadline());
        holder.tv_lead_paragraph.setText(current_item.getLead_paragraph());
        holder.tv_news_desk.setText(current_item.getNews_desk());
        String thumbnail = current_item.getThumbnail();
        holder.cv_container.setTag(position);
        holder.cv_container.setOnClickListener(this);
        if (!TextUtils.isEmpty(thumbnail)){
            Picasso.with(mContext).load(thumbnail).into(holder.iv_thumbnail);
        }

    }

    @Override
    public int getItemCount() {
        return itemsDataset.size();
    }

    @Override
    public void onClick(View view) {

        int position = (int)view.getTag();
        //create an intent to display the articvle
        Intent i = new Intent(mContext, ArticleActivity.class);
//                //get the article to display
        Article article = itemsDataset.get(position);
//                //pass in that article into intent
       i.putExtra("article", article);
//                //launch the activity
        mContext.startActivity(i);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title;
        TextView tv_news_desk;
        TextView tv_lead_paragraph;
        ImageView iv_thumbnail;
        CardView cv_container;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_news_desk = (TextView) itemView.findViewById(R.id.tv_news_desk);
            tv_lead_paragraph = (TextView) itemView.findViewById(R.id.tv_lead_paragraph);
            iv_thumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            cv_container = (CardView) itemView.findViewById(R.id.cv_container);
        }
    }
}
