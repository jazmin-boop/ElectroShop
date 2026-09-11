package com.example.electroshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnEntrar;
    TextView tvGoRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvGoRegistro = findViewById(R.id.tvGoRegistro);

        btnEntrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equals("admin@electroshop.com") && password.equals("admin123")) {
                iniciarSesion("admin");
            } else if (email.equals("empleado@electroshop.com") && password.equals("empleado123")) {
                iniciarSesion("empleado");
            } else {
                iniciarSesion("cliente");
            }
        });

        tvGoRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void iniciarSesion(String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
