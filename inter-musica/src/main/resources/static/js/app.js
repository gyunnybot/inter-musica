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
    loadingMe: false
  };

  // 간단 캐시(지원자 프로필 등 반복 조회 방지)
  const cache = {
    profileByUserId: Object.create(null),
    positionStatsByTeamId: Object.create(null)
  };

  function asArray(v) {
    return Array.isArray(v) ? v : [];
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
    if (!token) { state.me = null; return null; }
    if (state.me) return state.me;
    if (state.loadingMe) return state.me;

    state.loadingMe = true;
    try {
      state.me = await IM.apiFetch("/profiles/me", { auth: true });
    } catch {
      state.me = null;
    } finally {
      state.loadingMe = false;
      renderNav();
    }
    return state.me;
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
        navItem("#/profile", me ? (me.name + " 님") : "내 프로필", "bi-person-circle"),
      );

      const li = document.createElement("li");
      li.className = "nav-item";
      const btn = document.createElement("button");
      btn.className = "btn btn-sm btn-outline-light ms-lg-2";
      btn.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i>로그아웃';
      btn.addEventListener("click", () => {
        IM.setToken(null);
        state.me = null;
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
                  <th class="d-none d-lg-table-cell">남은 자리</th>
                  <th class="d-none d-md-table-cell">메모</th>
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
    rows.innerHTML = `<tr><td colspan="6" class="muted">불러오는 중...</td></tr>`;

    if (!rows || !countEl) return;

    const qs = region ? ("?region=" + encodeURIComponent(region)) : "";
    const raw = await IM.apiFetch("/teams" + qs, { auth: true });
    const list = asArray(raw);

    countEl.textContent = `${list.length}개`;
    if (!list.length) {
      rows.innerHTML = `<tr><td colspan="6" class="muted">아직 생성된 팀이 없습니다.</td></tr>`;
      return;
    }

    rows.innerHTML = list.map(t => `
      <tr>
        <td>
          <div class="fw-semibold">${IM.escapeHtml(t.teamName)}</div>
          <div class="muted small">leaderUserId: ${t.leaderUserId}</div>
        </td>
        <td>${IM.escapeHtml(fmtEnum("region", t.practiceRegion) || t.practiceRegion || "-")}</td>
        <td class="d-none d-lg-table-cell" id="teamSlot-${t.id}"><span class="muted small">-</span></td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(t.practiceNote || "-")}</td>
        <td class="d-none d-md-table-cell">${IM.escapeHtml(fmtDate(t.createdAt))}</td>
        <td class="text-end">
          <a class="btn btn-sm btn-outline-dark" href="#/teams/${t.id}">
            상세 <i class="bi bi-chevron-right ms-1"></i>
          </a>
        </td>
      </tr>
    `).join("");

    // 각 팀별 남은 자리 시각화(가능한 경우)
    enhanceTeamListSlots(list).catch(() => {});
  }

  async function enhanceTeamListSlots(list) {
    // 팀 목록이 많을 수 있어서 너무 무겁게 하지 않기 위해...
    for (const t of list) {
      const cell = document.getElementById(`teamSlot-${t.id}`);
      if (!cell) continue;

      // 이미 다른 화면으로 이동했으면 중단
      if (!document.getElementById("teamRows")) break;

      try {
        const [positions, stats] = await Promise.all([
          IM.apiFetch(`/teams/${t.id}/positions`, { auth: true, silent: true }),
          IM.apiFetch(`/teams/${t.id}/positions/stats`, { auth: true, silent: true })
        ]);

        const total = Array.isArray(positions) ? positions.reduce((acc, p) => acc + Number(p.capacity || 0), 0) : 0;
        const used = Array.isArray(stats) ? stats.reduce((acc, s) => acc + Number(s.acceptedCount || 0), 0) : 0;
        const remain = Math.max(0, total - used);
        const pct = total ? Math.round((used / total) * 100) : 0;

        cell.innerHTML = total
          ? `
            <div class="progress im-progress-sm">
              <div class="progress-bar" style="width:${pct}%"></div>
            </div>
            <div class="small muted mt-1">남은 ${remain}/${total}</div>
          `
          : `<span class="muted small">-</span>`;
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
              <div class="mb-1"><span class="muted">UserId</span> : <span class="fw-semibold">${me.userId}</span></div>
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
              <div class="fw-semibold mb-2">최근 지원(브라우저에 저장됨)</div>
              <div class="small-help mb-2">현재 백엔드에 “내 지원 목록 조회” API가 없어서, 지원 ID를 로컬에만 저장해요.</div>
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

  function renderMyJoinRequests() {
    const box = document.getElementById("jrList");
    if (!box) return;
    const ids = IM.getMyJoinRequestIds();
    if (!ids.length) {
      box.innerHTML = `<div class="muted">최근 지원 내역이 없습니다.</div>`;
      return;
    }
    box.innerHTML = ids.map(id => `
      <div class="d-flex align-items-center justify-content-between border rounded-3 p-2">
        <div>
          <div class="fw-semibold">joinRequestId: ${id}</div>
          <div class="muted small">취소는 /join-requests/{id}/cancel 호출</div>
        </div>
        <button class="btn btn-sm btn-outline-danger" data-jr="${id}">
          <i class="bi bi-x-circle me-1"></i>취소
        </button>
      </div>
    `).join("");

    box.querySelectorAll("button[data-jr]").forEach(btn => {
      btn.addEventListener("click", async () => {
        const id = Number(btn.getAttribute("data-jr"));
        await IM.apiFetch(`/join-requests/${id}/cancel`, { method: "POST" });
        IM.removeMyJoinRequestId(id);
        IM.showToast("지원이 취소되었습니다.", "secondary");
        renderMyJoinRequests();
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

        const getStatus = (e) =>
          e?.status ?? e?.statusCode ?? e?.httpStatus ?? e?.response?.status ?? e?.data?.status;

      try {
        // ✅ 백엔드가 제공해야 하는 엔드포인트(권장)
        // GET /teams/me  -> { id, teamName, leaderUserId, practiceRegion, practiceNote, createdAt }
        const raw = await IM.apiFetch("/teams/me", { auth: true, silent: true });
        const team = Array.isArray(raw) ? raw[0] : raw;

         if (!team || !team.id) {
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
                return;
              }

        // 로그인 후 우측에 노출되던 '빠른 메뉴'는 제거하고, 내 팀을 카드로 넓게 보여준다.
        renderShell("내 팀", `
          <div class="row justify-content-center">
            <div class="col-lg-8">
              <div class="card">
                <div class="card-body">
                  <div class="d-flex align-items-center justify-content-between mb-2">
                    <div class="fw-semibold">${IM.escapeHtml(team.teamName || "(이름 없음)")}</div>
                    <span class="badge text-bg-dark">참여중</span>
                  </div>
                  <div class="mb-1"><span class="muted">연습 지역</span> : ${IM.escapeHtml(fmtEnum("region", team.practiceRegion) || team.practiceRegion || "-")}</div>
                  <div class="mb-1"><span class="muted">팀장</span> : ${IM.escapeHtml(team.leaderName || "-")}</div>
                  <div class="mb-1"><span class="muted">생성일</span> : ${IM.escapeHtml(fmtDate(team.createdAt))}</div>
                  <div class="mt-2">
                    <div class="muted small">메모</div>
                    <div>${IM.escapeHtml(team.practiceNote || "-")}</div>
                  </div>
                  <div class="d-flex gap-2 mt-3">
                    <a class="btn btn-dark" href="#/teams/${IM.escapeHtml(team.id)}">
                      <i class="bi bi-box-arrow-up-right me-1"></i>팀 상세 보기
                    </a>
                    <a class="btn btn-outline-dark" href="#/teams">
                      <i class="bi bi-search me-1"></i>팀 더 찾기
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        `);
      } catch (e) {
            const status = getStatus(e);
            const msg = String(e?.message || "");

           // "팀 없음"을 404뿐 아니라 다양한 케이스로 처리 (apiFetch의 에러 shape/백엔드 응답이 달라도 안전)
            const noTeam =
              status === 404 ||
              status === 204 ||
              /팀.*없/.test(msg) ||
              /속해.*팀.*없/.test(msg);

            if (noTeam) {
              renderNoTeam();
              return;
            }

            // (선택) 서버가 팀 없음도 5xx로 내려주는 경우 UX 방어
            if (status >= 500) {
              console.warn("[my-team] server error treated as empty state:", e);
              renderNoTeam();
              return;
            }

            throw e;
      }
    }

  async function viewCreateTeam() {
    if (!requireAuth()) return;
    await ensureMe();

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
                  <select class="form-select" id="practiceRegion">
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
      const practiceRegion = document.getElementById("practiceRegion").value;
      const practiceNote = document.getElementById("practiceNote").value.trim();

      const res = await IM.apiFetch("/teams", {
        method: "POST",
        body: { teamName, practiceRegion, practiceNote }
      });

      IM.showToast("팀이 생성되었습니다.", "success");
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

    // ✅ 남은 슬롯 시각화를 위한 통계(있으면 사용)
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
              <div class="mb-1"><span class="muted">연습 지역</span> : ${IM.escapeHtml(fmtEnum("region", team.practiceRegion) || team.practiceRegion || "-")}</div>
              <div class="mb-1"><span class="muted">팀장</span> : userId ${team.leaderUserId}</div>
              <div class="mb-1"><span class="muted">생성일</span> : ${IM.escapeHtml(fmtDate(team.createdAt))}</div>
              <div class="mt-2">
                <div class="muted small">메모</div>
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

          ${isLeader ? leaderApplicantsCard(teamId) : ""}
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

    let slotHtml = `<span class="muted small">-</span>`;
    if (statMap && Object.prototype.hasOwnProperty.call(statMap, String(p.id))) {
      const used = Number(statMap[String(p.id)] || 0);
      const total = Number(p.capacity || 0);
      const remain = Math.max(0, total - used);
      const pct = total ? Math.round((used / total) * 100) : 0;
      slotHtml = `
        <div class="progress im-progress-sm">
          <div class="progress-bar" style="width:${pct}%"></div>
        </div>
        <div class="small muted mt-1">남은 ${remain}/${total}</div>
      `;
    }

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
          <div class="fw-semibold mb-2">팀장 기능: 포지션 슬롯 생성</div>
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
    titleEl.textContent = `프로필 (userId: ${uid})`;
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

      if (!list.length) {
        bodyEl.innerHTML = `<div class="muted">지원자가 없습니다.</div>`;
        return;
      }

      bodyEl.innerHTML = `
        <div class="table-responsive">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>지원자 userId</th>
                <th>상태</th>
                <th class="d-none d-md-table-cell">지원일</th>
                <th class="d-none d-md-table-cell">수정일</th>
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
                        ${jr.applicantUserId}
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
