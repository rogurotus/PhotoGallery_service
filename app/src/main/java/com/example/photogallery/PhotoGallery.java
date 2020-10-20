package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.PollService;
import com.QueryPreferences;
import com.example.photogallery.api.FetchItemTask;
import com.example.photogallery.api.FlickrFetch;
import com.example.photogallery.db.PhotosDB;
import com.google.gson.Gson;
import com.model.Example;
import com.model.Photo;
import com.model.Photos;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PhotoGallery extends AppCompatActivity {
    Context cont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        final PhotosDB db = Room.databaseBuilder(getApplicationContext(),
                PhotosDB.class, "photos-database").build();

        final RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        PollService.setServiceAlarm(this, true);
        final Gson gson = new Gson();
        Photos phs;
        String temp = QueryPreferences.getStoredQuery(this);
        if (temp != null)
        {
            phs = gson.fromJson(temp, Photos.class);
            rv.setAdapter(new PhotoAdapter(phs.getPhoto()));
        }
        else
        {
            Retrofit r = FetchItemTask.getRetrofit();
            r.create(FlickrFetch.class)
                    .getRecent(String.valueOf(1000))
                    .enqueue(new Callback<Example>()
                    {
                        @Override
                        public void onResponse(Call<Example> call, Response<Example> response) {
                            QueryPreferences.setLastResultId(cont, response.body().getPhotos().getPage().toString());
                            QueryPreferences.setStoredQuery(cont, gson.toJson(response.body().getPhotos()));
                            rv.setAdapter(new PhotoAdapter(response.body().getPhotos().getPhoto()));
                        }

                        @Override
                        public void onFailure(Call<Example> call, Throwable t) {
                        }
                    });
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        getDelegate().onDestroy();
        PollService.setServiceAlarm(this, false);
        Log.e("I SAY DIE", "DIE");
    }
}
