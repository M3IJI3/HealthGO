package com.example.healthgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BMIActivity extends AppCompatActivity {

    public EditText height, weight;
    public Button btnBMISave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bmiactivity);

        height = findViewById(R.id.editTextHeight);
        weight = findViewById(R.id.editTextWeight);
        btnBMISave = findViewById(R.id.btnBMISave);

        btnBMISave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userHeight = height.getText().toString();
                String userWeight = weight.getText().toString();

                if( userHeight.isEmpty() || userWeight.isEmpty() )
                {
                    Toast.makeText(BMIActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    float bmi = calBMI(userHeight, userWeight);
                    @SuppressLint("DefaultLocale") String formattedBMI = String.format("%.1f", bmi);
                    Intent intent = new Intent(BMIActivity.this, MainActivity.class);
                    intent.putExtra("bmi", formattedBMI);
                    startActivity(intent);
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public float calBMI(String height, String weight)
    {
        float heightInFloat = Float.parseFloat(height);
        float weightInFloat = Float.parseFloat(weight);
        return (weightInFloat / heightInFloat / heightInFloat) * 10000;
    }
}