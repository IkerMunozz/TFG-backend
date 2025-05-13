navigator.geolocation.getCurrentPosition(async (position) => {
    const lat = position.coords.latitude; // Latitud de la ubicación del usuario
    const lon = position.coords.longitude; // Longitud de la ubicación del usuario
  
  
  
    const map = L.map('map').setView([lat, lon], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);
  
    // Marcador de ubicación del usuario
    const userLocationIcon = L.icon({
      iconUrl: 'data:image/svg+xml;charset=UTF-8,<svg xmlns="http://www.w3.org/2000/svg" height="32" width="32" viewBox="0 0 32 32"><circle cx="16" cy="16" r="16" fill="%2300c4b0" /></svg>', // Cambia el color aquí
      iconSize: [32, 32], // Tamaño del icono
      iconAnchor: [16, 32], // Ancla del icono
      popupAnchor: [0, -32] // Ajuste de pop-up
    });
  
    // Marcador de ubicación del usuario con el icono personalizado
    L.marker([lat, lon], { icon: userLocationIcon }).addTo(map).bindPopup('Tu ubicación').openPopup();
    // Consulta a Overpass API
    const query = `
      [out:json];
      node
        ["amenity"="post_office"]
        (around:50000,${lat},${lon});
      out;
    `;
  
    const url = "https://overpass-api.de/api/interpreter?data=" + encodeURIComponent(query);
    const response = await fetch(url);
    const data = await response.json();
  
    if (data.elements.length === 0) {
      alert("No se encontraron puntos de envío cerca de Toledo.");
    } else {
      data.elements.forEach(p => {
        L.marker([p.lat, p.lon])
          .addTo(map)
          .bindPopup(p.tags.name || "Oficina de Correos");
      });
    }
  }, () => {
    alert("No se pudo obtener tu ubicación.");
  });
  