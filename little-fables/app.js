const BOOKINGS_KEY = "little-fables-bookings-v1";
const KDS_TOKEN_KEY = "little-fables-kds-token-v2";
const KDS_ADMIN_KEY = "little-fables-kds-admin-v2";
const KDS_POLL_MS = 10000;

const TIME_SLOTS = [
  "17:00",
  "17:30",
  "18:00",
  "18:30",
  "19:00",
  "19:30",
  "20:00",
  "20:30",
  "21:00",
  "21:30"
];

const STAGES = [
  { id: "todo", label: "To accept", status: 2, color: "#c64c37" },
  { id: "accepted", label: "Accepted", status: 3, color: "#e1b84c" },
  { id: "delivering", label: "Delivering", status: 4, color: "#315b68" }
];

const state = {
  availabilityNonce: 0,
  selectedSlot: null,
  currentBooking: null,
  currentView: "site",
  draggedTicketId: null,
  kdsToken: null,
  kdsAdminName: "",
  kdsOrders: [],
  kdsLoading: false,
  kdsError: "",
  kdsPendingOrderId: null
};

const elements = {
  availabilityMessage: document.querySelector("#availabilityMessage"),
  availabilityStatus: document.querySelector("#availabilityStatus"),
  availabilityStep: document.querySelector("#availabilityStep"),
  backToAvailability: document.querySelector("#backToAvailability"),
  bookingConsent: document.querySelector("#bookingConsent"),
  confirmationCode: document.querySelector("#confirmationCode"),
  confirmationLead: document.querySelector("#confirmationLead"),
  confirmationStep: document.querySelector("#confirmationStep"),
  confirmationSummary: document.querySelector("#confirmationSummary"),
  continueToDetails: document.querySelector("#continueToDetails"),
  detailsStatus: document.querySelector("#detailsStatus"),
  detailsStep: document.querySelector("#detailsStep"),
  footerYear: document.querySelector("#footerYear"),
  guestEmail: document.querySelector("#guestEmail"),
  guestName: document.querySelector("#guestName"),
  guestNotes: document.querySelector("#guestNotes"),
  guestPhone: document.querySelector("#guestPhone"),
  kdsAdminName: document.querySelector("#kdsAdminName"),
  kdsBoard: document.querySelector("#kdsBoard"),
  kdsConnectionState: document.querySelector("#kdsConnectionState"),
  kdsDateLine: document.querySelector("#kdsDateLine"),
  kdsLiveDot: document.querySelector("#kdsLiveDot"),
  kdsLoginButton: document.querySelector("#kdsLoginButton"),
  kdsLoginForm: document.querySelector("#kdsLoginForm"),
  kdsLogoutButton: document.querySelector("#kdsLogoutButton"),
  kdsPassword: document.querySelector("#kdsPassword"),
  kdsRefreshButton: document.querySelector("#kdsRefreshButton"),
  kdsSessionUser: document.querySelector("#kdsSessionUser"),
  kdsUsername: document.querySelector("#kdsUsername"),
  kdsView: document.querySelector("#kdsView"),
  manageCode: document.querySelector("#manageCode"),
  manageDialog: document.querySelector("#manageDialog"),
  managedBooking: document.querySelector("#managedBooking"),
  menuToggle: document.querySelector("#menuToggle"),
  occasion: document.querySelector("#occasion"),
  openTicketCount: document.querySelector("#openTicketCount"),
  guestCount: document.querySelector("#guestCount"),
  nextTable: document.querySelector("#nextTable"),
  partySize: document.querySelector("#partySize"),
  refreshAvailability: document.querySelector("#refreshAvailability"),
  reservationDate: document.querySelector("#reservationDate"),
  reservationForm: document.querySelector("#reservationForm"),
  seatingPreference: document.querySelector("#seatingPreference"),
  selectedSlotSummary: document.querySelector("#selectedSlotSummary"),
  siteView: document.querySelector("#siteView"),
  siteFooter: document.querySelector("#siteFooter"),
  siteNav: document.querySelector("#siteNav"),
  timeGrid: document.querySelector("#timeGrid"),
  toast: document.querySelector("#toast")
};

function initialize() {
  elements.footerYear.textContent = new Date().getFullYear();
  configureDateInput();
  bindNavigation();
  bindReservationFlow();
  bindKds();
  initializeKds();
  routeFromHash();
  refreshIcons();
}

