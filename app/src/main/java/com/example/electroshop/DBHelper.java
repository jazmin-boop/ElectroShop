package com.example.electroshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ElectroShop.db";
    private static final int DB_VERSION = 3;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE productos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "categoria TEXT NOT NULL," +
                "precio REAL NOT NULL," +
                "stock INTEGER NOT NULL," +
                "descripcion TEXT)");

        db.execSQL("CREATE TABLE clientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "dni TEXT UNIQUE NOT NULL," +
                "telefono TEXT)");

        db.execSQL("CREATE TABLE carrito (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto_id INTEGER NOT NULL UNIQUE," +
                "cantidad INTEGER NOT NULL," +
                "FOREIGN KEY(producto_id) REFERENCES productos(id))");

        db.execSQL("CREATE TABLE ventas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cliente_id INTEGER," +
                "total REAL NOT NULL," +
                "fecha TEXT NOT NULL," +
                "estado TEXT NOT NULL," +
                "operacion TEXT NOT NULL," +
                "FOREIGN KEY(cliente_id) REFERENCES clientes(id))");

        db.execSQL("CREATE TABLE detalle_venta (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "venta_id INTEGER NOT NULL," +
                "producto_id INTEGER NOT NULL," +
                "cantidad INTEGER NOT NULL," +
                "precio REAL NOT NULL," +
                "FOREIGN KEY(venta_id) REFERENCES ventas(id)," +
                "FOREIGN KEY(producto_id) REFERENCES productos(id))");

        db.execSQL("CREATE TABLE pagos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "venta_id INTEGER NOT NULL," +
                "banco TEXT NOT NULL," +
                "titular TEXT NOT NULL," +
                "ultimos4 TEXT NOT NULL," +
                "monto REAL NOT NULL," +
                "estado TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "FOREIGN KEY(venta_id) REFERENCES ventas(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS pagos");
        db.execSQL("DROP TABLE IF EXISTS detalle_venta");
        db.execSQL("DROP TABLE IF EXISTS ventas");
        db.execSQL("DROP TABLE IF EXISTS carrito");
        db.execSQL("DROP TABLE IF EXISTS clientes");
        db.execSQL("DROP TABLE IF EXISTS productos");
        onCreate(db);
    }

    // --- MÉTODOS DE BÚSQUEDA Y FILTRO ---
    public Cursor buscarProductos(String nombre, String categoria) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM productos WHERE 1=1";
        if (nombre != null && !nombre.isEmpty()) {
            query += " AND nombre LIKE '%" + nombre + "%'";
        }
        if (categoria != null && !categoria.equals("Todas")) {
            query += " AND categoria = '" + categoria + "'";
        }
        query += " ORDER BY nombre ASC";
        return db.rawQuery(query, null);
    }

    public Cursor obtenerCategorias() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT categoria FROM productos ORDER BY categoria ASC", null);
    }

    // --- GESTIÓN DE CLIENTES ---
    public long obtenerOInsertarCliente(String nombre, String dni) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM clientes WHERE dni = ?", new String[]{dni});
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            c.close();
            return id;
        }
        c.close();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("dni", dni);
        return db.insert("clientes", null, values);
    }

    public Cursor obtenerHistorialVentas() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT v.*, c.nombre as cliente_nombre FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id ORDER BY v.fecha DESC", null);
    }

    public Cursor obtenerDetalleVenta(long ventaId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT d.*, p.nombre FROM detalle_venta d " +
                "JOIN productos p ON d.producto_id = p.id WHERE d.venta_id = ?", new String[]{String.valueOf(ventaId)});
    }

    public boolean actualizarCliente(long id, String nombre, String dni, String telefono) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("dni", dni);
        values.put("telefono", telefono);
        return db.update("clientes", values, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean eliminarCliente(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("clientes", "id=?", new String[]{String.valueOf(id)}) > 0;
    }


    // CARGAR PRODUCTOS DE PRUEBA ELECTROSHOP
    public void cargarDatosIniciales() {
        Cursor c = mostrarProductos();

        if (c != null && c.getCount() > 0) {
            c.close();
            return;
        }

        if (c != null) {
            c.close();
        }

        insertarProducto(
                "Lavadora Automática 15kg",
                "Lavadoras",
                1699.00,
                10,
                "Lavadora automática con tecnología TurboWash y ahorro de energía"
        );

        insertarProducto(
                "Licuadora Oster Pro",
                "Licuadoras",
                249.90,
                15,
                "Licuadora de alta potencia con vaso de vidrio resistente"
        );

        insertarProducto(
                "Microondas Digital 25L",
                "Microondas",
                450.00,
                8,
                "Microondas digital con grill y plato giratorio"
        );

        insertarProducto(
                "Refrigeradora No Frost 300L",
                "Refrigeradoras",
                1799.00,
                6,
                "Refrigeradora inverter con gaveta de vegetales y dispensador"
        );

        insertarProducto(
                "Smart TV 50 pulgadas 4K",
                "Televisores",
                1299.00,
                7,
                "Smart TV LED 4K UHD con sonido Dolby Audio"
        );
    }

    // CREATE
    public boolean insertarProducto(String nombre, String categoria,
                                    double precio, int stock,
                                    String descripcion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("categoria", categoria);
        values.put("precio", precio);
        values.put("stock", stock);
        values.put("descripcion", descripcion);

        long resultado = db.insert("productos", null, values);
        return resultado != -1;
    }

    // READ
    public Cursor mostrarProductos() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM productos ORDER BY id DESC", null);
    }

    // READ por ID
    public Cursor buscarProducto(int id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM productos WHERE id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // UPDATE
    public boolean actualizarProducto(int id, String nombre, String categoria,
                                      double precio, int stock,
                                      String descripcion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("categoria", categoria);
        values.put("precio", precio);
        values.put("stock", stock);
        values.put("descripcion", descripcion);

        int resultado = db.update(
                "productos",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    // DELETE
    public boolean eliminarProducto(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int resultado = db.delete(
                "productos",
                "id=?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    // CARRO: agregar o incrementar
    public boolean agregarAlCarrito(int productoId, int cantidad) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT cantidad FROM carrito WHERE producto_id=?",
                new String[]{String.valueOf(productoId)}
        );

        if (cursor.moveToFirst()) {
            int actual = cursor.getInt(0);
            cursor.close();

            ContentValues values = new ContentValues();
            values.put("cantidad", actual + cantidad);

            return db.update(
                    "carrito",
                    values,
                    "producto_id=?",
                    new String[]{String.valueOf(productoId)}
            ) > 0;
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("producto_id", productoId);
        values.put("cantidad", cantidad);

        return db.insert("carrito", null, values) != -1;
    }

    public Cursor mostrarCarrito() {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT c.id, p.id, p.nombre, p.precio, " +
                "c.cantidad, (p.precio * c.cantidad) AS subtotal " +
                "FROM carrito c INNER JOIN productos p " +
                "ON c.producto_id = p.id " +
                "ORDER BY c.id DESC";

        return db.rawQuery(sql, null);
    }

    public boolean eliminarDelCarrito(int carritoId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("carrito", "id=?", new String[]{String.valueOf(carritoId)}) > 0;
    }

    public int obtenerStock(int productoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT stock FROM productos WHERE id = ?", new String[]{String.valueOf(productoId)});
        int stock = 0;
        if (c.moveToFirst()) stock = c.getInt(0);
        c.close();
        return stock;
    }

    public boolean actualizarCantidadCarrito(int productoId, int nuevaCantidad) {
        int stock = obtenerStock(productoId);
        if (nuevaCantidad > stock) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cantidad", nuevaCantidad);
        return db.update("carrito", values, "producto_id=?", new String[]{String.valueOf(productoId)}) > 0;
    }

    public Cursor obtenerClientes() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM clientes ORDER BY nombre ASC", null);
    }

    public Cursor obtenerStockProductos() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM productos ORDER BY stock ASC", null);
    }

    public void vaciarCarrito() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("carrito", null, null);
    }

    public long registrarVentaConPago(long clienteId,
                                      String banco,
                                      String titular,
                                      String ultimos4,
                                      double total) {

        SQLiteDatabase db = getWritableDatabase();
        long ventaId = -1;

        db.beginTransaction();

        try {
            String fecha = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault()
            ).format(new java.util.Date());

            String operacion = "OP-" + System.currentTimeMillis();

            ContentValues venta = new ContentValues();
            venta.put("cliente_id", clienteId);
            venta.put("total", total);
            venta.put("fecha", fecha);
            venta.put("estado", "PAGADA");
            venta.put("operacion", operacion);

            ventaId = db.insertOrThrow("ventas", null, venta);

            Cursor carrito = db.rawQuery(
                    "SELECT producto_id, cantidad FROM carrito",
                    null
            );

            while (carrito.moveToNext()) {

                int productoId = carrito.getInt(0);
                int cantidad = carrito.getInt(1);

                Cursor producto = db.rawQuery(
                        "SELECT precio, stock FROM productos WHERE id=?",
                        new String[]{String.valueOf(productoId)}
                );

                if (!producto.moveToFirst()) {
                    producto.close();
                    throw new Exception("Producto inexistente");
                }

                double precio = producto.getDouble(0);
                int stock = producto.getInt(1);
                producto.close();

                if (stock < cantidad) {
                    throw new Exception("Stock insuficiente");
                }

                ContentValues detalle = new ContentValues();
                detalle.put("venta_id", ventaId);
                detalle.put("producto_id", productoId);
                detalle.put("cantidad", cantidad);
                detalle.put("precio", precio);

                db.insertOrThrow("detalle_venta", null, detalle);

                ContentValues nuevoStock = new ContentValues();
                nuevoStock.put("stock", stock - cantidad);

                db.update(
                        "productos",
                        nuevoStock,
                        "id=?",
                        new String[]{String.valueOf(productoId)}
                );
            }

            carrito.close();

            ContentValues pago = new ContentValues();
            pago.put("venta_id", ventaId);
            pago.put("banco", banco);
            pago.put("titular", titular);
            pago.put("ultimos4", ultimos4);
            pago.put("monto", total);
            pago.put("estado", "APROBADA");
            pago.put("fecha", fecha);

            db.insertOrThrow("pagos", null, pago);

            db.delete("carrito", null, null);

            db.setTransactionSuccessful();

        } catch (Exception e) {
            ventaId = -1;
        } finally {
            db.endTransaction();
        }

        return ventaId;
    }
}