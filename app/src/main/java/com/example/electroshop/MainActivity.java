package com.example.electroshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvProductos;
    View btnNuevo, btnCarrito;
    EditText etBuscar;
    ChipGroup cgCategorias;
    TextView tvCount, tvRolHeader;
    DBHelper dbHelper;
    ProductoAdapter adapter;
    List<Producto> listaProductos;

    String filtroNombre = "";
    String filtroCategoria = "Todas";
    String role = "cliente";
    String adminTab = "Productos"; // "Productos", "Clientes", "Historial de Ventas", "Stock"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        role = getIntent().getStringExtra("role");
        if (role == null) role = "cliente";

        rvProductos = findViewById(R.id.rvProductos);
        btnNuevo = findViewById(R.id.btnNuevo);
        btnCarrito = findViewById(R.id.btnCarrito);
        etBuscar = findViewById(R.id.etBuscar);
        cgCategorias = findViewById(R.id.cgCategorias);
        tvCount = findViewById(R.id.tvCount);
        tvRolHeader = findViewById(R.id.tvRolHeader);
        View containerCarrito = findViewById(R.id.containerCarrito);

        // Mostrar bienvenida y nombre del rol en cabecera
        if (tvRolHeader != null) {
            String rolNombre = "Cliente";
            if ("admin".equals(role)) rolNombre = "Administrador";
            else if ("empleado".equals(role)) rolNombre = "Empleado";
            tvRolHeader.setText("Bienvenido, " + rolNombre);
        }

        // Control de visibilidad según rol
        if ("admin".equals(role)) {
            btnNuevo.setVisibility(View.VISIBLE);
            if (containerCarrito != null) containerCarrito.setVisibility(View.GONE);
        } else if ("empleado".equals(role)) {
            btnNuevo.setVisibility(View.VISIBLE);
            if (containerCarrito != null) containerCarrito.setVisibility(View.GONE);
        } else {
            // Cliente
            btnNuevo.setVisibility(View.GONE);
            if (containerCarrito != null) containerCarrito.setVisibility(View.VISIBLE);
        }

        dbHelper = new DBHelper(this);
        dbHelper.cargarDatosIniciales();
        actualizarBadgeCarrito();

        if ("admin".equals(role) || "empleado".equals(role)) {
            rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
        }

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroNombre = s.toString();
                cargarProductos();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnNuevo.setOnClickListener(v -> {
            if ("admin".equals(role) || "empleado".equals(role)) {
                mostrarOpcionesNuevo();
            } else {
                mostrarModalRegistrarProducto();
            }
        });
        
        btnCarrito.setOnClickListener(v -> startActivity(new Intent(this, CarritoActivity.class)));
        
        findViewById(R.id.btnSalirMain).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void mostrarOpcionesNuevo() {
        CharSequence[] opciones = {"Registrar Producto", "Registrar Cliente"};
        new AlertDialog.Builder(this)
                .setTitle("¿Qué deseas registrar?")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarModalRegistrarProducto();
                    } else {
                        mostrarModalRegistrarCliente();
                    }
                })
                .show();
    }

    private void mostrarModalRegistrarProducto() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registrar_producto, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etModalNombre);
        TextInputEditText etCategoria = dialogView.findViewById(R.id.etModalCategoria);
        TextInputEditText etPrecio = dialogView.findViewById(R.id.etModalPrecio);
        TextInputEditText etStock = dialogView.findViewById(R.id.etModalStock);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etModalDescripcion);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnModalGuardar);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnModalCancelar);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String cat = etCategoria.getText() != null ? etCategoria.getText().toString().trim() : "";
            String precioStr = etPrecio.getText() != null ? etPrecio.getText().toString().trim() : "";
            String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "";
            String desc = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

            if (nombre.isEmpty() || cat.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = dbHelper.insertarProducto(nombre, cat, Double.parseDouble(precioStr), Integer.parseInt(stockStr), desc);
            if (ok) {
                Toast.makeText(this, "Producto registrado correctamente", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                cargarProductos();
            } else {
                Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarModalRegistrarCliente() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registrar_cliente, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etModalCliNombre);
        TextInputEditText etDni = dialogView.findViewById(R.id.etModalCliDni);
        TextInputEditText etTel = dialogView.findViewById(R.id.etModalCliTel);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnModalCliGuardar);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnModalCliCancelar);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String dni = etDni.getText() != null ? etDni.getText().toString().trim() : "";
            String tel = etTel.getText() != null ? etTel.getText().toString().trim() : "";

            if (nombre.isEmpty() || dni.isEmpty()) {
                Toast.makeText(this, "Complete Nombre y DNI", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = dbHelper.obtenerOInsertarCliente(nombre, dni);
            if (id != -1) {
                Toast.makeText(this, "Cliente registrado correctamente", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if ("Clientes".equals(adminTab)) {
                    cargarDatosAdmin();
                }
            } else {
                Toast.makeText(this, "Error al registrar cliente", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarFiltros() {
        cgCategorias.removeAllViews();
        if ("admin".equals(role) || "empleado".equals(role)) {
            String[] tabs = {"Productos", "Clientes", "Historial de Ventas", "Stock"};
            for (String tab : tabs) {
                Chip chip = new Chip(this);
                chip.setText(tab);
                chip.setCheckable(true);
                chip.setChecked(adminTab.equals(tab));
                chip.setOnClickListener(v -> {
                    adminTab = tab;
                    cargarDatosAdmin();
                });
                cgCategorias.addView(chip);
            }
        } else {
            Cursor c = dbHelper.obtenerCategorias();
            Chip chipTodo = new Chip(this);
            chipTodo.setText("Todas");
            chipTodo.setCheckable(true);
            chipTodo.setChecked(filtroCategoria.equals("Todas"));
            chipTodo.setOnClickListener(v -> {
                filtroCategoria = "Todas";
                cargarProductos();
            });
            cgCategorias.addView(chipTodo);

            while (c.moveToNext()) {
                String cat = c.getString(0);
                Chip chip = new Chip(this);
                chip.setText(cat);
                chip.setCheckable(true);
                chip.setChecked(filtroCategoria.equals(cat));
                chip.setOnClickListener(v -> {
                    filtroCategoria = cat;
                    cargarProductos();
                });
                cgCategorias.addView(chip);
            }
            c.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("admin".equals(role) || "empleado".equals(role)) {
            cargarDatosAdmin();
        } else {
            cargarProductos();
        }
        configurarFiltros();
        actualizarBadgeCarrito();
    }

    private void cargarDatosAdmin() {
        if ("Productos".equals(adminTab)) {
            tvCount.setText("Gestión de Productos");
            rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
            cargarProductos();
        } else if ("Clientes".equals(adminTab)) {
            tvCount.setText("Gestión de Clientes");
            rvProductos.setLayoutManager(new LinearLayoutManager(this));
            Cursor c = dbHelper.obtenerClientes();
            AdminAdapter adminAdapter = new AdminAdapter(c, "clientes", null, dbHelper);
            rvProductos.setAdapter(adminAdapter);
        } else if ("Historial de Ventas".equals(adminTab)) {
            tvCount.setText("Historial de Ventas");
            rvProductos.setLayoutManager(new LinearLayoutManager(this));
            Cursor c = dbHelper.obtenerHistorialVentas();
            AdminAdapter adminAdapter = new AdminAdapter(c, "ventas", (id, cursor) -> {
                Intent intent = new Intent(this, DetalleTransaccionActivity.class);
                intent.putExtra("ventaId", id);
                startActivity(intent);
            }, dbHelper);
            rvProductos.setAdapter(adminAdapter);
        } else if ("Stock".equals(adminTab)) {
            tvCount.setText("Control de Stock");
            rvProductos.setLayoutManager(new GridLayoutManager(this, 2));
            boolean isCrud = true;
            boolean isStock = true;
            adapter = new ProductoAdapter(this, listaProductos, dbHelper, isCrud, isStock);
            rvProductos.setAdapter(adapter);
        }
    }

    public void actualizarBadgeCarrito() {
        if (dbHelper == null) return;
        Cursor c = dbHelper.mostrarCarrito();
        int count = 0;
        while (c.moveToNext()) {
            count += c.getInt(c.getColumnIndexOrThrow("cantidad"));
        }
        c.close();

        TextView tvBadge = findViewById(R.id.tvBadgeCarrito);
        if (tvBadge != null) {
            tvBadge.setText(String.valueOf(count));
            tvBadge.setVisibility(View.VISIBLE);
        }
    }

    private void cargarProductos() {
        listaProductos = new ArrayList<>();
        Cursor cursor = dbHelper.buscarProductos(filtroNombre, filtroCategoria);

        while (cursor.moveToNext()) {
            listaProductos.add(new Producto(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("stock")),
                    cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
            ));
        }
        cursor.close();

        tvCount.setText(listaProductos.size() + " resultados");

        boolean isCrud = "admin".equals(role) || "empleado".equals(role);
        adapter = new ProductoAdapter(this, listaProductos, dbHelper, isCrud);
        rvProductos.setAdapter(adapter);
    }
}
