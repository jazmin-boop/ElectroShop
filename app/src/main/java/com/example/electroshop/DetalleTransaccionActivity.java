package com.example.electroshop;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DetalleTransaccionActivity extends AppCompatActivity {

    TextView tvDetalleNombre, tvBadgeComprobante, tvBadgeItem, tvDetalleTotal, tvDetalleCantidad, tvDetallePrecioUnitario;
    ImageView ivDetalleProducto;
    Button btnVolverDetalle;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_transaccion);

        tvDetalleNombre = findViewById(R.id.tvDetalleNombre);
        tvBadgeComprobante = findViewById(R.id.tvBadgeComprobante);
        tvBadgeItem = findViewById(R.id.tvBadgeItem);
        tvDetalleTotal = findViewById(R.id.tvDetalleTotal);
        tvDetalleCantidad = findViewById(R.id.tvDetalleCantidad);
        tvDetallePrecioUnitario = findViewById(R.id.tvDetallePrecioUnitario);
        ivDetalleProducto = findViewById(R.id.ivDetalleProducto);
        btnVolverDetalle = findViewById(R.id.btnVolverDetalle);

        dbHelper = new DBHelper(this);

        long ventaId = getIntent().getLongExtra("ventaId", -1);
        findViewById(R.id.btnBackDetalle).setOnClickListener(v -> finish());
        btnVolverDetalle.setOnClickListener(v -> finish());

        cargarDetalle(ventaId);
    }

    private void cargarDetalle(long ventaId) {
        Cursor c = dbHelper.obtenerDetalleVenta(ventaId);
        StringBuilder nombresBuilder = new StringBuilder();
        double totalVenta = 0;
        int totalCantidad = 0;
        String primerNombre = "";

        while (c.moveToNext()) {
            String nombre = c.getString(c.getColumnIndexOrThrow("nombre"));
            int cantidad = c.getInt(c.getColumnIndexOrThrow("cantidad"));
            double precio = c.getDouble(c.getColumnIndexOrThrow("precio"));
            double subtotalItem = cantidad * precio;
            totalVenta += subtotalItem;
            totalCantidad += cantidad;

            if (primerNombre.isEmpty()) {
                primerNombre = nombre;
            }

            if (nombresBuilder.length() > 0) {
                nombresBuilder.append("\n");
            }
            nombresBuilder.append("• ").append(nombre).append(" (x").append(cantidad).append(") - S/ ").append(String.format(Locale.US, "%.2f", subtotalItem));
        }
        c.close();

        tvDetalleNombre.setText(nombresBuilder.toString());
        tvBadgeComprobante.setText(" Comprobante #" + ventaId + " ");
        tvBadgeItem.setText(" Venta Registrada ");
        tvDetalleTotal.setText(String.format(Locale.US, "S/ %.2f", totalVenta));
        tvDetalleCantidad.setText(totalCantidad + " unid. tot.");
        tvDetallePrecioUnitario.setText(String.format(Locale.US, "S/ %.2f", totalVenta));

        if (!primerNombre.isEmpty()) {
            int imgResId = ProductoAdapter.obtenerImagenPorNombre(this, primerNombre);
            if (imgResId != 0) {
                ivDetalleProducto.setImageResource(imgResId);
            } else {
                ivDetalleProducto.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
    }
}
