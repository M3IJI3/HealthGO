package com.example.healthgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    public TextView textViewFirstName, textViewDate, textViewBMI;
    public ImageButton imgButtonBMI;
    String firstName, lastName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // foot steps set up
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

        textViewFirstName = findViewById(R.id.textViewName);
        textViewDate = findViewById(R.id.textViewDate);
        imgButtonBMI = findViewById(R.id.imgButtonBMI);
        textViewBMI = findViewById(R.id.textViewBMI);

        Intent intent = getIntent();
        String bmi = intent.getStringExtra("bmi");

        if(intent != null && intent.hasExtra("firstName") && intent.hasExtra("lastName"))
        {
            firstName = intent.getStringExtra("firstName");
            lastName = intent.getStringExtra("lastName");
            saveUserData(firstName, lastName);
        } else {
            loadUserData();
            Log.d("123", firstName);
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveUserData(String firstName, String lastName)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName",firstName);
        editor.putString("lastName", lastName);
        Log.d("test", firstName);
        editor.apply();
    }

    private void loadUserData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE);
        firstName = sharedPreferences.getString("firstName","");
        lastName = sharedPreferences.getString("lastName", "");
        Log.d("123",firstName);
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