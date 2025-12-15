package kr.co.inter_musica.exception;

public enum ErrorCode {
    AUTH_UNAUTHORIZED(401, "AUTH_UNAUTHORIZED", "인증이 필요합니다."),
    AUTH_INVALID_CREDENTIALS(401, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),

    TEAM_FORBIDDEN(403, "TEAM_FORBIDDEN", "해당 팀에 대한 권한이 없습니다."),
    JOIN_REQUEST_CANNOT_CANCEL_FORBIDDEN(403, "JOIN_REQUEST_CANNOT_CANCEL_FORBIDDEN", "해당 요청에 대한 권한이 없습니다."),

    TEAM_NOT_FOUND(404, "TEAM_NOT_FOUND", "팀을 찾을 수 없습니다."),
    SLOT_NOT_FOUND(404, "SLOT_NOT_FOUND", "포지션을 찾을 수 없습니다."),
    JOIN_REQUEST_NOT_FOUND(404, "JOIN_REQUEST_NOT_FOUND", "해당 사용자의 참가 신청을 찾을 수 없습니다."),
    PROFILE_NOT_FOUND(404, "PROFILE_NOT_FOUND", "해당 사용자의 프로필을 찾을 수 없습니다."), /* 프로필은 회원 가입 시 반드시 작성되므로, 사실상 호출 가능성 0 */

    AUTH_EMAIL_ALREADY_EXISTS(409, "AUTH_EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
    JOIN_REQUEST_ALREADY_APPLIED(409, "JOIN_REQUEST_ALREADY_APPLIED", "이미 해당 포지션에 대한 참가 신청이 존재합니다."),
    JOIN_REQUEST_NOT_APPLIED(409, "JOIN_REQUEST_NOT_APPLIED", "지원자가 대기(APPLIED) 상태일 때만 처리할 수 있습니다."),
    JOIN_REQUEST_CANNOT_CANCEL_NOT_APPLIED(409, "JOIN_REQUEST_CANNOT_CANCEL_NOT_APPLIED", "이미 팀에 배정되었습니다."),
    SLOT_CAPACITY_FULL(409, "SLOT_CAPACITY_FULL", "포지션 정원이 가득 찼습니다."),
    SLOT_CAPACITY_TOO_SMALL_FOR_ACTIVE_MEMBERS(409, "SLOT_CAPACITY_TOO_SMALL_FOR_ACTIVE_MEMBERS", "현재 참가 중인 멤버 수보다 작게 정원을 줄일 수 없습니다."),
    SLOT_CANNOT_DELETE_HAS_PENDING_OR_ACTIVE(409, "SLOT_CANNOT_DELETE_HAS_PENDING_OR_ACTIVE", "이미 해당 포지션의 지원자 또는 활동 멤버가 있어 삭제할 수 없습니다."),
    TEAM_CANNOT_DELETE_HAS_ACTIVE_MEMBERS(409, "TEAM_CANNOT_DELETE_HAS_ACTIVE_MEMBERS", "이미 활동 중인 멤버가 있어 팀을 삭제할 수 없습니다."),
    USER_ALREADY_ACTIVE_IN_ANOTHER_TEAM(409, "USER_ALREADY_ACTIVE_IN_ANOTHER_TEAM", "지원자가 이미 다른 팀에 소속되어 있습니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
