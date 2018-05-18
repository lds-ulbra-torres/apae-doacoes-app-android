package com.example.lucca.doeamor_apaetorres;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lucca.doeamor_apaetorres.adapters.CategoryAdapter;
import com.example.lucca.doeamor_apaetorres.callbacks.CategoryCallBack;
import com.example.lucca.doeamor_apaetorres.controllers.CategoryController;
import com.example.lucca.doeamor_apaetorres.dao.CategoryDao;
import com.example.lucca.doeamor_apaetorres.dto.CategoryDTO;
import com.example.lucca.doeamor_apaetorres.models.Category;
import com.example.lucca.doeamor_apaetorres.retrofit.RetrofitInit;
import com.example.lucca.doeamor_apaetorres.views.ExpandableHeightGridView;
import com.example.lucca.doeamor_apaetorres.views.PartnersActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar, searchtollbar;
    Menu search_menu;
    MenuItem item_search;
    private CategoryAdapter adapter;
    private ArrayList<Category> searchableCategoryList;
    private CategoryController categoryController;
    private ExpandableHeightGridView grid;
    private CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSearchtollbar();
        categoryController = new CategoryController(MainActivity.this);


        grid = findViewById(R.id.gridView1);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,PartnersActivity.class);
                Category category = (Category) grid.getAdapter().getItem(i);
                intent.putExtra("id", String.valueOf(category.getId()));
                Toast.makeText(MainActivity.this, String.valueOf(category.getId()), Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
        retrofitInit();
   // loadGridCategories();
    }
    private void loadGridCategories(){
        categoryController.getCategories(new CategoryCallBack<ArrayList<Category>>() {
            @Override
            public void onSuccess(ArrayList<Category> categories) {
                adapter = new CategoryAdapter(MainActivity.this, categories);
                searchableCategoryList = categories;
                grid.setAdapter(adapter);
            }
            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Ocorreu um erro, por favor, feche e abra o aplicativo", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void retrofitInit(){
        Call <CategoryDTO> call= new RetrofitInit().getCategoryService().getCategories();

        call.enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(@NonNull Call<CategoryDTO> call, @NonNull Response<CategoryDTO> response) {
                CategoryDTO dto = response.body();
                categoryDao = new CategoryDao(getApplicationContext());
                searchableCategoryList = dto.getCategory();
                categoryDao.sync(searchableCategoryList);
                adapter = new CategoryAdapter(MainActivity.this, categoryDao.getCategoriesDataBase());
                grid.setAdapter(adapter);
                grid.setExpanded(true);
                searchableCategoryList.clear();
                Log.e("onResponse ",  "deu certo" );

            }
            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
                Log.e("onFailure: ",t.getMessage() );
            }
        });
    }
        protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_status:
                Toast.makeText(this, "status", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_search:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    circleReveal(R.id.searchtoolbar,1,true,true);
                else
                    searchtollbar.setVisibility(View.VISIBLE);

                item_search.expandActionView();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onSupportNavigateUp(){

        return true;
    }
    public void setSearchtollbar()
    {
        searchtollbar =  findViewById(R.id.searchtoolbar);
        if (searchtollbar != null) {
            searchtollbar.inflateMenu(R.menu.menu_search);
            search_menu=searchtollbar.getMenu();

            searchtollbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        circleReveal(R.id.searchtoolbar,1,true,false);
                    else
                        searchtollbar.setVisibility(View.GONE);
                }
            });

            item_search = search_menu.findItem(R.id.action_filter_search);

            MenuItemCompat.setOnActionExpandListener(item_search, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Do something when collapsed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        circleReveal(R.id.searchtoolbar,1,true,false);
                    }
                    else
                        searchtollbar.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }
            });

            initSearchView();


        } else
            Log.d("MyToolbar", "setSearchtollbar: NULL");
    }

    public void initSearchView()
    {

        final android.support.v7.widget.SearchView searchView =
                (android.support.v7.widget.SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();

        // Enable/Disable Submit button in the keyboard

        searchView.setSubmitButtonEnabled(false);

        // Change search close button image

        ImageView closeButton =  searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.close);

        // set hint and the text colors

        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Pesquisar..");
        txtSearch.setHintTextColor(Color.DKGRAY);
        txtSearch.setTextColor(getResources().getColor(R.color.black));


        // set the cursor

        AutoCompleteTextView searchTextView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Category> searchable = new ArrayList<>();
                for (Category p: searchableCategoryList) {
                    if(p.getNameCat().toLowerCase().contains(query.toLowerCase())){
                        searchable.add(p);
                    }
                }
                adapter = new CategoryAdapter(MainActivity.this, searchable);
                grid.setAdapter(adapter);
                grid.setExpanded(true);

                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                categoryDao = new CategoryDao(getApplicationContext());
                ArrayList<Category> searchable = new ArrayList<>();
                for (Category p: categoryDao.getCategoriesDataBase()) {
                    if(p.getNameCat().toLowerCase().contains(newText.toLowerCase())){
                        searchable.add(p);
                    }
                }
                adapter = new CategoryAdapter(MainActivity.this, searchable);
                grid.setAdapter(adapter);
                grid.setExpanded(true);


                return false;
            }

            public void callSearch(String query) {
                //Do searching
                Log.i("query", "" + query);

            }

        });

    }

    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow)
    {
        final View myView = findViewById(viewID);

        int width=myView.getWidth();

        if(posFromRight>0)
            width-=(posFromRight*getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material))-(getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)/ 2);
        if(containsOverflow)
            width-=getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx=width;
        int cy=myView.getHeight()/2;

        Animator anim;
        if(isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0,(float)width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float)width, 0);

        anim.setDuration((long)220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isShow)
                {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if(isShow)
            myView.setVisibility(View.VISIBLE);

        // start the animation
        anim.start();


    }


}
