const state = {
    page: 1,
    size: 6,
    total: 0,
    artists: []
};

const $ = (id) => document.getElementById(id);

async function fetchJson(url) {
    const response = await fetch(url);
    const data = await response.json();
    return { response, data };
}

function artistCard(a) {
    const years = `${a.birthYear}–${a.deathYear ?? "present"}`;
    return `
        <a class="artist-card" href="/artist?id=${a.id}">
            <img src="${a.portrait}" alt="Portrait illustration of ${a.name}">
            <div>
                <h3>${a.name}</h3>
                <div class="meta">${years}<br>${a.country}</div>
                <span class="tag">${a.movement}</span>
            </div>
        </a>`;
}

function paintingCard(p) {
    return `
        <a class="painting-card" href="/painting?id=${p.id}">
            <img src="${p.image}" alt="Illustration for ${p.title}">
            <div class="painting-card-body">
                <h3>${p.title}</h3>
                <div class="painting-card-row"><span>${p.artistName}</span><strong>${p.year}</strong></div>
                <span class="tag">${p.genre}</span>
            </div>
        </a>`;
}

async function loadArtists(search = "") {
    const url = `/api/artists${search ? `?search=${encodeURIComponent(search)}` : ""}`;
    const { data } = await fetchJson(url);
    state.artists = data;
    $("artist-grid").innerHTML = data.length ? data.map(artistCard).join("") : `<div class="empty">No artists found.</div>`;
    $("artist-count").textContent = `${data.length} artist${data.length === 1 ? "" : "s"}`;

    if (!search) {
        const filter = $("artist-filter");
        filter.innerHTML = `<option value="">All artists</option>` + data.map(a => `<option value="${a.id}">${a.name}</option>`).join("");
    }
}

function paintingQuery() {
    const params = new URLSearchParams();
    const search = $("painting-search").value.trim();
    const artistId = $("artist-filter").value;
    const yearFrom = $("year-from").value;
    const yearTo = $("year-to").value;
    const sort = $("sort").value;

    if (search) params.set("search", search);
    if (artistId) params.set("artistId", artistId);
    if (yearFrom) params.set("yearFrom", yearFrom);
    if (yearTo) params.set("yearTo", yearTo);
    params.set("sort", sort);
    params.set("page", state.page);
    params.set("size", state.size);
    return params.toString();
}

async function loadPaintings() {
    const { response, data } = await fetchJson(`/api/paintings?${paintingQuery()}`);
    state.total = Number(response.headers.get("X-Total-Count") || 0);

    $("painting-grid").innerHTML = data.length
        ? data.map(paintingCard).join("")
        : `<div class="empty">No paintings found for the selected filters.</div>`;

    $("painting-count").textContent = `${state.total} result${state.total === 1 ? "" : "s"}`;
    renderPagination();
}

function renderPagination() {
    const totalPages = Math.max(1, Math.ceil(state.total / state.size));
    const buttons = [];
    buttons.push(`<button class="page-btn" data-page="${state.page - 1}" ${state.page <= 1 ? "disabled" : ""}>Previous</button>`);
    for (let i = 1; i <= totalPages; i++) {
        buttons.push(`<button class="page-btn ${i === state.page ? "active" : ""}" data-page="${i}">${i}</button>`);
    }
    buttons.push(`<button class="page-btn" data-page="${state.page + 1}" ${state.page >= totalPages ? "disabled" : ""}>Next</button>`);
    $("pagination").innerHTML = buttons.join("");

    $("pagination").querySelectorAll("button[data-page]").forEach(button => {
        button.addEventListener("click", () => {
            if (button.disabled) return;
            state.page = Number(button.dataset.page);
            loadPaintings();
            document.getElementById("paintings").scrollIntoView({ behavior: "smooth" });
        });
    });
}

$("artist-search-btn").addEventListener("click", () => loadArtists($("artist-search").value.trim()));
$("artist-search").addEventListener("keydown", (e) => {
    if (e.key === "Enter") loadArtists($("artist-search").value.trim());
});

$("apply-filters").addEventListener("click", () => {
    loadPaintings();
});

$("reset-filters").addEventListener("click", () => {
    $("painting-search").value = "";
    $("artist-filter").value = "";
    $("year-from").value = "";
    $("year-to").value = "";
    $("sort").value = "title-asc";
    state.page = 1;
    loadPaintings();
});

$("page-size").addEventListener("change", () => {
    state.size = Number($("page-size").value);
    state.page = 1;
    loadPaintings();
});

Promise.all([loadArtists(), loadPaintings()]).catch(error => {
    console.error(error);
    $("painting-grid").innerHTML = `<div class="empty">Failed to load data.</div>`;
});
