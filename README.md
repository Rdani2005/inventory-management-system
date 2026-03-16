# Inventory Administrator - GUI

Proyecto Java con una interfaz gráfica en Swing para gestionar inventario de productos.

Se mantiene la idea de una arquitectura inspirada en DDD y un **Composition Root**, pero ahora el acceso del usuario se hace desde una ventana gráfica en lugar de consola.

## Estructuras de datos propias
- `Node<T>`
- `LinkedList<T>`
- `Stack<T>`
- `Queue<T>`

## Módulos principales
- `products`: entidad producto, repositorio y servicio de dominio
- `movements`: movimientos de inventario, historial y cola de despachos
- `datastructures`: implementaciones genéricas propias
- `services`: `CompositionRoot` para ensamblar dependencias
- `ui`: JFrame principal y paneles gráficos

## Qué permite hacer
- Registrar productos
- Listar y eliminar productos
- Registrar movimientos de ingreso, despacho, devolución, cancelación y corrección
- Deshacer el último movimiento de un producto usando una pila propia
- Ver y procesar la cola de despachos usando una cola propia
- Persistir todo en memoria con listas enlazadas propias

## Ejecutar con Maven
```bash
mvn compile
mvn exec:java -Dexec.mainClass="edu.inventory.administrator.application.MainApplication"
```

## Ejecutar con javac
```bash
javac -d out $(find src -name "*.java")
java -cp out edu.inventory.administrator.application.MainApplication
```
