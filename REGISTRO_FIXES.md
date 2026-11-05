# Registro de correcciones de BitFix

Este documento explica los cambios preparados en la rama local
`fix/correcciones-bitfix`. Los mensajes de commit se mantienen en una sola línea.
Los commits se crean después de su revisión y aprobación; este registro no implica
que los cambios se hayan subido a GitHub.

Los resultados de pruebas corresponden a las ejecuciones indicadas para cada
cambio. No representan una ejecución de toda la suite, de todos los controles de
calidad ni una validación manual completa de la aplicación.

## 1. Autenticación en recepción, cierre y comprobantes

**Commit:** `4e187cf`  
**Mensaje:** `fix: proteger rutas de recepción, cierre y comprobantes`

### Problema

Algunas rutas de recepción y cierre quedaban fuera del interceptor de seguridad.
Sus controladores podían recibir solicitudes sin que el usuario hubiera iniciado
sesión. Además, una URL pública permitía crear una orden de demostración.

### Cambios

- Agregar `/recepcion`, `/recepcion-home`, `/cerrar-orden` y `/comprobante/**`
  a las rutas protegidas por el interceptor existente.
- Eliminar el endpoint `/crear-orden-prueba` del controlador de cierre.
- Reemplazar la prueba de creación por URL por pruebas HTTP de protección de rutas.
- Conservar el acceso público al formulario de consulta de estado.

### Validación

Se ejecutaron 12 pruebas de `ControladorCierreOrdenTest` y `RutasProtegidasTest`,
sin fallos ni errores. Se verificó que las solicitudes sin sesión se redirijan al
login y no invoquen los servicios, que el cierre con sesión conserve su recorrido
y que la ruta de creación de prueba responda 404.

### Alcance

Este commit exige autenticación. La validación de roles y acceso a cada orden
se agrega en el siguiente commit.

## 2. Permisos por rol y por orden

**Commit:** `34e0b42`  
**Mensaje:** `fix: validar roles y acceso a órdenes asignadas`

### Problema

Tener una sesión era suficiente para acceder a varias operaciones del taller.
Faltaba comprobar si el usuario tenía el rol adecuado y si podía operar sobre
la orden indicada en la solicitud.

### Cambios

- Mantener `InterceptorSeguridad` para verificar la sesión, sin crear una nueva
  cuando no existe.
- Incorporar `InterceptorPermisos` para verificar roles y acceso a órdenes.
- Configurar explícitamente el orden: primero autenticación, después permisos.
  Ambos interceptores comparten la lista de rutas protegidas.
- Reservar administración para `ADMIN` y las operaciones del taller para técnicos
  o administradores.
- Limitar la edición, el cierre y los presupuestos de una orden al técnico
  asignado o al administrador.
- Permitir el comprobante por ID al cliente propietario, al técnico asignado
  o al administrador. Conservar la comprobación de propietario del comprobante
  por código que ya realiza su controlador.
- Reservar la aceptación y el rechazo de presupuestos al rol cliente; el servicio
  conserva la comprobación de propietario.
- Responder 403 ante permisos insuficientes, 400 ante identificadores inválidos
  o repetidos y 404 ante órdenes inexistentes.
- Mantener el cálculo de presupuesto sin orden, pero exigir una orden para enviarlo.
- Mantener públicas las rutas `/consultar-estado` y `/consulta-estado`.

### Validación

Se ejecutaron 102 pruebas de los controladores de administración, cierre, órdenes,
presupuestos y técnicos, junto con `RutasProtegidasTest`, sin fallos ni errores.

Las pruebas HTTP cubren accesos permitidos y denegados, técnicos ajenos a la orden,
clientes propietarios y ajenos, roles desconocidos, identificadores repetidos,
redirección con el contexto `/spring` y búsqueda pública por código mediante GET
y POST sin cuenta.

### Alcance

