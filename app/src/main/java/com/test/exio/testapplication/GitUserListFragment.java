package com.test.exio.testapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;


public class GitUserListFragment extends Fragment {
    private static final String STATE_USERS = "state_users";
    private static final String STATE_IS_ALL_USERS_LIST = "is_all_users_list";
    private static final String STATE_SEARCH_STRING = "search_string";

    private static int ix=0;
    private List<GitUser> users = new ArrayList<>();

    private RecyclerView mList;
    private GitUserListAdapter mAdapter;
    private SwipeRefreshLayout refreshLayout;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = RecyclerView.NO_POSITION;

    private Call<List<GitUser>> callAllUsers;
    private int totalSearchedCount;
    private Call<GitSearchUsersResponse> callSearchUsers;
    private String searchString;
    private boolean isAllUsersList=true;

    private Callbacks mCallbacks = sDummyCallbacks;

    public interface Callbacks {
        void onItemSelected(String login);

        void onServiceError(int code, ResponseBody body);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String login) {

        }

        @Override
        public void onServiceError(int code, ResponseBody body) {

        }
    };

    public GitUserListFragment() {
    }

    public void clearSearchString() {
        searchString="";

        if(!isAllUsersList){
            if(callAllUsers!=null){
                callAllUsers.cancel();
            }
            if(callSearchUsers!=null) {
                callSearchUsers.cancel();
            }
            users.clear();
            mAdapter.notifyDataSetChanged();
            isAllUsersList=true;
            fetchMoreAllUsers(0);
        }
    }

    public void setSearchString(String searchString) {
        if(callAllUsers!=null){
            callAllUsers.cancel();
        }
        if(callSearchUsers!=null) {
            callSearchUsers.cancel();
        }
        if(isAllUsersList||!searchString.equals(this.searchString)){
            this.searchString = searchString;
            users.clear();
            mAdapter.notifyDataSetChanged();
            isAllUsersList=false;
            fetchMoreSearchedUsers(0);
        }
    }

    private void fetchMoreSearchedUsers(int page) {
        if(!refreshLayout.isRefreshing()) {
            users.add(null);
            mAdapter.notifyItemInserted(users.size() - 1);
        }

        callSearchUsers = GitHubService.getService(getContext().getApplicationContext()).searchUsers(searchString,page);//+"+in:login");
        callSearchUsers.enqueue(new retrofit.Callback<GitSearchUsersResponse>() {
            @Override
            public void onResponse(Response<GitSearchUsersResponse> response, Retrofit retrofit) {
                if(!refreshLayout.isRefreshing()) {
                    users.remove(users.size() - 1);
                    mAdapter.notifyItemRemoved(users.size());
                }else{
                    users.clear();
                    mAdapter.notifyDataSetChanged();
                }
                refreshLayout.setRefreshing(false);

                if (response.body() != null) {
                    GitSearchUsersResponse searchedUsers = response.body();
                    totalSearchedCount=searchedUsers.total_count;
                    Log.d("ttt",Integer.toString(totalSearchedCount));
                    for (GitUser user : searchedUsers.items) {
                        users.add(user);
                        mAdapter.notifyItemInserted(users.size());
                    }
                } else {
                    mCallbacks.onServiceError(response.code(),response.errorBody());
                }

                mAdapter.setLoaded();
            }

            @Override
            public void onFailure(Throwable t) {
                users.remove(users.size() - 1);
                mAdapter.notifyItemRemoved(users.size());

                mAdapter.setLoaded();
            }
        });
    }

    private void fetchMoreAllUsers(int since){
        if(!refreshLayout.isRefreshing()) {
            users.add(null);
            mAdapter.notifyItemInserted(users.size() - 1);
        }

        callAllUsers = GitHubService.getService(getContext().getApplicationContext()).users(Integer.toString(since));
        callAllUsers.enqueue(new retrofit.Callback<List<GitUser>>() {
            @Override
            public void onResponse(Response<List<GitUser>> response, Retrofit retrofit) {
                if(!refreshLayout.isRefreshing()) {
                    users.remove(users.size() - 1);
                    mAdapter.notifyItemRemoved(users.size());
                }else{
                    users.clear();
                    mAdapter.notifyDataSetChanged();
                }
                refreshLayout.setRefreshing(false);

                if (response.body() != null) {
                    List<GitUser> newUsers = response.body();
                    for (GitUser user : newUsers) {
                        users.add(user);
                        mAdapter.notifyItemInserted(users.size());
                    }
                } else {
                    mCallbacks.onServiceError(response.code(),response.errorBody());
                }

                mAdapter.setLoaded();
            }

            @Override
            public void onFailure(Throwable t) {
                users.remove(users.size() - 1);
                mAdapter.notifyItemRemoved(users.size());

                mAdapter.setLoaded();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_content, null);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isAllUsersList) {
                    fetchMoreAllUsers(0);
                }else{
                    fetchMoreSearchedUsers(0);
                }
            }
        });

        mList = (RecyclerView)rootView.findViewById(R.id.list_view);
        mList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mList.setLayoutManager(llm);

        mList.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d("click", Integer.toString(position));
                        setActivatedPosition(position);
                    }
                })
        );

        mAdapter = new GitUserListAdapter(mList,users);
        mList.setAdapter(mAdapter);

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (users.size() > 0) {
                    if (isAllUsersList) {
                        fetchMoreAllUsers(users.get(users.size() - 1).id);
                    } else {
                        if (users.size() < totalSearchedCount) {
                            fetchMoreSearchedUsers((users.size() - 1) / 30 + 2);
                        }
                    }
                }
            }
        });

        users.clear();

        if(savedInstanceState!=null) {
            isAllUsersList = savedInstanceState.getBoolean(STATE_IS_ALL_USERS_LIST);
            searchString = savedInstanceState.getString(STATE_SEARCH_STRING);

            mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);

            ArrayList<GitUser> list = (ArrayList<GitUser>) savedInstanceState.getSerializable(STATE_USERS);
            if(callAllUsers!=null){
                callAllUsers.cancel();
            }
            if(callSearchUsers!=null) {
                callSearchUsers.cancel();
            }

            if(list!=null) {
                for (GitUser user : list) {
                    users.add(user);
                    mAdapter.notifyDataSetChanged();
                }
            }else{
                if(isAllUsersList) {
                    fetchMoreAllUsers(0);
                }else{
                    fetchMoreSearchedUsers(0);
                }
            }
        }else {
            isAllUsersList = true;
            fetchMoreAllUsers(0);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    public void setActivatedPosition(int position){
        mActivatedPosition=position;
        mCallbacks.onItemSelected(users.get(mActivatedPosition).login);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<GitUser> u = new ArrayList<>();
        for(GitUser user:users){
            u.add(user);
            if(u.size()>=30){
                break;
            }
        }

        outState.putParcelableArrayList(STATE_USERS, u);

        outState.putBoolean(STATE_IS_ALL_USERS_LIST,isAllUsersList);
        outState.putString(STATE_SEARCH_STRING,searchString);
    }
}