function configureDateInput() {
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);

  const maxDate = new Date(today);
  maxDate.setDate(today.getDate() + 60);

  elements.reservationDate.min = toDateInputValue(today);
  elements.reservationDate.max = toDateInputValue(maxDate);
  elements.reservationDate.value = toDateInputValue(tomorrow);
}

function bindNavigation() {
  document.addEventListener("click", (event) => {
    const viewTrigger = event.target.closest("[data-view]");
    if (viewTrigger) {
      event.preventDefault();
      const nextView = viewTrigger.dataset.view === "kds" ? "kds" : "site";
      history.pushState(null, "", nextView === "kds" ? "#kds" : "#home");
      setView(nextView);
      return;
    }

    const openManage = event.target.closest("[data-open-manage]");
    if (openManage) {
      elements.manageDialog.showModal();
      elements.manageCode.focus();
      refreshIcons();
    }
  });

  window.addEventListener("hashchange", routeFromHash);
  window.addEventListener("popstate", routeFromHash);
  window.addEventListener("scroll", handleScroll, { passive: true });

  elements.menuToggle.addEventListener("click", () => {
    const isOpen = elements.siteNav.classList.toggle("open");
    elements.menuToggle.setAttribute("aria-expanded", String(isOpen));
  });

  elements.siteNav.addEventListener("click", (event) => {
    if (event.target.closest("a")) {
      elements.siteNav.classList.remove("open");
      elements.menuToggle.setAttribute("aria-expanded", "false");
    }
  });
}

function routeFromHash() {
  setView(window.location.hash === "#kds" ? "kds" : "site");
}

function setView(view) {
  state.currentView = view;
  const isKds = view === "kds";

  elements.siteView.hidden = isKds;
  elements.siteFooter.hidden = isKds;
  elements.kdsView.hidden = !isKds;
  document.body.classList.toggle("kds-mode", isKds);
  elements.siteNav.classList.remove("open");
  elements.menuToggle.setAttribute("aria-expanded", "false");
  handleScroll();

  if (isKds) {
    renderKds();
    loadKdsOrders({ silent: true });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  refreshIcons();
}

function handleScroll() {
  document.querySelector("#topbar").classList.toggle(
    "scrolled",
    window.scrollY > 40 || state.currentView === "kds"
  );
}

function bindReservationFlow() {
  elements.reservationDate.addEventListener("change", resetAvailability);
  elements.partySize.addEventListener("change", resetAvailability);
  elements.refreshAvailability.addEventListener("click", () => {
    state.availabilityNonce += 1;
    renderAvailability();
    showToast("Availability refreshed.");
  });

  elements.continueToDetails.addEventListener("click", () => {
    if (!state.selectedSlot) {
      elements.availabilityStatus.textContent = "Choose an available time to continue.";
      return;
    }

    elements.availabilityStatus.textContent = "";
    elements.availabilityStep.hidden = true;
    elements.detailsStep.hidden = false;
    setStepIndicator(2);
    renderSelectedSlotSummary();
    elements.guestName.focus();
  });

  elements.backToAvailability.addEventListener("click", () => {
    elements.detailsStep.hidden = true;
    elements.availabilityStep.hidden = false;
    setStepIndicator(1);
  });

  elements.reservationForm.addEventListener("submit", handleReservationSubmit);
  document.querySelector("#addToCalendar").addEventListener("click", addCurrentBookingToCalendar);
  document.querySelector("#modifyReservation").addEventListener("click", modifyCurrentBooking);
  document.querySelector("#cancelReservation").addEventListener("click", cancelCurrentBooking);
  document.querySelector("#findBooking").addEventListener("click", findManagedBooking);

  elements.manageCode.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      findManagedBooking();
    }
  });
}

function resetAvailability() {
  state.selectedSlot = null;
  elements.timeGrid.innerHTML = "";
  elements.availabilityStatus.textContent = "";
  renderAvailability();
}

