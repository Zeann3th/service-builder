const movieGrid       = document.querySelector("#movieGrid");
const statusBox       = document.querySelector("#status");
const refreshButton   = document.querySelector("#refreshButton");
const form            = document.querySelector("#reservationForm");
const emptySelection  = document.querySelector("#selectionEmpty");
const selectedTitle   = document.querySelector("#selectedTitle");
const selectedMeta    = document.querySelector("#selectedMeta");
const ticketQuantity  = document.querySelector("#ticketQuantity");
const reserveButton   = document.querySelector("#reserveButton");
const resultBox       = document.querySelector("#reservationResult");
const qtyDown         = document.querySelector("#qtyDown");
const qtyUp           = document.querySelector("#qtyUp");

let movies   = [];
let selected = null;

/* ── Genre color map ────────────────────────────────── */
const GENRES = {
  "sci-fi":     { bg: "rgba(59,130,246,.14)",  color: "#93c5fd", border: "rgba(59,130,246,.28)",  poster: "linear-gradient(140deg,#060e24 0%,#0f2248 55%,#071630 100%)" },
  "animation":  { bg: "rgba(245,158,11,.14)",  color: "#fcd34d", border: "rgba(245,158,11,.28)",  poster: "linear-gradient(140deg,#1e0e00 0%,#5a2a00 55%,#160a00 100%)" },
  "drama":      { bg: "rgba(139,92,246,.14)",  color: "#c4b5fd", border: "rgba(139,92,246,.28)",  poster: "linear-gradient(140deg,#0e0620 0%,#2e1254 55%,#090418 100%)" },
  "action":     { bg: "rgba(239,68,68,.14)",   color: "#fca5a5", border: "rgba(239,68,68,.28)",   poster: "linear-gradient(140deg,#1a0606 0%,#4a1010 55%,#120404 100%)" },
  "comedy":     { bg: "rgba(34,197,94,.14)",   color: "#86efac", border: "rgba(34,197,94,.28)",   poster: "linear-gradient(140deg,#041408 0%,#0e3820 55%,#020d06 100%)" },
  "horror":     { bg: "rgba(71,85,105,.14)",   color: "#94a3b8", border: "rgba(71,85,105,.28)",   poster: "linear-gradient(140deg,#050506 0%,#111118 55%,#030304 100%)" },
  "romance":    { bg: "rgba(236,72,153,.14)",  color: "#f9a8d4", border: "rgba(236,72,153,.28)",  poster: "linear-gradient(140deg,#180612 0%,#4a1036 55%,#100408 100%)" },
  "thriller":   { bg: "rgba(100,116,139,.14)", color: "#cbd5e1", border: "rgba(100,116,139,.28)", poster: "linear-gradient(140deg,#0a0a10 0%,#181828 55%,#070710 100%)" },
};

function genreStyle(genre) {
  const key = (genre || "").toLowerCase().replace(/[^a-z-]/g, "");
  return GENRES[key] || { bg: "rgba(99,102,241,.12)", color: "#a5b4fc", border: "rgba(99,102,241,.28)", poster: "linear-gradient(140deg,#0a0a18 0%,#18183a 55%,#060612 100%)" };
}

/* ── Helpers ────────────────────────────────────────── */
function showStatus(msg, isError = false) {
  statusBox.hidden = !msg;
  statusBox.textContent = msg || "";
  statusBox.className = isError ? "status-banner error" : "status-banner";
}

function showResult(html, type) {
  if (!html) { resultBox.hidden = true; return; }
  resultBox.hidden = false;
  resultBox.innerHTML = html;
  resultBox.className = type;
}

function fmt(value) {
  return new Intl.DateTimeFormat("vi-VN", {
    month: "short", day: "2-digit",
    hour: "2-digit", minute: "2-digit", hour12: false
  }).format(new Date(value));
}

function vnd(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency", currency: "VND", maximumFractionDigits: 0
  }).format(value || 0);
}

function seatsInfo(avail, total) {
  if (avail === 0) return { label: "Sold out", cls: "seats-none" };
  if (avail / total < 0.25) return { label: `${avail} left!`, cls: "seats-low" };
  return { label: `${avail}/${total}`, cls: "seats-ok" };
}

/* ── Skeleton loading ───────────────────────────────── */
function renderSkeletons() {
  movieGrid.innerHTML = Array.from({ length: 3 }, () => `
    <article class="movie-card">
      <div class="skeleton" style="height:170px;border-radius:0"></div>
      <div class="movie-body">
        <div class="skeleton" style="height:14px;width:65%;margin-bottom:12px"></div>
        <div class="skeleton" style="height:12px;width:90%;margin-bottom:6px"></div>
        <div class="skeleton" style="height:12px;width:55%"></div>
      </div>
    </article>`).join("");
}

