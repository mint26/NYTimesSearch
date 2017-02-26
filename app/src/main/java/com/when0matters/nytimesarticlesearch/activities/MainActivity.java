package com.when0matters.nytimesarticlesearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.when0matters.nytimesarticlesearch.R;
import com.when0matters.nytimesarticlesearch.adapter.ArticleArrayAdapter;
import com.when0matters.nytimesarticlesearch.fragments.FilterFragment;
import com.when0matters.nytimesarticlesearch.listeners.EndlessScrollListener;
import com.when0matters.nytimesarticlesearch.models.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements FilterFragment.EditNameDialogListener{

    @BindView(R.id.gv_result)
    GridView gv_result;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    final String API_KEY = "c7e3bfb5b22a4b24b495485db61becaf";

    ArrayList<Article> articles;
    ArticleArrayAdapter adapters;
    String search_query;
    String search_begin_date;
    String search_sort_order;
    String search_news_desk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        articles = new ArrayList<>();
        adapters = new ArticleArrayAdapter(this,articles);
        gv_result.setAdapter(adapters);
        gv_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create an intent to display the articvle
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                //get the article to display
                Article article = articles.get(position);
                //pass in that article into intent
                i.putExtra("article", article);
                //launch the activity
                startActivity(i);
            }
        });

        gv_result.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                loadNextDataFromApi(page, search_query);
                return true;
            }
        });
    }

    public void loadNextDataFromApi(int offset, String query) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

        RequestParams params = new RequestParams();
        params.put("api-key" , API_KEY);
        params.put("page" , offset);
        params.put("q", query);

        if (!search_news_desk.isEmpty())
            params.put("fq", search_news_desk);
        if (!search_sort_order.isEmpty())
            params.put("sort", search_sort_order);
        if (!search_begin_date.isEmpty())
            params.put("begin_date",search_begin_date);

        client.get(url,params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Debug", response.toString());
                JSONArray articleJsonResults = null;
                try{
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    //modify the content in adapter directly, save a step in notifying the adapter
                    adapters.addAll(Article.fromJSONArray(articleJsonResults));
                    Log.d("Debug", articles.toString());
                }
                catch(JSONException ex){

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("Debug", errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.miSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        ImageView searchClearIV = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClearIV.setImageResource(R.drawable.ic_action_cancel);
        searchEditText.setTextColor(getResources().getColor(R.color.colorText));
        searchEditText.setHintTextColor(getResources().getColor(R.color.colorText));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!isNetworkAvailable()){
                    Toast.makeText(getApplicationContext(),"Network connectivity error",Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isOnline()){
                    Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
                    return false;
                }
                search_query = query;
                loadNextDataFromApi(0,search_query);
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miSearch:
                item.expandActionView();
                return true;
            case R.id.miFilter:
                FragmentManager fragmentManager = getSupportFragmentManager();
                FilterFragment filterFragment = new FilterFragment();
                filterFragment.show(fragmentManager,null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void onFinishEditDialog(String beginDate, String sortOrder, boolean isArts, boolean isFashionStyle, boolean isSports) {
        search_begin_date = beginDate;
        search_sort_order = sortOrder;
        search_news_desk = formatNewsDeskInput(isArts,isFashionStyle,isSports);
    }

    public String formatNewsDeskInput(boolean isArts, boolean isFashionStyle, boolean isSports){
        String newdesk_input = "news_desk:(";
        boolean noItemChecked = true;
        if (isArts){
            newdesk_input += "\"Arts\" ";
            noItemChecked = false;
        }

        if (isFashionStyle){
            newdesk_input += "\"Fashion %amp Style\" ";
            noItemChecked = false;
        }

        if (isArts){
            newdesk_input += "\"Sports\"";
            noItemChecked = false;
        }

        newdesk_input += ")";

        if (!noItemChecked)
            return newdesk_input;
        else
            return "";
    }


}
