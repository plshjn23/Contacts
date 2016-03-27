package com.niktin.adreporting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GlanceActivity extends AppCompatActivity {



    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    ImageView imgView;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;
    TextView tvAddress,tv,tv1,tv2,tvv,tvm,tve;
    private ProgressDialog loading;

    final DBAdapter db1 = new DBAdapter(GlanceActivity.this);

    AppLocationService appLocationService;
    static String strSDCardPathName = Environment.getExternalStorageDirectory() + "/temp_picture" + "/";
    static String strURLUpload = "http://www.learnhtml.provisor.in/android/uploadFile.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glance);




        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        appLocationService = new AppLocationService(
                GlanceActivity.this);

        //Showing the current logged in email to textview
        //Fetching email from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");




        tv1 = (TextView) findViewById(R.id.tv_amount);
        tv = (TextView) findViewById(R.id.textView2);
        tv.setText(username);
        tv.setVisibility(View.GONE);
        tv2 = (TextView) findViewById(R.id.textView3);
        tv2.setVisibility(View.GONE);
        tvv = (TextView) findViewById(R.id.tv1);
        String z = "0";
        String y = "1";
        tv2.setText(y);
        tvv.setText(z);
        tvm = (TextView)findViewById(R.id.tvmorning);
        tve = (TextView) findViewById(R.id.textView1);


        db1.open();

        Cursor c = db1.getContact(Integer.parseInt
                (tv2.getText().toString()));
        if (c.moveToFirst())
            DisplayContact(c);
        else
            Toast.makeText(getBaseContext(), "No contact found",
                    Toast.LENGTH_LONG).show();
        db1.close();




        Location location = appLocationService.getLocation(LocationManager.GPS_PROVIDER);

        getData();




        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());
        } else {
            showSettingsAlert();
        }

        tvAddress = (TextView)findViewById(R.id.textView);
        tvAddress.setVisibility(View.GONE);








// Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
// *** Create Folder
        createFolder();
// *** ImageView
        imgView = (ImageView) findViewById(R.id.imgview123);
        imgView.setVisibility(View.GONE);
        ImageView camera = (ImageView)findViewById(R.id.camera123);





// *** Take Photo

// Perform action on click
        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
// Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
// Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                    }
// Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                    }
                }

            }

        });



    }

    private void DisplayContact(Cursor c) {
        // TODO Auto-generated method stub
        tvv.setText(c.getString(1));

    }

    private void getData() {
        String id = tv.getText().toString();
        if (id.equals("")) {
            Toast.makeText(this, "Please enter your Email", Toast.LENGTH_LONG).show();
            return;
        }
        loading = ProgressDialog.show(this,"Please wait...","Fetching...",false,false);

        String url = Config.DATA_URL+id;

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.dismiss();
                showJSON(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GlanceActivity.this,error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showJSON(String response){
        String name="";
        String address="";
        String vc = "";
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config.JSON_ARRAY);
            JSONObject collegeData = result.getJSONObject(0);
            name = collegeData.getString(Config.KEY_NAME);
            address = collegeData.getString(Config.KEY_ADDRESS);
            vc = collegeData.getString(Config.KEY_VC);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tv1.setText(name);
        tvm.setText(address);
        tve.setText(vc);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                GlanceActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        GlanceActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    public class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            tvAddress.setText(locationAddress);
        }
    }

// Upload Image in Background

    public class UploadAsync extends AsyncTask<String, Void, Void> {



// ProgressDialog

        private ProgressDialog mProgressDialog;



        public UploadAsync(GlanceActivity activity) {

          //  mProgressDialog = new ProgressDialog(activity);

//            mProgressDialog.setMessage("Uploading please wait.....");

//            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

  //          mProgressDialog.setCancelable(true);



        }



        protected void onPreExecute() {

            super.onPreExecute();

    //        mProgressDialog.show();

        }



        @Override

        protected Void doInBackground(String... par) {



// *** Upload all file to Server

            File file = new File(strSDCardPathName);

            File[] files = file.listFiles();

            for (File sfil : files) {

                if (sfil.isFile()) {

                    uploadFiletoServer(sfil.getAbsolutePath(), strURLUpload);

                }

            }



//*** Clear Folder

            clearFolder();



            return null;

        }



        protected void onPostExecute(Void unused) {

//            mProgressDialog.dismiss();


            db1.open();
            String y1;
            y1 = tvv.getText().toString();
            int converted=Integer.parseInt(y1);
            converted = converted + 1 ;

            String aString = Integer.toString(converted);
            tvv.setText(aString);
            if (db1.updateContact
                    (Integer.parseInt(tv2.getText().toString()),
                            tvv.getText().toString(), tv.getText().toString()))

                Toast.makeText(getBaseContext(), "Photo Uploaded.",Toast.LENGTH_LONG).show();



            else
                Toast.makeText(getBaseContext(), "Update failed.",
                        Toast.LENGTH_LONG).show();
            db1.close();

        }



    }



    private File createImageFile() throws IOException {

// Create an image file name

        String y =  tvAddress.getText().toString();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_" + y + "_";

        File storageDir = new File(strSDCardPathName);

        File image = File.createTempFile(imageFileName, /* prefix */
                ".jpg", /* suffix */

                storageDir /* directory */



        );


// Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            imgView.setImageBitmap(bitmap);

            isInternetPresent = cd.isConnectingToInternet();

            // check for Internet status
            if (isInternetPresent) {
                // Internet Connection is Present
                // make HTTP requests
                new  UploadAsync(GlanceActivity.this).execute();
            } else {
                // Internet connection is not present
                // Ask user to connect to Internet
                Toast.makeText(getBaseContext(), "No Internet Connection.Please Check Your Internet Connection",Toast.LENGTH_LONG).show();

            }



        }
    }

    public static boolean uploadFiletoServer(String strSDPath, String strUrlServer) {

        int bytesRead, bytesAvailable, bufferSize;

        byte[] buffer;

        int maxBufferSize = 1 * 1024 * 1024;

        int resCode = 0;

        String resMessage = "";

        String lineEnd = "\r\n";

        String twoHyphens = "--";

        String boundary = "*****";

        try {

            File file = new File(strSDPath);
            if (!file.exists()) {
                return false;
            }
            FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));
            URL url = new URL(strUrlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes(
                    "Content-Disposition: form-data; name=\"filUpload\";filename=\"" + strSDPath + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
// Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
// Response Code and Message
            resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = is.read()) != -1) {
                    bos.write(read);
                }
                byte[] result = bos.toByteArray();
                bos.close();
                resMessage = new String(result);
            }
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception ex) {
// Exception handling
            return false;
        }
    }
    public static void createFolder() {
        File folder = new File(strSDCardPathName);
        try {
// Create folder
            if (!folder.exists()) {
                folder.mkdir();
            }
        } catch (Exception ex) {
        }
    }
    public static void clearFolder(){
        File dir = new File(strSDCardPathName);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int n = 0; n < children.length; n++)
            {
                new File(dir, children[n]).delete();
            }
        }
    }

}