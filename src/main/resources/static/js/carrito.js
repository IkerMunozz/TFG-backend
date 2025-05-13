// Elementos del carrito
const toggleCarrito = document.getElementById('toggle-carrito');
const carritoPanel = document.getElementById('carrito-panel');
const closeCarrito = document.getElementById('close-carrito');

// Elemento del panel de favoritos
const favoritesPanel = document.getElementById('favorites-panel'); // Asegúrate de que el panel tenga este ID

// Función para cerrar todos los paneles
function closeAllPanels() {
  if (favoritesPanel && !favoritesPanel.classList.contains('hidden')) {
    favoritesPanel.classList.add('hidden');
  }
  if (carritoPanel && !carritoPanel.classList.contains('hidden')) {
    carritoPanel.classList.add('hidden');
  }
}

// Toggle del carrito
toggleCarrito.addEventListener('click', (e) => {
  e.preventDefault(); // Para evitar el comportamiento por defecto del <a href="#">
  const isOpen = !carritoPanel.classList.contains('hidden');

  closeAllPanels(); // Cierra todo primero

  // Si no estaba abierto antes, lo mostramos
  if (!isOpen) {
    carritoPanel.classList.remove('hidden');
  }
});

// Botón de cerrar
closeCarrito.addEventListener('click', () => {
  carritoPanel.classList.add('hidden');
});


