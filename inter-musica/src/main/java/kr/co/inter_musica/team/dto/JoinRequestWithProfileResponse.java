package kr.co.inter_musica.team.dto;

import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;

public class JoinRequestWithProfileResponse {
    private Long joinRequestId;
    private Long applicantUserId;
    private MemberProfileResponse applicantProfile;
    private JoinRequestStatus status;
    private String createdAt;

    public JoinRequestWithProfileResponse() {}

    public Long getJoinRequestId() { return joinRequestId; }
    public void setJoinRequestId(Long joinRequestId) { this.joinRequestId = joinRequestId; }

    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long applicantUserId) { this.applicantUserId = applicantUserId; }

    public MemberProfileResponse getApplicantProfile() { return applicantProfile; }
    public void setApplicantProfile(MemberProfileResponse applicantProfile) { this.applicantProfile = applicantProfile; }

    public JoinRequestStatus getStatus() { return status; }
    public void setStatus(JoinRequestStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
