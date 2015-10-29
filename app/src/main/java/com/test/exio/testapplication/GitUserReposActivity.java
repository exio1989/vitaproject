package com.test.exio.testapplication;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONObject;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link GitUserListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link GitUserReposFragment}.
 */
public class GitUserReposActivity extends AppCompatActivity {
    public static final String TAG="GitUserListActivity";
    ActionBar mActionBar;
    String ownerLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_repos);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);

        String ownerLogin = null;
        if (savedInstanceState == null) {
            ownerLogin=getIntent().getStringExtra(GitUserReposFragment.ARG_OWNER_LOGIN);
        }else{
            savedInstanceState.getString(GitUserReposFragment.ARG_OWNER_LOGIN);
        }

        Call<GitUserDetailed> call = GitHubService.getService().user(ownerLogin);
        call.enqueue(new retrofit.Callback<GitUserDetailed>() {
            @Override
            public void onResponse(Response<GitUserDetailed> response, Retrofit retrofit) {
                if (response.body() != null) {
                    GitUserDetailed userInfo = response.body();
                    mActionBar.setSubtitle(userInfo.name);
                } else {
                    mActionBar.setSubtitle("");
                    try {

                        switch (response.code()) {
                            case 403://403 лимит запросов для неавторизованного пользователя исчерпан
                                break;
                            case 401://401 неправильные данные авторизации
                                break;
                        }

                        JSONObject obj = new JSONObject(response.errorBody().string());
                        String message = obj.get("message").toString();
                        Toast.makeText(GitUserReposActivity.this, getString(R.string.user_fetch_error) + message, Toast.LENGTH_LONG)
                                .show();
                    } catch (Exception ex) {
                        Log.d(TAG, ex.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                mActionBar.setSubtitle("");
            }
        });

        Bundle arguments = new Bundle();
        arguments.putString(GitUserReposFragment.ARG_OWNER_LOGIN,
                getIntent().getStringExtra(GitUserReposFragment.ARG_OWNER_LOGIN));
        GitUserReposFragment fragment = new GitUserReposFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.user_repos_list_pane, fragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GitUserReposFragment.ARG_OWNER_LOGIN,ownerLogin);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, GitUserListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
