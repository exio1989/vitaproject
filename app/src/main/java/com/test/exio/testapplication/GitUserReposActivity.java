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
    public static final String ARG_OWNER_NAME = TAG+"ownerlogin";
    ActionBar mActionBar;
    String ownerLogin;
    String ownerName;

    private void fetchName(){
        Call<GitUserDetailed> call = GitHubService.getService().user(ownerLogin);
        call.enqueue(new retrofit.Callback<GitUserDetailed>() {
            @Override
            public void onResponse(Response<GitUserDetailed> response, Retrofit retrofit) {
                if (response.body() != null) {
                    GitUserDetailed userInfo = response.body();
                    ownerName=userInfo.name;
                    mActionBar.setSubtitle(ownerName);
                } else {
                    mActionBar.setSubtitle("");
                    try {

                        switch (response.code()) {
                            case 403://403 ����� �������� ��� ����������������� ������������ ��������
                                break;
                            case 401://401 ������������ ������ �����������
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_repos);
        mActionBar = getSupportActionBar();
        if(mActionBar!=null)
            mActionBar.setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState!=null){
            ownerName=savedInstanceState.getString(ARG_OWNER_NAME);
            if(mActionBar!=null)
                mActionBar.setSubtitle(ownerName);
        }
        else {
            ownerLogin=getIntent().getStringExtra(GitUserReposFragment.ARG_OWNER_LOGIN);
            Bundle arguments = new Bundle();
            arguments.putString(GitUserReposFragment.ARG_OWNER_LOGIN,ownerLogin);
            GitUserReposFragment fragment = new GitUserReposFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.user_repos_list_pane, fragment)
                    .commit();

            fetchName();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GitUserReposFragment.ARG_OWNER_LOGIN,ownerLogin);
        outState.putString(ARG_OWNER_NAME,ownerName);
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
