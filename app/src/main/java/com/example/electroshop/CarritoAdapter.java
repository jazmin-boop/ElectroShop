package com.example.electroshop;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    private Context context;
    private Cursor cursor;
    private DBHelper dbHelper;
    private OnCarritoChangeListener listener;

    public interface OnCarritoChangeListener {
        void onChange();
    }

    public CarritoAdapter(Context context, Cursor cursor, DBHelper dbHelper, OnCarritoChangeListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        int carritoId = cursor.getInt(0);
        int productoId = cursor.getInt(1);
        String nombre = cursor.getString(2);
        double precio = cursor.getDouble(3);
        int cantidad = cursor.getInt(4);

        holder.tvNombre.setText(nombre);
        holder.tvPrecio.setText(String.format(Locale.US, "S/ %.2f", precio));
        holder.tvCantidad.setText(String.valueOf(cantidad));

        int imgResId = ProductoAdapter.obtenerImagenPorNombre(context, nombre);
        if (imgResId != 0) {
            holder.ivProducto.setImageResource(imgResId);
        } else {
            holder.ivProducto.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.btnMas.setOnClickListener(v -> {
            int stock = dbHelper.obtenerStock(productoId);
            if (cantidad < stock) {
                boolean ok = dbHelper.actualizarCantidadCarrito(productoId, cantidad + 1);
                if (ok) {
                    listener.onChange();
                } else {
                    Toast.makeText(context, "Stock insuficiente", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Stock máximo alcanzado (" + stock + ")", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnMenos.setOnClickListener(v -> {
            if (cantidad > 1) {
                dbHelper.actualizarCantidadCarrito(productoId, cantidad - 1);
                listener.onChange();
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            dbHelper.eliminarDelCarrito(carritoId);
            listener.onChange();
        });
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    public static class CarritoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvCantidad;
        ImageView ivProducto;
        MaterialButton btnMas, btnMenos;
        ImageButton btnEliminar;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreCarrito);
            tvPrecio = itemView.findViewById(R.id.tvPrecioCarrito);
            tvCantidad = itemView.findViewById(R.id.tvCantidadCarrito);
            ivProducto = itemView.findViewById(R.id.ivProductoCarrito);
            btnMas = itemView.findViewById(R.id.btnMas);
            btnMenos = itemView.findViewById(R.id.btnMenos);
            btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
        }
    }
}
