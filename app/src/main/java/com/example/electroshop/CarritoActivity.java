package com.example.electroshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class CarritoActivity extends AppCompatActivity {

    RecyclerView rvCarrito;
    TextView tvSubtotal, tvIgv, tvTotal;
    Button btnComprar, btnVaciar;
    DBHelper dbHelper;
    CarritoAdapter adapter;

    double subtotal = 0;
    final double IGV_PERCENT = 0.18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        rvCarrito = findViewById(R.id.rvCarrito);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvIgv = findViewById(R.id.tvIgv);
        tvTotal = findViewById(R.id.tvTotal);
        btnComprar = findViewById(R.id.btnComprar);
        btnVaciar = findViewById(R.id.btnVaciar);

        dbHelper = new DBHelper(this);
        rvCarrito.setLayoutManager(new LinearLayoutManager(this));

        btnComprar.setOnClickListener(v -> {
            if (subtotal <= 0) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, PagoActivity.class));
        });

        btnVaciar.setOnClickListener(v -> {
            dbHelper.vaciarCarrito();
            actualizarUI();
            Toast.makeText(this, "Carrito vaciado", Toast.LENGTH_SHORT).show();
        });

        actualizarUI();
    }

    private void actualizarUI() {
        Cursor cursor = dbHelper.mostrarCarrito();
        subtotal = 0;
        
        while (cursor.moveToNext()) {
            subtotal += cursor.getDouble(5);
        }
        
        if (adapter == null) {
            adapter = new CarritoAdapter(this, cursor, dbHelper, this::actualizarUI);
            rvCarrito.setAdapter(adapter);
        } else {
            adapter.setCursor(cursor);
        }

        double igv = subtotal * IGV_PERCENT;
        double total = subtotal + igv;

        tvSubtotal.setText(String.format(Locale.US, "Subtotal: S/ %.2f", subtotal));
        tvIgv.setText(String.format(Locale.US, "IGV (18%%): S/ %.2f", igv));
        tvTotal.setText(String.format(Locale.US, "TOTAL: S/ %.2f", total));
    }
}
