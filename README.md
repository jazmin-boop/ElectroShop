# ElectroShop

**ElectroShop** es una aplicacion movil desarrollada en Android (Java) que combina un sistema de comercio electronico (E-commerce) para clientes con un panel de gestion comercial (CRUD, Control de Stock, Historial de Ventas) para administradores y empleados.

---

## caracteristicas principales

### 1. Vista de Cliente (Usuario)
- **Catalogo de Productos**: Exploracion de electrodomesticos filtrados por categorias con imagenes responsivas.
- **Detalle de Producto**: Modal flotante profesional con informacion detallada, precio, stock y opcion directa de compra.
- **Carrito de Compras**: Gestion de cantidades con validacion de stock en tiempo real e indicador numerico (badge) flotante en el icono del carrito.
- **Pasarela de Pago (Checkout)**: Formulario de pago simulado con tarjeta de credito interactiva sincronizada en tiempo real.
- **Comprobante Digital (E-Receipt)**: Generacion de comprobante detallado con codigo de barras y resumen de costos.

### 2. Vista de Administrador y Empleado
- **Panel de Control con Pestanas (Chips)**:
  - **Productos**: Listado y gestion con modales profesionales de registro y edicion.
  - **Clientes**: Directorio de clientes con opcion de edicion y eliminacion.
  - **Historial de Ventas**: Registro de transacciones con acceso al Detalle de Venta detallado por items.
  - **Control de Stock**: Visualizacion de inventario con alerta de semaforo (Rojo, Amarillo, Verde) segun el nivel de existencias.
- **Restricciones por Rol**: El empleado y el administrador no visualizan el carrito de compras ni el banner de ofertas comerciales.

---

## Credenciales de Prueba (Login)

Puedes iniciar sesión con los siguientes usuarios de prueba:

| Rol | Correo Electronico | Contrasena |
| :--- | :--- | :--- |
| **Administrador** | `admin@electroshop.com` | `admin123` |
| **Empleado** | `empleado@electroshop.com` | `empleado123` |
| **Cliente** | `cliente@electroshop.com` | `cliente123` (O registrate con una cuenta nueva) |

---

## Tecnologias y Arquitectura

- **Lenguaje**: Java 100% nativo.
- **Base de Datos**: SQLite (SQLiteOpenHelper) con relaciones normalizadas (productos, clientes, carrito, ventas, detalle_venta, pagos).
- **Interfaz de Usuario (UI/UX)**: Material Design 3, ConstraintLayout, RecyclerView, CardView y dialogos modales personalizados.
- **Patron de Diseno**: Arquitectura orientada a actividades, adaptadores modulares (RecyclerView Adapters) y gestion transaccional segura en base de datos.

---

## Como Ejecutar el Proyecto

1. Clona o descarga este repositorio en tu equipo.
2. Abre **Android Studio** (version recomendada: Koala o superior).
3. Selecciona **Open** y busca la carpeta raiz del proyecto (`ElectroShop`).
4. Espera a que Gradle sincronice las dependencias.
5. Ejecuta la aplicacion en un emulador Android (API 26 o superior) o dispositivo fisico.
