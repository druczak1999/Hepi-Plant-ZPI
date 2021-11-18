package com.example.hepiplant.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.CalendarActivity;
import com.example.hepiplant.PostActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.PostsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PostDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PostsListFragment extends Fragment implements PostsRecyclerViewAdapter.ItemClickListener, AdapterView.OnItemSelectedListener  {

    private static final String TAG = "PostsListFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PostDto> postResponseHandler;
    private View postsFragmentView;
    private RecyclerView postsRecyclerView;
    private PostsRecyclerViewAdapter postsRecyclerViewAdapter;
    private PostDto[] posts = new PostDto[]{};
    private Button postSortButton, postFilterButton, postStartDateButton, postEndDateButton;
    private Spinner postFilterSpinner, categorySpinner;
    private EditText postTagsEditText;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;

    private static int sortClick = 0;
    private static int tagClick = 0;
    private static int dataClick = 0;
    private static int categoryClick = 0;
    private static int filterClick = 0;

    public PostsListFragment() {
    }

    public static PostsListFragment newInstance() {
        PostsListFragment fragment = new PostsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        config = (Configuration) getActivity().getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        postResponseHandler = new JSONResponseHandler<>(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        postsFragmentView = inflater.inflate(R.layout.fragment_posts_list, container, false);
        initView();
        setLayoutManager();
        makeGetDataRequest();
        postSortButton = postsFragmentView.findViewById(R.id.sortPostsButton);
        postFilterButton = postsFragmentView.findViewById(R.id.filterPostsButton);
        postFilterSpinner = postsFragmentView.findViewById(R.id.filterPostsSpinner);
        postStartDateButton = postsFragmentView.findViewById(R.id.startDateButtonInPostFilter);
        postEndDateButton = postsFragmentView.findViewById(R.id.endDateButtonInPostFilter);
        postTagsEditText = postsFragmentView.findViewById(R.id.tagEditTextInPostFilter);
        setPostsSortButtonOnClickListener();
        setPostsFilterButtonOnClickListener();
        setPostsFilterSpinnerAdapter();
        setDateButtonsOnClickListener();
        return postsFragmentView;
    }

    private void setDateButtonsOnClickListener(){
        postStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 1);
            }
        });

        postEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
               postStartDateButton.setText(data.getExtras().getString("data"));
            }
        }
        if(requestCode==2){
            if(resultCode==-1){
                postEndDateButton.setText(data.getExtras().getString("data"));
            }
        }
    }

    private void setPostsFilterSpinnerAdapter(){
        postFilterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, List.of("Filtruj","Tag","Kategoria","Data"));
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postFilterSpinner.setAdapter(dtoArrayAdapter);
    }

    private void setPostsSortButtonOnClickListener() {
        postSortButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Log.v(TAG,"On click");
                List<PostDto> postDtoList = new ArrayList<>(Arrays.asList(posts));
                List<PostDto> postDtos= new ArrayList<>();
                if(sortClick %2==0){
                    postDtos = postDtoList.stream()
                            .sorted(Comparator.comparing(PostDto::getCreatedDate).reversed())
                            .collect(Collectors.toList());
                }
                else{
                    postDtos = postDtoList.stream()
                            .sorted(Comparator.comparing(PostDto::getCreatedDate))
                            .collect(Collectors.toList());
                }
                sortClick++;
                PostDto [] newPosts = new PostDto[postDtos.size()];
                for (int i=0;i<postDtos.size();i++){
                    newPosts[i] = postDtos.get(i);
                }

                posts = newPosts;
                setAdapter();
            }
        });
    }

    private void setPostsFilterButtonOnClickListener() {
        postFilterButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                makeGetDataRequestWithParam();
                if(filterClick%2==0){
                    postFilterButton.setText(R.string.cleanButton);
                    makeGetDataRequestWithParam();
                    selectedCategory=null;
                }
                else {
                    postFilterButton.setText(R.string.filterButton);
                    makeGetDataRequest();
                }
                postFilterSpinner.setSelection(0);
                filterClick++;
                if(postTagsEditText.getVisibility()==View.VISIBLE)tagClick++;
                if(categorySpinner !=null && categorySpinner.getVisibility()==View.VISIBLE)categoryClick++;
                if(postStartDateButton.getVisibility()==View.VISIBLE)dataClick++;
                postTagsEditText.setVisibility(View.GONE);
                if(categorySpinner !=null)
                    categorySpinner.setVisibility(View.GONE);
                postStartDateButton.setVisibility(View.GONE);
                postEndDateButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
        intent.putExtra("postId", posts[position].getId());
        intent.putExtra("userId", posts[position].getUserId());
        startActivity(intent);
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl()+"posts";
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONArray response) {
                    onGetResponseReceived(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        });
    }

    private void makeGetDataRequestWithParam(){
        String url = getRequestUrl()+"posts?";
        url = prepareUrlForGetDataRequest(url);
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorResponseReceived(error);
                    }
                });
    }

    private String prepareUrlForGetDataRequest(String url) {
        if(postStartDateButton.getVisibility()== View.VISIBLE
                && postStartDateButton.getText()!=null && !postStartDateButton.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "startDate=" + postStartDateButton.getText().toString().substring(0, 10);
        }
        if(postEndDateButton.getVisibility()==View.VISIBLE
                && postEndDateButton.getText()!=null && !postEndDateButton.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "endDate=" + postEndDateButton.getText().toString().substring(0, 10);
        }
        if(postTagsEditText.getVisibility()==View.VISIBLE && postTagsEditText.getText()!=null && !postTagsEditText.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            if(postTagsEditText.getText().toString().charAt(0)=='#')
                url += "tag=" + postTagsEditText.getText().toString().substring(1, postTagsEditText.getText().toString().length());
            else  url += "tag=" + postTagsEditText.getText().toString();
        }
        if(selectedCategory!=null) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "categoryId=" + selectedCategory.getId().intValue();
        }
        return url;
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived(). Data is " + response);
        posts = postResponseHandler.handleArrayResponse(response, PostDto[].class);
        setAdapter();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void initView() {
        postsRecyclerView = postsFragmentView.findViewById(R.id.postsRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        postsRecyclerViewAdapter = new PostsRecyclerViewAdapter(getActivity(), posts);
        postsRecyclerViewAdapter.setClickListener(this);
        postsRecyclerView.setAdapter(postsRecyclerViewAdapter);
    }

    private void onGetResponseCategories(JSONArray response){
        categoryDtos = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
        List<String> categories = new ArrayList<String>();
        for (int i = 0; i < categoryDtos.length; i++) {
            categories.add(categoryDtos[i].getName());
        }
        Log.v(TAG,"DL: "+categoryDtos.length);
        getCategories(categories);
    }

    private void getCategoriesFromDB() {
        String url = getRequestUrl() + "categories";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseCategories(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { onErrorResponse(error);}
                });
    }

    private void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        categorySpinner = postsFragmentView.findViewById(R.id.categorySpinnerInPostFilter);
        categorySpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dtoArrayAdapter);
        categorySpinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner categorySpinner = (Spinner) parent;
        Spinner filterSpinner = (Spinner) parent;
        String selectedItem = (String) parent.getItemAtPosition(position);
        if(categorySpinner.getId()==R.id.categorySpinnerInPostFilter){
            for(CategoryDto c : categoryDtos){
                if(c.getName().equals(selectedItem)) selectedCategory = c;
            }
        }
        if(filterSpinner.getId()==R.id.filterPostsSpinner){
            switch (position){
                case 1:
                    if(tagClick%2==0){
                        postTagsEditText.setVisibility(View.VISIBLE);
                        postFilterButton.setVisibility(View.VISIBLE);
                    }
                    else postTagsEditText.setVisibility(View.GONE);
                   tagClick++;
                   break;
                case 2:
                    if(categoryClick%2==0){
                        getCategoriesFromDB();
                        postFilterButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        if(this.categorySpinner !=null) this.categorySpinner.setVisibility(View.GONE);
                    }
                    categoryClick++;
                    break;
                case 3:
                    if(dataClick%2==0){
                        postStartDateButton.setVisibility(View.VISIBLE);
                        postEndDateButton.setVisibility(View.VISIBLE);
                        postFilterButton.setVisibility(View.VISIBLE);
                    }else{
                        postStartDateButton.setVisibility(View.GONE);
                        postEndDateButton.setVisibility(View.GONE);
                    }
                    dataClick++;
                    break;
            }
            filterSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
