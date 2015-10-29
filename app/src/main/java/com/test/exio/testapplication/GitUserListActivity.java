package com.test.exio.testapplication;

import android.app.Dialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;


public class GitUserListActivity extends AppCompatActivity
        implements GitUserListFragment.Callbacks{
    public static final String TAG="GitUserListActivity";
    public static final String STATE_IS_SEARCH_ACTION_EXPANDED="is_search_action_expanded";
    public static final String STATE_SEARCH_VIEW_STRING="search_view_string";
    ActionBar mActionBar;
    MenuItem mSearchItem;
    private boolean mTwoPane;
    private TextView noSelectedUserText;
    private boolean isSearchActionExpanded=false;
    private String searchString="";

    private GitUserListFragment mUserListFragment;

        public void showLoginDialog() {
                final Dialog login = new Dialog(this);

                login.setContentView(R.layout.login_dialog);
                login.setTitle(getString(R.string.signin_dialog));

                Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
                Button btnCancel = (Button) login.findViewById(R.id.btnCancel);
                final EditText txtUsername = (EditText)login.findViewById(R.id.txtUsername);
                final EditText txtPassword = (EditText)login.findViewById(R.id.txtPassword);

                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(txtUsername.getText().toString().trim().length() > 0 && txtPassword.getText().toString().trim().length() > 0)
                        {
                            Toast.makeText(GitUserListActivity.this,
                                    getString(R.string.signin_dialog_success), Toast.LENGTH_LONG).show();

                            login.dismiss();
                        }
                        else
                        {
                            Toast.makeText(GitUserListActivity.this,
                                    getString(R.string.signin_dialog_entercreds), Toast.LENGTH_LONG).show();

                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        login.dismiss();
                    }
                });

                login.show();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mSearchItem = menu.findItem(R.id.action_search);

        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isSearchActionExpanded=true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "searchview collapsed");
                isSearchActionExpanded=false;
                mUserListFragment.clearSearchString();
                return true;
            }
        });


        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        if(isSearchActionExpanded){
            mSearchItem.expandActionView();
            searchView.setQuery(searchString,false);
        }

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
                searchString=newText;
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
                showLoginDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_SEARCH_ACTION_EXPANDED, isSearchActionExpanded);
        outState.putString(STATE_SEARCH_VIEW_STRING, searchString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null) {
            isSearchActionExpanded = savedInstanceState.getBoolean(STATE_IS_SEARCH_ACTION_EXPANDED);
            searchString = savedInstanceState.getString(STATE_SEARCH_VIEW_STRING);
            Log.d(TAG,searchString);
        }

        setContentView(R.layout.activity_user_list);
        mActionBar = getSupportActionBar();
        noSelectedUserText=(TextView)findViewById(R.id.no_selected_user_text);

        mUserListFragment = (GitUserListFragment)getSupportFragmentManager().findFragmentById(R.id.user_list);

        if (findViewById(R.id.user_repos_list_pane) != null) {
            mTwoPane = true;
            mActionBar.setTitle(getString(R.string.app_name));
        }else{
            mActionBar.setTitle(getString(R.string.user_list_caption));
        }
    }

    private void processServiceError(int code, ResponseBody body){
        try {

            switch (code) {
                case 403://403 ����� �������� ��� ����������������� ������������ ��������
                    break;
                case 401://401 ������������ ������ �����������

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
