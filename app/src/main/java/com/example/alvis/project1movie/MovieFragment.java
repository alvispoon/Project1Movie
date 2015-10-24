package com.example.alvis.project1movie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private String APIKEY = "";  //Enter APIKEY
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private ArrayAdapter<HashMap> movieAdaptor;

    public MovieFragment() {
    }


    private void updateMovie(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_default) );
        FetchMovieTask movieTask = new FetchMovieTask();

        movieTask.execute(sort);

    }

    public void onStart(){
        super.onStart();
        updateMovie();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*String[] movieArray = new String[]{
                "StarWar",
                "MI2",
                "MI3",
                "God",
                "Peter",
                "YEAH",
                "Summer"

        };*/
        //ArrayList<String> mMovieList= new ArrayList<String>(Arrays.asList(movieArray));

        ArrayList<HashMap> mMovieList = new ArrayList<>();


        movieAdaptor = new ImageAdapter(getActivity(), R.layout.list_item_movie,mMovieList);


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        gridView.setAdapter(movieAdaptor);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap movie = movieAdaptor.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra("map", movie);
                startActivity(intent);
                //Toast.makeText(getActivity(),movie, Toast.LENGTH_SHORT ).show();
            }
        });
        return rootView;
    }

    private HashMap[] getMovieDatafromJson(String movieJsonStr) throws JSONException{
        final String OWM_RESULTS = "results";
        final String OWM_TITLE = "original_title";
        final String OWM_PATH = "poster_path";
        final String OWM_RELEASE = "release_date";
        final String OWM_VOTE = "vote_average";
        final String OWM_SYNOPSIS = "overview";

        final String baseURL = "http://image.tmdb.org/t/p/w185";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray (OWM_RESULTS);
        HashMap[] resultStrs  = new HashMap[20];
        for (int i=0; i<movieArray.length(); i++) {
            HashMap<String, String> HMmovie = new HashMap<>();

            String path;
            String title;
            String release;
            String vote;
            String synopsis;

            JSONObject movie = movieArray.getJSONObject(i);
            path = movie.getString(OWM_PATH);
            title = movie.getString(OWM_TITLE);
            release = movie.getString(OWM_RELEASE);
            vote = movie.getString(OWM_VOTE);
            synopsis = movie.getString(OWM_SYNOPSIS);



            HMmovie.put("PATH", baseURL+path);
            HMmovie.put("TITLE", title);
            HMmovie.put("RELEASE", release);
            HMmovie.put("VOTE", vote);
            HMmovie.put("SYNOPSIS", synopsis);


            //resultStrs[i] = title + " - " + path ;

            resultStrs[i] = HMmovie;
        }

        return resultStrs;
    }

    class ImageAdapter extends ArrayAdapter<HashMap> {

        private Context mContext;
        private int mlayoutResourceId;
        LayoutInflater inflater;
        private ArrayList<HashMap> mitems = new ArrayList<>();

        public ImageAdapter(Context context, int layoutResourceId, ArrayList<HashMap> items) {
            super(context, layoutResourceId, items);
            mContext = context;
            mlayoutResourceId = layoutResourceId;
            mitems = items;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mitems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null){
                imageView = (ImageView)inflater.inflate(mlayoutResourceId, null);
            } else {
                imageView = (ImageView)convertView;
            }

            HashMap movie = mitems.get(position);
            String image =  (String)movie.get("PATH");

            Picasso.with(mContext).load(image).into(imageView);

            return imageView;


        }
    }






    public class FetchMovieTask extends AsyncTask<String, Void, HashMap[]>{

        protected void onPostExecute(HashMap[] hashmaps) {
            Log.v(LOG_TAG, "OnPostExecute" + hashmaps.length);
            movieAdaptor.clear();
            for (HashMap moviehm : hashmaps){
                //String movieurl = (String)moviehm.get("PATH");
                movieAdaptor.add(moviehm);
            }
        }

        @Override
        protected HashMap[] doInBackground(String... params) {
            //if (params.length ==0) {
            //    return null;
           // }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            try{
                final String baseURL = "http://api.themoviedb.org/3/discover/movie?";
                final String KEY_PARAM = "api_key";
                final String SORT_PARAM = "sort_by";
                Uri buildUri = Uri.parse(baseURL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, APIKEY)
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .build();
                URL url = new URL(buildUri.toString());
                Log.v(LOG_TAG, "Built URI"+ buildUri.toString());

                urlConnection  = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader (new InputStreamReader(inputStream));
                String line;
                while ((line=reader.readLine())!=null){
                    buffer.append (line+"\n");
                }
                if (buffer.length()==0){
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "JSON String"+movieJsonStr);

            }catch(IOException e){
                Log.e(LOG_TAG, "Error", e);
                return null;
            }finally {
                if (urlConnection !=null){
                    urlConnection.disconnect();
                }
                if (reader!=null){
                    try {
                        reader.close();
                    }catch (final IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try{
                return getMovieDatafromJson (movieJsonStr);
            }catch(JSONException e){
                Log.e(LOG_TAG, "Error", e);
                e.printStackTrace();
            }

            return null;
        }
    }
}