function renderAvailability() {
  const date = elements.reservationDate.value;
  const partySize = Number(elements.partySize.value);

  if (!date || !partySize) {
    elements.availabilityMessage.textContent = "Select a date and party size to see times.";
    elements.timeGrid.innerHTML = "";
    return;
  }

  if (partySize === 11) {
    elements.availabilityMessage.textContent = "For 11 or more guests, our events team can arrange the room.";
    elements.timeGrid.innerHTML = "";
    return;
  }

  elements.availabilityMessage.textContent = `${formatLongDate(date)} · ${partySize} ${partySize === 1 ? "guest" : "guests"}`;

  const slots = TIME_SLOTS.map((time) => ({
    time,
    status: availabilityFor(date, time, partySize)
  }));

  elements.timeGrid.innerHTML = slots
    .map(({ time, status }) => {
      const disabled = status !== "available";
      const label = status === "available" ? "Available" : "Waitlist";
      return `
        <button
          class="time-slot"
          type="button"
          role="radio"
          aria-checked="false"
          data-time="${time}"
          ${disabled ? "disabled" : ""}
        >
          <span>${formatTime(time)}</span>
          <small>${label}</small>
        </button>
      `;
    })
    .join("");

  elements.timeGrid.querySelectorAll(".time-slot:not(:disabled)").forEach((button) => {
    button.addEventListener("click", () => selectTimeSlot(button));
  });
}

function availabilityFor(date, time, partySize) {
  const seed = hashString(`${date}|${time}|${partySize}|${state.availabilityNonce}`);
  const score = seed % 100;

  if (partySize >= 7 && score < 32) {
    return "waitlist";
  }

  if (partySize >= 5 && score < 18) {
    return "waitlist";
  }

  return score < 25 ? "waitlist" : "available";
}

function selectTimeSlot(button) {
  elements.timeGrid.querySelectorAll(".time-slot").forEach((slot) => {
    slot.classList.remove("selected");
    slot.setAttribute("aria-checked", "false");
  });

  button.classList.add("selected");
  button.setAttribute("aria-checked", "true");
  state.selectedSlot = button.dataset.time;
  elements.availabilityStatus.textContent = "";
}

function renderSelectedSlotSummary() {
  const date = elements.reservationDate.value;
  const partySize = Number(elements.partySize.value);

  elements.selectedSlotSummary.innerHTML = `
    <span class="chip"><i data-lucide="calendar-days" aria-hidden="true"></i>${escapeHtml(formatLongDate(date))}</span>
    <span class="chip"><i data-lucide="clock-3" aria-hidden="true"></i>${escapeHtml(formatTime(state.selectedSlot))}</span>
    <span class="chip"><i data-lucide="users" aria-hidden="true"></i>${partySize} ${partySize === 1 ? "guest" : "guests"}</span>
    <span class="chip"><i data-lucide="armchair" aria-hidden="true"></i>${escapeHtml(elements.seatingPreference.value)}</span>
  `;
  refreshIcons();
}

function handleReservationSubmit(event) {
  event.preventDefault();
  clearFormErrors();

  const validationErrors = validateGuestDetails();
  if (validationErrors.length > 0) {
    elements.detailsStatus.textContent = "Review the highlighted fields.";
    return;
  }

  const booking = {
    code: state.currentBooking?.code || generateConfirmationCode(),
    date: elements.reservationDate.value,
    time: state.selectedSlot,
    partySize: Number(elements.partySize.value),
    seating: elements.seatingPreference.value,
    name: elements.guestName.value.trim(),
    phone: elements.guestPhone.value.trim(),
    email: elements.guestEmail.value.trim(),
    occasion: elements.occasion.value,
    notes: elements.guestNotes.value.trim(),
    createdAt: state.currentBooking?.createdAt || new Date().toISOString()
  };

  saveBooking(booking);
  state.currentBooking = booking;
  renderConfirmation(booking, Boolean(state.currentBooking && elements.guestName.dataset.wasModifying === "true"));
  elements.detailsStep.hidden = true;
  elements.confirmationStep.hidden = false;
  setStepIndicator(3);
  elements.confirmationStep.scrollIntoView({ behavior: "smooth", block: "center" });
  showToast("Reservation confirmed.");
}

function validateGuestDetails() {
  const errors = [];
  const name = elements.guestName.value.trim();
  const phone = elements.guestPhone.value.trim();
  const email = elements.guestEmail.value.trim();

  if (name.length < 2) {
    setFieldError("guestName", "Please enter your full name.");
    errors.push("name");
  }

  if (phone.replace(/\D/g, "").length < 7) {
    setFieldError("guestPhone", "Please enter a valid phone number.");
    errors.push("phone");
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    setFieldError("guestEmail", "Please enter a valid email address.");
    errors.push("email");
  }

  if (!elements.bookingConsent.checked) {
    setFieldError("bookingConsent", "Please agree before confirming.");
    errors.push("consent");
  }

  return errors;
}

function setFieldError(fieldName, message) {
  const target = document.querySelector(`[data-error-for="${fieldName}"]`);
  if (target) {
    target.textContent = message;
  }
}

