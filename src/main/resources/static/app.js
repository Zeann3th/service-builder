const movieGrid = document.querySelector("#movieGrid");
const statusBox = document.querySelector("#status");
const refreshButton = document.querySelector("#refreshButton");
const form = document.querySelector("#reservationForm");
const emptySelection = document.querySelector("#selectionEmpty");
const selectedTitle = document.querySelector("#selectedTitle");
const selectedMeta = document.querySelector("#selectedMeta");
const ticketQuantity = document.querySelector("#ticketQuantity");
const reserveButton = document.querySelector("#reserveButton");
const reservationResult = document.querySelector("#reservationResult");

let movies = [];
let selected = null;

function showStatus(message, isError = false) {
  statusBox.hidden = !message;
  statusBox.textContent = message || "";
  statusBox.className = isError ? "status error" : "status";
}

function showResult(message, isError = false) {
  reservationResult.hidden = !message;
  reservationResult.innerHTML = message || "";
  reservationResult.className = isError ? "result error" : "result success";
}

function formatDate(value) {
  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

function money(value) {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(value || 0);
}

function renderMovies() {
  movieGrid.innerHTML = "";
  for (const movie of movies) {
    const card = document.createElement("article");
    card.className = "movie-card";
    const showtimes = movie.showtimes || [];
    card.innerHTML = `
      <div class="poster"><strong>${movie.title}</strong></div>
      <div class="movie-body">
        <h2>${movie.title}</h2>
        <div class="meta">
          <span>${movie.genre}</span>
          <span>${movie.rating}</span>
          <span>${movie.durationMinutes} min</span>
        </div>
        <p class="synopsis">${movie.synopsis || ""}</p>
        <div class="showtimes"></div>
      </div>
    `;
    const showtimeList = card.querySelector(".showtimes");
    for (const showtime of showtimes) {
      const button = document.createElement("button");
      button.type = "button";
      button.className = selected?.showtime.id === showtime.id ? "showtime active" : "showtime";
      button.disabled = showtime.availableCapacity < 1;
      button.innerHTML = `
        <strong>${formatDate(showtime.startsAt)} - ${showtime.roomName}</strong>
        <span>${showtime.availableCapacity}/${showtime.totalCapacity} seats - ${money(showtime.price)}</span>
      `;
      button.addEventListener("click", () => selectShowtime(movie, showtime));
      showtimeList.appendChild(button);
    }
    movieGrid.appendChild(card);
  }
}

function selectShowtime(movie, showtime) {
  selected = { movie, showtime };
  emptySelection.hidden = true;
  form.hidden = false;
  selectedTitle.textContent = movie.title;
  selectedMeta.textContent = `${formatDate(showtime.startsAt)} - ${showtime.roomName} - ${showtime.availableCapacity} seats left`;
  ticketQuantity.max = showtime.availableCapacity;
  ticketQuantity.value = Math.min(Number(ticketQuantity.value || 1), showtime.availableCapacity);
  showResult("");
  renderMovies();
}

async function loadMovies() {
  showStatus("Loading movies...");
  try {
    const response = await fetch("/api/v1/cinema/movies");
    const payload = await response.json();
    if (payload.code !== "0000") {
      throw new Error(payload.message || "Failed to load movies");
    }
    movies = payload.data || [];
    showStatus("");
    renderMovies();
  } catch (error) {
    showStatus(error.message, true);
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!selected) {
    return;
  }
  reserveButton.disabled = true;
  showResult("");
  try {
    const response = await fetch("/api/v1/cinema/reservations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        showtimeId: selected.showtime.id,
        quantity: Number(ticketQuantity.value)
      })
    });
    const payload = await response.json();
    if (payload.code !== "0000") {
      throw new Error(payload.message || "Reservation failed");
    }
    const result = payload.data;
    showResult(`
      <strong>Reservation #${result.reservationId}</strong><br>
      Seats: ${result.allocatedSeats.join(", ")}<br>
      Remaining capacity: ${result.remainingCapacity}
    `);
    await loadMovies();
  } catch (error) {
    showResult(error.message, true);
  } finally {
    reserveButton.disabled = false;
  }
});

refreshButton.addEventListener("click", loadMovies);
loadMovies();
