document.addEventListener('DOMContentLoaded', function () {
  const toggleCarrito = document.getElementById('toggle-carrito');
  const carritoPanel = document.getElementById('carrito-panel');
  const closeCarrito = document.getElementById('close-carrito');
  const favoritesPanel = document.getElementById('favorites-panel');

  function closeAllPanels() {
    if (favoritesPanel && !favoritesPanel.classList.contains('hidden')) {
      favoritesPanel.classList.add('hidden');
    }
    if (carritoPanel && !carritoPanel.classList.contains('hidden')) {
      carritoPanel.classList.add('hidden');
    }
  }

  if (toggleCarrito && carritoPanel) {
    toggleCarrito.addEventListener('click', (e) => {
      e.preventDefault();
      const isOpen = !carritoPanel.classList.contains('hidden');
      actualizarCarrito(); 
      closeAllPanels();
      if (!isOpen) {
        carritoPanel.classList.remove('hidden');
      }
    });
  }

  if (closeCarrito && carritoPanel) {
    closeCarrito.addEventListener('click', () => {
      carritoPanel.classList.add('hidden');
    });
  }
});

function mostrarAviso(mensaje, tipo) {
  const aviso = document.getElementById('mensaje-aviso');
  aviso.textContent = mensaje;
  aviso.classList.remove('error', 'correcto');
  aviso.classList.add(tipo);
  aviso.style.display = 'block';
  setTimeout(() => {
    aviso.style.display = 'none';
  }, 3000);
}

function agregarAlCarrito(idProducto) {
  fetch(`/api/v1/agregarProductoAlCarrito/${idProducto}`, {
    method: 'POST'
  })
  .then(async response => {
    const data = await response.json().catch(() => ({}));
    if (response.ok) {
      mostrarAviso(data.correcto || "Producto agregado al carrito.", 'correcto');
      actualizarCarrito();
    } else if (response.status === 409) {
      mostrarAviso(data.error || "El producto ya está en tu carrito.", 'error');
    } else {
      mostrarAviso(data.error || "Error al agregar el producto.", 'error');
    }
  })
  .catch(error => {
    mostrarAviso("Error de red: " + error.message, 'error');
  });
}







function actualizarCarrito() {
  console.log("Actualizando carrito...");
  fetch('/api/v1/carrito')
    .then(res => {
      if (!res.ok) throw new Error("No se pudo cargar el carrito");
      return res.json();
    })
    .then(productos => {
      console.log("Productos en carrito:", productos);
      const contenedor = document.getElementById('carrito-list');
      contenedor.innerHTML = '';

        if (!productos || productos.length === 0) {
    contenedor.innerHTML = '<p>Tu carrito está vacío.</p>';
    if (btncrearCompra) {
      btncrearCompra.classList.add('hidden');
    }
    return;
  }

  if (btncrearCompra) {
    btncrearCompra.classList.remove('hidden');
  }

      productos.forEach(producto => {
        const item = document.createElement('div');
        item.classList.add('carrito-item');
        item.innerHTML = `
          <img src="/uploads/${producto.imagen}" alt="${producto.nombre}" />
          <div class="carrito-details">
            <h4>${producto.nombre}</h4>
            <p>${producto.descripcion}</p>
            <span class="price">${producto.precio} €</span>
            <div class="carrito-buttons">
              <button class="delete-favorite" onclick="eliminarDelCarrito(${producto.id})">Eliminar</button>              
            </div>           
          </div>
        `;
        contenedor.appendChild(item);
      });
    })
    .catch(err => {
      console.error("Error al actualizar el carrito:", err);
    });
}

function eliminarDelCarrito(idProducto) {
  fetch(`/api/v1/eliminarProductoDelCarrito/${idProducto}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(response => response.json().then(data => ({ status: response.status, body: data })))
  .then(({ status, body }) => {
    if (status === 200) {
      mostrarAviso(body.correcto, 'correcto');
      actualizarCarrito();
    } else {
      mostrarAviso(body.error, 'error');
    }
  })
  .catch(() => {
    mostrarAviso('Error de conexión. Intenta más tarde.', 'error');
  });
}