function clearFormErrors() {
  document.querySelectorAll(".field-error").forEach((element) => {
    element.textContent = "";
  });
  elements.detailsStatus.textContent = "";
}

function renderConfirmation(booking, wasModified) {
  elements.confirmationLead.textContent = wasModified
    ? "Your changes are saved and the kitchen has the latest details."
    : "The table is yours. We have sent the details to the restaurant.";
  elements.confirmationCode.textContent = booking.code;
  elements.confirmationSummary.innerHTML = `
    <div><span>When</span><strong>${escapeHtml(formatLongDate(booking.date))}<br>${escapeHtml(formatTime(booking.time))}</strong></div>
    <div><span>Party</span><strong>${booking.partySize} ${booking.partySize === 1 ? "guest" : "guests"}<br>${escapeHtml(booking.seating)}</strong></div>
    <div><span>Guest</span><strong>${escapeHtml(booking.name)}<br>${escapeHtml(booking.phone)}</strong></div>
    <div><span>Occasion</span><strong>${escapeHtml(booking.occasion)}<br>${escapeHtml(booking.notes || "No notes")}</strong></div>
  `;
}

function setStepIndicator(step) {
  document.querySelectorAll("[data-step-indicator]").forEach((indicator) => {
    const indicatorStep = Number(indicator.dataset.stepIndicator);
    indicator.classList.toggle("active", indicatorStep === step);
    indicator.classList.toggle("complete", indicatorStep < step);
  });
}

function modifyCurrentBooking() {
  if (!state.currentBooking) {
    return;
  }

  const booking = state.currentBooking;
  elements.reservationDate.value = booking.date;
  elements.partySize.value = String(booking.partySize);
  elements.seatingPreference.value = booking.seating;
  elements.guestName.value = booking.name;
  elements.guestPhone.value = booking.phone;
  elements.guestEmail.value = booking.email;
  elements.occasion.value = booking.occasion;
  elements.guestNotes.value = booking.notes || "";
  elements.bookingConsent.checked = true;
  elements.guestName.dataset.wasModifying = "true";
  state.selectedSlot = booking.time;

  elements.confirmationStep.hidden = true;
  elements.detailsStep.hidden = true;
  elements.availabilityStep.hidden = false;
  setStepIndicator(1);
  renderAvailability();

  requestAnimationFrame(() => {
    const selectedButton = elements.timeGrid.querySelector(`[data-time="${booking.time}"]`);
    if (selectedButton) {
      selectTimeSlot(selectedButton);
    }
  });

  document.querySelector("#reserve").scrollIntoView({ behavior: "smooth" });
  showToast("Edit the booking and confirm again.");
}

function cancelCurrentBooking() {
  if (!state.currentBooking) {
    return;
  }

  const confirmed = window.confirm("Cancel this reservation?");
  if (!confirmed) {
    return;
  }

  removeBooking(state.currentBooking.code);
  resetReservationForm();
  showToast("Reservation cancelled.");
}

function resetReservationForm() {
  state.currentBooking = null;
  state.selectedSlot = null;
  elements.reservationForm.reset();
  configureDateInput();
  elements.occasion.value = "Dinner";
  elements.seatingPreference.value = "No preference";
  elements.guestName.dataset.wasModifying = "false";
  elements.confirmationStep.hidden = true;
  elements.detailsStep.hidden = true;
  elements.availabilityStep.hidden = false;
  setStepIndicator(1);
  clearFormErrors();
  resetAvailability();
}

