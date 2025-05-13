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
const panelCarrito = document.getElementById("carrito-panel"); // Asegúrate de que este ID sea correcto

toggleBtn.addEventListener("click", (e) => {
  e.preventDefault();

  // Cierra el panel del carrito si está abierto
  if (!panelCarrito.classList.contains("hidden")) {
    panelCarrito.classList.add("hidden");
  }

  // Alternar el panel de favoritos
  panel.classList.toggle("hidden");
});

closeBtn.addEventListener("click", () => {
  panel.classList.add("hidden");
});

function addToFavorites({ nombre, descripcion, imagen, precio }) {
  const list = document.getElementById("favorites-list");

  const item = document.createElement("div");
  item.className = "favorite-item";
  item.innerHTML = `
    <img src="${imagen}" alt="${nombre}">
    <div class="favorite-details">
      <h4>${nombre}</h4>
      <p>${descripcion}</p>
      <div class="favorite-price">€${precio}</div>
      <button class="delete-favorite">Eliminar</button>
    </div>
  `;

  // Eliminar al hacer clic en el botón
  item.querySelector(".delete-favorite").addEventListener("click", () => {
    list.removeChild(item);
  });

  list.appendChild(item);
}




