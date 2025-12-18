// /js/api.js
// 아주 얇은 fetch 래퍼: JWT 자동 첨부 + ErrorResponse 파싱 + 토스트

(function () {
  const TOKEN_KEY = "im_accessToken";
  const MY_JR_KEY = "im_myJoinRequestIds";

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }
  function setToken(token) {
    if (!token) localStorage.removeItem(TOKEN_KEY);
    else localStorage.setItem(TOKEN_KEY, token);
  }

  function getMyJoinRequestIds() {
    try { return JSON.parse(localStorage.getItem(MY_JR_KEY) || "[]"); }
    catch { return []; }
  }
  function addMyJoinRequestId(id) {
    const list = getMyJoinRequestIds();
    if (!list.includes(id)) list.unshift(id);
    localStorage.setItem(MY_JR_KEY, JSON.stringify(list.slice(0, 20)));
  }
  function removeMyJoinRequestId(id) {
    const list = getMyJoinRequestIds().filter(x => x !== id);
    localStorage.setItem(MY_JR_KEY, JSON.stringify(list));
  }

  function showToast(message, variant = "primary") {
    const area = document.getElementById("toastArea");
    if (!area) return;

    const el = document.createElement("div");
    el.className = "toast align-items-center text-bg-" + variant + " border-0";
    el.setAttribute("role", "alert");
    el.setAttribute("aria-live", "assertive");
    el.setAttribute("aria-atomic", "true");
    el.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${escapeHtml(message)}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    `;
    area.appendChild(el);
    const t = new bootstrap.Toast(el, { delay: 2500 });
    t.show();
    el.addEventListener("hidden.bs.toast", () => el.remove());
  }

  function escapeHtml(str) {
    return String(str ?? "").replace(/[&<>"']/g, (m) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    }[m]));
  }

  async function apiFetch(path, options = {}) {
    const {
      method = "GET",
      body,
      auth = true,
      silent = false,
      headers = {},
    } = options;

    const h = { ...headers };
    if (body !== undefined && body !== null) h["Content-Type"] = "application/json";

    const token = getToken();
    if (auth && token) h["Authorization"] = "Bearer " + token;

    const res = await fetch(path, {
      method,
      headers: h,
      body: body !== undefined && body !== null ? JSON.stringify(body) : undefined
    });

    // 204 No Content
    if (res.status === 204) return null;

    const contentType = res.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");

    if (res.ok) {
      return isJson ? await res.json() : await res.text();
    }

    // error parsing
    let errPayload = null;
    try { errPayload = isJson ? await res.json() : { message: await res.text() }; } catch {}

    const msg = (errPayload && (errPayload.message || errPayload.error || errPayload.errorMessage))
      ? (errPayload.message || errPayload.error || errPayload.errorMessage)
      : ("요청에 실패했습니다. (" + res.status + ")");

    // 401이면 토큰 정리 + 로그인으로 (silent 여부와 관계없이 이동)
    if (res.status === 401) {
      setToken(null);
      if (!silent) showToast("로그인이 필요합니다.", "warning");
      window.location.hash = "#/login";
    } else if (res.status === 403) {
      // 토큰이 없으면 사실상 미인증으로 취급 (프로젝트 설정에 따라 403으로 내려올 수 있음)
      if (!getToken()) {
        setToken(null);
        if (!silent) showToast("로그인이 필요합니다.", "warning");
        window.location.hash = "#/login";
      } else {
        if (!silent) showToast("권한이 없습니다.", "danger");
      }
    } else {
      if (!silent) showToast(msg, "danger");
    }

    const error = new Error(msg);
    error.status = res.status;
    error.payload = errPayload;
    throw error;
  }

  window.IM = {
    getToken,
    setToken,
    apiFetch,
    showToast,
    getMyJoinRequestIds,
    addMyJoinRequestId,
    removeMyJoinRequestId,
    escapeHtml
  };
})();
