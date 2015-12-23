package com.thoughtworks.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thoughtworks.myapplication.domain.PM25;
import com.thoughtworks.myapplication.service.AirServiceClient;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by syn on 2015/12/22.
 */
public class AreaActivity extends Activity {
    public static String CITY_NAME;
    private TextView cityName;
    private TextView cityContent;
    private ProgressDialog loadingDialog;
    private LinearLayout layout;
    private TextView cityAverage;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_city);
        layout = (LinearLayout) findViewById(R.id.city);
        Intent intent = getIntent();
        CITY_NAME = intent.getStringExtra("cityname");
        cityName = (TextView) findViewById(R.id.city_name);
        cityContent = (TextView) findViewById(R.id.city_content);
        cityAverage = (TextView) findViewById(R.id.city_average);
        buttonBack = (Button) findViewById(R.id.button_back);


        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage(getString(R.string.loading_message));
        onQueryPM25Click();
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AreaActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onQueryPM25Click() {
        if (!TextUtils.isEmpty(CITY_NAME)) {
            showLoading();
            AirServiceClient.getInstance().requestPM25(CITY_NAME, new Callback<List<PM25>>() {
                @Override
                public void onResponse(Response<List<PM25>> response, Retrofit retrofit) {
                    showSuccessScreen(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("TAG", "failure");
                    showErrorScreen();
                }
            });
        }
    }

    private void showSuccessScreen(Response<List<PM25>> response) {
        hideLoading();
        if (response != null) {
            populate(response.body());
        }
    }

    private void showErrorScreen() {
        hideLoading();
        cityName.setText(R.string.error_message_query_pm25);
    }

    private void showLoading() {
        loadingDialog.show();
    }

    private void hideLoading() {
        loadingDialog.dismiss();
    }

    private void populate(List<PM25> data) {
        if (data != null && !data.isEmpty()) {
            PM25 average = data.get(data.size() - 1);
            cityName.setText(average.getArea());
            int aqi = Integer.parseInt(average.getAqi());
            changeBackgroundColor(aqi);
            String temp = "";
            for (int i = 0; i < data.size() - 2; i++) {
                PM25 pm25 = data.get(i);
                temp += "检测位置： " + pm25.getPositionName() + " 空气质量： " + pm25.getQuality() + "\n\n";
            }
            cityContent.setText(temp);
            cityAverage.setText("总体质量： " + average.getQuality() + " " + average.getAqi());
        }
    }

    private void changeBackgroundColor(int aqi) {
        cityContent.setTextColor(getResources().getColor(R.color.grade5));
        if (aqi < 50) {
            layout.setBackgroundColor(Color.WHITE);
        } else if (aqi < 100) {
            layout.setBackgroundColor(getResources().getColor(R.color.grade1));
        } else if (aqi < 150) {
            layout.setBackgroundColor(getResources().getColor(R.color.grade2));
        } else if (aqi < 200) {
            layout.setBackgroundColor(getResources().getColor(R.color.grade3));
        } else if (aqi < 250) {
            layout.setBackgroundColor(getResources().getColor(R.color.grade4));
        } else {
            layout.setBackgroundColor(getResources().getColor(R.color.grade5));
            cityContent.setTextColor(Color.WHITE);
        }

    }
}
