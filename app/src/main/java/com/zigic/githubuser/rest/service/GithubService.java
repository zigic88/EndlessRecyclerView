package com.zigic.githubuser.rest.service;

import com.zigic.githubuser.model.Limit;
import com.zigic.githubuser.model.Users;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by zigic on 10/06/17.
 */

public interface GithubService {
    @GET("search/users")
    Call<Users> usersSearch(@Query("q") String user);

    @GET("search/users")
    Call<Users> usersSearch(@Query("q") String user, @Query("page") int page,
                            @Query("per_page") int perPage, @Query("sort") String sort,
                            @Query("order") String order);

    @GET("rate_limit")
    Call<Limit> getLimitRate();

}
