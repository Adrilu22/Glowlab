// ═══════════════════════════════════════════
// ENDPOINTS
// ═══════════════════════════════════════════
const API_PRODUCTOS  = "https://api-skincare-v2-994118614969.us-central1.run.app/api/productos";
const API_CATEGORIAS = "https://api-skincare-v2-994118614969.us-central1.run.app/api/categorias";

// ═══════════════════════════════════════════
// USUARIOS (credenciales hardcoded HU-05)
// ═══════════════════════════════════════════
const USUARIOS = {
  admin: { password: "admin123", rol: "Admin",   nombre: "Admin" },
  user:  { password: "user123",  rol: "Usuario", nombre: "User"  }
};

let usuarioActual = null;
let carrito = [];

// ═══════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════
function iniciarSesion() {
  const username = document.getElementById("inputUsuario").value.trim().toLowerCase();
  const password = document.getElementById("inputPassword").value;
  const errorEl  = document.getElementById("loginError");

  const user = USUARIOS[username];

  if (!user || user.password !== password) {
    errorEl.textContent = "Usuario o contraseña incorrectos.";
    return;
  }

  errorEl.textContent = "";
  usuarioActual = { username, ...user };

  document.getElementById("loginScreen").classList.add("hidden");
  document.getElementById("mainApp").classList.remove("hidden");

  renderUI();
  cargarProductos();
}

function cerrarSesion() {
  usuarioActual = null;
  carrito = [];
  document.getElementById("mainApp").classList.add("hidden");
  document.getElementById("loginScreen").classList.remove("hidden");
  document.getElementById("inputUsuario").value = "";
  document.getElementById("inputPassword").value = "";
  mostrarSeccion("rutinas");
}

// ═══════════════════════════════════════════
// RENDERIZADO CONDICIONAL POR ROL
// ═══════════════════════════════════════════
function renderUI() {
  const esAdmin = usuarioActual.rol === "Admin";

  // Iniciales en avatar (primeras 2 letras del username en mayúsculas)
  const iniciales = usuarioActual.username.substring(0, 2).toUpperCase();
  document.getElementById("userAvatar").textContent = iniciales;
  document.getElementById("userName").textContent   = usuarioActual.nombre;
  document.getElementById("userRole").textContent   = usuarioActual.rol;

  // Pestaña "Categorías" solo visible para admin
  const tabCategorias = document.getElementById("tabCategorias");
  if (esAdmin) {
    tabCategorias.classList.remove("hidden");
  } else {
    tabCategorias.classList.add("hidden");
  }

  // Botón "Agregar producto" desactivado para usuarios estándar
  const btnAgregar = document.getElementById("btnAgregarProducto");
  btnAgregar.disabled = !esAdmin;
  btnAgregar.title = esAdmin ? "" : "Solo los administradores pueden agregar productos";
}

// ═══════════════════════════════════════════
// NAVEGACIÓN POR TABS
// ═══════════════════════════════════════════
function mostrarSeccion(nombre) {
  ["secRutinas", "secProductos", "secCategorias"].forEach(id => {
    document.getElementById(id).classList.add("hidden");
  });

  document.querySelectorAll(".nav-tab").forEach(tab => {
    tab.classList.toggle("active", tab.dataset.section === nombre);
  });

  document.getElementById("sec" + capitalizar(nombre)).classList.remove("hidden");

  if (nombre === "categorias") cargarCategorias();
}

