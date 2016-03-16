package com.yash.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Map<Integer,String> articleTitles = new HashMap<Integer,String>();
    Map<Integer,String> articleURLs = new HashMap<Integer,String>();
    ArrayList<Integer> articleIDs = new ArrayList<Integer>();
    SQLiteDatabase articleDB ;
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> urlsList = new ArrayList<String>();
    //ArrayList<String> content = new ArrayList<String>();
    ArrayAdapter arrayAdapter;

    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }



                //code move
                JSONArray jsonArray = new JSONArray(result);

                for(int i=0;i<20;i++){

                 url = new  URL("https://hacker-news.firebaseio.com/v0/item/"+jsonArray.getString(i)+".json?print=pretty");
                 urlConnection = (HttpURLConnection) url.openConnection();
                 in = urlConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();

                    String articleInfo = "";

                    while(data != -1){
                        char current = (char) data;
                        articleInfo += current;
                        data = reader.read();
                    }


                    JSONObject jsonObject = new JSONObject(articleInfo);
                    String articleID = jsonArray.getString(i);
                    String articleTitle = jsonObject.getString("title");
                    String articleURL = jsonObject.getString("url");

                    //code add
                    /*url = new  URL(articleURL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();

                    String articleContent = "";

                    while(data != -1){
                        char current = (char) data;
                        articleContent += current;
                        data = reader.read();
                    }

*/
                    //code add
                    articleIDs.add(Integer.valueOf(articleID));
                    articleTitles.put(Integer.valueOf(articleID), articleTitle);
                    articleURLs.put(Integer.valueOf(articleID), articleURL);



                    String sql = "INSERT into articles(articleId,url,title) VALUES(?,?,?)";
                    SQLiteStatement statement = articleDB.compileStatement(sql);

                    statement.bindString(1, articleID);
                    statement.bindString(2, articleURL);
                    statement.bindString(3, articleTitle);

                    statement.execute();

                    //articleDB.execSQL("INSERT into articles(articleId,url,title) VALUES("+ articleID+ ",'"+ articleURL+ "','"+ articleTitle+"')");
                }
                //code move
            }
            catch(Exception e){
                e.printStackTrace();
            }


            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView listView = (ListView) findViewById(R.id.mylist);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,titles);
        listView.setAdapter(arrayAdapter);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // Log.i("whatThe", urlsList.get(position));
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                i.putExtra("articleURL", urlsList.get(position));
                //i.putExtra("articleContent",content.get(position));
                startActivity(i);
            }
        });

        articleDB = this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articleDB.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY,articleId INTEGER, url VARCHAR, title VARCHAR, content VARCHAR)");

        updateListView();
        //remove
        articleDB.execSQL("DELETE FROM articles");
        DownloadTask task = new DownloadTask();
        //task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

          /*  JSONArray jsonArray = new JSONArray(result);

            for(int i=0;i<20;i++){
                DownloadTask getArticle = new DownloadTask();
                String articleInfo = getArticle.execute("https://hacker-news.firebaseio.com/v0/item/"+jsonArray.getString(i)+".json?print=pretty").get();

                JSONObject jsonObject = new JSONObject(articleInfo);
                String articleID = jsonArray.getString(i);
                String articleTitle = jsonObject.getString("title");
                String articleURL = jsonObject.getString("url");

                articleIDs.add(Integer.valueOf(articleID));
                articleTitles.put(Integer.valueOf(articleID), articleTitle);
                articleURLs.put(Integer.valueOf(articleID), articleURL);



                String sql = "INSERT into articles(articleId,url,title) VALUES(?,?,?)";
                SQLiteStatement statement = articleDB.compileStatement(sql);

                statement.bindString(1, articleID);
                statement.bindString(2, articleURL);
                statement.bindString(3, articleTitle);
                statement.execute();

                //articleDB.execSQL("INSERT into articles(articleId,url,title) VALUES("+ articleID+ ",'"+ articleURL+ "','"+ articleTitle+"')");
            }*/

            //updateListView();
        } catch (Exception e) {
            //arrayAdapter.notifyDataSetChanged();
            e.printStackTrace();
        }
    }

    public void updateListView(){
        try {
            Log.i("whatThe","Done");
            Cursor c = articleDB.rawQuery("SELECT * FROM articles ORDER BY articleId DESC", null);
            int contentIndex = c.getColumnIndex("content");
            int urlIndex = c.getColumnIndex("url");
            int titleIndex = c.getColumnIndex("title");
            titles.clear();
            c.moveToFirst();
            for (int i = 0; i < 20; i++) {
                titles.add(c.getString(titleIndex));
                urlsList.add(c.getString(urlIndex));
                //content.add(c.getString(contentIndex));
                c.moveToNext();
            }
            arrayAdapter.notifyDataSetChanged();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
