package com.example.electroshop;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {
    private Cursor cursor;
    private final String type; // "clientes", "ventas"
    private final OnItemClickListener listener;
    private final DBHelper db;

    public interface OnItemClickListener {
        void onItemClick(long id, Cursor cursor);
    }

    public AdminAdapter(Cursor cursor, String type, OnItemClickListener listener, DBHelper db) {
        this.cursor = cursor;
        this.type = type;
        this.listener = listener;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if ("clientes".equals(type)) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente_crud, parent, false);
            return new ViewHolder(v, "clientes");
        } else if ("ventas".equals(type)) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detalle_venta, parent, false);
            return new ViewHolder(v, "ventas");
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v, "default");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        if ("clientes".equals(type)) {
            long clienteId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            String dni = cursor.getString(cursor.getColumnIndexOrThrow("dni"));
            String telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"));
            
            holder.tvClienteNombre.setText(nombre);
            holder.tvClienteDni.setText("DNI: " + dni);
            holder.tvClienteTelefono.setText("Teléfono: " + (telefono != null && !telefono.isEmpty() ? telefono : "N/A"));

            if (holder.btnEditarCliente != null) {
                holder.btnEditarCliente.setOnClickListener(v -> mostrarModalEditarCliente(v.getContext(), clienteId, nombre, dni, telefono));
            }
            if (holder.btnEliminarCliente != null) {
                holder.btnEliminarCliente.setOnClickListener(v -> mostrarModalEliminarCliente(v.getContext(), clienteId, nombre));
            }
        } else if ("ventas".equals(type)) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
            String cliente = cursor.getString(cursor.getColumnIndexOrThrow("cliente_nombre"));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            
            if (holder.tvVentaTitulo != null) holder.tvVentaTitulo.setText("Venta #" + id);
            if (holder.tvVentaFecha != null) holder.tvVentaFecha.setText("Fecha: " + fecha);
            if (holder.tvVentaCliente != null) holder.tvVentaCliente.setText("Cliente: " + (cliente != null ? cliente : "N/A") + " | Total: S/ " + String.format(Locale.US, "%.2f", total));
            
            if (holder.btnVerDetalleVenta != null) {
                holder.btnVerDetalleVenta.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(id, cursor);
                });
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(id, cursor);
            });
        }
    }

    private void mostrarModalEditarCliente(Context context, long id, String nombreAnt, String dniAnt, String telAnt) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_registrar_cliente, null);
        TextView tvTitulo = dialogView.findViewById(R.id.tvModalTituloCliente);
        if (tvTitulo != null) tvTitulo.setText("Editar Cliente");
        TextInputEditText etNombre = dialogView.findViewById(R.id.etModalCliNombre);
        TextInputEditText etDni = dialogView.findViewById(R.id.etModalCliDni);
        TextInputEditText etTel = dialogView.findViewById(R.id.etModalCliTel);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnModalCliGuardar);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnModalCliCancelar);

        etNombre.setText(nombreAnt);
        etDni.setText(dniAnt);
        etTel.setText(telAnt);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String dni = etDni.getText() != null ? etDni.getText().toString().trim() : "";
            String tel = etTel.getText() != null ? etTel.getText().toString().trim() : "";

            if (nombre.isEmpty() || dni.isEmpty()) {
                Toast.makeText(context, "Complete Nombre y DNI", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = db.actualizarCliente(id, nombre, dni, tel);
            if (ok) {
                Toast.makeText(context, "Cliente actualizado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).onResume();
                }
            } else {
                Toast.makeText(context, "Error al actualizar cliente", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarModalEliminarCliente(Context context, long id, String nombre) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Cliente")
                .setMessage("¿Desea eliminar al cliente '" + nombre + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean ok = db.eliminarCliente(id);
                    if (ok) {
                        Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show();
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).onResume();
                        }
                    } else {
                        Toast.makeText(context, "Error al eliminar cliente", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2, tvClienteNombre, tvClienteDni, tvClienteTelefono, tvVentaTitulo, tvVentaFecha, tvVentaCliente;
        View btnEditarCliente, btnEliminarCliente, btnVerDetalleVenta;

        ViewHolder(View itemView, String layoutType) {
            super(itemView);
            if ("clientes".equals(layoutType)) {
                tvClienteNombre = itemView.findViewById(R.id.tvClienteNombre);
                tvClienteDni = itemView.findViewById(R.id.tvClienteDni);
                tvClienteTelefono = itemView.findViewById(R.id.tvClienteTelefono);
                btnEditarCliente = itemView.findViewById(R.id.btnEditarCliente);
                btnEliminarCliente = itemView.findViewById(R.id.btnEliminarCliente);
            } else if ("ventas".equals(layoutType)) {
                tvVentaTitulo = itemView.findViewById(R.id.tvVentaTitulo);
                tvVentaFecha = itemView.findViewById(R.id.tvVentaFecha);
                tvVentaCliente = itemView.findViewById(R.id.tvVentaCliente);
                btnVerDetalleVenta = itemView.findViewById(R.id.btnVerDetalleVenta);
            } else {
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
