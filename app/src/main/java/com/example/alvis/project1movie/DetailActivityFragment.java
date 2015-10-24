package com.example.alvis.project1movie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        if (intent!=null && intent.hasExtra("map")){
            HashMap movie = (HashMap)intent.getSerializableExtra("map");
            String movietitle = (String)movie.get("TITLE");
            String movierelease = (String)movie.get("RELEASE");
            String movievote = (String)movie.get("VOTE");
            String moviesynopsis = (String)movie.get("SYNOPSIS");
            String moviepath = (String)movie.get("PATH");

            ((TextView) rootView.findViewById(R.id.detail_movie_title)).setText(movietitle);
            ((TextView) rootView.findViewById(R.id.detail_movie_release)).setText(movierelease);
            ((TextView) rootView.findViewById(R.id.detail_movie_vote)).setText(movievote);
            ((TextView) rootView.findViewById(R.id.detail_movie_synopsis)).setText(moviesynopsis);
            ImageView image = (ImageView) rootView.findViewById(R.id.detail_movie_image);
            Picasso.with(this.getActivity()).load(moviepath).into(image);


        }
        return rootView;
    }
}
