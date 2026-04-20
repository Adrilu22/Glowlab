let carrito = [];

// 🔥 ENDPOINTS
const API_PRODUCTOS = "https://api-skincare-v2-994118614969.us-central1.run.app/api/productos";
const API_CATEGORIAS = "https://api-skincare-v2-994118614969.us-central1.run.app/api/categorias";

// 🔥 IMÁGENES
function imagenProducto(nombre) {
  const n = nombre.toLowerCase();

  if (n.includes("gel"))
    return "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=400&q=80";

  if (n.includes("crema"))
    return "https://images.unsplash.com/photo-1608248597279-f99d160bfcbc?auto=format&fit=crop&w=400&q=80";

  if (n.includes("vitamina"))
    return "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&w=400&q=80";

  if (n.includes("protector"))
    return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRXGPYjyqudAP0rGGZMpu2Z98-3_L2Bn7P_sg&s";

  if (n.includes("serum"))
    return "https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?auto=format&fit=crop&w=400&q=80";

  return "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=400&q=80";
}

// 🔥 GENERAR RUTINA
async function generarRutina() {
  const tipo = document.getElementById("tipoPiel").value;
  const problema = document.getElementById("problema").value;

  const res = await fetch(API_PRODUCTOS);
  const data = await res.json();

  let rutina = [];

  data.forEach(p => {
    const nombre = p.nombre.toLowerCase();
    const categoria = p.categoria?.nombre?.toLowerCase();

    if (tipo === "grasa" && nombre.includes("gel")) rutina.push(p);
    if (tipo === "seca" && nombre.includes("crema")) rutina.push(p);
    if (tipo === "mixta" && (nombre.includes("gel") || nombre.includes("crema"))) rutina.push(p);

    if (problema === "manchas" && (nombre.includes("vitamina") || categoria === "tratamiento")) rutina.push(p);
    if (problema === "hidratacion" && categoria === "hidratacion") rutina.push(p);
    if (problema === "poros" && (nombre.includes("serum") || categoria === "tratamiento")) rutina.push(p);
    if (problema === "acne" && categoria === "limpieza") rutina.push(p);
  });

  rutina = rutina.filter((v,i,a) => a.findIndex(t => (t.id === v.id)) === i);

  mostrarRutina(rutina);
}

// 🔥 MOSTRAR RUTINA
function mostrarRutina(lista) {
  const contenedor = document.getElementById("rutina");
  contenedor.innerHTML = "";

  lista.forEach(p => {
    contenedor.innerHTML += `
      <div class="card">
        <img src="${imagenProducto(p.nombre)}">
        <h4>${p.nombre}</h4>
        <p class="precio">$${p.precio}</p>
        <button onclick='agregarCarrito(${JSON.stringify(p)})'>Agregar</button>
        <button onclick='verCategoria(${p.id})'>Ver categoría</button>
      </div>
    `;
  });
}

// 🔥 CARGAR PRODUCTOS
async function cargarProductos() {
  const res = await fetch(API_PRODUCTOS);
  const data = await res.json();

  const contenedor = document.getElementById("productos");
  contenedor.innerHTML = "";

  data.forEach(p => {
    contenedor.innerHTML += `
      <div class="card">
        <img src="${imagenProducto(p.nombre)}">
        <h4>${p.nombre}</h4>
        <p class="precio">$${p.precio}</p>
        <button onclick='agregarCarrito(${JSON.stringify(p)})'>Añadir</button>
      </div>
    `;
  });
}

// 🔥 RELACIÓN
async function verCategoria(id) {
  const res = await fetch(`${API_PRODUCTOS}/${id}/categoria`);
  const categoria = await res.json();
  alert("Categoría: " + categoria.nombre);
}

// 🛒 CARRITO
function agregarCarrito(producto) {
  carrito.push(producto);
  mostrarCarrito();
}

function mostrarCarrito() {
  const contenedor = document.getElementById("carrito");
  const totalEl = document.getElementById("total");

  contenedor.innerHTML = "";
  let total = 0;

  carrito.forEach((p, i) => {
    total += p.precio;

    contenedor.innerHTML += `
      <div class="card">
        <h4>${p.nombre}</h4>
        <p>$${p.precio}</p>
        <button onclick="eliminarProducto(${i})">Eliminar</button>
      </div>
    `;
  });

  totalEl.innerText = "$" + total;
}

function eliminarProducto(index) {
  carrito.splice(index, 1);
  mostrarCarrito();
}

// 🧾 FINALIZAR COMPRA ✅
function finalizarCompra() {

  if (carrito.length === 0) {
    alert("El carrito está vacío ❌");
    return;
  }

  const datos = carrito.map(p => ({
    productoId: parseInt(p.id)  // ✅ fix casting
  }));

  console.log("Enviando datos:", datos);

  fetch("https://api-skincare-v2-994118614969.us-central1.run.app/api/compras", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(datos)
  })
  .then(res => {
    console.log("Status:", res.status);
    return res.text();
  })
  .then(data => {
    console.log("Respuesta backend:", data);
    alert("Compra guardada en BD ✅");
    carrito = [];
    mostrarCarrito();
  })
  .catch(err => {
    console.error(err);
    alert("Error al guardar compra ❌");
  });
}

// 🚀 INIT
window.onload = function () {
  cargarProductos();
};