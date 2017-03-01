package com.when0matters.nytimesarticlesearch.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.when0matters.nytimesarticlesearch.R;
import com.when0matters.nytimesarticlesearch.adapter.ArticleRecyclerAdapter;
import com.when0matters.nytimesarticlesearch.fragments.FilterFragment;
import com.when0matters.nytimesarticlesearch.listeners.EndlessRecyclerViewScrollListener;
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

//    @BindView(R.id.gv_result)
//    GridView gv_result;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.rv_result)
    RecyclerView rv_result;

    @BindView(R.id.swl_container)
    SwipeRefreshLayout swl_container;

    StaggeredGridLayoutManager staggeredGridLayoutManager;
    final String API_KEY = "c7e3bfb5b22a4b24b495485db61becaf";

    ArrayList<Article> articles;
    //ArticleArrayAdapter adapters;
    ArticleRecyclerAdapter adapters;
    String search_query;
    String search_begin_date;
    String search_sort_order;
    String search_news_desk;
    SwipeRefreshLayout.OnRefreshListener swipeRefreshLayoutListener;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        articles = new ArrayList<>();
        adapters = new ArticleRecyclerAdapter(articles,this);
        bundle = new Bundle();
        // First param is number of columns and second param is orientation i.e Vertical or Horizontal
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rv_result.setLayoutManager(staggeredGridLayoutManager);
        rv_result.setAdapter(adapters);
        rv_result.addOnScrollListener(new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager){
            @Override
            public void onLoadMore(int page, int totalItemsCount,RecyclerView recyclerView) {
               loadNextDataFromApi(page);
            }
        });

        loadNextDataFromApi(0);

        swipeRefreshLayoutListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadNextDataFromApi(0);
            }
        };
        swl_container.setOnRefreshListener(swipeRefreshLayoutListener);

//        rv_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //create an intent to display the articvle
//                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
//                //get the article to display
//                Article article = articles.get(position);
//                //pass in that article into intent
//                i.putExtra("article", article);
//                //launch the activity
//                startActivity(i);
//            }
//        });

//        rv_result.setOnScrollListener(new EndlessScrollListener() {
//            @Override
//            public boolean onLoadMore(int page, int totalItemsCount) {
//                loadNextDataFromApi(page, search_query);
//                return true;
//            }
//        });
    }

    public void loadNextDataFromApi(final int offset) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

        RequestParams params = new RequestParams();
        params.put("api-key" , API_KEY);
        params.put("page" , offset);
        if (search_query != null)
            params.put("q", search_query);

        if (search_news_desk != null && !search_news_desk.isEmpty())
            params.put("fq", search_news_desk);
        if (search_sort_order!= null)
            params.put("sort", search_sort_order);
        if (search_begin_date!= null)
            params.put("begin_date",search_begin_date);

        Log.d("Debug", url + params.toString());
        client.get(url,params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Debug", response.toString());
                JSONArray articleJsonResults = null;
                try{
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    if (offset == 0){
                        int prevSize = adapters.itemsDataset.size();
                        adapters.itemsDataset.clear();
                        adapters.notifyItemRangeRemoved(0,prevSize);
                    }
                    // record this value before making any changes to the existing list
                    int curSize = adapters.getItemCount();
                    //modify the content in adapter directly, save a step in notifying the adapter
                    adapters.itemsDataset.addAll(Article.fromJSONArray(articleJsonResults));
                    // curSize should represent the first element that got added
                    // newItems.size() represents the itemCount
                    adapters.notifyItemRangeInserted(curSize, adapters.itemsDataset.size());
                    Log.d("Debug", articles.toString());
                    swl_container.setRefreshing(false);

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
        final EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        ImageView searchClearIV = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClearIV.setImageResource(R.drawable.ic_action_cancel);

        searchClearIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_query = null;
                searchEditText.setText("");
            }
        });

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
                RefreshPage();
                //loadNextDataFromApi(0,search_query);
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
                filterFragment.setArguments(bundle);
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


        bundle.putString("beginDate", search_begin_date);
        bundle.putString("sortOrder", search_sort_order);
        bundle.putBoolean("isArts",isArts);
        bundle.putBoolean("isFashionStyle",isFashionStyle);
        bundle.putBoolean("isSports",isSports);

        search_query = null;
        RefreshPage();
        //loadNextDataFromApi(0,null);
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

        if (isSports){
            newdesk_input += "\"Sports\"";
            noItemChecked = false;
        }

        newdesk_input += ")";

        if (!noItemChecked)
            return newdesk_input;
        else
            return "";
    }

    public void RefreshPage(){

        swl_container.post(new Runnable(){

            @Override
            public void run(){
                swl_container.setRefreshing(true);
                swipeRefreshLayoutListener.onRefresh();
            }
        });
    }

}
