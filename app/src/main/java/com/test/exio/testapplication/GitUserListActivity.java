package com.test.exio.testapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.json.JSONObject;

import java.util.List;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;


public class GitUserListActivity extends AppCompatActivity
        implements GitUserListFragment.Callbacks{
    public static final String TAG="GitUserListActivity";
    ActionBar mActionBar;
    MenuItem mSearchItem;
    private boolean mTwoPane;
    private TextView noSelectedUserText;

    private GitUserListFragment mUserListFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "searchview collapsed");
                mUserListFragment.clearSearchString();
                return true;
            }
        });


        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,"searchview textSubmit");
                mUserListFragment.setSearchString(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"searchview textChange");
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                Log.d(TAG,"searchview button clicked");
                return true;
            case R.id.action_signin:
                Log.d(TAG,"signin button clicked");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        mActionBar = getSupportActionBar();
        noSelectedUserText=(TextView)findViewById(R.id.no_selected_user_text);

        mUserListFragment = (GitUserListFragment)getSupportFragmentManager().findFragmentById(R.id.user_list);

        if (findViewById(R.id.user_repos_list_pane) != null) {
            mTwoPane = true;
            mActionBar.setTitle(getString(R.string.app_name));
        }
    }

    private void processServiceError(int code, ResponseBody body){
        try {

            switch (code) {
                case 403://403 лимит запросов для неавторизованного пользователя исчерпан
                    break;
                case 401://401 неправильные данные авторизации

                    break;
            }

            JSONObject obj = new JSONObject(body.string());
            String message = obj.get("message").toString();
            Toast.makeText(GitUserListActivity.this, getString(R.string.user_fetch_error) + message, Toast.LENGTH_LONG)
                    .show();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    @Override
    public void onItemSelected(String login) {
        if (mTwoPane) {
//            if(mSearchItem.isActionViewExpanded()){
//                mSearchItem.collapseActionView();
//            }
            Call<GitUserDetailed> call = GitHubService.getService().user(login);
            call.enqueue(new retrofit.Callback<GitUserDetailed>() {
                @Override
                public void onResponse(Response<GitUserDetailed> response, Retrofit retrofit) {
                    if (response.body() != null) {
                        GitUserDetailed userInfo = response.body();
                        mActionBar.setSubtitle(userInfo.name);
                    } else {
                        mActionBar.setSubtitle("");
                        processServiceError(response.code(),response.errorBody());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mActionBar.setSubtitle("");
                }
            });

            Bundle arguments = new Bundle();
            arguments.putString(GitUserReposFragment.ARG_OWNER_LOGIN, login);
            GitUserReposFragment fragment = new GitUserReposFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.user_repos_list_pane, fragment)
                    .commit();
            if(noSelectedUserText.getVisibility()==TextView.VISIBLE){
                noSelectedUserText.setVisibility(TextView.INVISIBLE);
            }
        } else {
            Intent userReposIntent = new Intent(this, GitUserReposActivity.class);
            userReposIntent.putExtra(GitUserReposFragment.ARG_OWNER_LOGIN, login);
            startActivity(userReposIntent);
        }
    }

    @Override
    public void onServiceError(int code, ResponseBody body) {
        processServiceError(code,body);
    }

}