function capitalizar(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

// ═══════════════════════════════════════════
// IMÁGENES
// ═══════════════════════════════════════════
function imagenProducto(nombre) {
  const n = nombre.toLowerCase();
  if (n.includes("gel"))      return "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=400&q=80";
  if (n.includes("crema"))    return "https://images.unsplash.com/photo-1608248597279-f99d160bfcbc?auto=format&fit=crop&w=400&q=80";
  if (n.includes("vitamina")) return "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&w=400&q=80";
  if (n.includes("protector"))return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRXGPYjyqudAP0rGGZMpu2Z98-3_L2Bn7P_sg&s";
  if (n.includes("serum"))    return "https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?auto=format&fit=crop&w=400&q=80";
  return "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=400&q=80";
}

// ═══════════════════════════════════════════
// RUTINAS
// ═══════════════════════════════════════════
async function generarRutina() {
  const tipo     = document.getElementById("tipoPiel").value;
  const problema = document.getElementById("problema").value;

  try {
    const res  = await fetch(API_PRODUCTOS);
    const data = await res.json();

    let rutina = data.filter(p => {
      const nombre    = p.nombre.toLowerCase();
      const categoria = p.categoria?.nombre?.toLowerCase();
      return (
        (tipo === "grasa"  && nombre.includes("gel")) ||
        (tipo === "seca"   && nombre.includes("crema")) ||
        (tipo === "mixta"  && (nombre.includes("gel") || nombre.includes("crema"))) ||
        (problema === "manchas"    && (nombre.includes("vitamina") || categoria === "tratamiento")) ||
        (problema === "hidratacion"&& categoria === "hidratacion") ||
        (problema === "poros"      && (nombre.includes("serum")  || categoria === "tratamiento")) ||
        (problema === "acne"       && categoria === "limpieza")
      );
    });

    // eliminar duplicados
    rutina = [...new Map(rutina.map(p => [p.id, p])).values()];
    mostrarRutina(rutina);
  } catch {
    alert("No se pudo conectar con la API ❌");
  }
}

function mostrarRutina(lista) {
  const contenedor = document.getElementById("rutina");
  contenedor.innerHTML = lista.length === 0
    ? "<p style='padding:10px'>No se encontraron productos para tu selección.</p>"
    : lista.map(p => tarjetaProducto(p, true)).join("");
}

// ═══════════════════════════════════════════
// PRODUCTOS
// ═══════════════════════════════════════════
async function cargarProductos() {
  try {
    const res  = await fetch(API_PRODUCTOS);
    const data = await res.json();
    document.getElementById("productos").innerHTML = data.map(p => tarjetaProducto(p, false)).join("");
  } catch {
    document.getElementById("productos").innerHTML = "<p style='padding:10px'>Error al cargar productos ❌</p>";
  }
}

function tarjetaProducto(p, conCategoria) {
  return `
    <div class="card">
      <img src="${imagenProducto(p.nombre)}" alt="${p.nombre}">
      <h4>${p.nombre}</h4>
      <p class="precio">$${p.precio.toLocaleString()}</p>
      <button onclick='agregarCarrito(${JSON.stringify(p)})'>Añadir</button>
      ${conCategoria ? `<button onclick="verCategoria(${p.id})">Ver categoría</button>` : ""}
    </div>`;
}

async function verCategoria(id) {
  try {
    const res      = await fetch(`${API_PRODUCTOS}/${id}/categoria`);
    const categoria = await res.json();
    alert("Categoría: " + categoria.nombre);
  } catch {
    alert("Error al obtener categoría ❌");
  }
}

// ═══════════════════════════════════════════
// CATEGORÍAS (admin)
// ═══════════════════════════════════════════
async function cargarCategorias() {
  try {
    const res  = await fetch(API_CATEGORIAS);
    const data = await res.json();
    document.getElementById("listaCategorias").innerHTML = data.map(c => `
      <div class="categoria-card">
        <span class="cat-icono">${c.icono || "🏷️"}</span>
        <h4>${c.nombre}</h4>
        <p>${c.descripcion || ""}</p>
      </div>`).join("");
  } catch {
    document.getElementById("listaCategorias").innerHTML = "<p style='padding:10px'>Error al cargar categorías ❌</p>";
  }
}

// ═══════════════════════════════════════════
// CARRITO
// ═══════════════════════════════════════════
function agregarCarrito(producto) {
  carrito.push(producto);
  mostrarCarrito();
}

function mostrarCarrito() {
  const contenedor = document.getElementById("carrito");
  const totalEl    = document.getElementById("total");
  let total = 0;

  contenedor.innerHTML = carrito.map((p, i) => {
    total += p.precio;
    return `
      <div class="card">
        <h4>${p.nombre}</h4>
        <p class="precio">$${p.precio.toLocaleString()}</p>
        <button onclick="eliminarProducto(${i})">Eliminar</button>
      </div>`;
  }).join("");

  totalEl.innerText = "$" + total.toLocaleString();
}

function eliminarProducto(index) {
  carrito.splice(index, 1);
  mostrarCarrito();
}

// ═══════════════════════════════════════════
// COMPRA
// ═══════════════════════════════════════════
function finalizarCompra() {
  if (carrito.length === 0) {
    alert("El carrito está vacío ❌");
    return;
  }

  const datos = carrito.map(p => ({ productoId: parseInt(p.id) }));

  fetch("https://api-skincare-v2-994118614969.us-central1.run.app/api/compras", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(datos)
  })
  .then(res => res.text())
  .then(() => {
    alert("Compra guardada en BD ✅");
    carrito = [];
    mostrarCarrito();
  })
  .catch(() => alert("Error al guardar compra ❌"));
}
