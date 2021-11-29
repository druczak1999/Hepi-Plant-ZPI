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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.example.hepiplant.PostAddActivity;
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
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PostDto> postResponseHandler;
    private View postsFragmentView;
    private RecyclerView postsRecyclerView;
    private PostsRecyclerViewAdapter postsRecyclerViewAdapter;
    private PostDto[] posts = new PostDto[]{};
    private Button postSortButton, postFilterButton, postStartDateButton, postEndDateButton;
    private TextView closeTagFiler, closeCategoryFilter, closeDateFilters;
    private LinearLayout tagLinearLayout, categoryLinearLayout,datesLinearLayout;
    private Spinner postFilterSpinner, categorySpinner;
    private EditText postTagsEditText;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;

    private static int sortClick = 0;
    private static int filterClick=0;

    public PostsListFragment() {}

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
        setUpViewsForFilters();
        setPostsSortButtonOnClickListener();
        setPostsFilterButtonOnClickListener();
        setPostsFilterSpinnerAdapter();
        setDateButtonsOnClickListener();
        getCategoriesFromDB();
        setCloseViewsOnClickListeners();
        adjustLayoutForAdmin();
        return postsFragmentView;
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

    private void setUpViewsForFilters() {
        postSortButton = postsFragmentView.findViewById(R.id.sortPostsButton);
        postFilterButton = postsFragmentView.findViewById(R.id.filterPostsButton);
        postFilterSpinner = postsFragmentView.findViewById(R.id.filterPostsSpinner);
        postStartDateButton = postsFragmentView.findViewById(R.id.startDateButtonInPostFilter);
        postEndDateButton = postsFragmentView.findViewById(R.id.endDateButtonInPostFilter);
        postTagsEditText = postsFragmentView.findViewById(R.id.tagEditTextInPostFilter);
        closeTagFiler = postsFragmentView.findViewById(R.id.closeTagFilter);
        closeCategoryFilter = postsFragmentView.findViewById(R.id.closeCategoryFilter);
        closeDateFilters = postsFragmentView.findViewById(R.id.closeDateFilters);
        tagLinearLayout = postsFragmentView.findViewById(R.id.tagFilterLinearLayout);
        categoryLinearLayout = postsFragmentView.findViewById(R.id.categoryFilterLinearLayout);
        datesLinearLayout = postsFragmentView.findViewById(R.id.datesFilterLinearLayout);
    }

    private void setCloseViewsOnClickListeners(){

        closeTagFiler.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            postTagsEditText.setText("");
            postTagsEditText.setVisibility(View.GONE);
            tagLinearLayout.setVisibility(View.GONE);
            closeTagFiler.setVisibility(View.GONE);
        });

        closeCategoryFilter.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            categorySpinner.setVisibility(View.GONE);
            selectedCategory=null;
            categoryLinearLayout.setVisibility(View.GONE);
            closeCategoryFilter.setVisibility(View.GONE);
        });

        closeDateFilters.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            postStartDateButton.setText("");
            postEndDateButton.setText("");
            postStartDateButton.setVisibility(View.GONE);
            postEndDateButton.setVisibility(View.GONE);
            datesLinearLayout.setVisibility(View.GONE);
            closeDateFilters.setVisibility(View.GONE);
        });
    }

    private void setRecyclerViewLayoutParams(int plusOrMinus) {
        postsRecyclerView.setPadding(postsRecyclerView.getPaddingLeft(),
                postsRecyclerView.getPaddingTop(),
                postsRecyclerView.getPaddingRight(),
                postsRecyclerView.getPaddingBottom()
                + (int)(plusOrMinus * getResources().getDimension(R.dimen.SortLinearLayout)));
    }

    private void setDateButtonsOnClickListener(){
        postStartDateButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CalendarActivity.class);
            intent.putExtra("event","plant");
            startActivityForResult(intent, 1);
        });

        postEndDateButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CalendarActivity.class);
            intent.putExtra("event","plant");
            startActivityForResult(intent, 2);
        });
    }

    private void adjustLayoutForAdmin() {
        if(config.getUserRoles().contains(ROLE_ADMIN)){
            View postAddButton = postsFragmentView.findViewById(R.id.postAddButtonAdmin);
            postAddButton.setVisibility(View.VISIBLE);
            postAddButton.setOnClickListener((View v) -> {
                Intent intent = new Intent(getContext(), PostAddActivity.class);
                startActivity(intent);
            });
            postsRecyclerView.setPadding(postsRecyclerView.getPaddingLeft(),
                    postsRecyclerView.getPaddingTop(),
                    postsRecyclerView.getPaddingRight(),
                    (int)(getResources().getDimension(R.dimen.spaceForBottomBar)
                            + 2 * getResources().getDimension(R.dimen.margin_medium)));
            postsRecyclerView.setClipToPadding(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
               postStartDateButton.setText(data.getExtras().getString("date"));
            }
        }
        if(requestCode==2){
            if(resultCode==-1){
                postEndDateButton.setText(data.getExtras().getString("date"));
            }
        }
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
                    setRecyclerViewLayoutParams(1);
                    closeTagFiler.setVisibility(View.VISIBLE);
                    postTagsEditText.setVisibility(View.VISIBLE);
                    tagLinearLayout.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    getCategoriesFromDB();
                    setRecyclerViewLayoutParams(1);
                    closeCategoryFilter.setVisibility(View.VISIBLE);
                    categoryLinearLayout.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    setRecyclerViewLayoutParams(1);
                    postStartDateButton.setVisibility(View.VISIBLE);
                    postEndDateButton.setVisibility(View.VISIBLE);
                    datesLinearLayout.setVisibility(View.VISIBLE);
                    closeDateFilters.setVisibility(View.VISIBLE);
                    break;
            }
            filterSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void setPostsFilterSpinnerAdapter(){
        postFilterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, Arrays.asList("Filtruj","Tag","Kategoria","Data"));
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
        postFilterButton.setOnClickListener(v -> {
            if(filterClick%2==0){
                makeGetDataRequestWithParam();
                postFilterButton.setText(R.string.clean_button);
            }
            else{
                makeGetDataRequest();
                postFilterButton.setText(R.string.filter_button);
            }
            postFilterSpinner.setSelection(0);
            filterClick++;
        });
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl()+"posts";
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    private void makeGetDataRequestWithParam(){
        String url = getRequestUrl()+"posts?";
        url = prepareUrlForGetDataRequest(url);
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
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
                url += "tag=" + postTagsEditText.getText().toString().substring(1, postTagsEditText.getText().toString().length()).trim();
            else  url += "tag=" + postTagsEditText.getText().toString().trim();
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
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
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
        List<String> categories = new ArrayList<>();
        for (CategoryDto categoryDto : categoryDtos) {
            categories.add(categoryDto.getName());
        }
        Log.v(TAG,"DL: "+categoryDtos.length);
        getCategories(categories);
    }

    private void getCategoriesFromDB() {
        String url = getRequestUrl() + "categories";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseCategories, this::onErrorResponseReceived);
    }

    private void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        categorySpinner = postsFragmentView.findViewById(R.id.categorySpinnerInPostFilter);
        categorySpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dtoArrayAdapter);
        categorySpinner.setVisibility(View.VISIBLE);
    }
}
