package com.example.electroshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombreReg, etDniReg, etTelefonoReg;
    Button btnRegistrar;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombreReg = findViewById(R.id.etNombreReg);
        etDniReg = findViewById(R.id.etDniReg);
        etTelefonoReg = findViewById(R.id.etTelefonoReg);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        dbHelper = new DBHelper(this);

        findViewById(R.id.btnBackRegistro).setOnClickListener(v -> finish());

        boolean fromCheckout = getIntent().getBooleanExtra("fromCheckout", false);

        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombreReg.getText().toString().trim();
            String dni = etDniReg.getText().toString().trim();
            String telefono = etTelefonoReg.getText().toString().trim();

            if (nombre.isEmpty() || dni.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios (Nombre y DNI)", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = dbHelper.obtenerOInsertarCliente(nombre, dni);
            if (id != -1) {
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                if (fromCheckout) {
                    startActivity(new Intent(this, PagoActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Error al registrar cliente", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
