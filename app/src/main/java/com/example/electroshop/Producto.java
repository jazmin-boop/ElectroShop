package com.example.electroshop;

public class Producto {

    private int id;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;
    private String descripcion;

    public Producto(int id, String nombre, String categoria,
                    double precio, int stock, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getDescripcion() { return descripcion; }
}
