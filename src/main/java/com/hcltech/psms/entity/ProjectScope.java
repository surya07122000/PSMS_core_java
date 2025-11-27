package com.hcltech.psms.entity;

import java.time.LocalDate;
import java.util.List;

public class ProjectScope {
	private Long projectId;
	private String projectTitle;
	private String trainerName;
	private List<String> technologies;
	private LocalDate startDate;
	private LocalDate endDate;
	private DeliveryMode deliveryMode;
	private int participantsCount;
	private TrainingStatus trainingStatus;
	private List<String> deliverables;
	private List<String> milestones;
	private List<String> risks;
	private String remarks;
	
	public ProjectScope() {}
	
	public ProjectScope(Long projectId, String projectTitle, String trainerName, List<String> technologies,
			LocalDate startDate, LocalDate endDate, DeliveryMode deliveryMode, int participantsCount,
			TrainingStatus trainingStatus, List<String> deliverables, List<String> milestones, List<String> risks,
			String remarks) {
		super();
		this.projectId = projectId;
		this.projectTitle = projectTitle;
		this.trainerName = trainerName;
		this.technologies = technologies;
		this.startDate = startDate;
		this.endDate = endDate;
		this.deliveryMode = deliveryMode;
		this.participantsCount = participantsCount;
		this.trainingStatus = trainingStatus;
		this.deliverables = deliverables;
		this.milestones = milestones;
		this.risks = risks;
		this.remarks = remarks;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public String getProjectTitle() {
		return projectTitle;
	}
	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}
	public String getTrainerName() {
		return trainerName;
	}
	public void setTrainerName(String trainerName) {
		this.trainerName = trainerName;
	}
	public List<String> getTechnologies() {
		return technologies;
	}
	public void setTechnologies(List<String> technologies) {
		this.technologies = technologies;
	}
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public DeliveryMode getDeliveryMode() {
		return deliveryMode;
	}
	public void setDeliveryMode(DeliveryMode deliveryMode) {
		this.deliveryMode = deliveryMode;
	}
	public int getParticipantsCount() {
		return participantsCount;
	}
	public void setParticipantsCount(int participantsCount) {
		this.participantsCount = participantsCount;
	}
	public TrainingStatus getTrainingStatus() {
		return trainingStatus;
	}
	public void setTrainingStatus(TrainingStatus trainingStatus) {
		this.trainingStatus = trainingStatus;
	}
	public List<String> getDeliverables() {
		return deliverables;
	}
	public void setDeliverables(List<String> deliverables) {
		this.deliverables = deliverables;
	}
	public List<String> getMilestones() {
		return milestones;
	}
	public void setMilestones(List<String> milestones) {
		this.milestones = milestones;
	}
	public List<String> getRisks() {
		return risks;
	}
	public void setRisks(List<String> risks) {
		this.risks = risks;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	@Override
	public String toString() {
		return "ProjectScope [projectId=" + projectId + ", projectTitle=" + projectTitle + ", trainerName="
				+ trainerName + ", technologies=" + technologies + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", deliveryMode=" + deliveryMode + ", participantsCount=" + participantsCount + ", trainingStatus="
				+ trainingStatus + ", deliverables=" + deliverables + ", milestones=" + milestones + ", risks=" + risks
				+ ", remarks=" + remarks + "]";
	}

	public void autoUpdateStatusByDate(LocalDate today) {
		if (endDate != null && endDate.isBefore(today)) {
			this.trainingStatus = TrainingStatus.COMPLETED;
		}
	}

}
