package com.example.electroshop;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {

    private final Context context;
    private final List<Producto> lista;
    private final DBHelper db;
    private final boolean isCrudMode;
    private final boolean isStockMode;

    public ProductoAdapter(Context context, List<Producto> lista, DBHelper db, boolean isCrudMode) {
        this.context = context;
        this.lista = lista;
        this.db = db;
        this.isCrudMode = isCrudMode;
        this.isStockMode = false;
    }

    public ProductoAdapter(Context context, List<Producto> lista, DBHelper db, boolean isCrudMode, boolean isStockMode) {
        this.context = context;
        this.lista = lista;
        this.db = db;
        this.isCrudMode = isCrudMode;
        this.isStockMode = isStockMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isCrudMode ? R.layout.item_producto_crud : R.layout.item_producto;
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new ViewHolder(view, isCrudMode);
    }

    public static int obtenerImagenPorNombre(Context context, String nombreProducto) {
        if (nombreProducto == null) return 0;
        String lower = nombreProducto.toLowerCase(Locale.ROOT);
        String resName;

        if (lower.contains("lavadora")) {
            resName = "lavadora";
        } else if (lower.contains("licuadora")) {
            resName = "licuadora";
        } else if (lower.contains("microondas")) {
            resName = "microondas";
        } else if (lower.contains("refrig") || lower.contains("refigera")) {
            resName = "refigeradora";
        } else if (lower.contains("tv") || lower.contains("televis") || lower.contains("television") || lower.contains("televisor")) {
            resName = "television";
        } else {
            return 0;
        }

        return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto p = lista.get(position);

        holder.tvNombre.setText(p.getNombre());
        holder.tvPrecio.setText(String.format(Locale.US, "S/ %.2f", p.getPrecio()));

        int resId = obtenerImagenPorNombre(context, p.getNombre());
        if (resId != 0) {
            holder.ivProducto.setImageResource(resId);
        } else {
            holder.ivProducto.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        if (isCrudMode) {
            if (isStockMode) {
                if (holder.btnEditar != null) holder.btnEditar.setVisibility(View.GONE);
                if (holder.btnEliminar != null) holder.btnEliminar.setVisibility(View.GONE);
                if (holder.tvStockBadge != null) {
                    holder.tvStockBadge.setVisibility(View.VISIBLE);
                    int stock = p.getStock();
                    String estadoStock;
                    int colorBg;
                    if (stock <= 5) {
                        estadoStock = "¡Bajo: " + stock + " un.!";
                        colorBg = 0xFFD32F2F; // Rojo semáforo
                    } else if (stock <= 10) {
                        estadoStock = "Regular: " + stock + " un.";
                        colorBg = 0xFFFFB300; // Amarillo / Ámbar semáforo
                    } else {
                        estadoStock = "Óptimo: " + stock + " un.";
                        colorBg = 0xFF2E7D32; // Verde semáforo
                    }
                    holder.tvStockBadge.setText(estadoStock);
                    holder.tvStockBadge.setBackgroundColor(colorBg);
                }
            } else {
                if (holder.tvStockBadge != null) holder.tvStockBadge.setVisibility(View.GONE);
                if (holder.btnEditar != null) {
                    holder.btnEditar.setVisibility(View.VISIBLE);
                    holder.btnEditar.setOnClickListener(v -> mostrarModalEditar(p));
                }
                if (holder.btnEliminar != null) {
                    holder.btnEliminar.setVisibility(View.VISIBLE);
                    holder.btnEliminar.setOnClickListener(v -> mostrarModalEliminar(p));
                }
            }
        } else {
            if (holder.tvStockBadge != null) holder.tvStockBadge.setVisibility(View.GONE);
            if (holder.btnAgregar != null) {
                holder.btnAgregar.setOnClickListener(v -> {
                    if (p.getStock() <= 0) {
                        Toast.makeText(context, "Sin stock", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = db.agregarAlCarrito(p.getId(), 1);
                    if (ok) {
                        Toast.makeText(context, "¡Agregado al carrito!", Toast.LENGTH_SHORT).show();
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).actualizarBadgeCarrito();
                        }
                    }
                });
            }

            holder.itemView.setOnClickListener(v -> mostrarModalDetalleProducto(p));
        }
    }

    private void mostrarModalDetalleProducto(Producto p) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_detalle_producto, null);
        ImageView ivProd = dialogView.findViewById(R.id.ivModalDetalleProd);
        TextView tvNombre = dialogView.findViewById(R.id.tvModalDetalleNombre);
        TextView tvCat = dialogView.findViewById(R.id.tvModalDetalleCategoria);
        TextView tvPrecio = dialogView.findViewById(R.id.tvModalDetallePrecio);
        TextView tvStock = dialogView.findViewById(R.id.tvModalDetalleStock);
        TextView tvDesc = dialogView.findViewById(R.id.tvModalDetalleDesc);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnModalCerrar);
        MaterialButton btnAgregar = dialogView.findViewById(R.id.btnModalAgregarCarrito);

        tvNombre.setText(p.getNombre());
        tvCat.setText("Categoría: " + p.getCategoria());
        tvPrecio.setText(String.format(Locale.US, "S/ %.2f", p.getPrecio()));
        tvStock.setText("Stock disponible: " + p.getStock() + " unidades");
        tvDesc.setText(p.getDescripcion() != null && !p.getDescripcion().isEmpty() ? p.getDescripcion() : "Sin descripción disponible.");

        int resId = obtenerImagenPorNombre(context, p.getNombre());
        if (resId != 0) {
            ivProd.setImageResource(resId);
        } else {
            ivProd.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        btnAgregar.setOnClickListener(v -> {
            if (p.getStock() <= 0) {
                Toast.makeText(context, "Sin stock", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = db.agregarAlCarrito(p.getId(), 1);
            if (ok) {
                Toast.makeText(context, "¡Agregado al carrito!", Toast.LENGTH_SHORT).show();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).actualizarBadgeCarrito();
                }
                dialog.dismiss();
            }
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarModalEditar(Producto p) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_registrar_producto, null);
        TextView tvTitulo = dialogView.findViewById(R.id.tvModalTitulo);
        if (tvTitulo != null) tvTitulo.setText("Editar Producto");
        TextInputEditText etNombre = dialogView.findViewById(R.id.etModalNombre);
        TextInputEditText etCategoria = dialogView.findViewById(R.id.etModalCategoria);
        TextInputEditText etPrecio = dialogView.findViewById(R.id.etModalPrecio);
        TextInputEditText etStock = dialogView.findViewById(R.id.etModalStock);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etModalDescripcion);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnModalGuardar);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnModalCancelar);

        etNombre.setText(p.getNombre());
        etCategoria.setText(p.getCategoria());
        etPrecio.setText(String.valueOf(p.getPrecio()));
        etStock.setText(String.valueOf(p.getStock()));
        etDescripcion.setText(p.getDescripcion());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String cat = etCategoria.getText() != null ? etCategoria.getText().toString().trim() : "";
            String precioStr = etPrecio.getText() != null ? etPrecio.getText().toString().trim() : "";
            String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "";
            String desc = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

            if (nombre.isEmpty() || cat.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(context, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = db.actualizarProducto(p.getId(), nombre, cat, Double.parseDouble(precioStr), Integer.parseInt(stockStr), desc);
            if (ok) {
                Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).onResume();
                }
            } else {
                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarModalEliminar(Producto p) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Producto")
                .setMessage("¿Desea eliminar el producto '" + p.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean ok = db.eliminarProducto(p.getId());
                    if (ok) {
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show();
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).onResume();
                        }
                    } else {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvStockBadge;
        ImageView ivProducto;
        View btnAgregar, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView, boolean isCrudMode) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            ivProducto = itemView.findViewById(R.id.ivProducto);
            if (isCrudMode) {
                btnEditar = itemView.findViewById(R.id.btnEditar);
                btnEliminar = itemView.findViewById(R.id.btnEliminar);
                tvStockBadge = itemView.findViewById(R.id.tvStockBadge);
            } else {
                btnAgregar = itemView.findViewById(R.id.btnAgregarCarrito);
            }
        }
    }
}
