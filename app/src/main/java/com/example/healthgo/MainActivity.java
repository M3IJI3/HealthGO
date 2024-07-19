package com.example.healthgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BottomSheetFragment.BottomSheetListener {
    private static final int GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    public TextView textViewFirstName, textViewDate, textViewBMI;
    public ImageButton imgButtonBMI, imgButtonAddWeight;
    String firstName, lastName;

    // bar chart
    private BarChart barChartWeight;
    private BarDataSet dataSet;
    private BarData barData;
    List<Float> weightList;
    List<BarEntry> entries;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // foot steps set up
        if (isFirstTime())
        {
            FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .build();

            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

            if(!GoogleSignIn.hasPermissions(account, fitnessOptions))
            {
                GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSION_REQUEST_CODE, account, fitnessOptions);
            } else {
                accessGoogleFit();
            }
        } else {
            accessGoogleFit();
        }

        textViewFirstName = findViewById(R.id.textViewName);
        textViewDate = findViewById(R.id.textViewDate);
        imgButtonBMI = findViewById(R.id.imgButtonBMI);
        imgButtonAddWeight = findViewById(R.id.imgButtonAddWeight);
        textViewBMI = findViewById(R.id.textViewBMI);

        weightList = new LinkedList<>();
        entries = new ArrayList<>();

        // weight bar chart
        barChartWeight = findViewById(R.id.barChartWeight);

        // configure bar chart
        if (!entries.isEmpty())
        {
            configBarChart();
        }
        // configBarChart();

        Intent intent = getIntent();
        String bmi = intent.getStringExtra("bmi");

        if(intent.hasExtra("firstName") && intent.hasExtra("lastName"))
        {
            firstName = intent.getStringExtra("firstName");
            lastName = intent.getStringExtra("lastName");
            saveUserData(firstName, lastName);
        } else {
            loadUserData();
        }

        textViewFirstName.setText(firstName + " " + lastName);
        textViewDate.setText("Today is " + getCurrentDate());
        textViewBMI.setText(bmi);

        imgButtonBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BMIActivity.class);
                startActivity(intent);
            }
        });

        imgButtonAddWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configBarChart() {
        dataSet = new BarDataSet(entries, "Weight");
        dataSet.setColor(Color.parseColor("#FD6326"));
        barData = new BarData(dataSet);
        barChartWeight.setData(barData);
        barChartWeight.getDescription().setEnabled(false);
        barChartWeight.setDragEnabled(true);
        barData.setBarWidth(0.5f);

        XAxis xAxis = barChartWeight.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = barChartWeight.getAxisLeft();
        leftAxis.setAxisMinimum(0f);;
        leftAxis.setAxisMaximum(150f);
        leftAxis.setGranularity(50f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + " kg";
            }
        });

        YAxis rightAxis = barChartWeight.getAxisRight();
        rightAxis.setEnabled(false);

        barChartWeight.setDoubleTapToZoomEnabled(false);
        barChartWeight.setVisibleXRangeMaximum(6);
        barChartWeight.invalidate();
    }

    @Override
    public void onSaveClicked(Float weight) {
        weightList.add(weight);

        entries.add(new BarEntry(weightList.size(), weightList.get(weightList.size()-1)));

        configBarChart();
    }

    private void saveUserData(String firstName, String lastName)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putString("firstName",firstName);
        editor.putString("lastName", lastName);
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }

    private void loadUserData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        firstName = sharedPreferences.getString("firstName","");
        lastName = sharedPreferences.getString("lastName", "");

    }

    private boolean isFirstTime()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isFirstTime", true);
    }

    public String getCurrentDate()
    {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.CANADA);
        return dateFormat.format(currentDate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_FIT_PERMISSION_REQUEST_CODE) {
            accessGoogleFit();
        }
    }

    private void accessGoogleFit()
    {
        TextView stepsTextView = findViewById(R.id.textViewFootSteps);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if( account == null ) {
            stepsTextView.setText("2");
            return;
        }

        Fitness.getHistoryClient(this, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnCompleteListener(new OnCompleteListener<DataSet>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSet> task)
                    {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DataSet dataSet = task.getResult();
                            long totalSteps = 0;
                            if (!dataSet.isEmpty()) {
                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    totalSteps += dp.getValue(Field.FIELD_STEPS).asInt();
                                    Log.d(TAG, "Data point: " + dp.toString());
                                    Log.d(TAG, "Steps: " + dp.getValue(Field.FIELD_STEPS));
                                }
                            } else {
                                Log.d(TAG, "DataSet is empty.");
                            }
                            Log.d(TAG, "Total steps: " + totalSteps);
                            stepsTextView.setText(totalSteps + "");
                        } else {
                            Log.e("MainActivity", "There was a problem getting the step count.", task.getException());
                        }
                    }
                });
    }
}