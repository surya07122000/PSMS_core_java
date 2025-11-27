package com.hcltech.psms.entity;

public enum TrainingStatus {

	PLANNED, IN_PROGRESS, COMPLETED, CANCELLED;

	public static TrainingStatus fromString(String s) {
		if (s == null)
			return PLANNED;
		switch (s.trim().toUpperCase()) {
		case "IN_PROGRESS":
			return IN_PROGRESS;
		case "COMPLETED":
			return COMPLETED;
		case "CANCELLED":
			return CANCELLED;
		default:
			return PLANNED;
		}
	}

}