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
    loadingMyTeam: false
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

  async function ensureMe() {
    const token = IM.getToken();
    if (!token) { state.me = null; state.myTeam = undefined; return null; }
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
      state.loadingMyTeam = false;
      return null;
    }

    if (!force && state.myTeam !== undefined) return state.myTeam;
    if (state.loadingMyTeam) return state.myTeam;

    state.loadingMyTeam = true;
    try {
      const t = await IM.apiFetch("/teams/me", { auth: true, silent: true });
      state.myTeam = t || null;
    } catch (e) {
      const status = e?.status ?? e?.statusCode ?? e?.httpStatus ?? e?.response?.status ?? e?.data?.status;
      if (status === 404 || status === 204) {
        state.myTeam = null;
      } else {
        // 네트워크/서버 오류 등은 'unknown'으로 두고 UI를 과도하게 막지 않음
        if (state.myTeam === undefined) state.myTeam = undefined;
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
      if (!hasTeam()) {
        navRight.append(
          navItem("#/create-team", "팀 만들기", "bi-plus-circle"),
        );
      }
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
          <label class="form-label">지역(선택)</label>
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
                  <th>팀</th>
                  <th>연습 지역</th>
                  <th class="d-none d-lg-table-cell">남은 포지션</th>
                  <th>메모</th>
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
    return `
      <tr>
        <td class="text-truncate">${IM.escapeHtml(t.teamName || "")}</td>
        <td>${IM.escapeHtml(formatPracticeRegions(t))}</td>
        <td class="d-none d-lg-table-cell" id="teamSlot-${IM.escapeHtml(String(teamId))}">
          <span class="muted small">불러오는 중...</span>
        </td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(memo || "-")}</td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(t.createdAt))}</td>
        <td class="text-end">
          <a class="btn btn-sm btn-dark" href="#/teams/${IM.escapeHtml(String(teamId))}">
            상세 <i class="bi bi-chevron-right ms-1"></i>
          </a>
        </td>
      </tr>
    `;
  }).join("");

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
                  <label class="form-label">지역</label>
                  <select class="form-select" id="region">
                    ${ENUMS.region.map(v => `<option value="${v}">${fmtEnum("region", v)}</option>`).join("")}
                  </select>
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
      const region = document.getElementById("region").value;

      await IM.apiFetch("/auth/signup", {
        method: "POST",
        auth: false,
        body: { email, password, name, instrument, level, region }
      });

      IM.showToast("회원가입 완료! 로그인해 주세요.", "success");
      window.location.hash = "#/login";
    });
  }

  async function viewProfile() {
    if (!requireAuth()) return;
    renderShell("내 프로필", `
      <div class="card">
        <div class="card-body">
          <div class="muted">불러오는 중...</div>
        </div>
      </div>
    `);

    const me = await ensureMe();
    if (!me) return;

    renderShell("내 프로필", `
      <div class="row g-3">
        <div class="col-lg-5">
          <div class="card">
            <div class="card-body">
              <div class="fw-semibold mb-2">현재 정보</div>
              <div class="mb-1"><span class="muted">이름</span> : ${IM.escapeHtml(me.name)}</div>
              <div class="mb-1"><span class="muted">악기</span> : ${IM.escapeHtml(fmtEnum("instrument", me.instrument) || me.instrument)}</div>
              <div class="mb-1"><span class="muted">레벨</span> : ${IM.escapeHtml(fmtEnum("level", me.level) || me.level)}</div>
              <div class="mb-1"><span class="muted">지역</span> : ${IM.escapeHtml(fmtEnum("region", me.region) || me.region)}</div>
              <div class="mt-2 small muted">업데이트: ${IM.escapeHtml(fmtDate(me.updatedAt))}</div>
            </div>
          </div>
        </div>

        <div class="col-lg-7">
          <div class="card">
            <div class="card-body">
              <div class="fw-semibold mb-2">수정</div>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">이름</label>
                  <input class="form-control" id="name" value="${IM.escapeHtml(me.name)}"/>
                </div>
                <div class="col-md-6">
                  <label class="form-label">악기</label>
                  <select class="form-select" id="instrument">
                    ${ENUMS.instrument.map(v => `<option value="${v}" ${v===me.instrument?'selected':''}>${fmtEnum("instrument", v)}</option>`).join("")}
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">레벨</label>
                  <select class="form-select" id="level">
                    ${ENUMS.level.map(v => `<option value="${v}" ${v===me.level?'selected':''}>${fmtEnum("level", v)}</option>`).join("")}
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">지역</label>
                  <select class="form-select" id="region">
                    ${ENUMS.region.map(v => `<option value="${v}" ${v===me.region?'selected':''}>${fmtEnum("region", v)}</option>`).join("")}
                  </select>
                </div>
              </div>

              <button class="btn btn-dark mt-3" id="btnSave">
                <i class="bi bi-check2-circle me-1"></i>저장
              </button>
            </div>
          </div>

          <div class="card mt-3">
            <div class="card-body">
              <div class="fw-semibold mb-2">최근 지원</div>
              <div class="small-help mb-2">최근 지원한 팀/포지션과 상태를 확인할 수 있어요.</div>
              <div id="jrList" class="vstack gap-2"></div>
            </div>
          </div>
        </div>
      </div>
    `);

    document.getElementById("btnSave").addEventListener("click", async () => {
      const name = document.getElementById("name").value.trim();
      const instrument = document.getElementById("instrument").value;
      const level = document.getElementById("level").value;
      const region = document.getElementById("region").value;

      await IM.apiFetch("/profiles/me", { method: "PATCH", body: { name, instrument, level, region } });
      IM.showToast("저장되었습니다.", "success");
      state.me = null;
      await ensureMe();
      window.location.hash = "#/profile";
    });

    renderMyJoinRequests();
  }

