package com.example.healthgo;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
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
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements WeightBottomSheetFragment.BMIBottomSheetListener {
    private static final int GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1;
    private static final String  API_KEY = "lS0Hs1wMVLNljtLgGYbWaw==sl3oMVgmfEsaaDFR";
    private static final String TAG = "MainActivity";
    public TextView textViewFirstName, textViewDate, textViewBMI, textViewDailyCalorieTarget, textViewCalorieIntake;
    public ImageButton imgButtonBMI, imgButtonAddWeight, imgButtonAddCalorie;
    public CircularProgressBar circularProgressBarCalorie;
    private ConstraintLayout constraintLayoutCalorie;
    private EditText currentEditText;
    String firstName, lastName;

    // bar chart
    private BarChart barChartWeight;
    private BarDataSet dataSet;
    private BarData barData;
    List<Float> weightList;
    List<BarEntry> entries;
    private int intakeAccumulated;

    // search view
    public SearchView searchView;
    public TextView resultText;
    public RequestQueue requestQueue;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // foot steps set up
        if ( isFirstTime() )
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

        intakeAccumulated = 0;

        textViewFirstName = findViewById(R.id.textViewName);
        textViewDate = findViewById(R.id.textViewDate);
        imgButtonBMI = findViewById(R.id.imgButtonBMI);
        imgButtonAddWeight = findViewById(R.id.imgButtonAddWeight);
        textViewBMI = findViewById(R.id.textViewBMI);

        constraintLayoutCalorie = findViewById(R.id.constraintLayoutCalorie);
        imgButtonAddCalorie = findViewById(R.id.imgButtonAddCalorie);
        textViewDailyCalorieTarget = findViewById(R.id.textViewCalorieDailyTarget);
        textViewCalorieIntake = findViewById(R.id.textViewCalorieIntake);
        circularProgressBarCalorie = findViewById(R.id.circularProgressBarCalorie);

        // weight bar chart
        barChartWeight = findViewById(R.id.barChartWeight);

        searchView = findViewById(R.id.searchView);
        resultText = findViewById(R.id.textViewResult);
        requestQueue = Volley.newRequestQueue(this);

        // search
        weightList = new LinkedList<>();
        entries = new ArrayList<>();


        loadCalorieData();
        loadWeightData();

        // configure bar chart
        if (!entries.isEmpty())
        {
            configBarChart();
        }

        // configure calorie progress bar
        configProgressBar();

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
                WeightBottomSheetFragment bottomSheetFragment = new WeightBottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        });

        textViewDailyCalorieTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceTextViewWithEditText(textViewDailyCalorieTarget);
            }
        });

        textViewCalorieIntake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceTextViewWithEditText(textViewCalorieIntake);
            }
        });

        circularProgressBarCalorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String calorieIntake = intakeAccumulated + " Cal";
                Toast.makeText(MainActivity.this, "Calorie Intake: " + calorieIntake, Toast.LENGTH_SHORT).show();
            }
        });

        constraintLayoutCalorie.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(currentEditText != null)
                {
                    currentEditText.clearFocus();
                }
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchCalorieData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchCalorieData(String query) {
        String url = "https://api.api-ninjas.com/v1/nutrition?query=" + query;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            if (response.length() > 0) {
                                JSONObject foodItem = response.getJSONObject(0);
                                String foodName = foodItem.getString("name");
                                int fat = foodItem.getInt("fat_total_g");
                                int fatSaturated = foodItem.getInt("fat_saturated_g");
                                int sodium = foodItem.getInt("sodium_mg");
                                int potassium = foodItem.getInt("potassium_mg");
                                int cholesterol = foodItem.getInt("cholesterol_mg");
                                int carbohydrates = foodItem.getInt("carbohydrates_total_g");
                                String result = "Name: " + foodName + "\n" +
                                                "Fat: " + fat + "mg\n" +
                                                "Fat Saturated: " + fatSaturated + "mg\n" +
                                                "Sodium: " + sodium + "mg\n" +
                                                "Potassium: " + potassium + "mg\n" +
                                                "Cholesterol: " + cholesterol + "mg\n" +
                                                "Carbohydrates: " + carbohydrates + "mg\n";
                                resultText.setText(result);
                            } else {
                                resultText.setText("No data found.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            resultText.setText("Error parsing response");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        resultText.setText("Error fetching data.");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", API_KEY); // Replace with your actual API key
                return headers;
            }
        };
        requestQueue.add(jsonArrayRequest);
    }

    private void replaceTextViewWithEditText(final TextView textView) {
        final EditText editText = new EditText(this);
        editText.setId(textView.getId());
        editText.setLayoutParams(textView.getLayoutParams());
        editText.setText(textView.getText());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextSize(25);
        editText.setTextColor(getResources().getColor(R.color.primary));
        editText.setTypeface(textView.getTypeface());

        int index = constraintLayoutCalorie.indexOfChild(textView);
        constraintLayoutCalorie.removeView(textView);
        constraintLayoutCalorie.addView(editText, index);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String newText = editText.getText().toString();
                    textView.setText(newText);
                    replaceEditTextWithTextView(editText, textView);
                    updateCircularProgressBar();
                    saveCalorieData();
                }
            }
        });
        currentEditText = editText;
        editText.requestFocus();
    }

    private void updateCircularProgressBar() {
        int dailyTarget = Integer.parseInt(textViewDailyCalorieTarget.getText().toString());
        int calorieIntake = Integer.parseInt(textViewCalorieIntake.getText().toString());
        intakeAccumulated += calorieIntake;
        float progress = (float) intakeAccumulated / dailyTarget * 100;
        circularProgressBarCalorie.setProgressWithAnimation(progress, 1000L);
    }

    private void replaceEditTextWithTextView(EditText editText, TextView textView) {
        int index = constraintLayoutCalorie.indexOfChild(editText);
        constraintLayoutCalorie.removeView(editText);
        constraintLayoutCalorie.addView(textView, index);
        currentEditText = null;
        saveWeightData();
    }

    private void configProgressBar() {
        circularProgressBarCalorie.setProgressMax(100f);
        circularProgressBarCalorie.setBackgroundProgressBarColor(Color.parseColor("#F5F5F5"));
//        circularProgressBarCalorie.setProgressWithAnimation(50f, 1000L);
        circularProgressBarCalorie.setProgressBarWidth(20f);
        circularProgressBarCalorie.setBackgroundProgressBarWidth(20f);
        circularProgressBarCalorie.setRoundBorder(true);
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
        leftAxis.setAxisMinimum(0f);
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

        saveWeightData();

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


    private void saveCalorieData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("dailyCalorieTarget", textViewDailyCalorieTarget.getText().toString());
        editor.putString("calorieIntake", textViewCalorieIntake.getText().toString());
        editor.apply();
    }

    private void loadCalorieData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        String dailyCalorieTarget = sharedPreferences.getString("dailyCalorieTarget", "2000");
        String calorieIntake = sharedPreferences.getString("calorieIntake", "0");
        textViewDailyCalorieTarget.setText(dailyCalorieTarget);
        textViewCalorieIntake.setText(calorieIntake);
    }


    private void saveWeightData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder weightData = new StringBuilder();
        for (Float weight : weightList) {
            weightData.append(weight).append(",");
        }

        editor.putString("weightData", weightData.toString());
        editor.apply();
    }

    private void loadWeightData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        String weightData = sharedPreferences.getString("weightData", "");

        if (!weightData.isEmpty()) {
            String[] weights = weightData.split(",");
            for (String weightStr : weights) {
                if (!weightStr.isEmpty()) {
                    Float weight = Float.parseFloat(weightStr);
                    weightList.add(weight);
                    entries.add(new BarEntry(weightList.size(), weight));
                }
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        loadCalorieData();
        updateCircularProgressBar();
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