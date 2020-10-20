package com;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.photogallery.PhotoAdapter;
import com.example.photogallery.api.FetchItemTask;
import com.example.photogallery.api.FlickrFetch;
import com.google.gson.Gson;
import com.model.Example;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PollService extends IntentService
{
    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);

    public PollService() {
        super(TAG);
    }

    // Добавляем метод setServiceAlarm:
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent intent = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static Intent newIntent(Context context)
    {
        return new Intent(context, PollService.class);
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = PollService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {

        if (!isNetworkAvailableAndConnected()) {
            Log.i(TAG, "Нет соединения: " + intent);
            return;
        }

        Log.i(TAG, "Служба запущена: " + intent);
        //QueryPreferences.setStoredQuery(this, "тестище");
        String last_id = QueryPreferences.getLastResultId(this);
        if (last_id == null)
        {
            last_id = "1000";
        }
        Retrofit r = FetchItemTask.getRetrofit();
        //Log.e("_______________", "(" + QueryPreferences.getLastResultId(this).trim() + ")");


        BigInteger g = new BigInteger(last_id);
        g = g.add(BigInteger.ONE);

        final Context cont = this;

        r.create(FlickrFetch.class)
                .getRecent(g.toString())
                .enqueue(new Callback<Example>()
                {
                    @Override
                    public void onResponse(Call<Example> call, Response<Example> response) {
                        Gson gson = new Gson();
                        QueryPreferences.setLastResultId(cont, response.body().getPhotos().getPage().toString());
                        QueryPreferences.setStoredQuery(cont, gson.toJson(response.body().getPhotos()));
                    }

                    @Override
                    public void onFailure(Call<Example> call, Throwable t) {
                    }
                });
    }

    private boolean isNetworkAvailableAndConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = cm.getActiveNetworkInfo();
        return nwInfo != null && nwInfo.isConnected();
    }

}