async function renderMyJoinRequests() {
  const box = document.getElementById("jrList");
  if (!box) return;

  box.innerHTML = `<div class="muted">불러오는 중...</div>`;

  let list = [];
  try {
    const raw = await IM.apiFetch("/join-requests/me", { auth: true, silent: true });
    list = asArray(raw);
  } catch (e) {
    box.innerHTML = `<div class="text-danger small">지원 내역을 불러오지 못했어요.</div>`;
    return;
  }

  if (!list.length) {
    box.innerHTML = `
      <div class="border rounded-3 p-3">
        <div class="fw-semibold">최근 지원 내역이 없습니다</div>
        <div class="muted small mt-1">팀 찾기에서 원하는 팀에 지원해보세요.</div>
        <a class="btn btn-sm btn-dark mt-3" href="#/teams">
          <i class="bi bi-search me-1"></i>팀 찾기
        </a>
      </div>
    `;
    return;
  }

  box.innerHTML = list.map(jr => {
    const statusText = fmtEnum("joinStatus", jr.status) || jr.status;
    const teamName = jr.team?.teamName || "-";
    const region = jr.team ? formatPracticeRegions(jr.team) : "-";
    const instrument = jr.position?.instrument ? (fmtEnum("instrument", jr.position.instrument) || jr.position.instrument) : "-";
    const levelMin = jr.position?.requiredLevelMin ? (fmtEnum("level", jr.position.requiredLevelMin) || jr.position.requiredLevelMin) : "-";
    const createdAt = fmtDate(jr.createdAt);
    const cancelBtn = jr.cancellable
      ? `<button class="btn btn-sm btn-outline-danger" data-jr-cancel="${jr.id}">
           <i class="bi bi-x-circle me-1"></i>취소
         </button>`
      : `<span class="muted small">-</span>`;

    return `
      <div class="border rounded-3 p-3">
        <div class="d-flex align-items-start justify-content-between gap-2">
          <div class="min-w-0">
            <div class="fw-semibold text-truncate">${IM.escapeHtml(teamName)}</div>
            <div class="muted small mt-1">
              ${IM.escapeHtml(region)} · ${IM.escapeHtml(instrument)} · 최소 ${IM.escapeHtml(levelMin)}
            </div>
            <div class="small mt-2">
              <span class="badge text-bg-light">${IM.escapeHtml(statusText)}</span>
              <span class="muted ms-2">${IM.escapeHtml(createdAt)}</span>
            </div>
          </div>
          <div class="d-flex flex-column align-items-end gap-2">
            ${cancelBtn}
            ${jr.team?.id ? `<a class="btn btn-sm btn-outline-dark" href="#/teams/${jr.team.id}">
                상세 <i class="bi bi-chevron-right ms-1"></i>
              </a>` : ``}
          </div>
        </div>
      </div>
    `;
  }).join("");

  box.querySelectorAll("[data-jr-cancel]").forEach(btn => {
    btn.addEventListener("click", async () => {
      const id = btn.getAttribute("data-jr-cancel");
      try {
        await IM.apiFetch(`/join-requests/${id}/cancel`, { method: "POST" });
        IM.showToast("지원이 취소되었습니다.", "secondary");
        renderMyJoinRequests();
      } catch (e) {
        IM.showToast(e?.message || "취소에 실패했습니다.", "danger");
      }
    });
  });
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

  const renderTeam = (team) => {
    const teamId = pickTeamId(team);
    if (!teamId) return renderNoTeam();

    renderShell("내 팀", `
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="card">
            <div class="card-body">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <div class="fw-semibold">${IM.escapeHtml(team.teamName || "(이름 없음)")}</div>
              </div>

              <div class="mb-1">
                <span class="muted">연습 지역</span> :
                ${IM.escapeHtml(formatPracticeRegions(team))}
              </div>

              <div class="mb-1">
                <span class="muted">팀장</span> :
                ${IM.escapeHtml(team.leaderName || me?.name || "-")}
              </div>

              <div class="mb-1">
                <span class="muted">생성일</span> :
                ${IM.escapeHtml(fmtDate(team.createdAt))}
              </div>

              <div class="mt-2">
                <div class="muted small">메모</div>
                <div>${IM.escapeHtml(team.practiceNote || "-")}</div>
              </div>

              <div class="d-flex gap-2 mt-3">
                <a class="btn btn-dark" href="#/teams/${IM.escapeHtml(teamId)}">
                  <i class="bi bi-box-arrow-up-right me-1"></i>팀 상세 보기
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `);
  };

  // 팀장(내가 만든 팀) fallback: /teams 목록에서 leaderUserId로 내 팀 찾기
  async function findLeaderTeam() {
    if (!me?.userId) return null;
    const list = asArray(await IM.apiFetch("/teams", { auth: true, silent: true }));
    const mine = list
      .filter(t => Number(t.leaderUserId) === Number(me.userId))
      .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
    return mine[0] || null;
  }

  try {
    // 1) 우선 "내가 속한 팀" 조회 (팀원/팀장 모두 여기서 내려오면 가장 좋음)
    const raw = await IM.apiFetch("/teams/me", { auth: true, silent: true });
    let team = Array.isArray(raw) ? raw[0] : raw;

    // 2) /teams/me가 비어있으면 → "내가 만든 팀"으로 fallback
    if (!pickTeamId(team)) {
      const leaderTeam = await findLeaderTeam();
      if (leaderTeam) team = leaderTeam;
    }

    if (!pickTeamId(team)) {
      renderNoTeam();
      return;
    }

    renderTeam(team);
  } catch (e) {
    // 어떤 형태의 실패든: 우선 팀장 fallback을 시도하고, 실패하면 빈 상태로 처리 (라우터 에러 화면 방지)
    try {
      const leaderTeam = await findLeaderTeam();
      if (leaderTeam) {
        renderTeam(leaderTeam);
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
    if (hasTeam()) {
      IM.showToast("이미 팀에 속해 있어 팀을 생성할 수 없습니다.", "warning");
      window.location.hash = "#/my-team";
      return;
    }

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
                  <label class="form-label">연습 지역</label>
                  <select class="form-select" id="practiceRegions" multiple size="5">
                    ${ENUMS.region.map(v => `<option value="${v}">${fmtEnum("region", v)}</option>`).join("")}
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label">연습 메모(선택)</label>
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
      const practiceRegions = Array.from(document.getElementById("practiceRegions").selectedOptions).map(opt => opt.value);
      const practiceNote = document.getElementById("practiceNote").value.trim();

      if (!practiceRegions.length) {
              IM.showToast("연습 지역을 선택해 주세요.", "warning");
              return;
            }

      const res = await IM.apiFetch("/teams", {
        method: "POST",
        body: { teamName, practiceRegions, practiceNote }
      });

      IM.showToast("팀이 생성되었습니다.", "success");
      // 생성자는 자동으로 팀에 속한 상태이므로 UI에서도 팀 만들기 메뉴를 숨김
      state.myTeam = { id: res.teamId, teamId: res.teamId, leaderUserId: state.me?.userId, teamName, practiceRegions, practiceNote, createdAt: new Date().toISOString() };
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
              <div class="mb-1"><span class="muted">연습 지역</span> : ${IM.escapeHtml(formatPracticeRegions(team))}</div>
              <br><div class="mb-1"><span class="muted">팀 생성 날짜</span> : ${IM.escapeHtml(fmtDate(team.createdAt))}</div>
              <br><div class="mt-2">
                <div class="muted small">상세 정보</div>
                <div>${IM.escapeHtml(team.practiceNote || "-")}</div>
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
    `);

    // bind apply & leader buttons
    document.querySelectorAll("[data-apply]").forEach(btn => {
      btn.addEventListener("click", async () => {
        if (!requireAuth()) return;
        await ensureMyTeam();
        const myTeamId = pickTeamId(state.myTeam);
        if (myTeamId && Number(myTeamId) !== Number(teamId)) {
          IM.showToast("이미 다른 팀에 합류되어 있어 지원할 수 없습니다.", "warning");
          return;
        }
        if (isLeader) {
          IM.showToast("팀장은 지원할 수 없습니다.", "warning");
          return;
        }
        const positionId = btn.getAttribute("data-apply");
        const res = await IM.apiFetch(`/teams/${teamId}/positions/${positionId}/join-requests`, { method: "POST" });
        IM.addMyJoinRequestId(res);
        IM.showToast(`지원 완료! joinRequestId=${res}`, "success");
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
  }

  function positionRowHtml(p, teamId, isLeader, me, team, statMap) {
    const actions = isLeader
      ? `<button class="btn btn-sm btn-outline-dark" data-applicants="${p.id}"><i class="bi bi-people me-1"></i>지원자</button>`
      : `<button class="btn btn-sm btn-dark" data-apply="${p.id}"><i class="bi bi-send me-1"></i>지원</button>`;

    const total = Number(p.capacity || 0);
    const used = (statMap && Object.prototype.hasOwnProperty.call(statMap, String(p.id)))
      ? Number(statMap[String(p.id)] || 0)
      : 0;
    const remain = Math.max(0, total - used);
    const pct = total ? Math.round((used / total) * 100) : 0;
    const slotHtml = `
      <div class="progress im-progress-sm">
        <div class="progress-bar" style="width:${pct}%"></div>
      </div>
      <div class="small muted mt-1">남은 자리: ${remain}</div>
    `;

    return `
      <tr>
        <td>${IM.escapeHtml(fmtEnum("instrument", p.instrument) || p.instrument)}</td>
        <td>${p.capacity}</td>
        <td>${slotHtml}</td>
        <td>${IM.escapeHtml(fmtEnum("level", p.requiredLevelMin) || p.requiredLevelMin)}</td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(p.createdAt))}</td>
        <td class="text-end">${actions}</td>
      </tr>
    `;
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
          <div class="fw-semibold mb-2">팀장 기능: 지원자 관리</div>
          <div class="small-help">
            포지션 테이블에서 <span class="badge text-bg-light">지원자</span> 버튼을 눌러 목록을 보고 수락/거절할 수 있어요.
          </div>
        </div>
      </div>
    `;
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
              <div class="col-12"><span class="muted">지역</span> ${IM.escapeHtml(fmtEnum("region", p.region) || p.region || "-")}</div>
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

      bodyEl.innerHTML = `
        <div class="table-responsive">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>지원자</th>
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
                return `
                  <tr>
                    <td>
                      <button class="btn btn-link p-0 im-linklike" data-profile="${jr.applicantUserId}">
                        ${IM.escapeHtml(nameOf(jr.applicantUserId) || `userId ${jr.applicantUserId}`)}
                      </button>
                    </td>
                    <td><span class="badge text-bg-light">${IM.escapeHtml(fmtEnum("joinStatus", jr.status))}</span></td>
                    <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(jr.createdAt))}</td>
                    <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(jr.updatedAt))}</td>
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
    }

    document.getElementById("btnReloadApplicants").onclick = load;
    statusSel.onchange = load;

    await load();
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
