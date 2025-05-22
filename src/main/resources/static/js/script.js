// document.addEventListener('DOMContentLoaded', function () {
//     const searchBar = document.getElementById('searchBar');
//     const placeholder = document.getElementById('search-placeholder');
//     const header = document.querySelector('header');
//     const container = document.querySelector('.container');
//     let lastScrollTop = 0;
//     let isSticky = false;
    
//     // Detectamos el offset original del buscador (su posición inicial)
//     const originalOffset = placeholder.getBoundingClientRect().top + window.scrollY;
  
//     window.addEventListener('scroll', () => {
//       const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  
//       // Si estamos bajando
//       if (scrollTop > lastScrollTop) {
//         // El buscador se mueve al header solo cuando se desplaza hacia abajo y sale de la vista
//         if (!isSticky && scrollTop > originalOffset) {
//           header.appendChild(searchBar);
//           searchBar.classList.add('sticky-search');
//           placeholder.classList.add('active');
//           isSticky = true;
//         }
//       } 
//       // Si estamos subiendo
//       else if (scrollTop < lastScrollTop) {
//         // El buscador solo vuelve a su zona original cuando vuelve a su lugar
//         if (isSticky && scrollTop <= originalOffset) {
//           placeholder.parentNode.insertBefore(searchBar, placeholder.nextSibling);
//           searchBar.classList.remove('sticky-search');
//           placeholder.classList.remove('active');
//           isSticky = false;
//         }
//       }
  
//       lastScrollTop = scrollTop <= 0 ? 0 : scrollTop; // Para evitar números negativos
//     });
//   });

const toggleBtn = document.getElementById("toggle-favorites");
const closeBtn = document.getElementById("close-favorites");
const panel = document.getElementById("favorites-panel");
const panelCarrito = document.getElementById("carrito-panel"); 

toggleBtn.addEventListener("click", (e) => {
  e.preventDefault();

  if (!panelCarrito.classList.contains("hidden")) {
    panelCarrito.classList.add("hidden");
  }

  panel.classList.toggle("hidden");
});

closeBtn.addEventListener("click", () => {
  panel.classList.add("hidden");
});

function agregarAFavoritos(idProducto) {
  fetch(`/api/v1/agregarProductoAFavoritos/${idProducto}`, {
    method: 'POST'
  })
  .then(async response => {
    const data = await response.json().catch(() => ({}));
    if (response.ok) {
      mostrarAviso(data.correcto || "Producto agregado a favoritos.", 'correcto');
      cargarFavoritos();
    } else if (response.status === 409) {
      mostrarAviso(data.error || "El producto ya está en favoritos.", 'error');
    } else if (response.status === 401) {
      mostrarAviso(data.error || "Debes iniciar sesión.", 'error');
    } else {
      mostrarAviso(data.error || "Error al agregar a favoritos.", 'error');
    }
  })
  .catch(error => {
    mostrarAviso("Error de red: " + error.message, 'error');
  });
}

function agregarAlCarritoDesdeFavoritos(idProducto) {
  fetch(`/api/v1/agregarProductoAlCarritoEnFavoritos/${idProducto}`, {
    method: 'POST'
  })
  .then(async response => {
    const data = await response.json().catch(() => ({}));
    if (response.ok) {
      mostrarAviso(data.correcto || "Producto agregado al carrito y eliminado de favoritos.", 'correcto');
      actualizarCarrito();
      cargarFavoritos();  // refresca la lista de favoritos
    } else if (response.status === 409) {
      mostrarAviso(data.error || "El producto ya está en tu carrito.", 'error');
    } else if (response.status === 401) {
      mostrarAviso(data.error || "Debes iniciar sesión.", 'error');
    } else {
      mostrarAviso(data.error || "Error al agregar producto.", 'error');
    }
  })
  .catch(error => {
    mostrarAviso("Error de red: " + error.message, 'error');
  });
}

function cargarFavoritos() {
  fetch('/api/v1/productosFavoritos', { cache: "no-store" })
    .then(res => {
      if (!res.ok) {
        if (res.status === 401) {
          mostrarAviso("Debes iniciar sesión para ver favoritos.", 'error');
        }
        throw new Error("Error al cargar favoritos");
      }
      return res.json();
    })
    .then(favoritos => {
      console.log("Favoritos recibidos:", favoritos);
      const contenedor = document.getElementById('favorites-list');
      contenedor.innerHTML = '';

      if (!favoritos || favoritos.length === 0) {
        contenedor.innerHTML = '<p>No tienes productos favoritos.</p>';
        return;
      }

      favoritos.forEach(producto => {
        const item = document.createElement('div');
        item.classList.add('favorite-item');
        item.innerHTML = `
          <img src="/uploads/${producto.imagen}" alt="Imagen del producto" />
          <div class="favorite-details">
            <h4>${producto.nombre}</h4>
            <p>${producto.descripcion}</p>
            <span class="price">${producto.precio} €</span>
            <div class="favorite-buttons">
              <button class="delete-favorite" onclick="eliminarFavorito(${producto.id})">Eliminar</button>
              <button class="add-to-cart-favorites" onclick="agregarAlCarritoDesdeFavoritos(${producto.id})">Añadir al carrito</button>
            </div>
          </div>
        `;
        contenedor.appendChild(item);
      });
    })
    .catch(err => {
      console.error("Error al obtener favoritos:", err);
    });
}






function eliminarFavorito(idProducto) {
  fetch(`/api/v1/productosFavoritos/${idProducto}`, {
    method: 'POST'
  })
  .then(async response => {
    const data = await response.json().catch(() => ({}));
    if (response.ok) {
      mostrarAviso(data.correcto || "Producto eliminado de favoritos.", 'correcto');
      cargarFavoritos(); 
    } else {
      mostrarAviso(data.error || "Error al eliminar el producto de favoritos.", 'error');
    }
  })
  .catch(error => {
    mostrarAviso("Error de red: " + error.message, 'error');
  });
}




  /*--------------------------------------------Pestañas--------------------------------------------*/

  function mostrarTab(tab) {
    document.getElementById('venta').classList.add('hidden');
    document.getElementById('vendidos').classList.add('hidden');
    document.getElementById(tab).classList.remove('hidden');

    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    if (tab === 'venta') {
      document.querySelector('.tab-btn:nth-child(1)').classList.add('active');
    } else {
      document.querySelector('.tab-btn:nth-child(2)').classList.add('active');
    }
  }

document.addEventListener("DOMContentLoaded", () => {
  cargarFavoritos(); 
});