function addCurrentBookingToCalendar() {
  if (!state.currentBooking) {
    return;
  }

  const booking = state.currentBooking;
  const start = new Date(`${booking.date}T${booking.time}:00`);
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000);
  const calendar = [
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//Little Fables//Reservations//EN",
    "BEGIN:VEVENT",
    `UID:${booking.code}@littlefables.example`,
    `DTSTAMP:${toIcsDate(new Date())}`,
    `DTSTART:${toIcsDate(start)}`,
    `DTEND:${toIcsDate(end)}`,
    "SUMMARY:Reservation at Little Fables",
    "LOCATION:12 Rosemary Lane, North Quarter, NY 10014",
    `DESCRIPTION:${escapeIcs(`Confirmation ${booking.code}. Party of ${booking.partySize}.`)}`,
    "END:VEVENT",
    "END:VCALENDAR"
  ].join("\r\n");

  const blob = new Blob([calendar], { type: "text/calendar;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = `little-fables-${booking.code}.ics`;
  document.body.append(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
  showToast("Calendar file downloaded.");
}

function findManagedBooking() {
  const code = elements.manageCode.value.trim().toUpperCase();
  const booking = loadBookings().find((item) => item.code === code);

  if (!booking) {
    elements.managedBooking.hidden = false;
    elements.managedBooking.innerHTML = "<p>No reservation found with that code in this browser.</p>";
    return;
  }

  elements.managedBooking.hidden = false;
  elements.managedBooking.innerHTML = `
    <h3>${escapeHtml(booking.code)}</h3>
    <p>${escapeHtml(formatLongDate(booking.date))} at ${escapeHtml(formatTime(booking.time))}</p>
    <p>${booking.partySize} ${booking.partySize === 1 ? "guest" : "guests"} · ${escapeHtml(booking.name)}</p>
    <div class="managed-booking-actions">
      <button class="button button-primary" type="button" data-managed-action="modify">
        <i data-lucide="pencil" aria-hidden="true"></i>
        <span>Modify reservation</span>
      </button>
      <button class="button button-danger" type="button" data-managed-action="cancel">
        <i data-lucide="x" aria-hidden="true"></i>
        <span>Cancel reservation</span>
      </button>
    </div>
  `;

  elements.managedBooking.querySelector('[data-managed-action="modify"]').addEventListener("click", () => {
    state.currentBooking = booking;
    elements.manageDialog.close();
    modifyCurrentBooking();
  });

  elements.managedBooking.querySelector('[data-managed-action="cancel"]').addEventListener("click", () => {
    removeBooking(booking.code);
    elements.managedBooking.innerHTML = "<p>The reservation has been cancelled.</p>";
    if (state.currentBooking?.code === booking.code) {
      resetReservationForm();
    }
    showToast("Reservation cancelled.");
  });

  refreshIcons();
}

function loadBookings() {
  try {
    return JSON.parse(localStorage.getItem(BOOKINGS_KEY) || "[]");
  } catch {
    return [];
  }
}

function saveBooking(booking) {
  const bookings = loadBookings();
  const existingIndex = bookings.findIndex((item) => item.code === booking.code);

  if (existingIndex >= 0) {
    bookings[existingIndex] = booking;
  } else {
    bookings.push(booking);
  }

  localStorage.setItem(BOOKINGS_KEY, JSON.stringify(bookings));
}

function removeBooking(code) {
  const bookings = loadBookings().filter((booking) => booking.code !== code);
  localStorage.setItem(BOOKINGS_KEY, JSON.stringify(bookings));
}

function generateConfirmationCode() {
  const randomPart = Array.from(crypto.getRandomValues(new Uint8Array(2)))
    .map((value) => value.toString(16).padStart(2, "0"))
    .join("")
    .toUpperCase();
  return `LF-${randomPart}`;
}

function bindKds() {
  elements.kdsLoginForm.addEventListener("submit", loginKds);
  elements.kdsLogoutButton.addEventListener("click", () => logoutKds());
  elements.kdsRefreshButton.addEventListener("click", () => loadKdsOrders());

  elements.kdsBoard.addEventListener("click", (event) => {
    const actionButton = event.target.closest("[data-order-action]");
    if (actionButton) {
      performOrderAction(
        actionButton.dataset.orderId,
        actionButton.dataset.orderAction,
        actionButton
      );
      return;
    }
  });

  elements.kdsBoard.addEventListener("dragstart", (event) => {
    const ticket = event.target.closest(".ticket");
    if (!ticket) {
      return;
    }

    state.draggedTicketId = ticket.dataset.ticketId;
    ticket.classList.add("dragging");
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/plain", state.draggedTicketId);
  });

  elements.kdsBoard.addEventListener("dragend", (event) => {
    event.target.closest(".ticket")?.classList.remove("dragging");
    elements.kdsBoard.querySelectorAll(".kds-column").forEach((column) => {
      column.classList.remove("drag-over");
    });
    state.draggedTicketId = null;
  });

  elements.kdsBoard.addEventListener("dragover", (event) => {
    const column = event.target.closest(".kds-column");
    if (!column) {
      return;
    }

    event.preventDefault();
    column.classList.add("drag-over");
  });

  elements.kdsBoard.addEventListener("dragleave", (event) => {
    const column = event.target.closest(".kds-column");
    if (column && !column.contains(event.relatedTarget)) {
      column.classList.remove("drag-over");
    }
  });

  elements.kdsBoard.addEventListener("drop", (event) => {
    const column = event.target.closest(".kds-column");
    if (!column) {
      return;
    }

    event.preventDefault();
    const ticketId = event.dataTransfer.getData("text/plain") || state.draggedTicketId;
    const order = state.kdsOrders.find((item) => String(item.id) === String(ticketId));
    const stage = STAGES.find((item) => item.id === column.dataset.stage);
    if (order && stage) {
      transitionOrder(order, stage.status);
    }
  });
}

function initializeKds() {
  state.kdsToken = readKdsSession(KDS_TOKEN_KEY);
  state.kdsAdminName = readKdsSession(KDS_ADMIN_KEY) || "";
  syncKdsSessionUi();
  if (state.kdsToken) {
    loadKdsOrders({ silent: true });
  }
  window.setInterval(() => {
    if (state.currentView === "kds" && state.kdsToken) {
      loadKdsOrders({ silent: true });
    }
  }, KDS_POLL_MS);
}

async function loginKds(event) {
  event.preventDefault();

  const username = elements.kdsUsername.value.trim();
  const password = elements.kdsPassword.value;
  if (!username || !password) {
    showToast("Enter the admin username and password.");
    return;
  }

  setKdsLoading(true);
  state.kdsError = "";
  try {
    const response = await fetch("/api/admin/employee/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    const payload = await response.json().catch(() => ({}));

    if (!response.ok || payload.code !== 200 || !payload.data?.token) {
      throw new Error(payload.message || "Admin login failed.");
    }

    state.kdsToken = payload.data.token;
    state.kdsAdminName = payload.data.name || payload.data.username || username;
    writeKdsSession(KDS_TOKEN_KEY, state.kdsToken);
    writeKdsSession(KDS_ADMIN_KEY, state.kdsAdminName);
    elements.kdsPassword.value = "";
    syncKdsSessionUi();
    await loadKdsOrders({ silent: true });
    showToast("Kitchen connected.");
  } catch (error) {
    state.kdsError = error.message;
    showToast(error.message);
    renderKds();
  } finally {
    setKdsLoading(false);
  }
}

function logoutKds(showMessage = true) {
  state.kdsToken = null;
  state.kdsAdminName = "";
  state.kdsOrders = [];
  state.kdsError = "";
  clearKdsSession(KDS_TOKEN_KEY);
  clearKdsSession(KDS_ADMIN_KEY);
  syncKdsSessionUi();
  renderKds();
  if (showMessage) {
    showToast("Kitchen disconnected.");
  }
}

function syncKdsSessionUi() {
  const connected = Boolean(state.kdsToken);
  elements.kdsLoginForm.hidden = connected;
  elements.kdsSessionUser.hidden = !connected;
  elements.kdsAdminName.textContent = state.kdsAdminName || "Admin";
  elements.kdsLoginButton.disabled = state.kdsLoading;
  elements.kdsRefreshButton.disabled = !connected || state.kdsLoading;
  elements.kdsLiveDot.classList.toggle(
    "offline",
    !connected || Boolean(state.kdsError)
  );

  if (!connected) {
    elements.kdsConnectionState.textContent = "Backend sign-in required";
  } else if (state.kdsError) {
    elements.kdsConnectionState.textContent = "Backend unavailable";
  } else {
    elements.kdsConnectionState.textContent = state.kdsLoading
      ? "Refreshing orders"
      : "Backend connected";
  }
}

function setKdsLoading(loading) {
  state.kdsLoading = loading;
  syncKdsSessionUi();
}

async function loadKdsOrders({ silent = false } = {}) {
  if (!state.kdsToken) {
    state.kdsOrders = [];
    renderKds();
    return;
  }

  if (!silent) {
    setKdsLoading(true);
  }
  try {
    const pages = await Promise.all(
      STAGES.map((stage) => kdsApi(
        `/admin/order/conditionSearch?status=${stage.status}&withDetails=true&page=1&pageSize=100`
      ))
    );
    state.kdsOrders = pages
      .flatMap((page) => page?.records || [])
      .sort((left, right) => orderTimestamp(left) - orderTimestamp(right));
    state.kdsError = "";
  } catch (error) {
    state.kdsError = error.message;
    if (!silent) {
      showToast(error.message);
    }
  } finally {
    setKdsLoading(false);
    renderKds();
  }
}

async function kdsApi(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body) {
    headers.set("Content-Type", "application/json");
  }
  if (state.kdsToken) {
    headers.set("Authorization", `Bearer ${state.kdsToken}`);
  }

  const response = await fetch(`/api${path}`, { ...options, headers });
  const payload = await response.json().catch(() => ({}));

  if (response.status === 401) {
    logoutKds(false);
    throw new Error("Admin session expired. Sign in again.");
  }
  if (!response.ok || payload.code !== 200) {
    throw new Error(payload.message || "Kitchen request failed.");
  }

  return payload.data;
}

async function performOrderAction(orderId, action, button) {
  if (state.kdsPendingOrderId) {
    return;
  }

  state.kdsPendingOrderId = orderId;
  button.disabled = true;
  try {
    await submitOrderAction(orderId, action);
    showToast(`Order #${String(orderId).padStart(2, "0")} updated.`);
    await loadKdsOrders({ silent: true });
  } catch (error) {
    showToast(error.message);
    await loadKdsOrders({ silent: true });
  } finally {
    state.kdsPendingOrderId = null;
    renderKds();
  }
}

async function transitionOrder(order, targetStatus) {
  if (!order || targetStatus === order.status || state.kdsPendingOrderId) {
    return;
  }
  if (targetStatus < order.status) {
    showToast("Orders move forward only.");
    return;
  }

  const actions = [];
  if (order.status === 2 && targetStatus >= 3) {
    actions.push("confirm");
  }
  if (order.status <= 3 && targetStatus >= 4) {
    actions.push("delivery");
  }
  if (actions.length === 0) {
    return;
  }

  state.kdsPendingOrderId = order.id;
  renderKds();
  try {
    for (const action of actions) {
      await submitOrderAction(order.id, action);
    }
    showToast(`Order #${String(order.id).padStart(2, "0")} updated.`);
    await loadKdsOrders({ silent: true });
  } catch (error) {
    showToast(error.message);
    await loadKdsOrders({ silent: true });
  } finally {
    state.kdsPendingOrderId = null;
    renderKds();
  }
}

async function submitOrderAction(orderId, action) {
  if (action === "confirm") {
    return kdsApi("/admin/order/confirm", {
      method: "PUT",
      body: JSON.stringify({ id: Number(orderId) })
    });
  }
  if (action === "delivery") {
    return kdsApi(`/admin/order/delivery/${orderId}`, { method: "PUT" });
  }
  if (action === "complete") {
    return kdsApi(`/admin/order/complete/${orderId}`, { method: "PUT" });
  }
  throw new Error("Unsupported order action.");
}

function renderKds() {
  if (!elements.kdsBoard) {
    return;
  }

  const orders = state.kdsOrders;
  elements.openTicketCount.textContent = String(orders.length);
  elements.guestCount.textContent = String(
    orders.reduce((total, order) => total + totalOrderItems(order), 0)
  );
  const nextOrder = orders
    .filter((order) => order.status === 2)
    .sort((left, right) => orderTimestamp(left) - orderTimestamp(right))[0];
  elements.nextTable.textContent = nextOrder
    ? `#${shortOrderNumber(nextOrder)}`
    : "—";
  elements.kdsDateLine.textContent = new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  }).format(new Date());

  const emptyMessage = !state.kdsToken
    ? "Sign in to load orders"
    : state.kdsLoading
      ? "Loading orders"
      : state.kdsError
        ? "Backend unavailable"
        : "No active orders";

  elements.kdsBoard.innerHTML = STAGES.map((stage) => {
    const stageOrders = orders
      .filter((order) => order.status === stage.status)
      .sort((left, right) => orderTimestamp(left) - orderTimestamp(right));
    return `
      <section class="kds-column" data-stage="${stage.id}" style="--stage-color:${stage.color}">
        <div class="kds-column-head">
          <h2>${stage.label}</h2>
          <span>${stageOrders.length}</span>
        </div>
        <div class="ticket-list">
          ${stageOrders.length
            ? stageOrders.map(renderTicket).join("")
            : `<div class="empty-column">${emptyMessage}</div>`}
        </div>
      </section>
    `;
  }).join("");

  syncKdsSessionUi();
  refreshIcons();
}

