package com.example.electroshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ConfirmacionActivity extends AppCompatActivity {

    TextView tvConfirmacionDetalle;
    Button btnVerOrden, btnComprobante, btnInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion);

        tvConfirmacionDetalle = findViewById(R.id.tvConfirmacionDetalle);
        btnVerOrden = findViewById(R.id.btnVerOrden);
        btnComprobante = findViewById(R.id.btnComprobante);
        btnInicio = findViewById(R.id.btnInicio);

        long ventaId = getIntent().getLongExtra("ventaId", -1);
        double total = getIntent().getDoubleExtra("total", 0);

        tvConfirmacionDetalle.setText(String.format(
                Locale.US,
                "Venta N.º: %d | Total: S/ %.2f\nEstado: APROBADA (PAGADA)",
                ventaId,
                total
        ));

        btnVerOrden.setOnClickListener(v -> mostrarPanelMisOrdenes(ventaId, total));
        
        btnComprobante.setOnClickListener(v -> mostrarEReceipt(ventaId, total));

        btnInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void mostrarEReceipt(long ventaId, double total) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_e_receipt, null);
        TextView tvReceiptId = dialogView.findViewById(R.id.tvReceiptId);
        TextView tvReceiptSubtotal = dialogView.findViewById(R.id.tvReceiptSubtotal);
        TextView tvReceiptTax = dialogView.findViewById(R.id.tvReceiptTax);
        TextView tvReceiptTotal = dialogView.findViewById(R.id.tvReceiptTotal);
        Button btnDownload = dialogView.findViewById(R.id.btnDownloadReceipt);

        tvReceiptId.setText("CDR4GHGJF-" + ventaId);
        double subtotal = total / 1.18;
        double tax = total - subtotal;
        tvReceiptSubtotal.setText(String.format(Locale.US, "S/ %.2f", subtotal));
        tvReceiptTax.setText(String.format(Locale.US, "+ S/ %.2f", tax));
        tvReceiptTotal.setText(String.format(Locale.US, "S/ %.2f", total));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnDownload.setOnClickListener(v -> {
            Toast.makeText(this, "¡Comprobante descargado con éxito!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarPanelMisOrdenes(long ventaId, double total) {
        String ordenInfo = "Mi orden (activa)\n\n" +
                "Transacción ID: #GR45HGJF (Venta #" + ventaId + ")\n" +
                "Fecha: 10 Sep, 2026\n" +
                "Total Pago: S/ " + String.format(Locale.US, "%.2f", total) + "\n" +
                "Estado: En proceso de entrega";

        new AlertDialog.Builder(this)
                .setTitle("Mis Órdenes")
                .setMessage(ordenInfo)
                .setPositiveButton("Rastrear orden", (dialog, which) -> {
                    Toast.makeText(this, "Rastreando pedido...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }
}
