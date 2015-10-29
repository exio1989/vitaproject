package com.test.exio.testapplication;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by exio1989 on 26.10.2015.
 */
public interface GitHub {
    @GET("/users/{owner}")
    Call<GitUserDetailed> user(@Path("owner") String owner);

    @GET("/users")
    Call<List<GitUser>> users(@Query("since") String since);

    @GET("/users/{owner}/repos")
    Call<List<GitUserRepo>> userRepos(@Path("owner") String owner,@Query("page") int page);

    @GET("/users/mojombo/repos")
    Call<List<GitUserRepo>> userReposs();

    //https://api.github.com/search/users?q=rustam+in:login&page=3
    @GET("/search/users")
    Call<GitSearchUsersResponse> searchUsers(@Query("q") String q, @Query("page") int page);
}