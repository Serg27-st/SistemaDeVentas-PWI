(function () {
  const widget = document.getElementById('chatbotWidget');
  if (!widget) return;

  const yapeNumero      = widget.dataset.yapeNumero || '51977968942';
  const yapeNombre      = widget.dataset.yapeNombre || 'Tu Licorería';
  const pedidoMinimo    = widget.dataset.pedidoMinimo || '50.00';
  const whatsappNumero  = widget.dataset.whatsappNumero || '51977968942';

  const toggle    = document.getElementById('chatbotToggle');
  const win       = document.getElementById('chatbotWindow');
  const closeBtn  = document.getElementById('chatbotClose');
  const body      = document.getElementById('chatbotBody');
  const iconOpen  = document.getElementById('chatbotIconOpen');
  const iconClose = document.getElementById('chatbotIconClose');

  let yaSaludo = false;

  const MENU_PRINCIPAL = [
    { label: '🛒 ¿Cómo comprar?',        handler: respuestaComoComprar },
    { label: '💳 Métodos de pago',        handler: respuestaMetodosPago },
    { label: '🚚 Envíos y pedido mínimo', handler: respuestaEnvios },
    { label: '🕒 Horario de atención',    handler: respuestaHorario },
    { label: '📦 Rastrear mi pedido',     handler: respuestaRastreo },
    { label: '💬 Hablar con un asesor',   handler: respuestaAsesor },
  ];

  function abrirChat() {
    win.classList.add('is-open');
    iconOpen.style.display = 'none';
    iconClose.style.display = '';
    toggle.setAttribute('aria-expanded', 'true');
    if (!yaSaludo) {
      yaSaludo = true;
      mostrarSaludo();
    }
  }

  function cerrarChat() {
    win.classList.remove('is-open');
    iconOpen.style.display = '';
    iconClose.style.display = 'none';
    toggle.setAttribute('aria-expanded', 'false');
  }

  toggle.addEventListener('click', function () {
    win.classList.contains('is-open') ? cerrarChat() : abrirChat();
  });
  closeBtn.addEventListener('click', cerrarChat);

  function scrollAbajo() {
    body.scrollTop = body.scrollHeight;
  }

  function mensajeBot(html) {
    const div = document.createElement('div');
    div.className = 'chatbot-msg chatbot-msg-bot';
    div.innerHTML = html;
    body.appendChild(div);
    scrollAbajo();
  }

  function mensajeUsuario(texto) {
    const div = document.createElement('div');
    div.className = 'chatbot-msg chatbot-msg-user';
    div.textContent = texto;
    body.appendChild(div);
    scrollAbajo();
  }

  function mostrarOpciones(opciones) {
    const cont = document.createElement('div');
    cont.className = 'chatbot-opciones';
    opciones.forEach(function (op) {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'chatbot-opcion-btn';
      btn.textContent = op.label;
      btn.addEventListener('click', function () {
        cont.remove();
        mensajeUsuario(op.label);
        op.handler();
      });
      cont.appendChild(btn);
    });
    body.appendChild(cont);
    scrollAbajo();
  }

  function mostrarMenuPrincipal() {
    mensajeBot('¿En qué te puedo ayudar? Elige una opción:');
    mostrarOpciones(MENU_PRINCIPAL);
  }

  function volverAlMenu() {
    mostrarOpciones([{ label: '⬅ Volver al menú', handler: mostrarMenuPrincipal }]);
  }

  function mostrarSaludo() {
    mensajeBot('¡Hola! 👋 Soy el asistente virtual de <b>Tu Licorería</b>. Puedo ayudarte con info rápida sobre compras, pagos y envíos.');
    mostrarMenuPrincipal();
  }

  function respuestaComoComprar() {
    mensajeBot(
      '<b>Comprar es fácil:</b><br>' +
      '1️⃣ Explora el catálogo y agrega productos al carrito.<br>' +
      '2️⃣ Abre tu carrito y presiona "Iniciar compra".<br>' +
      '3️⃣ Ingresa a tu cuenta o continúa como invitado.<br>' +
      '4️⃣ Completa tus datos de envío.<br>' +
      '5️⃣ Elige tu método de pago y confirma. ¡Listo! 🎉'
    );
    volverAlMenu();
  }

  function respuestaMetodosPago() {
    mensajeBot(
      'Aceptamos:<br>' +
      '💜 <b>Yape</b> — al número <b>' + formatearYape(yapeNumero) + '</b> a nombre de <b>' + yapeNombre + '</b>.<br>' +
      '💵 <b>Efectivo</b> contra entrega.<br><br>' +
      'Tip: envíanos la captura de tu Yape por WhatsApp para confirmar el pago más rápido.'
    );
    volverAlMenu();
  }

  function respuestaEnvios() {
    mensajeBot(
      'El pedido mínimo para delivery es de <b>S/ ' + Number(pedidoMinimo).toFixed(2) + '</b>.<br>' +
      'El costo de envío varía según tu distrito — lo verás calculado automáticamente en tu carrito antes de pagar.'
    );
    volverAlMenu();
  }

  function respuestaHorario() {
    mensajeBot(
      'Nuestro equipo está disponible de <b>lunes a domingo, de 9am a 10pm</b>.<br>' +
      'La tienda en línea está disponible las 24 horas. 🕒'
    );
    volverAlMenu();
  }

  function respuestaRastreo() {
    mensajeBot(
      'Puedes ver el estado de tus pedidos en <a href="/mi-cuenta/pedidos">Mi cuenta → Mis pedidos</a> ' +
      '(necesitas haber iniciado sesión con tu cuenta de cliente).'
    );
    volverAlMenu();
  }

  function respuestaAsesor() {
    const url = 'https://wa.me/' + whatsappNumero + '?text=' +
      encodeURIComponent('Hola, tengo una consulta sobre mi pedido.');
    mensajeBot(
      'Con gusto. Escríbenos directo por WhatsApp y te atenderá un asesor:<br>' +
      '<a href="' + url + '" target="_blank" rel="noopener noreferrer" class="chatbot-link-boton">' +
      '<i class="bi bi-whatsapp"></i> Abrir WhatsApp</a>'
    );
    volverAlMenu();
  }

  function formatearYape(numero) {
    const limpio = String(numero).replace(/[^0-9]/g, '');
    if (limpio.startsWith('51') && limpio.length === 11) {
      const local = limpio.substring(2);
      return '+51 ' + local.substring(0, 3) + ' ' + local.substring(3, 6) + ' ' + local.substring(6);
    }
    return '+' + limpio;
  }
})();
