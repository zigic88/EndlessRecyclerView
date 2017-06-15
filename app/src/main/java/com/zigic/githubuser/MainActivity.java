package com.zigic.githubuser;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.zigic.githubuser.adapter.GithubUserAdapter;
import com.zigic.githubuser.adapter.listener.OnLoadMoreListener;
import com.zigic.githubuser.model.Item;
import com.zigic.githubuser.model.Limit;
import com.zigic.githubuser.model.Resources;
import com.zigic.githubuser.model.Search;
import com.zigic.githubuser.model.Users;
import com.zigic.githubuser.rest.ApiClient;
import com.zigic.githubuser.rest.service.GithubService;
import com.zigic.githubuser.utils.Utils;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getName();
    private final String COLUMN_SORT = "followers";
    private final String ASCENDING = "asc";
    private int INIT_PAGE = 1;
    private int PAGING = 20;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rv_users)
    RecyclerView rvUsers;
    @BindView(R.id.search_view)
    SearchView searchView;

    List<Item> usersList = new ArrayList<>();
    GithubService githubService;
    GithubUserAdapter githubUserAdapter;
    ProgressDialog pDialog;
    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        handler = new Handler();
        pDialog = new ProgressDialog(this);

        searchView.setQueryHint(Utils.getResString(this, R.string.search_hint));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showDialog(query);
                searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                showDialog(query);
                filterSearchFor(query);
                return true;
            }
        });

        githubService = ApiClient.getClient().create(GithubService.class);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvUsers.setLayoutManager(mLayoutManager);
        rvUsers.setItemAnimator(new DefaultItemAnimator());
        githubUserAdapter = new GithubUserAdapter(this, usersList, rvUsers);
        rvUsers.setAdapter(githubUserAdapter);

        githubUserAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (usersList.size() > 0) {
                    usersList.add(null);
                    handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            githubUserAdapter.notifyItemInserted(usersList.size() - 1);
                        }
                    };
                    handler.post(r);
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (usersList.size() > 0) {
                            usersList.remove(usersList.size() - 1);
//                            githubUserAdapter.notifyItemRemoved(usersList.size());
                            validateRateLimit(searchView.getQuery().toString(), 1,true);
                        }
                    }
                }, 2000);

            }
        });


    }

    private void loadUserList(String username, int pageNumber, final boolean loadMore) {
        INIT_PAGE = INIT_PAGE + pageNumber;
        final Call<Users> usersSearch = githubService.usersSearch(username, INIT_PAGE, PAGING, COLUMN_SORT, ASCENDING);
        usersSearch.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful()) {
                    Users users = response.body();
                    if (users.getTotalCount() == 0 || users.getTotalCount().equals(0)) {
                        Toast.makeText(MainActivity.this, R.string.not_found, Toast.LENGTH_SHORT).show();
                    } else {
                        if(loadMore){
                            usersList.addAll(response.body().getItems());
                        }else{
                            usersList.clear();
                            usersList.addAll(response.body().getItems());
                        }
                        githubUserAdapter.updateUserList(usersList);
                        githubUserAdapter.setUnloaded();
                        hideDialog();
                    }
                } else {
                    tryAgainToast();
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                if (t instanceof SocketTimeoutException) {
                    tryAgainToast();
                }
            }
        });
    }

    private void validateRateLimit(final String username, final int pageNumber, final boolean loadMore) {
        final Call<Limit> rateLimit = githubService.getLimitRate();
        rateLimit.enqueue(new Callback<Limit>() {
            @Override
            public void onResponse(Call<Limit> call, Response<Limit> response) {
                if (response.isSuccessful()) {
                    Limit limit = response.body();
                    if (limit != null) {
                        Resources resources = limit.getResources();
                        if (resources != null) {
                            Search search = resources.getSearch();
                            if (search != null) {
                                if (search.getRemaining() > 0) {
                                    loadUserList(username, pageNumber,loadMore);
                                } else {
                                    limitRateToast();
                                }
                            } else {
                                limitRateToast();
                            }
                        } else {
                            limitRateToast();
                        }
                    } else {
                        limitRateToast();
                    }
                } else {
                    limitRateToast();
                }
            }

            @Override
            public void onFailure(Call<Limit> call, Throwable t) {
                tryAgainToast();
            }
        });
    }

//    private void validateApiRateLimit() {
//        Synchronous Method
//        Intent intent = new Intent(MainActivity.this, BackgroundService.class);
//        startService(intent);
//    }

    private void searchFor(String query) {
        if (query.isEmpty()) {
            usersList.clear();
            githubUserAdapter.updateUserList(usersList);
        } else {
            usersList = new ArrayList<>();
            githubUserAdapter.updateUserList(usersList);
            INIT_PAGE = 1;
            validateRateLimit(query, 0,false);
        }
    }

    private void filterSearchFor(String query) {
        githubUserAdapter.setUnloaded();
        if (query.isEmpty()) {
            usersList.clear();
            githubUserAdapter.updateUserList(usersList);
            githubUserAdapter.setUnloaded();
        } else {
            usersList = new ArrayList<>();
            githubUserAdapter.updateUserList(usersList);
            githubUserAdapter.setUnloaded();
            INIT_PAGE = 1;
            validateRateLimit(query, 0,false);
        }
    }

    private void limitRateToast() {
        Toast.makeText(MainActivity.this, R.string.limit_rate_notice, Toast.LENGTH_SHORT).show();
    }

    private void tryAgainToast() {
        Toast.makeText(MainActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
    }

    private void hideDialog(){
        if (pDialog != null) {
            pDialog.dismiss();
        }
    }

    private void showDialog(String query){
        if(!query.isEmpty()){
            pDialog.setMessage(Utils.getResString(MainActivity.this,R.string.progress_dialog_text));
            pDialog.show();
        }
    }
}
