# Registro de correcciones de BitFix

Este documento explica los cambios preparados en la rama local
`fix/correcciones-bitfix`. Los mensajes de commit se mantienen en una sola línea.
Los commits se crean después de su revisión y aprobación; este registro no implica
que los cambios se hayan subido a GitHub.

Cada sección describe el alcance al momento de su commit. Las correcciones
posteriores pueden resolver limitaciones documentadas en secciones anteriores.

Los resultados de pruebas corresponden a las ejecuciones indicadas para cada
cambio. No representan una ejecución de toda la suite, de todos los controles de
calidad ni una validación manual completa de la aplicación.

## 1. Autenticación en recepción, cierre y comprobantes

**Commit:** `77238e9`

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

**Commit:** `1016c35`

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

**Commit:** `bc45346`

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

**Commit:** `28e2454`

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

## 6. Códigos de seguimiento únicos

**Mensaje:** `fix: garantizar unicidad de los códigos de seguimiento`

Se agrega una restricción única en la base y una consulta de existencia previa al
registro. Los códigos mantienen seis cifras y se generan con `SecureRandom`.
El servicio reintenta las colisiones con un límite de intentos; si no encuentra
un código libre, no guarda la orden. Se aplica al registro tradicional y online.
Una colisión simultánea se resuelve con la restricción de la base: no se guardan
dos órdenes con el mismo código, aunque una solicitud deba reintentarse.

Validación: 61 pruebas de registro, repositorios y códigos, incluida una prueba de
la restricción real en HSQLDB, sin errores. Antes de actualizar una base existente
hay que revisar códigos duplicados; no se renumeraron órdenes de una base real.

## 7. Reasignación y carga activa de técnicos

**Mensaje:** `fix: validar reasignaciones y excluir órdenes finalizadas de la carga`

Se comprueba en el servicio que la orden siga activa y que el destinatario sea un
técnico activo con el mismo nivel requerido. Los errores no modifican la asignación
ni su fecha. Se centralizan los estados finales: entregado, presupuesto rechazado
y cotización descartada. El conteo del repositorio y el panel de contingencia usan
esa definición, evitando que trabajos descartados bloqueen bajas o distorsionen
la asignación automática.

Validación: 73 pruebas de servicios, administración, reasignación y carga en HSQLDB,
sin errores. El fix de emails duplicados de la lista original ya está cubierto
por el commit de contraseñas; no se duplica esa implementación.

## 8. Historial del presupuesto e importes decimales

**Mensaje:** `fix: conservar el detalle histórico y calcular importes con decimales`

Los precios, totales y componentes del presupuesto se almacenan con `BigDecimal`
y columnas decimales de dos posiciones, con redondeo HALF_UP. Los accesores públicos
que ya usaban las vistas y formularios mantienen compatibilidad con `Double`.
Las sumas y multiplicaciones se hacen en decimal, evitando diferencias como las
producidas por sumar 0,10 y 0,20 en binario.

La reserva incorpora una copia del nombre y precio unitario original. Se guardan
el subtotal y la mano de obra aplicados; los presupuestos con repuestos recalculan
el total a partir de ese detalle en el servicio. Se conserva el importe explícito
para presupuestos sin repuestos. Las modificaciones del catálogo no alteran el
historial ni el desglose del comprobante.

Se habilita la vista de detalle existente desde el panel del cliente, protegida
por propietario, técnico asignado o administrador. El comprobante muestra los
renglones históricos. Los presupuestos antiguos sin desglose mantienen un cálculo
estimado compatible y no reciben precios históricos inventados.

Validación: 92 pruebas sin errores, incluidas conservación del historial al cambiar
el catálogo, centavos exactos, acceso al detalle y renderizado real de Thymeleaf.
También se aisló la configuración de concurrencia mediante un perfil de pruebas
para que no interfiera en los contextos de integración.