Los controles se aplican a las rutas HTTP configuradas. Este cambio no incorpora
autorización por anotaciones ni una cadena de filtros de Spring Security.
Las consultas del interceptor y la modificación posterior del servicio no forman
una única operación atómica; el control de concurrencia queda fuera de este commit.

## 3. Transiciones de estado y entrega

**Commit:** `b62a06a`  
**Mensaje:** `fix: validar transiciones de estado y condiciones de entrega`

### Problema

El formulario ofrecía una lista de estados permitidos, pero el servicio de edición
aceptaba directamente el estado recibido. Una petición podía saltarse el flujo
y marcar una orden como entregada sin cumplir los requisitos del cierre.

### Cambios

- Validar los cambios de estado en `actualizarEstadoYNotaTecnica` usando las
  transiciones que ya ofrece el servicio al formulario.
- Rechazar órdenes inexistentes, estados nulos y transiciones no permitidas antes
  de cambiar el estado, la nota o la fecha de entrega.
- Exigir un monto positivo y finito antes de pasar a `ENTREGADO` desde la edición.
  Rechazar también valores no finitos en el servicio de cierre.
- Conservar la fecha de entrega cuando se edita una nota sin cambiar el estado
  de una orden ya entregada.
- Usar `RECIBIDO` al registrar una orden cuyo estado sea nulo.
- Mostrar el error de transición en la vista de edición con respuesta HTTP 409.
  Agregar manejo de errores de estado en el controlador de órdenes.

### Validación

Se ejecutaron 118 pruebas de servicios de órdenes y cierre, del controlador de
órdenes, de rutas protegidas y de `TransicionesOrdenTest`, sin fallos ni errores.

Se probaron saltos desde recepción hasta entrega, montos inválidos, conservación
de la fecha de entrega, estados finales, el flujo de cotización online y la
respuesta HTTP cuando una transición se rechaza.

### Alcance

Se valida el flujo de edición de estado. La reserva, devolución y concurrencia
del stock de presupuestos requieren un cambio separado.

## 4. Contraseñas y cuentas inactivas

**Commit:** `6c70d1c`

**Mensaje:** `fix: proteger contraseñas y bloquear login de usuarios inactivos`

### Problema

El login buscaba directamente por email y contraseña en texto plano, y no rechazaba
cuentas inactivas. El registro detectaba duplicados comparando también la contraseña,
por lo que un email existente con otra clave llegaba hasta la restricción de la base.

### Cambios

- Agregar `spring-security-crypto` 6.5.11 y usar su implementación de PBKDF2
  con sal aleatoria para registros y cambios de contraseña.
- Guardar el prefijo `{pbkdf2}` para distinguir el formato del hash.
- Buscar el usuario por email y verificar la contraseña en el servicio.
- Rechazar el login si el usuario no existe, está inactivo, tiene credenciales
  incompletas o un hash inválido.
- Migrar una contraseña antigua en texto plano a hash únicamente después de
  verificarla correctamente. Los intentos incorrectos no modifican credenciales.
- Detectar emails existentes sin comparar la contraseña.
- Reemplazar las claves de los datos iniciales por hashes, conservando las claves
  de demostración anteriores.
- Documentar el funcionamiento y los límites de la migración en el README.

### Validación

Se ejecutaron 73 pruebas de seguridad de login, servicio de login, persistencia,
controladores de login y administración y rutas protegidas, sin fallos ni errores.

Las pruebas verifican hashes distintos para la misma clave, autenticación correcta
e incorrecta, migración de claves antiguas, cuentas inactivas, emails duplicados
y hashes corruptos. Con HSQLDB en memoria se comprobó que el hash y la migración
persistan después de vaciar la sesión de Hibernate.

### Alcance y limitaciones

- Las cuentas antiguas que no inicien sesión conservan su contraseña anterior
  hasta cambiarla. No se hizo una migración masiva de una base existente.
- Se bloquean nuevos logins de cuentas inactivas; no se revocan sesiones abiertas.
- No se modificó el comportamiento de los datos iniciales que puede reactivar
  técnicos precargados al arrancar. Esa corrección queda pendiente.