function renderTicket(order) {
  const createdAt = orderTimestamp(order);
  const elapsedMinutes = Math.max(1, Math.floor((Date.now() - createdAt) / 60000));
  const currentStageIndex = STAGES.findIndex((stage) => stage.status === order.status);
  const currentStage = STAGES[currentStageIndex] || STAGES[0];
  const nextStage = STAGES[currentStageIndex + 1]
    || (currentStageIndex === STAGES.length - 1
      ? { label: "Complete", status: 5 }
      : null);
  const canDrag = Boolean(STAGES[currentStageIndex + 1]);
  const orderItems = Array.isArray(order.orderDetailList)
    ? order.orderDetailList
    : [];
  const orderTime = new Date(order.orderTime);
  const pending = String(state.kdsPendingOrderId) === String(order.id);

  return `
    <article class="ticket" draggable="${canDrag}" data-ticket-id="${order.id}">
      <div class="ticket-head">
        <div>
          <strong>#${escapeHtml(shortOrderNumber(order))}</strong>
          <span>${escapeHtml(order.number)} · ${escapeHtml(order.consignee)}</span>
        </div>
        <span class="ticket-timer ${elapsedMinutes >= 15 ? "late" : ""}">${formatElapsed(elapsedMinutes)}</span>
      </div>
      <div class="ticket-meta">
        <span>${currentStage.label}</span>
        <span>${orderItems.length} lines</span>
        <span>${formatCurrency(order.amount)}</span>
        <span>${orderTime.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })}</span>
      </div>
      <ul class="ticket-items">
        ${orderItems.map((item) => `
          <li>
            <b>${item.number}</b>
            <div>
              ${escapeHtml(item.name)}
              ${item.dishFlavor ? `<small>${escapeHtml(item.dishFlavor)}</small>` : ""}
            </div>
          </li>
        `).join("")}
      </ul>
      ${order.remark ? `<p class="ticket-note">${escapeHtml(order.remark)}</p>` : ""}
      <div class="ticket-actions">
        <span>${escapeHtml(order.phone)}</span>
        ${nextStage
          ? `<button type="button"
                    data-order-id="${order.id}"
                    data-order-action="${actionForStatus(nextStage.status)}"
                    ${pending ? "disabled" : ""}>
              <span>${nextStage.label}</span>
              <i data-lucide="arrow-right" aria-hidden="true"></i>
            </button>`
          : "<span></span>"}
      </div>
    </article>
  `;
}