Se conservan los porcentajes y tramos comerciales originales: la mano de obra
puede bajar al superar 50.000 o 150.000 de repuestos. Es una inconsistencia comercial
documentada, pendiente de definición; no se inventó una tarifa nueva. El cambio
de tipo de columnas debe comprobarse antes de aplicarlo sobre una base MySQL existente.

## 9. Errores controlados y recursos inexistentes

**Mensaje:** `fix: mostrar errores controlados y responder con estados HTTP adecuados`

Se agrega la plantilla `error.html` que faltaba y un manejador de errores para
formularios inválidos, órdenes inexistentes y conflictos de datos. El cierre
responde 404 cuando la orden no existe y 409 cuando su estado impide entregarla.
La edición de una orden o un repuesto inexistente ya no intenta renderizar datos
nulos. El registro rechaza contraseñas vacías sin lanzar un error inesperado.

Los mensajes genéricos de conflictos de base no muestran consultas SQL ni detalles
internos. Se conservan los mensajes específicos de negocio y los manejadores de
concurrencia. Validación: 91 pruebas sin errores, incluido renderizado real de la
vista de error y respuesta 400 al ingresar un código con formato incorrecto.

## 10. Inicialización sin reactivar usuarios existentes

**Mensaje:** `fix: preservar usuarios existentes al cargar los datos iniciales`

El SQL agrega las cuentas de demostración solamente cuando su email no existe.
Al repetirlo no cambia roles, niveles, contraseñas ni estados de actividad. La
inicialización espera a que exista la SessionFactory y deja de ocultar errores de
SQL mediante `continueOnError`.

Validación: prueba del script real en HSQLDB con compatibilidad MySQL. Se cargó
dos veces, verificando tres cuentas sin duplicados y que una baja, un nivel y una
contraseña modificados se conserven. La prueba verifica además el hash inicial.
No se ejecutó el script contra una instancia MySQL de uso real.

## 11. Calidad y validación final

**Mensaje:** `fix: simplificar validaciones y normalizar finales de línea`

Se resuelven las observaciones de PMD sin desactivar sus reglas: las transiciones
quedan junto a `EstadoOrden`, la política de asignación se concentra en
`AsignadorTecnicos` y se separan validaciones e identificación de parámetros en
métodos pequeños. El servicio sigue delimitando las transacciones. Se evita llamar
a un método redefinible desde el constructor de un repuesto y se separan las
capturas de excepciones del cierre.

`.gitattributes` fija LF para JavaScript y módulos MJS, evitando que un checkout de
Windows haga fallar ESLint por CRLF. La normalización no cambia la lógica de esos
archivos.

### Resultados finales

- Java: **322 pruebas**, cero fallos, cero errores y cero omitidas en la ejecución
  configurada de Surefire.
- Cobertura JaCoCo: **93,57% de líneas** (1.265 cubiertas y 87 sin cubrir), con las
  exclusiones existentes de configuración e inicializador. Se supera el mínimo
  configurado del 80%.
- Checkstyle, PMD y CPD: fase `validate` aprobada.
- JavaScript del frontend: **8 pruebas Jasmine** aprobadas y ESLint aprobado.
- Diferencias: `git diff --check` sin errores.

Comandos usados desde la raíz con JDK 25:

```shell
mvn -B validate
mvn -B jacoco:prepare-agent@default resources:resources compiler:compile resources:testResources compiler:testCompile surefire:test jacoco:report@report jacoco:check@check-coverage
```

Desde `src/main/webapp/resources/core/js`:

```shell
npm ci --ignore-scripts
npm run lint
npm test
```

La ejecución por objetivos evita activar el formateador con escritura automática
de Maven durante la revisión. Los reportes locales quedan en
`.calidad-de-codigo/` y `target/surefire-reports/`; no se incluyen en Git.

### Correspondencia con la lista original

