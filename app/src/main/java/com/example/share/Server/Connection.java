package com.example.share.Server;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {

    String inputString="";
    Bitmap bmp;

    public String httpURLConnectionGet(final String urlString) {

        String result="";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    stringBuilder .append(line);
                }
                result = stringBuilder .toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String httpURLConnectionPost(final String urlString){

        String result="";

        try {
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.connect();

            String body= setInputString();

            Log.d("serverPostData","body = " +body);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(body);
            writer.close();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    stringBuilder .append(line);
                }
                result = stringBuilder .toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    public void addBMP (String phpName,Bitmap btm) {
        if (inputString.length()>0){
            inputString = inputString+phpName +"&"+phpName+"=";
            bmp = btm;
        }else {
            inputString = inputString+ phpName+"=";
            bmp = btm;
        }
    }
    public void addData (String phpName, String data) {

        if (inputString.length()>0){
            inputString = inputString+"&"+phpName+"="+data;
        }else {
            inputString = inputString+ phpName+"="+data;


        }
    }

    public String setInputString(){
        return inputString;
    }

}
