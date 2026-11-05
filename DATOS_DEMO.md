# Datos de demostración

La aplicación ejecuta `data.sql` (cuentas), `catalogo.sql` (catálogo integrado de
Merge) y luego `demo.sql` (inventario y órdenes de ejemplo)
al iniciar, después de preparar el esquema con Hibernate. En Docker basta con
`docker compose up --build -d`. Acceso: http://localhost:8080/spring/login.

## Cuentas

| Correo | Rol / nivel | Clave inicial |
| --- | --- | --- |
| admin@bitfix.com | Administrador | admin123 |
| cliente@bitfix.com | Cliente de las órdenes demo | 1234 |
| tecnico.nivel1@bitfix.com | Técnico / NIVEL_1 | 1234 |
| tecnico.nivel2@bitfix.com | Técnico / NIVEL_2 | 1234 |
| tecnico.nivel3@bitfix.com | Técnico / NIVEL_3 | 1234 |

La carga está preparada para una base nueva, con correos uniformes por rol y nivel.
`data.sql` solo inserta cuentas ausentes; no actualiza ni renombra usuarios de una
base existente. Las claves de esta tabla aplican a las cuentas nuevas; reiniciar
no restablece contraseñas cambiadas.

## Inventario

Los componentes usan sus nombres habituales, sin prefijos. Los importes son ejemplos en pesos.

| Componente | Precio | Stock disponible inicial |
| --- | ---: | ---: |
| Memoria RAM DDR4 8GB | 28000 | 8 |
| SSD SATA 480GB | 48000 | 4 |
| Batería notebook | 65000 | 3 |
| Teclado notebook | 35000 | 2 |
| Pantalla LED 15.6 pulgadas | 90000 | 0 |
| Pasta térmica | 7500 | 20 |
| Fuente ATX 500W | 55000 | 1 |
| Cable SATA | 3000 | 15 |

El stock ya excluye las unidades reservadas o consumidas por los presupuestos
demo. Hay casos con stock normal, bajo y agotado.

Además de estos ocho componentes, `catalogo.sql` conserva los 18 repuestos
incorporados desde `origin/Merge`: SSD, NVMe, memorias, pantallas, baterías,
refrigeración, componentes electrónicos, teclado y cargador. Una base nueva
contiene 26 repuestos en total. Los precios y cantidades remotos se conservan;
los IDs se generan automáticamente. El marcador independiente `carga_catalogo`
evita duplicar o restaurar ese catálogo después de modificarlo o eliminar filas.

## Equipos y órdenes

El equipo forma parte de la orden de reparación en este modelo. Todas las órdenes
pertenecen a Carlos Gomez (`cliente@bitfix.com`) y tienen un técnico
del mismo nivel de reparación. Los días se calculan respecto de la primera carga
para mostrar distintas antigüedades y demoras.

| Código | Equipo | Estado inicial | Nivel |
| --- | --- | --- | --- |
| 910001 | Lenovo IdeaPad 3 | RECIBIDO | 1 |
| 910002 | Dell Inspiron 15 | EN_DIAGNOSTICO | 2 |
| 910003 | ASUS VivoBook 15 | ESPERANDO_REPUESTO | 3 |
| 910004 | HP Pavilion 14 | PRESUPUESTO_ENVIADO | 2 |
| 910005 | Acer Aspire 5 | EN_REPARACION | 3 |
| 910006 | PC de escritorio - limpieza | REPARADO | 1 |
| 910007 | PC de escritorio - fuente | ENTREGADO | 2 |
| 910008 | MacBook Air | SOLICITUD_COTIZACION | 3 |
| 910009 | Toshiba Satellite | COTIZACION_PREVIA_ENVIADA | 1 |
| 910010 | Samsung Book | PRESUPUESTO_RECHAZADO | 2 |

La cotización previa de 910009 contiene una nota técnica; el presupuesto definitivo
se confecciona después del ingreso físico del equipo.

Los códigos pueden consultarse sin cuenta desde la consulta de seguimiento. Con
el cliente demo se pueden revisar las órdenes, responder el presupuesto 910004 y
ver el comprobante de 910007. Los técnicos ven sus órdenes y su historial; el
administrador puede revisar el inventario y la carga de trabajo.

| Código | Repuesto (1 unidad) | Subtotal | Mano de obra | Total |
| --- | --- | ---: | ---: | ---: |
| 910004 | RAM | 28000 | 20000 | 48000 |
| 910005 | SSD | 48000 | 20000 | 68000 |
| 910006 | Pasta térmica | 7500 | 20000 | 27500 |
| 910007 | Fuente | 55000 | 19250 | 74250 |
| 910010 | Sin repuestos | 0 | 22000 | 22000 |

Las cuatro reservas conservan nombre, cantidad y precio del repuesto al presupuestar.
Rechazar 910004 devuelve su unidad de RAM al inventario mediante el flujo habitual.

## Conservación de cambios

La carga demo usa una transacción y registra su finalización en `carga_demo`, con
`id = 1`. Se ejecuta una vez por base de datos: reiniciar no duplica órdenes,
restaura stock, repone registros eliminados ni revierte estados. Los registros
existentes no se sobrescriben. Las cuentas ausentes se siguen creando desde
`data.sql`, que es independiente del marcador demo.

La prueba `DatosDemoTest` verifica importes, niveles, reservas y conservación de
cambios al repetir la carga, incluyendo el rechazo real de un presupuesto.
