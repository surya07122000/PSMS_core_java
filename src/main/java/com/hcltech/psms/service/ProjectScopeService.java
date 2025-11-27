package com.hcltech.psms.service;

import java.util.List;

import com.hcltech.psms.entity.ProjectScope;

public interface ProjectScopeService {
	void addProject(ProjectScope projectScope);
	List<ProjectScope> getAllProjects();
	ProjectScope getProjectById(Long id);
	
}
