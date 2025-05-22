/*--------------------------------------------Buscador--------------------------------------------*/

const inputBusqueda = document.getElementById('terminoBusqueda');
const productsDiv = document.getElementById('products-container');

inputBusqueda.addEventListener('input', () => {
  const termino = inputBusqueda.value.trim();

  // Si el input está vacío, mostrar todos los productos
  if (termino === '') {
    productsDiv.querySelectorAll('.card').forEach(card => {
      card.style.display = '';
    });
    return;
  }

  // Buscar productos que coincidan
  fetch(`/api/v1/buscarproductos?terminoBusqueda=${encodeURIComponent(termino)}`)
    .then(res => {
      if (!res.ok) throw new Error("Error en la búsqueda");
      return res.json();
    })
    .then(productos => {
      const idsCoincidentes = new Set(productos.map(p => p.id.toString()));

      // Mostrar solo los productos que coincidan
      productsDiv.querySelectorAll('.card').forEach(card => {
        const id = card.getAttribute('data-id');
        if (idsCoincidentes.has(id)) {
          card.style.display = '';
        } else {
          card.style.display = 'none';
        }
      });
    })
    .catch(err => {
      console.error("Error al buscar productos:", err);
    });
});
