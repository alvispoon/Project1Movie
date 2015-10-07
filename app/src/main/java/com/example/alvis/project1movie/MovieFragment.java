package com.example.alvis.project1movie;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private String APIKEY = "e4476354de0e78e27196b077f6a6d24e";
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private ArrayAdapter<String> movieAdaptor;

    public MovieFragment() {
    }


    private void updateWeather(){
        FetchMovieTask movieTask = new FetchMovieTask();

        movieTask.execute();

    }

    public void onStart(){
        super.onStart();
        updateWeather();
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

        ArrayList<String> mMovieList = new ArrayList<String>();


        movieAdaptor = new ImageAdapter(getActivity(), R.layout.list_item_movie,mMovieList);


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        gridView.setAdapter(movieAdaptor);

        return rootView;
    }

    private String[] getMovieDatafromJson(String movieJsonStr) throws JSONException{
        final String OWM_RESULTS = "results";
        final String OWM_TITLE = "original_title";
        final String OWM_PATH = "backdrop_path";
        final String baseURL = "http://image.tmdb.org/t/p/w185";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray (OWM_RESULTS);
        String[] resultStrs  = new String[20];
        for (int i=0; i<movieArray.length(); i++) {
            String path;
            String title;
            JSONObject movie = movieArray.getJSONObject(i);
            path = movie.getString(OWM_PATH);
            title = movie.getString(OWM_TITLE);

            //resultStrs[i] = title + " - " + path ;

            resultStrs[i] = baseURL+path;
        }

        return resultStrs;
    }

    class ImageAdapter extends ArrayAdapter<String> {

        private Context mContext;
        private int mlayoutResourceId;
        LayoutInflater inflater;
        private ArrayList<String> mitems = new ArrayList<String>();

        public ImageAdapter(Context context, int layoutResourceId, ArrayList<String> items) {
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


            String image =  mitems.get(position);
            
            Picasso.with(mContext).load(image).into(imageView);

            return imageView;


        }
    }






    public class FetchMovieTask extends AsyncTask<String, Void, String[]>{

        protected void onPostExecute(String[] strings) {
            Log.v(LOG_TAG, "OnPostExecute" + strings.length);
            if (strings != null)
                movieAdaptor.clear();
            for (String movieStr : strings){
                movieAdaptor.add(movieStr);
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            //if (params.length ==0) {
            //    return null;
           // }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            try{
                final String baseURL = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=";
                final String KEY_PARAM = "api_key";
                Uri buildUri = Uri.parse(baseURL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, APIKEY)
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
