package com.zigic.githubuser.rest.background;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.zigic.githubuser.model.Limit;
import com.zigic.githubuser.model.Resources;
import com.zigic.githubuser.model.Search;
import com.zigic.githubuser.rest.service.GithubService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zigic on 15/06/17.
 */

public class BackgroundService extends IntentService {
    //for Synchronous Method
    private static final String TAG = BackgroundService.class.getName();
    public static final String API_URL = "https://api.github.com/";
    private static Retrofit retrofit = null;

    public BackgroundService() {
        super("BackgroundService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            GithubService githubService = retrofit.create(GithubService.class);
            final Call<Limit> rateLimit = githubService.getLimitRate();
            try {
                Response response = rateLimit.execute();
                Limit limit = (Limit) response.body();
                if (limit != null) {
                    Resources resources = limit.getResources();
                    if (resources != null) {
                        Search search = resources.getSearch();
                        if (search != null) {
                            Log.d(TAG, "LIMIT >> " + search.getRemaining());
                        }
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "Validate API Limit Failure");
            }
        }

    }
}