| Fix original | Corrección documentada |
| --- | --- |
| 1. Rutas protegidas | Sección 1 |
| 2. Roles y pertenencia | Sección 2 |
| 3. Estados y entrega | Sección 3 |
| 4. Contraseñas y cuentas inactivas | Sección 4 |
| 5. Ciclo del stock | Sección 5 |
| 6. Seguimiento único | Sección 6 |
| 7. Emails duplicados | Sección 4 |
| 8. Reasignación | Sección 7 |
| 9. Carga activa | Sección 7 |
| 10. Detalle histórico | Sección 8 |
| 11. Importes | Sección 8; tarifas comerciales conservadas |
| 12. Errores y recursos inexistentes | Sección 9 |
| 13. Datos iniciales | Sección 10 |
| 14. Pruebas y controles | Sección 11 y validaciones por cambio |

### Límites de la revisión local

- No se ejecutaron las pruebas Playwright de punta a punta que requieren un
  servidor levantado; Surefire incluye las clases terminadas en `Test`.
- Las verificaciones de persistencia, concurrencia y SQL se realizaron con HSQLDB.
  El motor de Docker no está iniciado, por lo que no se levantó un MySQL desechable.
- Antes de usar una base existente se deben revisar códigos duplicados, cambios
  de esquema y los presupuestos antiguos sin detalle. No se modificaron datos de
  una base de uso real ni se creó una migración masiva de contraseñas.
- Se mantienen los tramos comerciales originales de mano de obra. Sus saltos
  requieren una definición de negocio; quedan descritos en la sección 8.
- Todo se mantiene local en `fix/correcciones-bitfix`, con la identidad Git de
  Jhony. No se hizo push, despliegue ni modificación del historial previo.

## 12. Arranque verificado en Docker

**Mensaje:** `fix: esperar a MySQL y documentar el empaquetado para Docker`

El intento de `docker compose up --build` fallaba porque la imagen copiaba un WAR
que aún no existía. Se generó con JDK 25 y `mvn -B war:war`, usando las clases y
recursos ya compilados y validados. El README aclara el empaquetado previo con
`mvn clean package` para futuras compilaciones desde cero.

Se elimina la propiedad obsoleta `version` de Compose y se agrega un healthcheck
de MySQL. Jetty depende de `service_healthy`, evitando iniciar la aplicación
cuando la base todavía está inicializándose.

Validación local posterior a la sección 11:

- `docker compose config --quiet`: configuración válida.
- `docker compose up --build -d`: ambas imágenes construidas y contenedores iniciados.
- MySQL 8.0.46: saludable. Jetty 12.0.37: aplicación `/spring` disponible.
- Login y formulario de seguimiento público: HTTP 200.
- Login de la cuenta de administración de demostración: acceso al panel con HTTP 200,
  verificando los datos iniciales y el hash contra MySQL.
- Recepción sin sesión: redirección al login.
- Hibernate creó en el volumen nuevo las tablas, los importes decimales, las
  versiones y las restricciones de unicidad y relaciones del presupuesto.

Los contenedores quedan levantados para la revisión. Esta comprobación cubre un
arranque con una base nueva en Docker; no prueba la migración de una base anterior
ni sustituye las pruebas de concurrencia realizadas en HSQLDB.

## 13. Construcción autónoma y agrupación estable de Compose

**Mensaje:** `fix: compilar el proyecto dentro de Docker y unificar el arranque`

Jetty y Tomcat usan una etapa de compilación con Maven 3.9.16 y JDK 25. El
comando `mvn package` ejecuta las validaciones, las pruebas y el empaquetado dentro
de la imagen de construcción. El servidor recibe el WAR mediante `COPY --from`,
sin depender del directorio `target/` de la máquina anfitriona. BuildKit conserva
una caché de dependencias Maven para las siguientes construcciones.

El proyecto de Compose se fija explícitamente como `tw1-bitfix`. Se eliminan los
nombres de contenedor fijos para que Compose administre ambos servicios y se
conserva el volumen `tw1-bitfix_mysql_data`. Se elimina el montaje de uploads
sin uso que apuntaba fuera de la instalación de Jetty.

