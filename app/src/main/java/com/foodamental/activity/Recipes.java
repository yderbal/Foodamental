package com.foodamental.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.foodamental.R;
import com.foodamental.dao.DBHelper;
import com.foodamental.dao.DatabaseManager;
import com.foodamental.dao.dbimpl.FrigoDB;
import com.foodamental.dao.dbimpl.ProductDB;
import com.foodamental.dao.model.FrigoObject;
import com.foodamental.util.JsonUtilTools;
import com.foodamental.util.MyMenu;
import com.foodamental.util.RecipeItem;
import com.foodamental.util.RecipesArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Activité des recettes
 */
public class Recipes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnTaskComplete {

    ProductDB productDb;
    String ingredientsEng = "";
    TextView tv;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        DatabaseManager.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        tv = (TextView) findViewById(R.id.id_recipes);
        createQuery();
    }

    /**
     * Fonction qui traduit les produits du frigo
     */
    private void createQuery() {
        FrigoDB fdb = new FrigoDB();
        EditText ingredientText = (EditText) findViewById(R.id.ingredientText);
        query = "";
        List<FrigoObject> listDTO = fdb.getAllProductOrderBy(DBHelper.FRIGODB_COLUMN_DATE_PEREMPT);
        if (listDTO.isEmpty())
            Toast.makeText(Recipes.this, "Erreur lors de la récuperation des données", Toast.LENGTH_LONG).show();
        else {
            for (int i = 0; i < listDTO.size() && i < 7; i++) {
                String Name = listDTO.get(i).getName();
                String[] word = Name.split(" ");
                for (String w : word)
                    query += w + ",";
            }
            query = query.substring(0, query.length() - 1);
            String url = "https://www.wecook.fr/web-api/recipes/search?q=" + query;
            new RecipeAsyncTask().execute(url);
        }
        //query = query.substring(0,query.length()-3);
        //String url = "http://api.yummly.com/v1/api/recipes?_app_id=80ae101e&_app_key=85289ec3509333e07e8112b54c053726"+query;
    }

    /**
     * Fonction du menu
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Fonction du menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipes, menu);
        return true;
    }

    /**
     * Fonction du menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fonction du menu
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return MyMenu.onNavigationItemSelected(this, this, item);
    }

    /**
     * @param output
     */
    @Override
    public void onTaskCompleted(String output) {
        String query = "";
        String[] ingredients = output.split(" ");
        for (int i = 0; i < ingredients.length; i++)
            query += ingredients[i] + "%2C";
        query.substring(0, query.length() - 3);
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/findByIngredients?fillIngredients=false&ingredients=" + query;
        new RecipeAsyncTask().execute(url);
    }

    public class RecipeAsyncTask extends AsyncTask<String, Void, List<RecipeItem>> {

        public OnTaskComplete response = null;
        private ProgressDialog dialog = new ProgressDialog(Recipes.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please Wait");
            this.dialog.show();
        }

        @Override
        protected List<RecipeItem> doInBackground(String... url) {
            try {
                List<RecipeItem> liste = new ArrayList<>();
                JSONObject jsonUrlResponse = JsonUtilTools.getJSONFromRecipesRequest(url[0]);
                //creation recipeItem
                String s = jsonUrlResponse.getString("result");
                JSONObject jresult = new JSONObject(s);
                JSONArray listRecipes = jresult.getJSONArray("resources");
                for (int i = 0; i < listRecipes.length() && i < 7; i++) {
                    RecipeItem item = new RecipeItem();
                    JSONObject json = new JSONObject((String) listRecipes.get(i).toString());
                    if (json.has("picture_url")) {
                        URL imageUrl = new URL(json.getString("picture_url"));
                        Bitmap image = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                        item.setImage(image);
                    } else {
                        int id = getResources().getIdentifier("food_default", "drawable", getPackageName());
                        Bitmap image = BitmapFactory.decodeResource(getResources(), id);
                        item.setImage(image);
                    }
                    item.setName(json.getString("name"));
                    item.setId(json.getInt("id"));
                    item.setCookingTime(json.getJSONObject("time").getInt("total"));
                    item.setNbServe(json.getInt("portions"));
                    //ajout de l'element dans la liste
                    liste.add(item);
                }
                return liste;
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        /**
         * apres doInBackGround cette methode est executée
         *
         * @param result
         */
        protected void onPostExecute(List<RecipeItem> result) {
            this.dialog.dismiss();
            //prepare la listeView
            final ListView lv = (ListView) findViewById(R.id.listRecipes);
            //creation de l'adapter avec les recipeItem
            RecipesArrayAdapter recipesArrayAdapter = new RecipesArrayAdapter(Recipes.this,
                    android.R.layout.simple_list_item_1,
                    result);
            lv.setAdapter(recipesArrayAdapter);
            //si on clique sur l'item on change d'activité
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RecipeItem content = (RecipeItem) lv.getItemAtPosition(position);
                    //Toast.makeText(getApplicationContext(),content.toString(),Toast.LENGTH_LONG).show();
                    Intent intentRecipeContent = new Intent(Recipes.this, RecipeContentActivity.class);
                    intentRecipeContent.putExtra("id", content.getId());
                    startActivity(intentRecipeContent);
                }
            });
        }
    }

}
