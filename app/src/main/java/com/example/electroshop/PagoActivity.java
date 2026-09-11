package com.example.electroshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class PagoActivity extends AppCompatActivity {

    EditText etNumeroTarjeta, etTitular, etExpiracion, etCVV;
    TextView tvMonto, tvCardNumber, tvCardHolder, tvCardExpiry;
    Button btnPagar, btnCancelar;
    DBHelper dbHelper;

    double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etTitular = findViewById(R.id.etTitular);
        etExpiracion = findViewById(R.id.etExpiracion);
        etCVV = findViewById(R.id.etCVV);
        tvMonto = findViewById(R.id.tvMonto);
        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvCardHolder = findViewById(R.id.tvCardHolder);
        tvCardExpiry = findViewById(R.id.tvCardExpiry);

        btnPagar = findViewById(R.id.btnPagar);
        btnCancelar = findViewById(R.id.btnCancelar);

        findViewById(R.id.btnBackPago).setOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);

        calcularTotal();

        // Formateador automático de tarjeta de crédito (con espacios y ceros por defecto)
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digitsOnly = s.toString().replaceAll("\\D", "");
                if (digitsOnly.length() > 16) {
                    digitsOnly = digitsOnly.substring(0, 16);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digitsOnly.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(digitsOnly.charAt(i));
                }

                String formattedStr = formatted.toString();
                if (!s.toString().equals(formattedStr)) {
                    etNumeroTarjeta.setText(formattedStr);
                    etNumeroTarjeta.setSelection(formattedStr.length());
                }

                // Sincronizar tarjeta virtual con ceros de relleno
                if (digitsOnly.isEmpty()) {
                    tvCardNumber.setText("0000  0000  0000  0000");
                } else {
                    StringBuilder cardPreview = new StringBuilder(digitsOnly);
                    while (cardPreview.length() < 16) {
                        cardPreview.append("0");
                    }
                    StringBuilder finalPreview = new StringBuilder();
                    for (int i = 0; i < cardPreview.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            finalPreview.append("  ");
                        }
                        finalPreview.append(cardPreview.charAt(i));
                    }
                    tvCardNumber.setText(finalPreview.toString());
                }

                isFormatting = false;
            }
        });

        etTitular.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    tvCardHolder.setText("NOMBRE DEL TITULAR");
                } else {
                    tvCardHolder.setText(s.toString().toUpperCase());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etExpiracion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    tvCardExpiry.setText("MM/AA");
                } else {
                    tvCardExpiry.setText(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnPagar.setOnClickListener(v -> procesarPago());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void calcularTotal() {
        Cursor cursor = dbHelper.mostrarCarrito();
        double subtotal = 0;

        while (cursor.moveToNext()) {
            subtotal += cursor.getDouble(5);
        }
        cursor.close();

        total = subtotal + (subtotal * 0.18);
        tvMonto.setText(String.format(Locale.US, "Total a pagar: S/ %.2f", total));
    }

    private void procesarPago() {
        String numero = etNumeroTarjeta.getText().toString().replace(" ", "");
        String titular = etTitular.getText().toString().trim();
        String expiracion = etExpiracion.getText().toString().trim();
        String cvv = etCVV.getText().toString().trim();

        if (numero.isEmpty() || titular.isEmpty() || expiracion.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numero.length() < 13) {
            Toast.makeText(this, "Número de tarjeta inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String ultimos4 = numero.substring(Math.max(0, numero.length() - 4));
        long clienteId = dbHelper.obtenerOInsertarCliente(titular, "DNI-" + System.currentTimeMillis());

        long ventaId = dbHelper.registrarVentaConPago(
                clienteId,
                "Tarjeta Bancaria",
                titular,
                ultimos4,
                total
        );

        if (ventaId == -1) {
            Toast.makeText(this, "Error al procesar la compra. Verifique el stock.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ConfirmacionActivity.class);
        intent.putExtra("ventaId", ventaId);
        intent.putExtra("total", total);
        startActivity(intent);
        finish();
    }
}
