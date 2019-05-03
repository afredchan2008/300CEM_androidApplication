package com.example.share.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.share.R;
import com.example.share.Server.Connection;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddActivity extends AppCompatActivity {

    ImageButton imageButton;
    EditText editText;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    String latitude, longitude;

    Connection connection = new Connection();
    Switch aSwitch;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        imageButton =(ImageButton)findViewById(R.id.imageButton);
        editText =(EditText)findViewById(R.id.editText) ;
        aSwitch =(Switch)findViewById(R.id.aSwitch);
        send =(Button)findViewById(R.id.send);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String data = getIntent().getExtras().getString("photos");
        byte[] bitmapArray = Base64.decode(data, Base64.DEFAULT);
        final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);

        imageButton.setImageBitmap(bitmap);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            String currentText = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                int lines = editText.getLineCount();
                if (lines > editText.getMaxLines()) {
                    editText.setText(currentText);
                    editText.setSelection(currentText.length());
                } else if (lines <= editText.getMaxLines()) {
                    currentText = s != null ? s.toString() : "";
                }
            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fetchLocation();
                } else {
                    latitude="";
                    longitude="";
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checking()==true){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
                    builder.setTitle(getResources().getString(R.string.confirm_requirement));
                    builder.setCancelable(false);
                    builder.setPositiveButton(getResources().getString(R.string.yes),new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Bitmap bitmap = ((BitmapDrawable) imageButton.getDrawable()).getBitmap();

                            connection.addData("photos",toStringImage(bitmap));
                            connection.addData("writing",editText.getText().toString());
                            connection.addData("latitude",latitude);
                            connection.addData("longitude",longitude);

                            new MyAsyncTask().execute("https://knobbier-mosses.000webhostapp.com/add_connect.php");
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.show();
                }else {
                    return;
                }
            }
        });
    }

    public boolean checking(){

        Bitmap bitmap = ((BitmapDrawable) imageButton.getDrawable()).getBitmap();

        if (toStringImage(bitmap).length()<1){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.errorMessage_photo),Toast.LENGTH_LONG).show();
            return false;
        }else if (editText.getText().toString().length()<1){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.errorMessage_writing),Toast.LENGTH_LONG).show();
            return false;
        }else {
            return true;
        }
    }



    private void SelectImage(){
        final CharSequence[] items={getResources().getString(R.string.camera),getResources().getString(R.string.gallery),getResources().getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle(getResources().getString(R.string.changeImage ));

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
                imageButton.setImageBitmap(bmp1);

            }else if(requestCode==SELECT_FILE){
                Uri uri = data.getData();
                try {
                    Bitmap bmp2 = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), uri);
                    imageButton.setImageBitmap(bmp2);
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

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(AddActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(AddActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.requiredLocation))
                        .setMessage(getResources().getString(R.string.permission))
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(AddActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(AddActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Double latitude1 = location.getLatitude();
                                Double longitude1 = location.getLongitude();

                                latitude = Double.toString(latitude1);
                                longitude = Double.toString(longitude1);
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{

            }
        }
    }

    public class MyAsyncTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final ProgressDialog dialog = ProgressDialog.show(AddActivity.this, getResources().getString(R.string.loading), getResources().getString(R.string.wait), true);
            dialog.show();

            Runnable progressRunnable = new Runnable() {
                @Override
                public void run() {
                    dialog.cancel();
                }
            };
            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 500);
        }

        @Override
        protected String doInBackground(String... url) {
            return connection.httpURLConnectionPost(url[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("message","message = " + result);
            handleResult(result);
        }
    }

    public void handleResult(String string){
        String result = string.trim();

        if (result.equals("successful")){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.sending_successful), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AddActivity.this,MainActivity.class);
            startActivity(intent);
        }else if (result.equals("unsuccessful")){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.sending_unsuccessful), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.haveNotConnect), Toast.LENGTH_LONG).show();
        }

    }
}
