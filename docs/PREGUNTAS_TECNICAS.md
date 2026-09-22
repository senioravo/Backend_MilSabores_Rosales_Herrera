Preguntas de Evaluación Técnica
1. ¿Qué modelo de base de datos implementaron y cómo se relaciona con los requisitos del cliente?
2. ¿Cómo configuraron la conexión a la base de datos en Spring Boot y qué propiedades
fueron necesarias?
3. ¿Qué entidades crearon y cómo decidieron sus relaciones (OneToMany, ManyToMany, etc.)?
4. ¿Cómo aseguraron que la lógica de negocio cumple los requerimientos del cliente?
5. ¿Qué validaciones se implementaron a nivel de servicio o entidad?
6. ¿Qué capas componen su arquitectura Backend y qué responsabilidad tiene cada una?
7. ¿Por qué eligieron JPA/Hibernate y qué ventajas les otorgó?
8. ¿Cómo manejaron errores en el backend? ¿Usaron @ControllerAdvice?
9. ¿Cómo implementaron la paginación o filtrado en algún endpoint?
10. ¿Qué técnicas usaron para asegurar que su backend es escalable o mantenible?
11. ¿Cómo estructuraron sus controladores REST y qué convenciones de rutas siguieron?
12. Explica un flujo completo de un CRUD: desde la vista en el front hasta la persistencia en la BD.
13. ¿Cómo documentaron sus endpoints en Swagger? ¿Qué ventajas obtuvieron?
14. ¿Cómo se manejan los estados HTTP en sus respuestas del backend?
15. ¿Qué formato utilizan para enviar datos al frontend y por qué?
16. ¿Cómo gestionaron los CORS para permitir comunicación entre sus proyectos?
17. ¿Cómo protegerían un endpoint que actualmente está público?
18. ¿Qué diferencia hay entre un DTO y una entidad? ¿Los utilizaron?
19. ¿Qué librería usaron para las solicitudes HTTP desde React y por qué?
20. ¿Cómo estructuraron los servicios del frontend para consumir la API?
21. ¿Cómo se aseguran de que los datos lleguen correctamente parseados desde el backend?
22. ¿Cómo manejaron errores cuando el backend devuelve mensajes de error?
23. Explica el ciclo de vida de un llamado asincrónico en React usando Hooks.
24. ¿Cómo sincronizan la interfaz con los cambios de datos?
25. ¿Qué mecanismo implementaron para validar el estado de carga (loading) o errores en
pantalla?
26. ¿Cómo justifican que la integración entre front y back es eficiente?
27. ¿Cómo implementaron la autenticación con JWT en su backend?
28. ¿Dónde y cómo se generan los tokens JWT?
29. ¿Cómo valida el backend que un token JWT es válido?
30. ¿Cómo implementaron los roles de usuario y cómo se aplican en el backend?
31. ¿Cómo manejan la expiración del token?
32. ¿Cómo protegerías un endpoint en Spring Security para que solo roles específicos accedan?
33. ¿Cómo manejan en el frontend el estado autenticado del usuario?
34. ¿Por qué almacenar el token en localStorage puede ser riesgoso?
35. ¿Cómo persiste la sesión después de recargar la página?
36. ¿Dónde almacenan el token y qué medidas de seguridad consideraron?
37. ¿Cómo verifican el estado de la sesión al cargar una vista protegida?
38. ¿Cómo manejarían el cierre automático de sesión al expirar un token?
39. ¿Cómo actualizarían o renovarían el token desde el frontend?
40. ¿Cómo implementaron rutas protegidas en React Router modo Data?
41. ¿Cómo ocultaron o mostraron ciertas interfaces según el rol del usuario?
42. ¿Qué pasaría si un usuario intenta acceder manualmente a una ruta protegida?
43. ¿Cómo comprobaron desde React qué rol tiene el usuario?
44. Muestra un ejemplo de un componente o vista restringida.
45. ¿Cómo gestionaron el estado global? ¿Usaron contextos, reducers o estados locales?
46. ¿Cómo implementaron la navegación con React Router Data y loaders?
47. ¿Cómo estructuraron la arquitectura del frontend?
48. ¿Qué Hook utilizaron para manejar efectos secundarios y por qué?
49. ¿Qué ventajas tiene React Router Data API frente al routing clásico?
50. ¿Qué mejoras aplicarían al frontend ahora que conocen Hooks y Router?
51. ¿Qué problemas puede generar el uso excesivo de relaciones bidireccionales en JPA?
52. Explica cuándo conviene usar @Transactional y riesgos de usarlo incorrectamente.
53. ¿Por qué es importante separar controllers, services y repositories?
54. ¿Cómo evitaron LazyInitializationException? ¿Qué estrategias usaron?
55. ¿Cómo evitaron ciclos infinitos en JSON?
56. ¿Qué patrón aplicaron para la lógica de negocio y por qué?
57. ¿Cómo garantiza Spring el rollback de transacciones?
58. ¿Cómo diseñarían su backend para soportar 10 veces más carga?
59. ¿Por qué no es buena práctica exponer entidades JPA en la API?
60. ¿Qué problemas de concurrencia podría tener su aplicación?
61. ¿Qué criterios usaron para elegir códigos de estado HTTP?
62. ¿Qué riesgos hay al revelar demasiada información en errores?
63. ¿Cómo versionarían su API sin romper compatibilidad?
64. ¿Cómo optimizar un endpoint que devuelve miles de registros?
65. ¿Cómo implementar idempotencia en operaciones sensibles?
66. ¿Diferencia entre validación en backend y en frontend?
67. ¿Cómo minimizar llamadas innecesarias desde React?
68. ¿Cómo diagnosticarían un endpoint lento?
69. ¿Cómo implementar caching en su API?
70. ¿Cómo hacer su API tolerante a caídas parciales?
71. ¿Qué ataque ocurre si guardan JWT en localStorage?
72. ¿Qué pasa si un usuario altera su JWT manualmente?
73. ¿Cómo evitar escalamiento de privilegios?
74. Diferencia entre autenticación y autorización.
75. ¿Cómo manejar rotación de claves JWT?
76. ¿Qué es un ataque CSRF y afecta a JWT?
77. ¿Qué información es insegura dentro de un JWT?
78. ¿Cómo invalidar un token comprometido?
79. ¿Cómo configurar CORS para bloquear sitios no autorizados?
80. ¿Cómo evitar ataques de fuerza bruta en login?
81. ¿Cómo sincronizan las actualizaciones sin recargar vistas?
82. ¿Qué harían si un cambio en backend rompe el frontend?
83. ¿Cómo centralizar headers y errores en cliente HTTP?
84. ¿Cómo evitar duplicación de lógica al consumir API?
85. ¿Cómo manejar errores globales HTTP en React?
86. ¿Cómo validan consistencia entre front y back?
87. ¿Cómo sincronizar formularios complejos con BD?
88. ¿Cómo combinar datos de múltiples endpoints en una sola vista?
89. ¿Qué problemas causa depender mal de variables en useEffect?
90. Diferencias técnicas entre loaders y actions de React Router.
91. ¿Cómo manejar loaders que requieren token y rol?
92. ¿Qué sucede si un loader falla antes del render?
93. ¿Cómo evitar prop drilling en su arquitectura?
94. ¿Qué ventajas tienen los custom hooks? Ejemplo.
95. ¿Cómo revalidan datos después de mutaciones?
96. ¿Qué hacer si una vista depende de múltiples requests?
97. ¿Qué criterios usan para separar componentes presentacionales y lógicos?
98. ¿Cómo manejar estados múltiples que dependen entre sí?
99. ¿Cómo mejorar rendimiento general del frontend?
100. ¿Cómo evitar renderizados innecesarios?
101. ¿Qué es Webpay Plus y qué servicios de pago permite (tarjeta crédito, débito, prepago, cuotas)?
102. ¿Cuál es el flujo básico de una transacción con Webpay Plus desde que el usuario hace clic
“Pagar” hasta que recibe confirmación?
103. ¿Qué credenciales o parámetros son necesarios para configurar la integración de Webpay Plus
en modo “producción”?
104. ¿Qué diferencias existen entre los ambientes de “prueba” (sandbox) y “producción” para
Webpay Plus?
105. ¿Cómo se maneja la redirección del usuario desde el comercio hacia Webpay y luego de vuelta
al comercio?
106. ¿Qué es el “código de comercio” (commerce code) en Transbank y por qué es clave en la
integración?
107. ¿Qué pasos de certificación técnica obliga Transbank para que un código de comercio esté
habilitado?
108. En el backend del comercio, ¿cómo se valida que una transacción fue aprobada por Webpay
Plus?
109. ¿Qué datos devuelve Webpay Plus al comercio tras una transacción (por ejemplo token,
resultado, estado)?
110. ¿Cómo se deben manejar los estados de “aprobada”, “rechazada”, “pendiente” u otros que
retorne la pasarela?
111. ¿Cómo se gestiona la seguridad en la integración de Webpay Plus (por ejemplo firma digital,
validación de token, uso de certificados)?
112. ¿Qué consideraciones hay para el manejo de datos sensibles en este flujo de pago (tarjeta,
usuario, token)?
113. ¿Cómo el frontend de React puede gestionar el flujo de pago: por ejemplo iniciar la transacción,
llamar al backend y redirigir al checkout Webpay?
114. ¿Qué responsabilidad tiene el backend del comercio al recibir el resultado de Webpay Plus y
persistirlo en la base de datos?
115. ¿Cómo se implementa un “callback” o “webhook” (si aplica) para que el comercio sea notificado
de cambios en la transacción?
116. ¿Cómo se maneja el reintento o error en transacciones fallidas con Webpay Plus?
117. ¿Cuál es la implicancia para el comercio de ofrecer cuotas sin interés a través de Webpay Plus?
118. ¿Cómo afecta el tipo de tarjeta (nacional, internacional) o el medio de pago (crédito, débito,
prepago) la comisión de la transacción con Transbank?
119. ¿Qué ocurre si el usuario abandona el pago en Medios de Pago de Webpay (por ejemplo cierra
la pestaña)? ¿Cómo debe manejarlo el comercio?
120. ¿Cómo se manejan los “malls” o sub‐comercios cuando un comercio integra Webpay Plus como
parte de un conjunto?
121. ¿Qué parámetros específicos de REST (endpoint, método HTTP, ruta) se utilizan para “iniciar”
una transacción Webpay Plus?
122. ¿Qué parámetros se envían al Webpay (por ejemplo monto, orden de compra, retorno URL,
etc.)?
123. ¿Qué tipo de validación debe realizar el comercio antes de enviar el monto o datos de la
transacción al Webpay?
124. ¿Cómo se estructura la respuesta del Webpay y qué debe hacer el comercio con ella (por
ejemplo actualizar estado, mostrar mensaje al usuario)?
125. ¿Qué diferencia hay entre “commit” y “capture” de la transacción (si aplica) en Webpay Plus?
126. ¿Cómo se implementa el reembolso (refund) o reversa de una transacción desde Webpay Plus?
127. ¿Cómo el frontend puede mostrar al usuario el estado del pago y qué sucede si ocurre un
error?
128. ¿Qué medidas adoptaron para asegurar que el pago no sea duplicado (por ejemplo doble clic
en pagar)?
129. ¿Cómo manejarías el caso de conciliación: ventas hechas, abonos cobrados, diferencia de
montos?
130. ¿Qué implicancias legales o de cumplimiento tiene utilizar la pasarela Webpay Plus (datos de
tarjeta, cookies, privacidad)?
131. ¿Cómo depurarías un problema donde Webpay devuelve error “código inválido de comercio” o
“token inválido”?
132. ¿Cuál es el impacto de la latencia o fallo en la red durante el proceso de pago y cómo mitigarlo?
133. ¿Cómo se deberían registrar los logs tanto en frontend como backend para transacciones
Webpay Plus con fines de auditoría?
134. ¿Cómo se puede testear localmente la integración Webpay Plus sin afectar producción?
135. ¿Cómo gestionarías el cambio de “modo prueba” a “modo producción” en React/Backend sin
impactar al usuario final?
136. ¿Qué ocurre si el comercio recibe una transacción aprobada pero el abono de Transbank falla o
demora? ¿Cómo reflejarlo en su sistema?
137. ¿Cómo integraste el Webpay Plus en el flujo de checkout desarrollado por ustedes (¿Qué
componente React, qué servicio backend)?
138. ¿Qué responsabilidades de UI/UX tienes al integrar Webpay Plus para dar al usuario
seguridad/confianza?
139. ¿Cómo se hace la validación y verificación de montos entre lo que el usuario pagó y lo que el
comercio espera?
140. ¿Qué diferencias técnicas hay con otros métodos de pago que podrían implementarse (por
ejemplo PayPal, Stripe) respecto a Webpay Plus?
141. ¿Cómo defenderías ante un auditor que tu implementación del pago es segura, auditable y
escalable?
142. ¿Cómo manejarías un escenario de “pagos recurrentes” o suscripción con Webpay Plus si fuera
necesario?
143. ¿Qué versiones de SDK o librerías existen para Webpay Plus y cómo la tienda verificada se
actualiza?
144. ¿Cómo documentaron en su proyecto la integración de Webpay Plus (cómo config, qué
endpoints, qué servicios)?
145. ¿Cómo asegurarían que el frontend no exponga claves privadas o datos sensibles de Webpay
Plus?
146. ¿Qué impacto tiene el soporte de múltiples monedas o pagos internacionales en Webpay Plus?
147. ¿Cómo se define en tu proyecto el “orden de compra” (buyOrder) para Webpay Plus y cuál es su
importancia para la conciliación?
148. ¿Qué sucede si el usuario cancela el pago desde la página de Webpay y cómo lo registra el
comercio?
149. ¿Cómo gestionarías la actualización de datos del cliente (tarjeta, usuario, perfil) en relación a
Webpay Plus?
150. ¿Cómo plantearías un test de integración (end-to-end) que cubra el flujo completo desde
usuario React Backend Transbank resultado 