Se agrega `.dockerignore` para excluir `.env`, metadatos de Git, dependencias Node
y artefactos locales del contexto. `.env.example` permite preparar una primera
instalación sin copiar credenciales de otra máquina. No se reemplazó el `.env`
existente. El README queda actualizado para iniciar con un solo comando:

```shell
docker compose up --build -d
```

La ejecución completa de Maven en Linux detectó contaminación entre bases HSQLDB
de distintos contextos de prueba, dependiente del orden de ejecución. Las dos
configuraciones de pruebas usan ahora un nombre de base único por contexto; se
mantuvieron todos los tests activos. La segunda construcción pasó las 322 pruebas,
la validación de cobertura y el empaquetado.

La alternativa Tomcat tenía una descarga que devolvía 404. Se reemplazó por la
imagen oficial `tomcat:10.1-jdk25-temurin` y se verificó su construcción; el WAR
se despliega también bajo `/spring`. No se inició un tercer servidor Tomcat.

Validación final:

- Configuración de Compose válida y construcción de Jetty desde el código fuente.
- Construcción de la imagen alternativa de Tomcat aprobada.
- Servicios activos: `tw1-bitfix-mysql-1` saludable y `tw1-bitfix-jetty-app-1` iniciado.
- Ambos servicios conservan el proyecto `tw1-bitfix` y MySQL reutiliza el volumen anterior.
- Login, consulta pública y panel de administración: HTTP 200.
- Cambios y commits locales; sin publicación de imágenes ni push a GitHub.

## 14. Integración por rebase sobre Merge

El 5 de noviembre de 2026 se actualizaron las referencias remotas con `fetch` y
la rama local `Merge` mediante un `pull --ff-only`. El commit recibido fue
`48b09b8`, de Yamila, con unificación visual de las vistas administrativas y CSS.

Con autorización del usuario, se reaplicaron los 13 commits de correcciones sobre
`origin/Merge` en `fix/correcciones-bitfix`. Se mantiene el respaldo local
`backup/correcciones-bitfix-antes-rebase-20261105`, que apunta al estado anterior
`e60f008`. Los hashes de los commits cambiaron; sus autores y mensajes se conservaron.
Las referencias de las primeras secciones se actualizaron al historial nuevo.

Hubo un solo conflicto, en `inventario.html`. Se conservó la vista rediseñada y se
agregó el campo oculto `version` al formulario existente. Ese campo mantiene la
protección ante ediciones de inventario desactualizadas. No se descartó ninguna
de las dos funcionalidades. `git range-diff` confirmó 12 parches idénticos y un
ajuste de contexto del campo `version` en el commit de stock.

Validación posterior al rebase:

- Construcción Docker con `mvn package`: 322 pruebas aprobadas, cero fallos y errores.
- Validación de cobertura aprobada.
- HTTP 200 en panel de administración, nómina de técnicos, alta, edición,
  contingencia e inventario, con la barra lateral de administración renderizada.
- MySQL conservado en el volumen existente y Jetty actualizado con las vistas nuevas.
- Sin push ni modificación de la rama remota `Merge`.

## 15. Técnico de nivel 1 y cliente de demostración

**Mensaje:** `feat: agregar técnico de nivel 1 y cliente a los datos iniciales`

Se agregan a `data.sql` las cuentas activas `tecnico.nivel1@bitfix.com`, con rol
`TECNICO` y soporte `NIVEL_1`, y `cliente@bitfix.com`, con rol `CLIENTE` y sin
nivel de soporte. Ambas tienen la clave inicial de demostración `1234`, almacenada
con PBKDF2 y sal independiente. Cada inserción conserva la condición de email
inexistente para no duplicar ni sobrescribir cuentas ya creadas.

La prueba del script inicial se actualiza a cinco usuarios y comprueba los roles,
el nivel, la actividad, los hashes y la conservación de datos al repetir la carga.

Validación: construcción Docker con 322 pruebas y cobertura aprobadas. Las dos
cuentas se cargaron en MySQL y accedieron por HTTP 200 a sus paneles correspondientes.
