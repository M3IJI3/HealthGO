package com.example.healthgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    public DatabaseHelper db;
    public EditText firstName, lastName, email, password, confirmedPassword, phone;
    public Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        db = new DatabaseHelper(this);

        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        email = findViewById(R.id.editTextRegisterEmail);
        password = findViewById(R.id.editTextRegisterPassword);
        confirmedPassword = findViewById(R.id.editTextRegisterConfirmedPassword);
        phone = findViewById(R.id.editTextRegisterPhone);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( firstName.getText().toString().isEmpty() || lastName.getText().toString().isEmpty() ||
                     email.getText().toString().isEmpty() || password.getText().toString().isEmpty() ||
                    confirmedPassword.getText().toString().isEmpty() || phone.getText().toString().isEmpty())
                {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    if ( password.getText().toString().equals(confirmedPassword.getText().toString())
                            && email.getText().toString().matches("^\\w+(-+.\\w+)*@\\w+(-.\\w+)*.\\w+(-.\\w+)*$"))
                    {
                        boolean isInserted = db.insertData(firstName.getText().toString(),
                                                           lastName.getText().toString(),
                                                           email.getText().toString(),
                                                           password.getText().toString(),
                                                           phone.getText().toString());
                        if ( isInserted )
                        {
                            Toast.makeText(RegisterActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                            try {
                                Thread.sleep(1500);
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed...", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords or E-mail do not match..." + email.getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}