/* ── Render movies ──────────────────────────────────── */
function renderMovies() {
  movieGrid.innerHTML = "";
  for (const movie of movies) {
    const g = genreStyle(movie.genre);
    const card = document.createElement("article");
    card.className = "movie-card";

    const showtimeHTML = (movie.showtimes || []).map(st => {
      const isActive = selected?.showtime.id === st.id;
      const { label, cls } = seatsInfo(st.availableCapacity, st.totalCapacity);
      return `
        <button type="button" class="showtime${isActive ? " active" : ""}"
                data-id="${st.id}"${st.availableCapacity < 1 ? " disabled" : ""}>
          <div class="st-left">
            <div class="st-time">${fmt(st.startsAt)} &middot; ${vnd(st.price)}</div>
            <div class="st-room">${st.roomName}</div>
          </div>
          <div class="st-seats ${cls}">${label}</div>
        </button>`;
    }).join("");

    card.innerHTML = `
      <div class="poster" style="background:${g.poster}">
        <div class="poster-bg"></div>
        <div class="poster-shade"></div>
        <div class="poster-title">${movie.title}</div>
      </div>
      <div class="movie-body">
        <div class="badges">
          <span class="badge genre" style="--g-bg:${g.bg};--g-color:${g.color};--g-border:${g.border}">${movie.genre}</span>
          <span class="badge rating">${movie.rating}</span>
          <span class="badge duration">${movie.durationMinutes}m</span>
        </div>
        <p class="synopsis">${movie.synopsis || ""}</p>
        ${showtimeHTML ? `<p class="showtimes-label">Showtimes</p><div class="showtimes">${showtimeHTML}</div>` : ""}
      </div>`;

    card.querySelectorAll(".showtime:not([disabled])").forEach(btn => {
      const id = Number(btn.dataset.id);
      const st = (movie.showtimes || []).find(s => s.id === id);
      if (st) btn.addEventListener("click", () => selectShowtime(movie, st));
    });

    movieGrid.appendChild(card);
  }
}

function selectShowtime(movie, showtime) {
  selected = { movie, showtime };
  emptySelection.hidden = true;
  form.hidden = false;
  selectedTitle.textContent = movie.title;
  selectedMeta.textContent =
    `${fmt(showtime.startsAt)} · ${showtime.roomName} · ${showtime.availableCapacity} seats left`;
  ticketQuantity.max = showtime.availableCapacity;
  ticketQuantity.value = Math.min(Number(ticketQuantity.value || 1), showtime.availableCapacity);
  showResult(null);
  renderMovies();
}

/* ── Load movies ────────────────────────────────────── */
async function loadMovies() {
  showStatus("Loading movies…");
  renderSkeletons();
  try {
    const res = await fetch("/api/v1/cinema/movies");
    const payload = await res.json();
    if (payload.code !== "0000") throw new Error(payload.message || "Failed to load movies");
    movies = payload.data || [];
    showStatus("");
    renderMovies();
  } catch (err) {
    showStatus(err.message, true);
    movieGrid.innerHTML = "";
  }
}

/* ── Reserve ────────────────────────────────────────── */
const RESERVE_ICON = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/></svg>`;

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!selected) return;

  const origHTML = reserveButton.innerHTML;
  reserveButton.disabled = true;
  reserveButton.textContent = "Reserving…";
  showResult(null);

  try {
    const res = await fetch("/api/v1/cinema/reservations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        showtimeId: selected.showtime.id,
        quantity: Number(ticketQuantity.value)
      })
    });
    const payload = await res.json();
    if (payload.code !== "0000") throw new Error(payload.message || "Reservation failed");

    const r = payload.data;
    showResult(`
      <div class="ticket-id">Booking #${r.reservationId}</div>
      <div class="ticket-detail">Seats: ${r.allocatedSeats.join(", ")}</div>
      <div class="ticket-detail">${r.remainingCapacity} seats remaining</div>
    `, "success");

    await loadMovies();
  } catch (err) {
    showResult(`${err.message}`, "error");
  } finally {
    reserveButton.disabled = false;
    reserveButton.innerHTML = origHTML;
  }
});

/* ── Qty stepper ────────────────────────────────────── */
qtyDown.addEventListener("click", () => {
  const v = Number(ticketQuantity.value);
  if (v > 1) ticketQuantity.value = v - 1;
});

qtyUp.addEventListener("click", () => {
  const v = Number(ticketQuantity.value);
  const max = Number(ticketQuantity.max) || 99;
  if (v < max) ticketQuantity.value = v + 1;
});

refreshButton.addEventListener("click", loadMovies);
loadMovies();
