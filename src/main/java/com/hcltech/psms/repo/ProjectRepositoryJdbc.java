package com.hcltech.psms.repo;

import com.hcltech.psms.entity.DeliveryMode;
import com.hcltech.psms.entity.ProjectScope;
import com.hcltech.psms.entity.TrainingStatus;
import com.hcltech.psms.exception.DuplicateProjectException;
import com.hcltech.psms.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ProjectRepositoryJdbc {

    // ---------- CREATE ----------
    public Long insert(ProjectScope p) {
        String sql = "INSERT INTO projects " +
                "(project_title, trainer_name, start_date, end_date, delivery_mode, " +
                " participants_count, training_status, remarks) " +
                "VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getProjectTitle());
                ps.setString(2, p.getTrainerName());
                ps.setDate(3, toSqlDate(p.getStartDate()));
                ps.setDate(4, toSqlDate(p.getEndDate()));
                ps.setString(5, p.getDeliveryMode() == null ? null : p.getDeliveryMode().name());
                ps.setInt(6, p.getParticipantsCount());
                ps.setString(7, p.getTrainingStatus() == null ? TrainingStatus.PLANNED.name() : p.getTrainingStatus().name());
                ps.setString(8, p.getRemarks());

                ps.executeUpdate();
                Long projectId = getGeneratedId(ps);

                // child tables (all lists of strings)
                insertList(conn, "technologies", "name", projectId, p.getTechnologies());
                insertList(conn, "deliverables", "name", projectId, p.getDeliverables());
                insertList(conn, "milestones",   "name", projectId, p.getMilestones());
                insertList(conn, "risks",        "name", projectId, p.getRisks());

                conn.commit();
                return projectId;
            } catch (SQLIntegrityConstraintViolationException dup) {
                conn.rollback();
                throw new DuplicateProjectException("Duplicate project title: " + p.getProjectTitle());
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new RuntimeException("Insert failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- READ ----------
    public Collection<ProjectScope> findAll() {
        String sql = "SELECT * FROM projects ORDER BY project_title";
        List<ProjectScope> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProjectScope p = mapProject(rs);
                loadChildren(conn, p);
                list.add(p);
            }
        } catch (Exception ex) {
            throw new RuntimeException("findAll failed: " + ex.getMessage(), ex);
        }
        return list;
    }

    public ProjectScope findById(Long id) {
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProjectScope p = mapProject(rs);
                    loadChildren(conn, p);
                    return p;
                }
                return null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("findById failed: " + ex.getMessage(), ex);
        }
    }

    public List<ProjectScope> findByTrainer(String trainer) {
        String sql = "SELECT * FROM projects WHERE trainer_name = ?";
        List<ProjectScope> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProjectScope p = mapProject(rs);
                    loadChildren(conn, p);
                    list.add(p);
                }
            }
            return list;
        } catch (Exception ex) {
            throw new RuntimeException("findByTrainer failed: " + ex.getMessage(), ex);
        }
    }

    public boolean titleExists(String title) {
        String sql = "SELECT 1 FROM projects WHERE project_title = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new RuntimeException("titleExists failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- UPDATE ----------
    public void update(ProjectScope p) {
        String sql = "UPDATE projects SET project_title=?, trainer_name=?, start_date=?, end_date=?, " +
                " delivery_mode=?, participants_count=?, training_status=?, remarks=? " +
                " WHERE project_id=?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getProjectTitle());
                ps.setString(2, p.getTrainerName());
                ps.setDate(3, toSqlDate(p.getStartDate()));
                ps.setDate(4, toSqlDate(p.getEndDate()));
                ps.setString(5, p.getDeliveryMode() == null ? null : p.getDeliveryMode().name());
                ps.setInt(6, p.getParticipantsCount());
                ps.setString(7, p.getTrainingStatus() == null ? TrainingStatus.PLANNED.name() : p.getTrainingStatus().name());
                ps.setString(8, p.getRemarks());
                ps.setLong(9, p.getProjectId());
                ps.executeUpdate();

                // Replace children (simple strategy)
                deleteChildren(conn, p.getProjectId());
                insertList(conn, "technologies", "name", p.getProjectId(), p.getTechnologies());
                insertList(conn, "deliverables", "name", p.getProjectId(), p.getDeliverables());
                insertList(conn, "milestones",   "name", p.getProjectId(), p.getMilestones());
                insertList(conn, "risks",        "name", p.getProjectId(), p.getRisks());

                conn.commit();
            } catch (SQLIntegrityConstraintViolationException dup) {
                conn.rollback();
                throw new DuplicateProjectException("Duplicate project title: " + p.getProjectTitle());
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new RuntimeException("update failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- DELETE ----------
    public boolean delete(Long id) {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0; // children cascade
        } catch (Exception ex) {
            throw new RuntimeException("delete failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- Helpers ----------
    private ProjectScope mapProject(ResultSet rs) throws Exception {
        ProjectScope p = new ProjectScope();
        p.setProjectId(rs.getLong("project_id"));
        p.setProjectTitle(rs.getString("project_title"));
        p.setTrainerName(rs.getString("trainer_name"));
        p.setStartDate(toLocalDate(rs.getDate("start_date")));
        p.setEndDate(toLocalDate(rs.getDate("end_date")));
        String dm = rs.getString("delivery_mode");
        p.setDeliveryMode(dm == null ? null : DeliveryMode.valueOf(dm));
        p.setParticipantsCount(rs.getInt("participants_count"));
        String ts = rs.getString("training_status");
        p.setTrainingStatus(ts == null ? TrainingStatus.PLANNED : TrainingStatus.valueOf(ts));
        p.setRemarks(rs.getString("remarks"));
        return p;
    }

    private void loadChildren(Connection conn, ProjectScope p) throws Exception {
        p.setTechnologies(loadStringList(conn, "technologies", "name", p.getProjectId()));
        p.setDeliverables(loadStringList(conn, "deliverables", "name", p.getProjectId()));
        p.setMilestones(loadStringList(conn, "milestones", "name", p.getProjectId()));
        p.setRisks(loadStringList(conn, "risks", "name", p.getProjectId()));
    }

    private List<String> loadStringList(Connection conn, String table, String col, Long projectId) throws Exception {
        String sql = "SELECT " + col + " FROM " + table + " WHERE project_id = ? ORDER BY " + col;
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }

    private void insertList(Connection conn, String table, String col, Long projectId, List<String> values) throws Exception {
        if (values == null) return;
        String sql = "INSERT INTO " + table + " (project_id, " + col + ") VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String v : values) {
                if (v == null || v.trim().isEmpty()) continue;
                ps.setLong(1, projectId);
                ps.setString(2, v.trim());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteChildren(Connection conn, Long projectId) throws Exception {
        for (String t : Arrays.asList("technologies", "deliverables", "milestones", "risks")) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + t + " WHERE project_id=?")) {
                ps.setLong(1, projectId);
                ps.executeUpdate();
            }
        }
    }

    private Long getGeneratedId(PreparedStatement ps) throws Exception {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getLong(1);
            throw new RuntimeException("No generated key returned");
        }
    }

    private java.sql.Date toSqlDate(LocalDate d) { return (d == null) ? null : java.sql.Date.valueOf(d); }
    private LocalDate toLocalDate(java.sql.Date d) { return (d == null) ? null : d.toLocalDate(); }
}
