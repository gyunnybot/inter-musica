// /js/app.js
(function () {
  const appEl = document.getElementById("app");
  const navLeft = document.getElementById("nav-left");
  const navRight = document.getElementById("nav-right");

  const ENUMS = {
    instrument: [
      "VIOLIN","PIANO","VIOLA","CELLO",
      "VOCAL","GUITAR","BASS","DRUMS","KEYBOARD"
    ],
    level: [
      "HOBBY_UNDER_A_YEAR",
      "HOBBY_UNDER_FIVE_YEAR",
      "HOBBY_OVER_FIVE_YEAR",
      "MAJOR_STUDENT"
    ],
    region: [
      "SEOUL_SEOCHO",
      "SEOUL_HONGDAE",
      "SEOUL_ITAEWON",
      "SEOUL_SEONGSU",
      "SEOUL_SEOUL_FOREST"
    ],
    joinStatus: ["APPLIED","ACCEPTED","REJECTED","CANCELED"]
  };

  const LABELS = {
    instrument: {
      VIOLIN: "바이올린", PIANO: "피아노", VIOLA: "비올라", CELLO: "첼로",
      VOCAL: "보컬", GUITAR: "기타", BASS: "베이스", DRUMS: "드럼", KEYBOARD: "키보드"
    },
    level: {
      HOBBY_UNDER_A_YEAR: "취미 1년 미만",
      HOBBY_UNDER_FIVE_YEAR: "취미 5년 미만",
      HOBBY_OVER_FIVE_YEAR: "취미 5년 이상",
      MAJOR_STUDENT: "전공(학생/전공자)"
    },
    region: {
      SEOUL_SEOCHO: "서울 서초",
      SEOUL_HONGDAE: "서울 홍대",
      SEOUL_ITAEWON: "서울 이태원",
      SEOUL_SEONGSU: "서울 성수",
      SEOUL_SEOUL_FOREST: "서울숲"
    },
    joinStatus: {
      APPLIED: "지원",
      ACCEPTED: "수락",
      REJECTED: "거절",
      CANCELED: "취소"
    }
  };

  const state = {
    me: null,     // ProfileResponse
    loadingMe: false,
    myTeam: undefined, // undefined: unknown, null: no team, object: team
    myTeams: undefined, // array or null
    loadingMyTeam: false,
    activeIntervals: []
  };

  // 간단 캐시(지원자 프로필 등 반복 조회 방지)
  const cache = {
    profileByUserId: Object.create(null),
    positionStatsByTeamId: Object.create(null)
  };

  function asArray(v) {
    return Array.isArray(v) ? v : [];
  }

  function practiceRegionList(team) {
      const list = asArray(team?.practiceRegions);
      if (list.length) return list;
      if (team?.practiceRegion) return [team.practiceRegion];
      return [];
  }

  function profilePracticeRegionList(profile) {
        const list = asArray(profile?.practiceRegions);
        if (list.length) return list;
        if (profile?.region) return [profile.region];
        return [];
  }

  function formatPracticeRegions(team) {
        const regions = practiceRegionList(team);
        if (!regions.length) return "-";
        return regions.map(r => fmtEnum("region", r) || r).join(", ");
      }

  function h(tag, attrs = {}, children = []) {
    const el = document.createElement(tag);
    Object.entries(attrs).forEach(([k, v]) => {
      if (k === "class") el.className = v;
      else if (k.startsWith("on") && typeof v === "function") el.addEventListener(k.slice(2), v);
      else if (v !== null && v !== undefined) el.setAttribute(k, v);
    });
    for (const c of children) el.append(c);
    return el;
  }

  function setHtml(el, html) { el.innerHTML = html; }

  function registerInterval(intervalId) {
    state.activeIntervals.push(intervalId);
  }

  function clearIntervals() {
    state.activeIntervals.forEach((intervalId) => clearInterval(intervalId));
    state.activeIntervals = [];
  }

  function fmtEnum(group, value) {
    if (!value) return "";
    return (LABELS[group] && LABELS[group][value]) ? LABELS[group][value] : value;
  }

  function fmtDate(iso) {
    if (!iso) return "-";
    try {
      const d = new Date(iso);
      return d.toLocaleString("ko-KR");
    } catch { return iso; }
  }

  function renderPracticeNoteModal() {
    return `
      <div class="modal fade" id="practiceNoteModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="practiceNoteTitle">팀 안내사항/공지</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="practiceNoteBody">
              <div class="muted">불러오는 중...</div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  function openPracticeNoteModal(teamName, note) {
    const modalEl = document.getElementById("practiceNoteModal");
    if (!modalEl) return;
    const titleEl = document.getElementById("practiceNoteTitle");
    const bodyEl = document.getElementById("practiceNoteBody");
    if (titleEl) {
      titleEl.textContent = teamName ? `팀 안내사항/공지` : "팀 안내사항/공지";
    }
    if (bodyEl) {
      const sanitized = note ? IM.escapeHtml(note).replace(/\n/g, "<br>") : "";
      bodyEl.innerHTML = sanitized ? `<div class="im-join-message">${sanitized}</div>` : `<div class="muted">등록된 공지가 없습니다.</div>`;
    }
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
  }

  function renderSlotHtml(total, used) {
    const remain = Math.max(0, total - used);
    const remainRatio = total ? (remain / total) : 0;
    const slotStateClass = total
      ? (remainRatio <= 0.2 ? "im-slot-danger" : remainRatio <= 0.4 ? "im-slot-warning" : "")
      : "";
    const slotTextClass = slotStateClass === "im-slot-danger"
      ? "im-slot-text-danger"
      : slotStateClass === "im-slot-warning"
        ? "im-slot-text-warning"
        : "";
    const slotSegments = total
      ? Array.from({ length: total }, (_, idx) => {
        const isRemaining = idx < remain;
        return `<span class="im-slot-segment ${isRemaining ? "is-remaining" : "is-used"}"></span>`;
      }).join("")
      : "";
    return `
      <div class="im-slot-track ${total ? slotStateClass : "im-slot-empty"}">
        ${slotSegments || `<span class="muted small">-</span>`}
      </div>
      <div class="small muted mt-1 ${slotTextClass}">남은 자리: ${remain}</div>
    `;
  }

  async function ensureMe() {
    const token = IM.getToken();
    if (!token) { state.me = null; state.myTeam = undefined; state.myTeams = undefined; return null; }
    if (state.me) return state.me;
    if (state.loadingMe) return state.me;

    state.loadingMe = true;
    try {
      state.me = await IM.apiFetch("/profiles/me", { auth: true });
    } catch {
      state.me = null;
    } finally {
      state.loadingMe = false;
    }

    // 내 팀 상태도 같이 로드해서 네비게이션(팀 만들기 노출 여부 등)을 결정
    await ensureMyTeam();
    renderNav();
    return state.me;
  }



  function pickTeamId(t) {
    return t?.id ?? t?.teamId ?? t?.team_id;
  }

  function hasTeam() {
    const id = pickTeamId(state.myTeam);
    return id !== null && id !== undefined;
  }

  async function ensureMyTeam(force = false) {
    const token = IM.getToken();
    if (!token) {
      state.myTeam = undefined;
      state.myTeams = undefined;
      state.loadingMyTeam = false;
      return null;
    }

    if (!force && state.myTeam !== undefined) return state.myTeam;
    if (state.loadingMyTeam) return state.myTeam;

    state.loadingMyTeam = true;
    try {
      const res = await IM.apiFetch("/teams/me", { auth: true, silent: true });
      const list = Array.isArray(res) ? res : (res ? [res] : []);
      state.myTeams = list.length ? list : null;
      state.myTeam = list.length ? list[0] : null;
    } catch (e) {
      const status = e?.status ?? e?.statusCode ?? e?.httpStatus ?? e?.response?.status ?? e?.data?.status;
      if (status === 404 || status === 204) {
        state.myTeam = null;
        state.myTeams = null;
      } else {
        // 네트워크/서버 오류 등은 'unknown'으로 두고 UI를 과도하게 막지 않음
        if (state.myTeam === undefined) state.myTeam = undefined;
        if (state.myTeams === undefined) state.myTeams = undefined;
      }
    } finally {
      state.loadingMyTeam = false;
    }
    return state.myTeam;
  }

  function renderNav() {
    const token = IM.getToken();
    const me = state.me;

    setHtml(navLeft, "");
    setHtml(navRight, "");

    // Left
    navLeft.append(
      navItem("#/teams", "팀 찾기", "bi-search"),
    );
    if (token) {
      navLeft.append(
        navItem("#/my-team", "내 팀", "bi-people-fill"),
      );
    }

    // Right
    if (!token) {
      navRight.append(
        navItem("#/login", "로그인", "bi-box-arrow-in-right"),
        navItem("#/signup", "회원가입", "bi-person-plus")
      );
    } else {
      navRight.append(
        navItem("#/create-team", "팀 만들기", "bi-plus-circle"),
      );
      navRight.append(
        navItem("#/profile", me ? (me.name + " 님") : "내 프로필", "bi-person-circle"),
      );

      const li = document.createElement("li");
      li.className = "nav-item";
      const btn = document.createElement("button");
      btn.className = "btn btn-sm btn-outline-light ms-lg-2 align-self-center my-2";
      btn.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i>로그아웃';
      btn.addEventListener("click", () => {
        IM.setToken(null);
        state.me = null;
        state.myTeam = undefined;
        state.myTeams = undefined;
        renderNav();
        IM.showToast("로그아웃 되었습니다.", "secondary");
        window.location.hash = "#/login";
      });
      li.appendChild(btn);
      navRight.appendChild(li);
    }
  }

  function navItem(hash, label, icon) {
    const li = document.createElement("li");
    li.className = "nav-item";
    const a = document.createElement("a");
    a.className = "nav-link";
    a.href = hash;
    a.innerHTML = icon ? `<i class="bi ${icon} me-1"></i>${IM.escapeHtml(label)}` : IM.escapeHtml(label);
    li.appendChild(a);
    return li;
  }

  function requireAuth() {
    if (!IM.getToken()) {
      IM.showToast("이 기능은 로그인이 필요합니다.", "warning");
      window.location.hash = "#/login";
      return false;
    }
    return true;
  }

  // ---------- Views ----------
  function renderShell(titleHtml, bodyHtml) {
    setHtml(appEl, `
      <div class="d-flex align-items-center justify-content-between mb-3">
        <div>
          <h2 class="page-title mb-0">${titleHtml}</h2>
        </div>
      </div>
      ${bodyHtml}
    `);
  }

  async function viewTeamsList() {
    if (!requireAuth()) return;
    renderShell("팀 찾기", `
      <div class="row g-3 mb-3">
        <div class="col-lg-4">
          <label class="form-label">연습 위치(선택)</label>
          <select class="form-select" id="regionFilter">
            <option value="">전체</option>
            ${ENUMS.region.map(r => `<option value="${r}">${fmtEnum("region", r)}</option>`).join("")}
          </select>
        </div>
        <div class="col-lg-8 d-flex align-items-end">
          <button class="btn btn-dark" id="btnSearch"><i class="bi bi-funnel me-1"></i>조회</button>
        </div>
      </div>

      <div class="card">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <div class="fw-semibold">팀 목록</div>
            <div class="muted small" id="teamCount"></div>
          </div>
          <div class="table-responsive">
            <table class="table align-middle">
              <thead>
                <tr>
                  <th>팀 이름</th>
                  <th>조율 중인 연습 위치</th>
                  <th class="d-none d-lg-table-cell">남은 포지션</th>
                  <th>팀 안내사항/공지</th>
                  <th class="d-none d-md-table-cell">생성일</th>
                  <th></th>
                </tr>
              </thead>
              <tbody id="teamRows">
                <tr><td colspan="6" class="muted">불러오는 중...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      ${renderPracticeNoteModal()}
    `);

    const regionFilter = document.getElementById("regionFilter");
    document.getElementById("btnSearch").addEventListener("click", () => loadTeams(regionFilter.value));

    await loadTeams("");
  }

async function loadTeams(region) {
  const rows = document.getElementById("teamRows");
  const countEl = document.getElementById("teamCount");
  if (!rows || !countEl) return;

  rows.innerHTML = `<tr><td colspan="6" class="muted">불러오는 중...</td></tr>`;

  const qs = region ? ("?region=" + encodeURIComponent(region)) : "";
  const raw = await IM.apiFetch("/teams" + qs, { auth: true });
  const list = asArray(raw);

  countEl.textContent = `${list.length}개`;
  if (!list.length) {
    rows.innerHTML = `<tr><td colspan="6" class="muted">아직 생성된 팀이 없습니다.</td></tr>`;
    return;
  }

  rows.innerHTML = list.map(t => {
    const teamId = (t.id ?? t.teamId ?? t.team_id);
    const memo = (t.practiceNote ?? t.memo ?? t.note ?? "");
    const memoButton = memo
      ? `<button class="btn btn-sm btn-outline-secondary" data-team-note="${IM.escapeHtml(String(teamId))}">보기</button>`
      : `<span class="muted small">-</span>`;
    return `
      <tr>
        <td class="text-truncate">${IM.escapeHtml(t.teamName || "")}</td>
        <td>${IM.escapeHtml(formatPracticeRegions(t))}</td>
        <td class="d-none d-lg-table-cell" id="teamSlot-${IM.escapeHtml(String(teamId))}">
          <span class="muted small">불러오는 중...</span>
        </td>
        <td class="d-none d-md-table-cell">${memoButton}</td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(t.createdAt))}</td>
        <td class="text-end">
          <a class="btn btn-sm btn-dark" href="#/teams/${IM.escapeHtml(String(teamId))}">
            상세 <i class="bi bi-chevron-right ms-1"></i>
          </a>
        </td>
      </tr>
    `;
  }).join("");

  const memoById = new Map(list.map(t => {
    const teamId = (t.id ?? t.teamId ?? t.team_id);
    const memo = (t.practiceNote ?? t.memo ?? t.note ?? "");
    return [String(teamId), memo || ""];
  }));
  rows.querySelectorAll("[data-team-note]").forEach(btn => {
    btn.addEventListener("click", () => {
      const teamId = btn.getAttribute("data-team-note");
      const team = list.find(t => String(t.id ?? t.teamId ?? t.team_id) === String(teamId));
      openPracticeNoteModal(team?.teamName || "팀", memoById.get(String(teamId)));
    });
  });

  // 남은 포지션 정보는 팀별로 추가 조회해서 채움(비동기)
  enhanceTeamListSlots(list);
}


    async function enhanceTeamListSlots(list) {
    // 팀 목록이 많을 수 있어서 너무 무겁게 하지 않기 위해 순차적으로 채웁니다.
    for (const t of list) {
      const teamId = (t.id ?? t.teamId ?? t.team_id);
      const cell = document.getElementById(`teamSlot-${teamId}`);
      if (!cell) continue;

      // 이미 다른 화면으로 이동했으면 중단
      if (!document.getElementById("teamRows")) break;

      try {
        const [positions, stats] = await Promise.all([
          IM.apiFetch(`/teams/${teamId}/positions`, { auth: true, silent: true }),
          IM.apiFetch(`/teams/${teamId}/positions/stats`, { auth: true, silent: true })
        ]);

        const statMap = Object.create(null);
        if (Array.isArray(stats)) {
          for (const s of stats) statMap[String(s.positionId)] = Number(s.acceptedCount || 0);
        }

        const remainByInst = Object.create(null);
        if (Array.isArray(positions)) {
          for (const p of positions) {
            const total = Number(p.capacity || 0);
            const used = Number(statMap[String(p.id)] || 0);
            const remain = Math.max(0, total - used);
            if (remain <= 0) continue;
            const inst = p.instrument || "-";
            remainByInst[inst] = (remainByInst[inst] || 0) + remain;
          }
        }

        const entries = Object.entries(remainByInst);
        if (!entries.length) {
          cell.innerHTML = `<span class="muted small">-</span>`;
          continue;
        }

        const lines = entries.map(([inst, cnt]) => {
          const name = fmtEnum("instrument", inst) || inst;
          return `${IM.escapeHtml(name)} ${IM.escapeHtml(String(cnt))}명`;
        });

        cell.innerHTML = `<div class="small">${lines.join("<br>")}</div>`;
      } catch {
        // stats API가 없거나 권한/오류가 있으면 조용히 패스
        cell.innerHTML = `<span class="muted small">-</span>`;
      }
    }
  }

  async function viewLogin() {
    renderShell("로그인", `
      <div class="row justify-content-center">
        <div class="col-lg-6">
          <div class="card">
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label">이메일</label>
                <input class="form-control" id="email" type="email" placeholder="you@example.com"/>
              </div>
              <div class="mb-3">
                <label class="form-label">비밀번호</label>
                <input class="form-control" id="password" type="password" placeholder="••••••••"/>
              </div>
              <button class="btn btn-dark w-100" id="btnLogin">
                <i class="bi bi-box-arrow-in-right me-1"></i>로그인
              </button>
              <div class="small-help mt-3">
                계정이 없나요? <a href="#/signup">회원가입</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `);

    document.getElementById("btnLogin").addEventListener("click", async () => {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;

      const res = await IM.apiFetch("/auth/login", {
        method: "POST",
        auth: false,
        body: { email, password }
      });

      IM.setToken(res.accessToken);
      state.me = null;
      state.myTeam = undefined;
      state.myTeams = undefined;
      await ensureMe();
      IM.showToast("로그인 성공!", "success");
      window.location.hash = '#/teams';
    });
  }

  async function viewSignup() {
    renderShell("회원가입", `
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="card">
            <div class="card-body">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">이메일</label>
                  <input class="form-control" id="email" type="email" placeholder="you@example.com"/>
                </div>
                <div class="col-md-6">
                  <label class="form-label">비밀번호</label>
                  <input class="form-control" id="password" type="password" placeholder="••••••••"/>
                </div>
                <div class="col-md-6">
                  <label class="form-label">이름</label>
                  <input class="form-control" id="name" type="text" placeholder="홍길동"/>
                </div>
                <div class="col-md-6">
                  <label class="form-label">악기</label>
                  <select class="form-select" id="instrument">
                    ${ENUMS.instrument.map(v => `<option value="${v}">${fmtEnum("instrument", v)}</option>`).join("")}
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">레벨</label>
                  <select class="form-select" id="level">
                    ${ENUMS.level.map(v => `<option value="${v}">${fmtEnum("level", v)}</option>`).join("")}
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">참여 가능한 연습 위치</label>
                                    <div class="border rounded-3 p-2">
                                      ${ENUMS.region.map(v => `
                                        <div class="form-check">
                                          <input class="form-check-input" type="checkbox" value="${v}" id="profileRegion-${v}">
                                          <label class="form-check-label" for="profileRegion-${v}">
                                            ${fmtEnum("region", v)}
                                          </label>
                                        </div>
                                      `).join("")}
                                    </div>
                </div>
              </div>

              <button class="btn btn-dark w-100 mt-3" id="btnSignup">
                <i class="bi bi-person-plus me-1"></i>가입하기
              </button>

              <div class="small-help mt-3">
                이미 계정이 있나요? <a href="#/login">로그인</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `);

    document.getElementById("btnSignup").addEventListener("click", async () => {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const name = document.getElementById("name").value.trim();
      const instrument = document.getElementById("instrument").value;
      const level = document.getElementById("level").value;
      const regions = Array.from(document.querySelectorAll("input[id^='profileRegion-']:checked"))
        .map(input => input.value);

      if (!regions.length) {
        IM.showToast("참여 가능한 연습 위치를 선택해 주세요.", "warning");
        return;
      }

      const res = await IM.apiFetch("/auth/signup", {
        method: "POST",
        auth: false,
        body: { email, password, name, instrument, level, practiceRegions: regions }
      });

      IM.setToken(res.accessToken);
      state.me = null;
      state.myTeam = undefined;
      state.myTeams = undefined;
      await ensureMe();
      IM.showToast("회원가입 완료!", "success");
      window.location.hash = '#/teams';
    });
  }

  async function viewProfile() {
    if (!requireAuth()) return;
    await ensureMe();
    const me = state.me;

    renderShell("내 프로필", `
      <div class="row justify-content-center">
        <div class="col-lg-6">
          <div class="card">
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label">이메일</label>
                <input class="form-control" value="${IM.escapeHtml(me?.email || "")}" disabled/>
              </div>
              <div class="mb-3">
                <label class="form-label">이름</label>
                <input class="form-control" id="profileName" value="${IM.escapeHtml(me?.name || "")}"/>
              </div>
              <div class="mb-3">
                <label class="form-label">악기</label>
                <select class="form-select" id="profileInstrument">
                  ${ENUMS.instrument.map(v => `<option value="${v}" ${me?.instrument === v ? "selected" : ""}>${fmtEnum("instrument", v)}</option>`).join("")}
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">레벨</label>
                <select class="form-select" id="profileLevel">
                  ${ENUMS.level.map(v => `<option value="${v}" ${me?.level === v ? "selected" : ""}>${fmtEnum("level", v)}</option>`).join("")}
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">참여 가능한 연습 위치</label>
                <div class="border rounded-3 p-2">
                  ${ENUMS.region.map(v => `
                    <div class="form-check">
                      <input class="form-check-input" type="checkbox" value="${v}" id="profileRegion-${v}" ${profilePracticeRegionList(me).includes(v) ? "checked" : ""}>
                      <label class="form-check-label" for="profileRegion-${v}">
                        ${fmtEnum("region", v)}
                      </label>
                    </div>
                  `).join("")}
                </div>
              </div>

              <button class="btn btn-dark w-100" id="btnUpdateProfile">
                <i class="bi bi-save me-1"></i>저장
              </button>
            </div>
          </div>
        </div>
      </div>
    `);

    document.getElementById("btnUpdateProfile").addEventListener("click", async () => {
      const name = document.getElementById("profileName").value.trim();
      const instrument = document.getElementById("profileInstrument").value;
      const level = document.getElementById("profileLevel").value;
      const regions = Array.from(document.querySelectorAll("input[id^='profileRegion-']:checked"))
        .map(input => input.value);

      if (!regions.length) {
        IM.showToast("참여 가능한 연습 위치를 선택해 주세요.", "warning");
        return;
      }

      const res = await IM.apiFetch("/profiles/me", {
        method: "PATCH",
        body: { name, instrument, level, practiceRegions: regions }
      });
      state.me = res;
      IM.showToast("프로필이 수정되었습니다.", "success");
      renderNav();
      viewProfile();
    });
  }

  async function renderMyJoinRequests() {
    const bodyEl = document.getElementById("myJoinRequestBody");
    if (!bodyEl) return;

    bodyEl.innerHTML = `<div class="muted">불러오는 중...</div>`;

    try {
      const list = await IM.apiFetch("/join-requests/me", { auth: true, silent: true });
      const cleaned = (Array.isArray(list) ? list : []).filter(Boolean);

      if (!cleaned.length) {
        bodyEl.innerHTML = `<div class="muted">지원 내역이 없습니다.</div>`;
        return;
      }

      const rows = cleaned.map(jr => `
        <tr>
          <td>${IM.escapeHtml(jr.team?.teamName || "-")}</td>
          <td>${IM.escapeHtml(jr.positionName || "-")}</td>
          <td><span class="badge text-bg-light">${IM.escapeHtml(fmtEnum("joinStatus", jr.status))}</span></td>
          <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(jr.createdAt))}</td>
          <td class="text-end">
            ${jr.status === "APPLIED"
              ? `<button class="btn btn-sm btn-outline-danger" data-cancel="${jr.id}">
                  <i class="bi bi-x me-1"></i>취소
                </button>`
              : `<span class="muted small">-</span>`
            }
          </td>
        </tr>
      `).join("");

      bodyEl.innerHTML = `
        <div class="table-responsive">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>팀</th>
                <th>포지션</th>
                <th>상태</th>
                <th class="d-none d-md-table-cell">지원일</th>
                <th class="text-end">관리</th>
              </tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
        </div>
      `;

      bodyEl.querySelectorAll("[data-cancel]").forEach(btn => {
        btn.addEventListener("click", async () => {
          const id = btn.getAttribute("data-cancel");
          try {
            await IM.apiFetch(`/join-requests/${id}/cancel`, { method: "POST" });
            IM.showToast("지원이 취소되었습니다.", "secondary");
            renderMyJoinRequests();
          } catch (e) {
            IM.showToast(e?.message || "취소에 실패했습니다.", "danger");
          }
        });
      });
    } catch (e) {
      bodyEl.innerHTML = `<div class="text-danger">불러오기 실패: ${IM.escapeHtml(e.message || "")}</div>`;
    }
  }


async function viewMyTeam() {
  if (!requireAuth()) return;

  renderShell("내 팀", `
    <div class="card">
      <div class="card-body">
        <div class="muted">불러오는 중...</div>
      </div>
    </div>
  `);

  await ensureMe();
  const me = state.me; // { userId, name, ... }
  if (me?.userId) {
      cache.profileByUserId[String(me.userId)] = me;
    }

  const pickTeamId = (t) => (t?.id ?? t?.teamId ?? t?.team_id);

  const renderNoTeam = () => {
    renderShell("내 팀", `
      <div class="card">
        <div class="card-body">
          <div class="fw-semibold">아직 합류한 팀이 없습니다</div>
          <div class="muted mt-1">팀을 만들거나, 팀에 지원해보세요.</div>
          <div class="d-flex gap-2 mt-3">
            <a class="btn btn-dark" href="#/teams"><i class="bi bi-search me-1"></i>팀 찾기</a>
            <a class="btn btn-outline-dark" href="#/create-team"><i class="bi bi-plus-circle me-1"></i>팀 만들기</a>
          </div>
        </div>
      </div>
    `);
  };

  const renderTeams = (teams) => {
    if (!teams.length) {
      renderNoTeam();
      return;
    }

    const cards = teams.map(team => {
      const teamId = pickTeamId(team);
      const isLeader = Number(team.leaderUserId) === Number(me?.userId);
      const roleLabel = isLeader ? "팀장" : "팀원";
      const roleBadge = isLeader ? "text-bg-dark" : "text-bg-secondary";
      const leaderName = team.leaderName
              || cache.profileByUserId[String(team.leaderUserId)]?.name
              || "-";
      const memo = team.practiceNote || "";
      const memoButton = memo
        ? `<button class="btn btn-sm btn-outline-secondary" data-team-note="${IM.escapeHtml(String(teamId))}">보기</button>`
        : `<span class="muted small">-</span>`;

      return `
        <div class="col-lg-6">
          <div class="card h-100">
            <div class="card-body d-flex flex-column">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <div class="fw-semibold text-truncate">${IM.escapeHtml(team.teamName || "(이름 없음)")}</div>
                <span class="badge ${roleBadge}">${roleLabel}</span>
              </div>
              <div class="mb-1">
                <span class="muted">조율 중인 연습 위치</span> :
                ${IM.escapeHtml(formatPracticeRegions(team))}
              </div>
              <div class="mb-1">
                <span class="muted">팀장</span> :
                ${IM.escapeHtml(leaderName)}
              </div>
              <div class="mb-1">
                <span class="muted">생성일</span> :
                ${IM.escapeHtml(fmtDate(team.createdAt))}
              </div>
              <div class="mt-2">
                <br>
                <div class="muted small">팀 안내사항/공지</div>
                <div>${memoButton}</div>
              </div>
              <div class="mt-auto pt-3">
                <a class="btn btn-dark w-100" href="#/teams/${IM.escapeHtml(teamId)}">
                  <i class="bi bi-box-arrow-up-right me-1"></i>팀 상세 보기
                </a>
              </div>
            </div>
          </div>
        </div>
      `;
    }).join("");

    renderShell("내 팀", `
      <div class="row g-3">
        ${cards}
      </div>
      ${renderPracticeNoteModal()}
    `);

    const memoById = new Map(teams.map(team => [String(pickTeamId(team)), team.practiceNote || ""]));
    document.querySelectorAll("[data-team-note]").forEach(btn => {
      btn.addEventListener("click", () => {
        const teamId = btn.getAttribute("data-team-note");
        const team = teams.find(t => String(pickTeamId(t)) === String(teamId));
        openPracticeNoteModal(team?.teamName || "팀", memoById.get(String(teamId)));
      });
    });
  };

  // 팀장(내가 만든 팀) fallback: /teams 목록에서 leaderUserId로 내 팀 찾기
  async function findLeaderTeams() {
    if (!me?.userId) return null;
    const list = asArray(await IM.apiFetch("/teams", { auth: true, silent: true }));
    return list.filter(t => Number(t.leaderUserId) === Number(me.userId));
  }

  async function ensureLeaderNames(teams) {
      const leaderIds = [...new Set(teams.map(team => String(team.leaderUserId)).filter(Boolean))];
      await Promise.all(leaderIds.map(async (leaderId) => {
        if (cache.profileByUserId[leaderId]) return;
        try {
          const profile = await IM.apiFetch(`/profiles/${encodeURIComponent(leaderId)}`, { auth: true, silent: true });
          cache.profileByUserId[leaderId] = profile;
        } catch (_) {
          cache.profileByUserId[leaderId] = { name: null };
        }
      }));
  }

  try {
    // 1) 우선 "내가 속한 팀" 조회 (팀원/팀장 모두 여기서 내려오면 가장 좋음)
    const raw = await IM.apiFetch("/teams/me", { auth: true, silent: true });
    const fromMe = Array.isArray(raw) ? raw : (raw ? [raw] : []);
    const leaderTeams = await findLeaderTeams();
    const allTeams = [...fromMe, ...leaderTeams].filter(team => pickTeamId(team));

    const unique = [];
    const seen = new Set();
    for (const team of allTeams) {
      const teamId = String(pickTeamId(team));
      if (seen.has(teamId)) continue;
      seen.add(teamId);
      unique.push(team);
    }

    await ensureLeaderNames(unique);
    renderTeams(unique);
  } catch (e) {
    // 어떤 형태의 실패든: 우선 팀장 fallback을 시도하고, 실패하면 빈 상태로 처리 (라우터 에러 화면 방지)
    try {
      const leaderTeams = await findLeaderTeams();
      if (leaderTeams.length) {
        await ensureLeaderNames(leaderTeams);
        renderTeams(leaderTeams);
        return;
      }
    } catch (_) {
      // ignore
    }

    // 팀원인데 /teams/me가 실패하는 경우엔 빈 상태로 우선 처리 + 토스트
    console.warn("[my-team] failed to load:", e);
    IM.showToast(e?.message || "내 팀을 불러오지 못했어요.", "warning");
    renderNoTeam();
  }
}


  async function viewCreateTeam() {
    if (!requireAuth()) return;
    await ensureMe();
    await ensureMyTeam(true);

    renderShell("팀 만들기", `
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="card">
            <div class="card-body">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">팀 이름</label>
                  <input class="form-control" id="teamName" placeholder="예: 홍대 스트링 앙상블"/>
                </div>
                <div class="col-md-6">
                  <label class="form-label">연습 위치</label>
                  <div class="border rounded-3 p-2">
                    ${ENUMS.region.map(v => `
                      <div class="form-check">
                        <input class="form-check-input" type="checkbox" value="${v}" id="practiceRegion-${v}">
                        <label class="form-check-label" for="practiceRegion-${v}">
                          ${fmtEnum("region", v)}
                        </label>
                      </div>
                    `).join("")}
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label">팀 안내사항/공지</label>
                  <textarea class="form-control" id="practiceNote" rows="3" placeholder="주 1회 / 주말 / 합주실 장소 등"></textarea>
                </div>
              </div>

              <button class="btn btn-dark mt-3" id="btnCreate">
                <i class="bi bi-plus-circle me-1"></i>생성
              </button>
            </div>
          </div>
        </div>
      </div>
    `);

    document.getElementById("btnCreate").addEventListener("click", async () => {
      const teamName = document.getElementById("teamName").value.trim();
      const practiceRegions = Array.from(document.querySelectorAll("input[id^='practiceRegion-']:checked"))
        .map(input => input.value);
      const practiceNote = document.getElementById("practiceNote").value.trim();

      if (!practiceRegions.length) {
              IM.showToast("연습 위치를 선택해 주세요.", "warning");
              return;
            }

      const res = await IM.apiFetch("/teams", {
        method: "POST",
        body: { teamName, practiceRegions, practiceNote }
      });

      IM.showToast("팀이 생성되었습니다.", "success");
      // 생성자는 자동으로 팀에 속한 상태이므로 UI에서도 팀 만들기 메뉴를 숨김
      state.myTeam = { id: res.teamId, teamId: res.teamId, leaderUserId: state.me?.userId, teamName, practiceRegions, practiceNote, createdAt: new Date().toISOString() };
      state.myTeams = [state.myTeam];
      renderNav();
      window.location.hash = `#/teams/${res.teamId}`;
    });
  }

  async function viewTeamDetail(teamId) {
    if (!requireAuth()) return;
    renderShell("팀 상세", `
      <div class="muted">불러오는 중...</div>
    `);

    const [team, positions] = await Promise.all([
      IM.apiFetch(`/teams/${teamId}`, { auth: true }),
      IM.apiFetch(`/teams/${teamId}/positions`, { auth: true })
    ]);

    // 남은 슬롯 시각화를 위한 통계(있으면 사용)
    // 권장 API: GET /teams/{teamId}/positions/stats -> [{ positionId, acceptedCount }]
    let stats = null;
    try {
      stats = await IM.apiFetch(`/teams/${teamId}/positions/stats`, { auth: true, silent: true });
      cache.positionStatsByTeamId[teamId] = stats;
    } catch {
      stats = cache.positionStatsByTeamId[teamId] || null;
    }
    const statMap = stats
      ? Object.fromEntries(stats.map(s => [String(s.positionId), Number(s.acceptedCount || 0)]))
      : null;

    await ensureMe();
    const me = state.me;
    const isLeader = me && (Number(me.userId) === Number(team.leaderUserId));

    renderShell(IM.escapeHtml(team.teamName), `
      <div class="row g-3">
        <div class="col-lg-5">
          <div class="card">
            <div class="card-body">
              <div class="fw-semibold mb-2">팀 정보</div>
              <div class="mb-1"><span class="muted">조율 중인 연습 위치</span> : ${IM.escapeHtml(formatPracticeRegions(team))}</div>
              <br><div class="mb-1"><span class="muted">팀 생성 날짜</span> : ${IM.escapeHtml(fmtDate(team.createdAt))}</div>
              <br><div class="mt-2">
                <div class="d-flex align-items-center justify-content-between">
                  <div class="muted small">팀 안내사항/공지</div>
                  <div class="d-flex gap-2">
                    <button class="btn btn-sm btn-outline-secondary" id="btnViewPracticeNote">보기</button>
                    ${isLeader ? `<button class="btn btn-sm btn-dark" id="btnEditPracticeNote">수정</button>` : ""}
                  </div>
                </div>
              </div>
            </div>
          </div>

          ${isLeader ? leaderCreatePositionCard(teamId) : ""}
        </div>

        <div class="col-lg-7">
          <div class="card">
            <div class="card-body">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <div class="fw-semibold">모집 포지션</div>
                <div class="muted small">${positions.length}개</div>
              </div>

              <div class="table-responsive">
                <table class="table align-middle">
                  <thead>
                    <tr>
                      <th>악기</th>
                      <th>정원</th>
                      <th>남은 자리</th>
                      <th>최소 레벨</th>
                      <th class="d-none d-md-table-cell">생성일</th>
                      <th class="text-end"></th>
                    </tr>
                  </thead>
                  <tbody id="posRows">
                    ${positions.length ? positions.map(p => positionRowHtml(p, teamId, isLeader, me, team, statMap)).join("") : `<tr><td colspan="6" class="muted">등록된 포지션이 없습니다.</td></tr>`}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="card mt-3" id="teamChatCard">
                      <div class="card-body">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                          <div class="fw-semibold">팀 메시지</div>
                          <button class="btn btn-sm btn-outline-dark" id="btnReloadChat">
                            <i class="bi bi-arrow-clockwise me-1"></i>새로고침
                          </button>
                        </div>
                        <div id="teamChatBody" class="im-chat-body muted">불러오는 중...</div>
                        <div class="input-group mt-2">
                          <input class="form-control" id="teamChatInput" placeholder="메시지를 입력하세요" maxlength="500"/>
                          <button class="btn btn-dark" id="btnSendChat"><i class="bi bi-send me-1"></i>보내기</button>
                        </div>
                        <div class="small-help mt-2" id="teamChatHelp">팀 멤버만 이용 가능</div>
                      </div>
                    </div>

        </div>
      </div>

      <!-- Applicants modal -->
      <div class="modal fade" id="applicantsModal" tabindex="-1">
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">지원자 목록</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <div class="row g-2 mb-2">
                <div class="col-md-5">
                  <label class="form-label">상태 필터</label>
                  <select class="form-select" id="jrStatusFilter">
                    <option value="">전체</option>
                    ${ENUMS.joinStatus.map(s => `<option value="${s}">${fmtEnum("joinStatus", s)}</option>`).join("")}
                  </select>
                </div>
                <div class="col-md-7 d-flex align-items-end">
                  <button class="btn btn-dark" id="btnReloadApplicants"><i class="bi bi-arrow-clockwise me-1"></i>새로고침</button>
                </div>
              </div>
              <div id="applicantsBody" class="muted">불러오는 중...</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Apply modal -->
      <div class="modal fade" id="applyModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">지원 소개글</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <label class="form-label">소개글</label>
              <textarea class="form-control" id="applyMessage" rows="4" maxlength="500" placeholder="간단히 자신을 소개해 주세요."></textarea>
              <div class="small muted mt-2">최대 500자</div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
              <button class="btn btn-dark" id="btnSubmitApply">지원하기</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Join message modal -->
      <div class="modal fade" id="joinMessageModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="joinMessageTitle">소개글</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="joinMessageBody">
              <div class="muted">불러오는 중...</div>
            </div>
          </div>
        </div>
      </div>
      ${renderPracticeNoteModal()}
      <div class="modal fade" id="practiceNoteEditModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">팀 안내사항/공지 수정</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <label class="form-label">공지 내용</label>
              <textarea class="form-control" id="practiceNoteEditInput" rows="4" maxlength="500"></textarea>
              <div class="small muted mt-2">최대 500자</div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
              <button class="btn btn-dark" id="btnSavePracticeNote">저장</button>
            </div>
          </div>
        </div>
      </div>
    `);

    // bind apply & leader buttons
    let pendingApplyPositionId = null;
    const applyModalEl = document.getElementById("applyModal");
    const applyModal = applyModalEl ? new bootstrap.Modal(applyModalEl) : null;
    const applyMessageEl = document.getElementById("applyMessage");
    const applySubmitBtn = document.getElementById("btnSubmitApply");

    if (applyModalEl) {
      applyModalEl.addEventListener("hidden.bs.modal", () => {
        pendingApplyPositionId = null;
        if (applyMessageEl) applyMessageEl.value = "";
      });
    }

    const viewNoteBtn = document.getElementById("btnViewPracticeNote");
    if (viewNoteBtn) {
      viewNoteBtn.addEventListener("click", () => {
        openPracticeNoteModal(team.teamName || "팀", team.practiceNote || "");
      });
    }

    const editNoteBtn = document.getElementById("btnEditPracticeNote");
    const editModalEl = document.getElementById("practiceNoteEditModal");
    const editModal = editModalEl ? new bootstrap.Modal(editModalEl) : null;
    const editInput = document.getElementById("practiceNoteEditInput");
    const saveNoteBtn = document.getElementById("btnSavePracticeNote");

    if (editNoteBtn && editModal && editInput && saveNoteBtn) {
      editNoteBtn.addEventListener("click", () => {
        editInput.value = team.practiceNote || "";
        editModal.show();
      });

      saveNoteBtn.addEventListener("click", async () => {
        const nextNote = editInput.value.trim();
        saveNoteBtn.disabled = true;
        try {
          const updated = await IM.apiFetch(`/teams/${teamId}/practice-note`, {
            method: "PATCH",
            body: { practiceNote: nextNote || null }
          });
          team.practiceNote = updated?.practiceNote ?? nextNote;
          IM.showToast("공지사항이 수정되었습니다.", "success");
          editModal.hide();
        } finally {
          saveNoteBtn.disabled = false;
        }
      });
    }

    if (applySubmitBtn) {
      applySubmitBtn.addEventListener("click", async () => {
        if (!pendingApplyPositionId) return;
        const message = applyMessageEl?.value?.trim();
        applySubmitBtn.disabled = true;
        try {
          const res = await IM.apiFetch(`/teams/${teamId}/positions/${pendingApplyPositionId}/join-requests`, {
            method: "POST",
            body: { message: message || null }
          });
          IM.addMyJoinRequestId(res);
          IM.showToast(`지원 완료!`, "success");
          if (applyModal) applyModal.hide();
        } finally {
          applySubmitBtn.disabled = false;
          if (applyMessageEl) applyMessageEl.value = "";
          pendingApplyPositionId = null;
        }
      });
    }

    document.querySelectorAll("[data-apply]").forEach(btn => {
      btn.addEventListener("click", async () => {
        if (!requireAuth()) return;
        await ensureMyTeam();
        const myTeams = Array.isArray(state.myTeams) ? state.myTeams : (state.myTeam ? [state.myTeam] : []);
        const myTeamIds = myTeams.map(t => String(pickTeamId(t)));
        const isMemberOfOtherTeam = myTeamIds.some(id => id && id !== String(teamId));
        const isLeaderOfOtherTeam = myTeams.some(t => Number(t.leaderUserId) === Number(state.me?.userId) && String(pickTeamId(t)) !== String(teamId));

        if (isMemberOfOtherTeam && !isLeaderOfOtherTeam) {
          IM.showToast("이미 다른 팀에 합류되어 있어 지원할 수 없습니다.", "warning");
          return;
        }

        pendingApplyPositionId = btn.getAttribute("data-apply");
        if (applyMessageEl) applyMessageEl.value = "";
        if (applyModal) applyModal.show();
      });
    });

    if (isLeader) {
      // create position
      document.getElementById("btnCreatePos").addEventListener("click", async () => {
        const instrument = document.getElementById("posInstrument").value;
        const capacity = Number(document.getElementById("posCapacity").value);
        const requiredLevelMin = document.getElementById("posLevel").value;

        const id = await IM.apiFetch(`/teams/${teamId}/positions`, {
          method: "POST",
          body: { instrument, capacity, requiredLevelMin }
        });
        IM.showToast(`포지션 생성됨 (id=${id})`, "success");
        window.location.hash = `#/teams/${teamId}`; // reload
      });

      // open applicants modal
      document.querySelectorAll("[data-applicants]").forEach(btn => {
        btn.addEventListener("click", async () => {
          const positionId = btn.getAttribute("data-applicants");
          openApplicantsModal(teamId, positionId);
        });
      });
    }

    initPositionStatsPolling(teamId, positions);
    initTeamChat(teamId);
  }

  function positionRowHtml(p, teamId, isLeader, me, team, statMap) {
    const actions = isLeader
      ? `<button class="btn btn-sm btn-outline-dark" data-applicants="${p.id}"><i class="bi bi-people me-1"></i>지원자</button>`
      : `<button class="btn btn-sm btn-dark" data-apply="${p.id}"><i class="bi bi-send me-1"></i>지원</button>`;

    const total = Number(p.capacity || 0);
    const used = (statMap && Object.prototype.hasOwnProperty.call(statMap, String(p.id)))
      ? Number(statMap[String(p.id)] || 0)
      : 0;
    const slotHtml = renderSlotHtml(total, used);

    return `
      <tr>
        <td>${IM.escapeHtml(fmtEnum("instrument", p.instrument) || p.instrument)}</td>
        <td>${p.capacity}</td>
        <td id="posSlot-${p.id}" data-position-id="${p.id}" data-capacity="${total}">${slotHtml}</td>
        <td>${IM.escapeHtml(fmtEnum("level", p.requiredLevelMin) || p.requiredLevelMin)}</td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(p.createdAt))}</td>
        <td class="text-end">${actions}</td>
      </tr>
    `;
  }

  function updatePositionSlots(positions, stats) {
    const statMap = Array.isArray(stats)
      ? Object.fromEntries(stats.map(s => [String(s.positionId), Number(s.acceptedCount || 0)]))
      : {};
    positions.forEach((position) => {
      const cell = document.getElementById(`posSlot-${position.id}`);
      if (!cell) return;
      const total = Number(position.capacity || 0);
      const used = Number(statMap[String(position.id)] || 0);
      cell.innerHTML = renderSlotHtml(total, used);
    });
  }

  function initPositionStatsPolling(teamId, positions) {
    if (!positions.length) return;
    const rows = document.getElementById("posRows");
    if (!rows) return;

    const loadStats = async () => {
      try {
        const stats = await IM.apiFetch(`/teams/${teamId}/positions/stats`, { auth: true, silent: true });
        cache.positionStatsByTeamId[teamId] = stats;
        updatePositionSlots(positions, stats);
      } catch {
        // ignore background errors
      }
    };

    registerInterval(setInterval(() => {
      if (!document.getElementById("posRows")) return;
      loadStats();
    }, 10000));
    loadStats();
  }

  function leaderCreatePositionCard(teamId) {
    return `
      <div class="card mt-3">
        <div class="card-body">
          <div class="fw-semibold mb-2">포지션 슬롯 생성</div>
          <div class="row g-2">
            <div class="col-md-5">
              <label class="form-label">악기</label>
              <select class="form-select" id="posInstrument">
                ${ENUMS.instrument.map(v => `<option value="${v}">${fmtEnum("instrument", v)}</option>`).join("")}
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label">정원</label>
              <input class="form-control" id="posCapacity" type="number" min="1" value="1">
            </div>
            <div class="col-md-4">
              <label class="form-label">최소 레벨</label>
              <select class="form-select" id="posLevel">
                ${ENUMS.level.map(v => `<option value="${v}">${fmtEnum("level", v)}</option>`).join("")}
              </select>
            </div>
          </div>
          <button class="btn btn-dark mt-3" id="btnCreatePos">
            <i class="bi bi-plus-circle me-1"></i>추가
          </button>
        </div>
      </div>
    `;
  }

  function leaderApplicantsCard(teamId) {
    return `
      <div class="card mt-3">
        <div class="card-body">
          <div class="fw-semibold mb-2">장 기능: 지원자 관리</div>
          <div class="small-help">
            포지션 테이블에서 <span class="badge text-bg-light">지원자</span> 버튼을 눌러 목록을 보고 수락/거절할 수 있어요.
          </div>
        </div>
      </div>
    `;
  }

  function renderChatMessages(list) {
      if (!list.length) {
        return `<div class="muted">아직 메시지가 없습니다.</div>`;
      }

      return list.map(msg => `
        <div class="im-chat-message">
          <div class="d-flex justify-content-between">
            <div class="fw-semibold">${IM.escapeHtml(msg.senderName || "-")}</div>
            <div class="small muted">${IM.escapeHtml(fmtDate(msg.createdAt))}</div>
          </div>
          <div class="mt-1">${IM.escapeHtml(msg.message || "")}</div>
        </div>
      `).join("");
    }

    async function initTeamChat(teamId) {
      const bodyEl = document.getElementById("teamChatBody");
      const inputEl = document.getElementById("teamChatInput");
      const sendBtn = document.getElementById("btnSendChat");
      const reloadBtn = document.getElementById("btnReloadChat");
      const helpEl = document.getElementById("teamChatHelp");
      if (!bodyEl || !inputEl || !sendBtn || !reloadBtn) return;

      const setEnabled = (enabled) => {
        inputEl.disabled = !enabled;
        sendBtn.disabled = !enabled;
      };

      let pollingEnabled = true;

      async function loadMessages({ showLoading = true } = {}) {
        if (showLoading) {
          bodyEl.innerHTML = `<div class="muted">불러오는 중...</div>`;
        }
        try {
          const list = await IM.apiFetch(`/teams/${teamId}/chat`, { auth: true, silent: !showLoading });
          const lastId = list && list.length ? String(list[list.length - 1].id || "") : "";
          const rendered = renderChatMessages(list || []);
          if (!showLoading && bodyEl.getAttribute("data-last-chat") === lastId) {
            setEnabled(true);
            if (helpEl) helpEl.textContent = "팀 멤버만 이용 가능";
            return;
          }
          bodyEl.setAttribute("data-last-chat", lastId);
          bodyEl.innerHTML = rendered;
          setEnabled(true);
          if (helpEl) helpEl.textContent = "팀 멤버만 이용 가능";
        } catch (e) {
          if (e && e.status === 403) {
            bodyEl.innerHTML = `<div class="muted">팀 멤버만 이용할 수 있습니다.</div>`;
            setEnabled(false);
            if (helpEl) helpEl.textContent = "팀 멤버만 이용 가능";
            pollingEnabled = false;
            return;
          }
          bodyEl.innerHTML = `<div class="text-danger">불러오기 실패: ${IM.escapeHtml(e.message || "")}</div>`;
          setEnabled(false);
          if (helpEl) helpEl.textContent = "오류가 발생했습니다.";
        }
      }

      reloadBtn.addEventListener("click", async () => {
        await loadMessages();
      });

      sendBtn.addEventListener("click", async () => {
        const text = inputEl.value.trim();
        if (!text) return;
        try {
          sendBtn.disabled = true;
          await IM.apiFetch(`/teams/${teamId}/chat`, { method: "POST", body: { message: text } });
          inputEl.value = "";
          await loadMessages();
        } finally {
          sendBtn.disabled = false;
        }
      });

      inputEl.addEventListener("keydown", async (event) => {
        if (event.key === "Enter" && !event.shiftKey) {
          event.preventDefault();
          sendBtn.click();
        }
      });

      await loadMessages();
      registerInterval(setInterval(() => {
        if (!pollingEnabled || !document.getElementById("teamChatBody")) return;
        loadMessages({ showLoading: false });
      }, 8000));
    }

  async function openProfileModal(userId) {
    const modalEl = document.getElementById("profileModal");
    const titleEl = document.getElementById("profileModalTitle");
    const bodyEl = document.getElementById("profileModalBody");
    if (!modalEl || !titleEl || !bodyEl) {
      IM.showToast("프로필 모달을 찾을 수 없습니다.", "warning");
      return;
    }

    const uid = String(userId);
    titleEl.textContent = `프로필`;
    bodyEl.innerHTML = `<div class="muted">불러오는 중...</div>`;

    bootstrap.Modal.getOrCreateInstance(modalEl).show();

    try {
      let p = cache.profileByUserId[uid];
      if (!p) {
        // 권장 API: GET /profiles/{userId}
        p = await IM.apiFetch(`/profiles/${encodeURIComponent(uid)}`, { auth: true, silent: true });
        cache.profileByUserId[uid] = p;
      }

      bodyEl.innerHTML = `
        <div class="card im-flat-card">
          <div class="card-body">
            <div class="fw-semibold mb-2">${IM.escapeHtml(p.name || "-")}</div>
            <div class="row g-2">
              <div class="col-6"><span class="muted">악기</span> ${IM.escapeHtml(fmtEnum("instrument", p.instrument) || p.instrument || "-")}</div>
              <div class="col-6"><span class="muted">레벨</span> ${IM.escapeHtml(fmtEnum("level", p.level) || p.level || "-")}</div>
              <div class="col-12"><span class="muted">참여 가능한 연습 위치</span> ${IM.escapeHtml(profilePracticeRegionList(p).map(r => fmtEnum("region", r) || r).join(", ") || "-")}</div>
            </div>
            <div class="mt-2 small muted">업데이트: ${IM.escapeHtml(fmtDate(p.updatedAt))}</div>
          </div>
        </div>
      `;
    } catch (e) {
      if (e && e.status === 404) {
        bodyEl.innerHTML = `<div class="muted">프로필 정보가 없습니다.</div>`;
        return;
      }
      bodyEl.innerHTML = `<div class="text-danger">불러오기 실패: ${IM.escapeHtml(e.message || "")}</div>`;
    }
  }

  async function openApplicantsModal(teamId, positionId) {
    const modalEl = document.getElementById("applicantsModal");
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    const statusSel = document.getElementById("jrStatusFilter");
    const bodyEl = document.getElementById("applicantsBody");

    async function load() {
      bodyEl.innerHTML = `<div class="muted">불러오는 중...</div>`;
      const qs = statusSel.value ? `?joinRequestStatus=${encodeURIComponent(statusSel.value)}` : "";
      const list = await IM.apiFetch(`/teams/${teamId}/positions/${positionId}/join-requests${qs}`, { method: "GET" });

      const uids = [...new Set((list || []).map(jr => String(jr.applicantUserId)))];

      await Promise.all(uids.map(async (uid) => {
        if (cache.profileByUserId[uid]) return;
        try {
          const p = await IM.apiFetch(`/profiles/${encodeURIComponent(uid)}`, { auth: true, silent: true });
          cache.profileByUserId[uid] = p;
        } catch (_) {
          // 프로필 조회 실패 시에도 화면 깨지지 않게 최소값 저장
          cache.profileByUserId[uid] = { name: null };
        }
      }));

      const nameOf = (uid) => {
        const p = cache.profileByUserId[String(uid)];
        return p?.name || null;
      };

      if (!list.length) {
        bodyEl.innerHTML = `<div class="muted">지원자가 없습니다.</div>`;
        return;
      }

      const messageById = new Map(list.map(jr => [String(jr.id), jr.message || ""]));

      bodyEl.innerHTML = `
        <div class="table-responsive">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>지원자</th>
                <th>소개글</th>
                <th>상태</th>
                <th class="d-none d-md-table-cell">지원일</th>
                <th class="text-end">처리</th>
              </tr>
            </thead>
            <tbody>
              ${list.map(jr => {
                const decidable = jr.status === "APPLIED";
                const actions = decidable
                  ? `
                    <button class="btn btn-sm btn-success me-1" data-accept="${jr.id}">
                      <i class="bi bi-check2 me-1"></i>수락
                    </button>
                    <button class="btn btn-sm btn-outline-danger" data-reject="${jr.id}">
                      <i class="bi bi-x me-1"></i>거절
                    </button>`
                  : `<span class="muted small">처리불가</span>`;
                const memoButton = jr.message
                  ? `<button class="btn btn-sm btn-outline-secondary" data-message="${jr.id}">보기</button>`
                  : `<span class="muted small">없음</span>`;
                return `
                  <tr>
                    <td>
                      <button class="btn btn-link p-0 im-linklike" data-profile="${jr.applicantUserId}">
                        ${IM.escapeHtml(nameOf(jr.applicantUserId) || `userId ${jr.applicantUserId}`)}
                      </button>
                    </td>
                    <td>${memoButton}</td>
                    <td><span class="badge text-bg-light">${IM.escapeHtml(fmtEnum("joinStatus", jr.status))}</span></td>
                    <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(jr.createdAt))}</td>
                    <td class="text-end">${actions}</td>
                  </tr>
                `;
              }).join("")}
            </tbody>
          </table>
        </div>
      `;

      bodyEl.querySelectorAll("[data-accept]").forEach(btn => {
        btn.addEventListener("click", async () => {
          const id = btn.getAttribute("data-accept");
          await IM.apiFetch(`/join-requests/${id}/accept`, { method: "POST" });
          IM.showToast("수락 처리 완료", "success");
          await load();
        });
      });
      bodyEl.querySelectorAll("[data-reject]").forEach(btn => {
        btn.addEventListener("click", async () => {
          const id = btn.getAttribute("data-reject");
          await IM.apiFetch(`/join-requests/${id}/reject`, { method: "POST" });
          IM.showToast("거절 처리 완료", "secondary");
          await load();
        });
      });

      bodyEl.querySelectorAll("[data-profile]").forEach(btn => {
        btn.addEventListener("click", () => {
          const uid = btn.getAttribute("data-profile");
          openProfileModal(uid);
        });
      });

      bodyEl.querySelectorAll("[data-message]").forEach(btn => {
        btn.addEventListener("click", () => {
          const id = btn.getAttribute("data-message");
          const row = btn.closest("tr");
          const name = row?.querySelector("[data-profile]")?.textContent?.trim() || "";
          openJoinMessageModal(name, messageById.get(String(id)));
        });
      });
    }

    document.getElementById("btnReloadApplicants").onclick = load;
    statusSel.onchange = load;

    await load();
  }

  function openJoinMessageModal(applicantName, message) {
    const modalEl = document.getElementById("joinMessageModal");
    if (!modalEl) return;
    const titleEl = document.getElementById("joinMessageTitle");
    const bodyEl = document.getElementById("joinMessageBody");
    if (titleEl) {
      titleEl.textContent = applicantName ? `${applicantName}님의 소개글` : "소개글";
    }
    if (bodyEl) {
      const sanitized = message ? IM.escapeHtml(message).replace(/\n/g, "<br>") : "";
      bodyEl.innerHTML = sanitized ? `<div class="im-join-message">${sanitized}</div>` : `<div class="muted">소개글이 없습니다.</div>`;
    }
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }

  function viewNotFound() {
    renderShell("페이지를 찾을 수 없어요", `
      <div class="card">
        <div class="card-body">
          <div class="muted">요청하신 화면이 없습니다.</div>
          <a href="#/login" class="btn btn-dark mt-3">로그인으로</a>
        </div>
      </div>
    `);
  }

  // ---------- Router ----------
  function parseHash() {
    const defaultHash = IM.getToken() ? "#/teams" : "#/login";
    const raw = (window.location.hash || defaultHash).replace(/^#/, "");
    const parts = raw.split("/").filter(Boolean);
    return parts; // e.g. ["teams", "3"]
  }

  async function route() {
    const parts = parseHash();

    // re-render nav state
    clearIntervals();
    renderNav();

    try {
      if (parts.length === 0 || (parts[0] === "teams" && parts.length === 1)) {
        await viewTeamsList();
        return;
      }
      if (parts[0] === "login") { await viewLogin(); return; }
      if (parts[0] === "signup") { await viewSignup(); return; }
      if (parts[0] === "profile") { await viewProfile(); return; }
      if (parts[0] === "my-team") { await viewMyTeam(); return; }
      if (parts[0] === "create-team") { await viewCreateTeam(); return; }
      if (parts[0] === "teams" && parts[1]) {
        await viewTeamDetail(parts[1]);
        return;
      }
      viewNotFound();
    } catch (e) {
      // apiFetch already shows toast. Still show fallback.
      console.error(e);
      renderShell("오류", `
        <div class="card">
          <div class="card-body">
            <div class="muted">화면을 불러오는 중 오류가 발생했습니다.</div>
            <div class="small-help mt-2">${IM.escapeHtml(e.message || "")}</div>
            <a href="#/login" class="btn btn-dark mt-3">로그인으로</a>
          </div>
        </div>
      `);
    }
  }

  // boot
  (async () => {
    await ensureMe();
    renderNav();
    window.addEventListener("hashchange", route);
    await route();
  })();
})();
