package com.test.exio.testapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

public class GitUserReposFragment extends Fragment {
    public final static String TAG = "GitUserReposFragment";
    public static final String ARG_OWNER_LOGIN = TAG+"ownerlogin";
    public static final String ARG_REPOS = TAG+"repos";

    private String ownerLogin;
    private List<GitUserRepo> repos = new ArrayList<>();

    private TextView dummyText;

    private RecyclerView mList;
    private GitUserReposListAdapter mAdapter;
    private Handler handler;
    private SwipeRefreshLayout refreshLayout;

    private void fetchMore(int page){
        if(!refreshLayout.isRefreshing()) {
            repos.add(null);
            mAdapter.notifyItemInserted(repos.size() - 1);
        }

        Call<List<GitUserRepo>> call = GitHubService.getService(getContext().getApplicationContext()).userRepos(ownerLogin,page);
        call.enqueue(new retrofit.Callback<List<GitUserRepo>>() {
            @Override
            public void onResponse(Response<List<GitUserRepo>> response, Retrofit retrofit) {
                if(!refreshLayout.isRefreshing()) {
                    repos.remove(repos.size() - 1);
                    mAdapter.notifyItemRemoved(repos.size());
                }else{
                    repos.clear();
                    mAdapter.notifyDataSetChanged();
                }
                refreshLayout.setRefreshing(false);

                if (response.body() != null) {
                    List<GitUserRepo> newRepos = response.body();
                    for (GitUserRepo repo : newRepos) {
                        repos.add(repo);
                        mAdapter.notifyItemInserted(repos.size());
                    }

                    if(repos.size()==0){
                        dummyText.setVisibility(TextView.VISIBLE);
                    }else{
                        dummyText.setVisibility(TextView.INVISIBLE);
                    }
                } else {
                    GitHubService.processServiceError(getContext().getApplicationContext(),response.code(),response.errorBody());
                }

                mAdapter.setLoaded();
            }

            @Override
            public void onFailure(Throwable t) {
                repos.remove(repos.size() - 1);
                mAdapter.notifyItemRemoved(repos.size());

                mAdapter.setLoaded();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_content,null);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMore(0);
            }
        });

        dummyText = (TextView)rootView.findViewById(R.id.dummyText);

        mList = (RecyclerView)rootView.findViewById(R.id.list_view);
        mList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mList.setLayoutManager(llm);

        mList.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d("click", Integer.toString(position));
                        itemClicked(position);
                    }
                })
        );

        mAdapter = new GitUserReposListAdapter(mList,repos);
        mList.setAdapter(mAdapter);
        handler = new Handler();

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (repos.get(repos.size() - 1).has_pages) {
                    fetchMore((repos.size() - 1) / 30 + 2);
                }
            }
        });

        repos.clear();

        if(savedInstanceState!=null){
            ownerLogin = savedInstanceState.getString(ARG_OWNER_LOGIN);

            ArrayList<GitUserRepo> list = (ArrayList<GitUserRepo>) savedInstanceState.getSerializable(ARG_REPOS);
            if(list!=null) {
                for (GitUserRepo repo : list) {
                    repos.add(repo);
                    mAdapter.notifyDataSetChanged();
                }
            }else{
                fetchMore(1);
            }
        }else {
            if (getArguments() != null && getArguments().containsKey(ARG_OWNER_LOGIN)) {
                ownerLogin = getArguments().getString(ARG_OWNER_LOGIN, "");
                fetchMore(1);
            }
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<GitUserRepo> r = new ArrayList<>();
        for(GitUserRepo repo:repos){
            r.add(repo);
            if(r.size()>=30){
                break;
            }
        }

        outState.putParcelableArrayList(ARG_REPOS, r);
        outState.putString(ARG_OWNER_LOGIN, ownerLogin);
    }

    private void itemClicked(int i){
        String url = repos.get(i).html_url;
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.no_browser_error), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
