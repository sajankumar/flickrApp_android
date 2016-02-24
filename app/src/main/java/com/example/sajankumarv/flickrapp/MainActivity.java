package com.example.sajankumarv.flickrapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sajankumarv.girdDetailActivity.DetailActivity;
import com.example.sajankumarv.gridAdapter.GridItem;
import com.example.sajankumarv.gridAdapter.GridViewContainer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewContainer mGridAdapter;
    private Button searchBtn;
    private EditText searchBox;
    private ArrayList<GridItem> mGridData;
    private  AsyncHttpTask task;
    private String FEED_URL = "http://api.flickr.com/services/feeds/photos_public.gne?format=json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBox = (EditText) findViewById(R.id.searchBox);

        init();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                //Pass the image title and url to DetailsActivity
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });

        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBox.setFocusableInTouchMode(true);
                searchBox.setFocusable(true);
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags = searchBox.getText().toString();
                FEED_URL = FEED_URL.concat("&tags="+ tags);
                mProgressBar.setVisibility(View.VISIBLE);
                init();

            }
        });
    }

    private void init(){

        if(mGridAdapter != null && mGridData != null){
            mGridAdapter = null;
            mGridData = null;
        }
        //Initialize with empty data
        task = new AsyncHttpTask();
        searchBox.setFocusable(false);
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewContainer(this, R.layout.grid_view_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);
        task.execute(FEED_URL);
        mProgressBar.setVisibility(View.VISIBLE);
    }
    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(httpResponse.getEntity().getContent());
                    Log.d("response : ", response);
                    parseResult(response);
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }

    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     * @param result
     */
    private void parseResult(String result) {
        try {
            String removeJsonP = result.substring(15, result.length() - 1);
            Log.d("substring",removeJsonP);


            JsonElement jparser = new JsonParser().parse(removeJsonP);
            JsonObject jobject = jparser.getAsJsonObject();
            Log.d("Convert", jobject.toString());

            JsonArray posts = jobject.get("items").getAsJsonArray();
            Log.d("posts", String.valueOf(posts.size()));

            GridItem item;
            for (int i = 0; i < posts.size(); i++) {
                JsonElement post = posts.get(i);
                JsonObject fObj  = post.getAsJsonObject();
                String title = fObj.get("title").toString();
                item = new GridItem();
                item.setTitle(title);

                JsonObject attachment = fObj.get("media").getAsJsonObject();
                    if (attachment != null){
                        item.setImage(attachment.get("m").toString());
                        Log.d("Media URL : ", attachment.get("m").toString());

                    }
                mGridData.add(item);
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }
}