- No se inició la aplicación contra una base de datos de uso real.

Referencia del algoritmo y la API utilizada:
[almacenamiento de contraseñas de Spring Security](https://docs.spring.io/spring-security/reference/6.5/features/authentication/password-storage.html).

## 5. Reserva y devolución de stock de presupuestos

**Estado:** aprobado e incluido en el commit identificado por el siguiente mensaje.

**Mensaje:** `fix: reservar y devolver stock de presupuestos sin descuentos duplicados`

### Problema

Enviar un presupuesto descontaba stock, pero rechazarlo no lo devolvía. La orden
no guardaba qué repuestos se habían descontado. Además, después de aceptar o
rechazar se podía volver a enviar un presupuesto y descontar otra vez. Dos
solicitudes simultáneas tampoco tenían protección contra actualizaciones perdidas.

### Cambios

- Mantener el descuento al enviar como una reserva: aceptar conserva el descuento
  y rechazar devuelve las cantidades reservadas una sola vez.
- Guardar los repuestos y cantidades de cada reserva mediante `ReservaRepuesto`
  y una colección persistida en la orden. Conservar ese detalle tras el rechazo.
- Permitir el envío desde `RECIBIDO`, `EN_DIAGNOSTICO` o `ESPERANDO_REPUESTO`,
  siempre que la orden no tenga una reserva previa. Rechazar los reenvíos después
  de una aceptación o un rechazo.
- Agrupar renglones del mismo repuesto y validar todas las cantidades acumuladas
  antes de descontar el primer repuesto. Aplicar la misma agrupación al cálculo.
- Exigir un monto positivo y finito al enviar y cantidades positivas en las
  operaciones directas de descuento para evitar incrementos por valores negativos.
- Agregar control optimista con `@Version` en órdenes y repuestos. Si dos
  operaciones usan una versión anterior, la que entra en conflicto se revierte.
- Incluir la versión del repuesto en el formulario de inventario para detectar
  ediciones desactualizadas y permitir guardar una edición con la versión vigente.
- Mostrar el error de envío repetido en el formulario de presupuesto y responder
  HTTP 409 ante conflictos de concurrencia o eliminación de repuestos referenciados.

### Validación

Se ejecutaron 123 pruebas de servicios de presupuesto, órdenes e inventario,
repuestos, controladores afectados, rutas protegidas y los nuevos casos de ciclo
de stock, persistencia y conflictos HTTP, sin fallos ni errores.

Con HSQLDB en memoria y transacciones independientes se verificó:

- Persistencia de la reserva y devolución desde otra transacción.
- Reversión conjunta de stock, estado y reservas ante un error.
- Dos envíos simultáneos de una misma orden: solo uno confirma la reserva.
- Dos órdenes que compiten por stock insuficiente para ambas: solo una confirma.
- Una edición vieja del inventario no sobrescribe una reserva, y una edición
  cargada con la versión vigente puede guardarse.

### Alcance y limitaciones

- Se agregan columnas `version` a las tablas de órdenes y repuestos, y la tabla
  `reserva_presupuesto`. La configuración existente usa actualización automática
  del esquema de Hibernate. No se validó esa actualización contra una base MySQL
  existente ni se modificó una base de uso real.
- Los presupuestos enviados antes de este cambio no tienen detalle de reserva.
  Al rechazarlos no se inventan cantidades para devolver; requieren revisión manual.
- El detalle conserva repuestos y cantidades, pero todavía no guarda una copia
  histórica de sus nombres y precios al presupuestar.
- Los repuestos referenciados por reservas no pueden eliminarse físicamente:
  se conserva la relación para permitir la devolución y mantener el detalle.
- Los conflictos requieren recargar y revisar los datos; no se reintentan
  automáticamente operaciones que podrían cambiar stock.
- Las pruebas de concurrencia corresponden a HSQLDB; queda pendiente validar el
  despliegue y el comportamiento sobre MySQL.
