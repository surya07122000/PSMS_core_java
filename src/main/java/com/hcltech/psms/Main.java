package com.hcltech.psms;

import com.hcltech.psms.entity.DeliveryMode;
import com.hcltech.psms.entity.ProjectScope;
import com.hcltech.psms.entity.TrainingStatus;
import com.hcltech.psms.repo.ProjectRepositoryJdbc;
import com.hcltech.psms.service.impl.ProjectScopeService;
import com.hcltech.psms.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner sc = new Scanner(System.in);
    private final ProjectScopeService service = new ProjectScopeService(new ProjectRepositoryJdbc());

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        boolean exit = false;
        while (!exit) {
            menu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        addProject();
                        break;
                    case "2":
                        viewAll();
                        break;
                    case "3":
                        searchById();
                        break;
                    case "4":
                        searchByTrainer();
                        break;
                    case "5":
                        updateProject();
                        break;
                    case "6":
                        deleteProject();
                        break;
                    case "7":
                        sortProjects();
                        break;
                    case "8":
                        exit = true;
                        System.out.println("Bye!");
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            } catch (RuntimeException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Unexpected error: " + ex);
            }
        }
    }

    private void menu() {
        System.out.println("\n=== Project Scope Management System ===");
        System.out.println("1. Add Project Scope");
        System.out.println("2. View All Projects");
        System.out.println("3. Search Project by ID");
        System.out.println("4. Search Project by Trainer Name");
        System.out.println("5. Update Project");
        System.out.println("6. Delete Project");
        System.out.println("7. Sort Projects (By Title / Date / Status)");
        System.out.println("8. Export All Projects to File (CSV or JSON)");
        System.out.println("9. Import Projects from File (CSV)");
        System.out.println("10. Exit");
        System.out.print("Choose: ");
    }

    private void addProject() {
        ProjectScope p = new ProjectScope();
        System.out.print("Project Title: ");
        p.setProjectTitle(sc.nextLine().trim());
        System.out.print("Trainer Name: ");
        p.setTrainerName(sc.nextLine().trim());
        System.out.print("Technologies (comma-separated): ");
        p.setTechnologies(readListComma());
        System.out.print("Start Date (DD-MM-YYYY): ");
        p.setStartDate(DateUtil.parseDMY(sc.nextLine().trim()));
        System.out.print("End Date (DD-MM-YYYY): ");
        p.setEndDate(DateUtil.parseDMY(sc.nextLine().trim()));
        System.out.print("Delivery Mode (ONLINE/OFFLINE/HYBRID): ");
        p.setDeliveryMode(DeliveryMode.valueOf(sc.nextLine().trim().toUpperCase()));
        System.out.print("Participants Count: ");
        p.setParticipantsCount(Integer.parseInt(sc.nextLine().trim()));
        p.setTrainingStatus(TrainingStatus.PLANNED); // default
        System.out.print("Deliverables (comma-separated): ");
        p.setDeliverables(readListComma());
        System.out.print("Milestones (comma-separated): ");
        p.setMilestones(readListComma());
        System.out.println("Risks (enter as 'desc:mitigation', one per line; blank to stop):");
        p.setRisks(readListComma());
        System.out.print("Remarks: ");
        p.setRemarks(sc.nextLine());

        ProjectScope created = service.add(p);
        System.out.println("Added: " + created);
    }

    private void viewAll() {
        service.refreshStatuses();
        for (ProjectScope p : service.all()) {
            printProjectDetail(p);
        }
    }

    private void searchById() {
        System.out.print("ID: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        ProjectScope p = service.byId(id);
        if (p == null) System.out.println("Not found");
        else printProjectDetail(p);
    }

    private void searchByTrainer() {
        System.out.print("Trainer Name: ");
        String trainer = sc.nextLine().trim();
        List<ProjectScope> list = service.byTrainer(trainer);
        if (list.isEmpty()) System.out.println("No projects for trainer");
        for (ProjectScope p : list) printProjectDetail(p);
    }

    private void updateProject() {
        System.out.print("ID to update: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        ProjectScope existing = service.byId(id);
        if (existing == null) {
            System.out.println("Not found");
            return;
        }
        ProjectScope p = new ProjectScope();
        p.setProjectId(id);

        System.out.print("New Title (blank to keep): ");
        String t = sc.nextLine();
        p.setProjectTitle(t.isEmpty() ? existing.getProjectTitle() : t.trim());

        System.out.print("New Trainer (blank to keep): ");
        String tr = sc.nextLine();
        p.setTrainerName(tr.isEmpty() ? existing.getTrainerName() : tr.trim());

        System.out.print("Technologies (comma, blank to keep): ");
        String te = sc.nextLine();
        p.setTechnologies(te.isEmpty() ? existing.getTechnologies() : listFromComma(te));

        System.out.print("Start Date (DD-MM-YYYY, blank to keep): ");
        String sd = sc.nextLine();
        p.setStartDate(sd.isEmpty() ? existing.getStartDate() : DateUtil.parseDMY(sd.trim()));

        System.out.print("End Date (DD-MM-YYYY, blank to keep): ");
        String ed = sc.nextLine();
        p.setEndDate(ed.isEmpty() ? existing.getEndDate() : DateUtil.parseDMY(ed.trim()));

        System.out.print("Delivery Mode (ONLINE/OFFLINE/HYBRID, blank to keep): ");
        String dm = sc.nextLine();
        p.setDeliveryMode(dm.isEmpty() ? existing.getDeliveryMode() : DeliveryMode.valueOf(dm.trim().toUpperCase()));

        System.out.print("Participants Count (blank to keep): ");
        String pc = sc.nextLine();
        p.setParticipantsCount(pc.isEmpty() ? existing.getParticipantsCount() : Integer.parseInt(pc.trim()));

        System.out.print("Status (PLANNED/IN_PROGRESS/COMPLETED/CANCELLED, blank to auto/date): ");
        String st = sc.nextLine();
        p.setTrainingStatus(st.isEmpty() ? existing.getTrainingStatus() : TrainingStatus.fromString(st));

        System.out.print("Deliverables (comma, blank to keep): ");
        String dv = sc.nextLine();
        p.setDeliverables(dv.isEmpty() ? existing.getDeliverables() : listFromComma(dv));

        System.out.print("Milestones (comma, blank to keep): ");
        String ms = sc.nextLine();
        p.setMilestones(ms.isEmpty() ? existing.getMilestones() : listFromComma(ms));

        System.out.println("Risks ('desc:mitigation' lines, blank to keep):");
        String rs = sc.nextLine();
        p.setRisks(rs.isEmpty() ? existing.getRisks() : listFromComma(rs));

        System.out.print("Remarks (blank to keep): ");
        String rm = sc.nextLine();
        p.setRemarks(rm.isEmpty() ? existing.getRemarks() : rm);

        ProjectScope upd = service.update(p);
        System.out.println("Updated: " + upd);
    }

    private void deleteProject() {
        System.out.print("ID to delete: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        boolean ok = service.delete(id);
        System.out.println(ok ? "Deleted" : "Not found");
    }

    private void sortProjects() {
        System.out.println("Sort by: 1) Title  2) Start Date  3) Status");
        String s = sc.nextLine().trim();
        List<ProjectScope> list;
        switch (s) {
            case "1":
                list = service.sortByTitle();
                break;
            case "2":
                list = service.sortByStartDate();
                break;
            case "3":
                list = service.sortByStatus();
                break;
            default:
                System.out.println("Invalid");
                return;
        }
        for (ProjectScope p : list) printProjectDetail(p);
    }

    private List<String> listFromComma(String s) {
        List<String> list = new ArrayList<>();
        if (s == null) return list;
        for (String t : s.split(",")) {
            String x = t.trim();
            if (!x.isEmpty()) list.add(x);
        }
        return list;
    }

    private List<String> readListComma() { return listFromComma(sc.nextLine()); }

    private void printProjectDetail(ProjectScope p) {
        System.out.println(p.toString());
        System.out.println("  Technologies: " + String.join(", ", p.getTechnologies()));
        System.out.println("  Deliverables: " + String.join(", ", p.getDeliverables()));
        System.out.println("  Milestones:   " + String.join(", ", p.getMilestones()));
        System.out.println("  Risks:" + String.join(", ", p.getRisks()));
        System.out.println("  Remarks: " + (p.getRemarks() == null ? "" : p.getRemarks()));
    }
}
