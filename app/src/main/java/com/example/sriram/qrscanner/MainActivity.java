package com.example.sriram.qrscanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements QRCodeReaderView.OnQRCodeReadListener {


    ProgressDialog pDialog;
    Context context;
    TextView myTextView;
    String auth_pin,user_roll,user_hash;
    private QRCodeReaderView mydecoderview;
    private ImageView line_image;
    static boolean QRcoderead=false;
    String gender=null;
    String tshirt_size,size,OC_gender,amount;
    boolean tshirt_given,fcard_given,extra_given;
    static int count=0;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=getApplicationContext();

        sharedPreferences=getSharedPreferences("Security", MODE_PRIVATE);
        editor=sharedPreferences.edit();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);



        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setOnQRCodeReadListener(this);

        myTextView = (TextView) findViewById(R.id.exampleTextView);

        line_image = (ImageView) findViewById(R.id.red_line_image);

        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.5f);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        line_image.setAnimation(mAnimation);


    }


    @Override
    public void onQRCodeRead(String s, PointF[] pointFs) {


        if(!QRcoderead) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Scanning QRCode...");
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
            QRcoderead = true;
            if(count==0){
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                editor.putString("Pin",s);
                editor.apply();
            }
            else {
                checkQR(s);
            }
        }

    }

    @Override
    public void cameraNotFound() {

    }

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mydecoderview.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mydecoderview.getCameraManager().stopPreview();
    }
    public void checkQR(String s){
        final String new_str=s;
        final String old_str=sharedPreferences.getString("Security",null);
        String api=getString(R.string.api);
        String url="https://"+api+"/tshirt/getDetails";
        StringRequest postRequest = new StringRequest(Request.Method.POST,url ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status_code");
                            String message=jsonResponse.getString("message");

                            Log.d("Debug","json: "+jsonResponse);
                            Toast.makeText(context,"Successful... Please Wait for processing",Toast.LENGTH_LONG).show();
                            pDialog.dismiss();
                            new update_QRboolean().execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pDialog.dismiss();
                            new update_QRboolean().execute();


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError data) {
                        pDialog.dismiss();
                        new update_QRboolean().execute();
                        data.printStackTrace();
                        Toast.makeText(context, "Error Response", Toast.LENGTH_SHORT).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("new",new_str);
                params.put("old",old_str);

                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);

    }
    private class update_QRboolean extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try{
                wait(1000);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void params){
            QRcoderead=false;
            return ;
        }
    }
    private boolean USER_IS_GOING_TO_EXIT=false;
    @Override
    public void onBackPressed() {
        if(USER_IS_GOING_TO_EXIT){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startActivity(startMain);
        }
        else {
            USER_IS_GOING_TO_EXIT=true;
            Toast.makeText(getApplicationContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
        }
    }


}
