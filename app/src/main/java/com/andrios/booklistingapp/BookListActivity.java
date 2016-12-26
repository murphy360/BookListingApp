package com.andrios.booklistingapp;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ArrayList<Book>>{

    private static final int BOOK_LOADER_ID = 1;

    private static final String TAG = "Book List Activity: ";

    private static final int PROGRESS = 0;
    private static final int EMPTY_TEXT = 1;
    private static final int LIST_VIEW = 2;
    private static final int NETWORK_ERROR = 3;

    ProgressBar progressBar;
    TextView emptyText;
    RecyclerView recyclerView;

    SimpleItemRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        emptyText = (TextView) findViewById(R.id.emptytext);

        recyclerView = (RecyclerView) findViewById(R.id.book_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);


        Log.d(TAG, "onCreate: ");
        if(!isNetworkConnected()){
            setView(NETWORK_ERROR);
        }else{
            setView(EMPTY_TEXT);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        Log.d(TAG, "onCreateOptionsMenu: ");
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if(!isNetworkConnected()){
                setView(NETWORK_ERROR);
            }else{
                String query = intent.getStringExtra(SearchManager.QUERY);
                Bundle b = new Bundle();
                b.putString("query", query);
                Log.d(TAG, "handleIntent: "+ b.get("query"));

                if(getLoaderManager().getLoader(BOOK_LOADER_ID) == null){
                    Log.d(TAG, "handleIntent: Loader == Null");
                    getLoaderManager().initLoader(BOOK_LOADER_ID,b,this).forceLoad();
                }else{
                    Log.d(TAG, "handleIntent: Loader != Null");
                    getLoaderManager().restartLoader(BOOK_LOADER_ID,b,this).forceLoad();
                }
            }

        }
    }

    private void setView(int whichView){
        if(whichView == PROGRESS){
            progressBar.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }else if(whichView == EMPTY_TEXT){

            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No Results");
            recyclerView.setVisibility(View.GONE);
        }else if(whichView == NETWORK_ERROR){

            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No Network");
            recyclerView.setVisibility(View.GONE);
        }else if(whichView == LIST_VIEW){

            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }



    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {


        mAdapter = new SimpleItemRecyclerViewAdapter(new ArrayList<Book>());
        recyclerView.setAdapter(mAdapter);

        //TODO Hide recycler View if Dataset is empty
        //TODO Check if network connectivity, hide
    }

    @Override
    public Loader<ArrayList<Book>> onCreateLoader(int id, Bundle args) {
        String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=";
        String maxResultsUrl = "&maxResults=30";
        //TODO Allow user to choose Max Results
        String query = args.getString("query");
        Log.d(TAG, "onCreateLoader: " + query);
        String mUrl = baseUrl+query+maxResultsUrl;

        setView(PROGRESS);
        return new BookLoader(this, mUrl);

    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Book>> loader, ArrayList<Book> data) {
        Log.d(TAG, "onLoadFinished: " + data.size());
        if(data.size() > 0){
            Log.d(TAG, "onLoadFinished: data ");
            setView(LIST_VIEW);
            mAdapter.add(data);
        }else{
            mAdapter.clear();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Book>> loader) {
        Log.d(TAG, "onLoaderReset: ");
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ArrayList<Book> mValues;

        public SimpleItemRecyclerViewAdapter(ArrayList<Book> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.book_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //holder.mImageView.setImageResource(R.drawable.placeholder_book);
            holder.mItem = mValues.get(position);
            holder.mAuthorView.setText(holder.mItem.getAuthor());
            holder.mContentView.setText(mValues.get(position).getTitle());


            BitmapWorkerTask asyncBitmap = new BitmapWorkerTask(holder.mImageView, getApplicationContext(), holder.mItem);
            asyncBitmap.execute();

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Not required in scope of project
                    /**
                     *  if (mTwoPane) {
                     Bundle arguments = new Bundle();
                     arguments.putString(BookDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                     BookDetailFragment fragment = new BookDetailFragment();
                     fragment.setArguments(arguments);
                     getSupportFragmentManager().beginTransaction()
                     .replace(R.id.book_detail_container, fragment)
                     .commit();
                     } else {
                     Context context = v.getContext();
                     Intent intent = new Intent(context, BookDetailActivity.class);
                     intent.putExtra(BookDetailFragment.ARG_ITEM_ID, holder.mItem.getId());

                     context.startActivity(intent);
                     }
                     */
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void add(ArrayList<Book> data) {
            Log.d(TAG, "add: " + mValues.size());
            mValues.clear();
            Log.d(TAG, "add: " + mValues.size());
            mValues.addAll(data);
            Log.d(TAG, "add: " + mValues.size());
            notifyDataSetChanged();
        }

        public void clear() {
            mValues.clear();
            notifyDataSetChanged();
            setView(EMPTY_TEXT);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mAuthorView;
            public final TextView mContentView;
            public final ImageView mImageView;
            public Book mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mAuthorView = (TextView) view.findViewById(R.id.id);
                mImageView = (ImageView) view.findViewById(R.id.imageView);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        File dir = this.getCacheDir();
        if (dir != null && dir.isDirectory()) {
            deleteDir(dir);
        }

    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
