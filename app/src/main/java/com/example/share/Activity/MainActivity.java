package com.example.share.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.share.MapsActivity;
import com.example.share.R;
import com.example.share.Server.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ImageButton setting;
    ListView listView;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    Geocoder geocoder;
    List<Address> addresses;
    Connection connection =new Connection();
    String info ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        geocoder = new Geocoder(this, Locale.getDefault());

        setting =(ImageButton)findViewById(R.id.setting);
        listView=(ListView)findViewById(R.id.listView);

        new MyAsyncTask().execute("https://knobbier-mosses.000webhostapp.com/main_connect.php");

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });
    }

    private void displayArrayList(String jsonStr) {
        String[] from = {"listview_image", "listview_writing", "listview_info"};
        int[] to = {R.id.listview_image, R.id.listview_writing, R.id.listview_info};

        SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this.getBaseContext(), convertToＷordArrayList(jsonStr), R.layout.listview_design, from, to);
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            public boolean setViewValue(View view, Object data,String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView iv = (ImageView) view;
                    iv.setImageBitmap((Bitmap) data);
                    return true;
                }else
                    return false;
            }
        });
        simpleAdapter.notifyDataSetChanged();
        listView.setAdapter(simpleAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                ListView listView = (ListView) arg0;

                Intent intent =new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("location_map",listView.getItemAtPosition(arg2).toString());

                startActivity(intent);
            }
        });
    }

    private List<HashMap<String, Object>> convertToＷordArrayList(String jsonStr) {
        JSONObject jsonObject;
        Bitmap bitmap = null;

        List<HashMap<String, Object>> aList = new ArrayList<HashMap<String, Object>>();
        try {
            jsonObject = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray("articles");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjRow = jsonArray.getJSONObject(i);

                String photos = jsonObjRow.getString("photos");
                String latitude = jsonObjRow.getString("latitude").toString().trim();
                String longitude = jsonObjRow.getString("longitude").toString().trim();
                String writing = jsonObjRow.getString("writing");
                String dateTime = jsonObjRow.getString("dateTime");

                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(photos).getContent());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String fullAddress="";

                if ((!latitude.equals("null"))){
                    double longitude_double = Double.parseDouble(longitude);
                    double latitude_double = Double.parseDouble(latitude);

                    try {
                        addresses = geocoder.getFromLocation(latitude_double,longitude_double,1);

                        String address =addresses.get(0).getCountryName();
                        String area = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getThoroughfare();

                        fullAddress = address+" "+area+" "+country;

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("latitude", latitude);
                hm.put("longitude", longitude);
                hm.put("listview_image", bitmap);
                hm.put("listview_writing", writing);
                hm.put("listview_info", "On "+dateTime+" in "+fullAddress);
                aList.add(hm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return aList;
    }

    private void SelectImage(){
        final CharSequence[] items={getResources().getString(R.string.camera),getResources().getString(R.string.gallery),getResources().getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.addImage ));

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals(getResources().getString(R.string.camera))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[i].equals(getString(R.string.gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);
                } else if (items[i].equals(getResources().getString(R.string.cancel))) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if(resultCode== Activity.RESULT_OK){
            if(requestCode==REQUEST_CAMERA){
                Bitmap bmp1 = (Bitmap) data.getExtras().get("data");
                Intent intent =new Intent(MainActivity.this, AddActivity.class);
                intent.putExtra("photos",toStringImage(bmp1));
                startActivity(intent);

            }else if(requestCode==SELECT_FILE){
                Uri uri = data.getData();
                try {
                    Bitmap bmp2 = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), uri);
                    Intent intent =new Intent(MainActivity.this,AddActivity.class);
                    intent.putExtra("photos",toStringImage(bmp2));
                    startActivity(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String toStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options = options - 10;
        }

        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        return encodedImage;
    }


    private void showChangeLanguageDialog(){
        final CharSequence[] items={"English","繁體中文"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.choose_language));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("English")) {
                    setLocale("en");
                    recreate();
                } else if (items[i].equals("繁體中文")) {
                    setLocale("zh");
                    recreate();
                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }


    public void setLocale(String language){
        Locale locale =new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration =new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor =getSharedPreferences("settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",language);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences preferences =getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("My_Lang","");
        setLocale(language);
    }

    public class MyAsyncTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... url) {
            return connection.httpURLConnectionGet(url[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            displayArrayList(result);


        }
    }

}
