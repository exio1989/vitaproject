package com.test.exio.testapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link GitUserListActivity}
 * in two-pane mode (on tablets) or a {@link GitUserReposActivity}
 * on handsets.
 */
public class GitUserReposFragment extends Fragment {
    public final static String TAG = "GitUserReposFragment";
    public static final String ARG_OWNER_LOGIN = TAG+"ownerlogin";
    public static final String ARG_REPOS = TAG+"repos";

    private String ownerLogin;
    private List<GitUserRepo> repos = new ArrayList<>();

    private TextView dummyText;

    private RecyclerView mList;
    private GitUserReposListAdapter mAdapter;
    protected Handler handler;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = RecyclerView.NO_POSITION;

    private void fetchMore(int page){
        repos.add(null);
        mAdapter.notifyItemInserted(repos.size() - 1);

        Call<List<GitUserRepo>> call = GitHubService.getService().userRepos(ownerLogin,page);
        call.enqueue(new retrofit.Callback<List<GitUserRepo>>() {
            @Override
            public void onResponse(Response<List<GitUserRepo>> response, Retrofit retrofit) {
                repos.remove(repos.size() - 1);
                mAdapter.notifyItemRemoved(repos.size());

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
                    //TODO вынести в коллбак в активити
                    try {

                        switch (response.code()) {
                            case 403://403 лимит запросов для неавторизованного пользователя исчерпан
                                break;
                            case 401://401 неправильные данные авторизации
                                break;
                        }

                        JSONObject obj = new JSONObject(response.errorBody().string());
                        String message = obj.get("message").toString();
                        Toast.makeText(getContext(), getString(R.string.user_fetch_error) + message, Toast.LENGTH_LONG)
                                .show();
                    } catch (Exception ex) {
                        Log.d(TAG, ex.getMessage());
                    }
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
            Toast.makeText(getContext(), "No application can handle this request."
                    + " Please install a webbrowser", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
