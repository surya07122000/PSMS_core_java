package com.hcltech.psms.service.impl;

import java.time.LocalDate;
import java.util.*;

import com.hcltech.psms.entity.ProjectScope;
import com.hcltech.psms.entity.TrainingStatus;
import com.hcltech.psms.exception.DuplicateProjectException;
import com.hcltech.psms.exception.IncompleteScopeException;
import com.hcltech.psms.exception.InvalidDateRangeException;
import com.hcltech.psms.exception.MissingRemarksException;
import com.hcltech.psms.repo.ProjectRepositoryJdbc;

public class ProjectScopeService {

    private final ProjectRepositoryJdbc repo;

    public ProjectScopeService(ProjectRepositoryJdbc repo) {
        this.repo = repo;
    }

    // Create
    public ProjectScope add(ProjectScope p) {
        validate(p, true);
        Long id = repo.insert(p);
        p.setProjectId(id);
        return p;
    }

    // Read
    public Collection<ProjectScope> all() {
        return repo.findAll();
    }

    public ProjectScope byId(Long id) {
        return repo.findById(id);
    }

    public List<ProjectScope> byTrainer(String trainer) {
        return repo.findByTrainer(trainer);
    }

    // Update
    public ProjectScope update(ProjectScope p) {
        validate(p, false);
        ProjectScope existing = repo.findById(p.getProjectId());
        if (existing == null) throw new NoSuchElementException("Project not found: " + p.getProjectId());
        repo.update(p);
        return p;
    }

    // Delete
    public boolean delete(Long id) {
        return repo.delete(id);
    }

    // Sorters (sort in-memory for console display)
    public List<ProjectScope> sortByTitle() {
        List<ProjectScope> list = new ArrayList<>(repo.findAll());
        list.sort(Comparator.comparing(ProjectScope::getProjectTitle, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<ProjectScope> sortByStartDate() {
        List<ProjectScope> list = new ArrayList<>(repo.findAll());
        list.sort(Comparator.comparing(ProjectScope::getStartDate));
        return list;
    }

    public List<ProjectScope> sortByStatus() {
        List<ProjectScope> list = new ArrayList<>(repo.findAll());
        list.sort(Comparator.comparing(p -> p.getTrainingStatus().ordinal()));
        return list;
    }

    public void refreshStatuses() {
        LocalDate today = LocalDate.now();
        for (ProjectScope p : repo.findAll()) {
            p.autoUpdateStatusByDate(today);
        }
    }

    private void validate(ProjectScope p, boolean isCreate) {
        if (p.getStartDate() != null && p.getEndDate() != null && !p.getStartDate().isBefore(p.getEndDate())) {
            throw new InvalidDateRangeException("startDate must be before endDate");
        }
        p.autoUpdateStatusByDate(LocalDate.now());

        if (p.getTrainingStatus() == TrainingStatus.CANCELLED) {
            if (p.getRemarks() == null || p.getRemarks().trim().isEmpty()) {
                throw new MissingRemarksException("remarks are mandatory when status = CANCELLED");
            }
        }
        if (p.getMilestones() == null || p.getMilestones().isEmpty()
                || p.getDeliverables() == null || p.getDeliverables().isEmpty()) {
            throw new IncompleteScopeException("At least 1 milestone and 1 deliverable required");
        }
        if (p.getProjectTitle() == null || p.getProjectTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("projectTitle is required");
        }
        if (isCreate && repo.titleExists(p.getProjectTitle())) {
            throw new DuplicateProjectException("Duplicate project title: " + p.getProjectTitle());
        }
        if (!isCreate) {
            ProjectScope existing = repo.findById(p.getProjectId());
            if (existing != null) {
                String oldTitle = existing.getProjectTitle();
                if (!p.getProjectTitle().equalsIgnoreCase(oldTitle) && repo.titleExists(p.getProjectTitle())) {
                    throw new DuplicateProjectException("Duplicate project title: " + p.getProjectTitle());
                }
            }
        }
    }

}