function actionForStatus(status) {
  if (status === 3) {
    return "confirm";
  }
  if (status === 4) {
    return "delivery";
  }
  return "complete";
}

function shortOrderNumber(order) {
  return String(order.number || order.id).slice(-6);
}

function totalOrderItems(order) {
  if (!Array.isArray(order.orderDetailList)) {
    return 0;
  }
  return order.orderDetailList.reduce(
    (total, item) => total + Number(item.number || 0),
    0
  );
}

function orderTimestamp(order) {
  const timestamp = new Date(order.orderTime).getTime();
  return Number.isNaN(timestamp) ? 0 : timestamp;
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value || 0));
}

function readKdsSession(key) {
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeKdsSession(key, value) {
  try {
    sessionStorage.setItem(key, value);
  } catch {
    // Session storage is optional; the in-memory session still works.
  }
}

function clearKdsSession(key) {
  try {
    sessionStorage.removeItem(key);
  } catch {
    // Nothing to clear when session storage is unavailable.
  }
}

function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.add("show");
  window.clearTimeout(showToast.timeoutId);
  showToast.timeoutId = window.setTimeout(() => {
    elements.toast.classList.remove("show");
  }, 2800);
}

function refreshIcons() {
  if (window.lucide) {
    window.lucide.createIcons({ attrs: { "stroke-width": 1.8 } });
  }
}

function formatLongDate(dateValue) {
  const date = new Date(`${dateValue}T12:00:00`);
  return new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "long",
    day: "numeric",
    year: "numeric"
  }).format(date);
}

function formatTime(timeValue) {
  return new Intl.DateTimeFormat("en-US", {
    hour: "numeric",
    minute: "2-digit"
  }).format(new Date(`2026-01-01T${timeValue}:00`));
}

function formatElapsed(minutes) {
  if (minutes < 60) {
    return `${minutes}m`;
  }
  const hours = Math.floor(minutes / 60);
  return `${hours}h ${minutes % 60}m`;
}

function toDateInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function toIcsDate(date) {
  return date.toISOString().replace(/[-:]/g, "").replace(/\.\d{3}/, "");
}

function escapeIcs(value) {
  return String(value).replace(/\\/g, "\\\\").replace(/,/g, "\\,").replace(/;/g, "\\;").replace(/\n/g, "\\n");
}

function hashString(value) {
  let hash = 0;
  for (let index = 0; index < value.length; index += 1) {
    hash = (hash * 31 + value.charCodeAt(index)) | 0;
  }
  return Math.abs(hash);
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

initialize();
