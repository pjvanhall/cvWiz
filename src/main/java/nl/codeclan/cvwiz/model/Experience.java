package nl.codeclan.cvwiz.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Experience {

    @Id
    Long id;
    String company;
    String period;
    String jobTitle;
    String branche;
    @Column(columnDefinition = "TEXT")
    String techStack;
    @Column(columnDefinition = "TEXT")
    String situation;
    @Column(columnDefinition = "TEXT")
    String task;

    public Experience() {
    }

    public Experience(String company, String period, String jobTitle, String branche, String techStack, String situation, String task) {
        this.company = company;
        this.period = period;
        this.jobTitle = jobTitle;
        this.branche = branche;
        this.techStack = techStack;
        this.situation = situation;
        this.task = task;
    }

    public Experience(Long id, String company, String period, String jobTitle, String branche, String techStack, String situation, String task) {
        this.id = id;
        this.company = company;
        this.period = period;
        this.jobTitle = jobTitle;
        this.branche = branche;
        this.techStack = techStack;
        this.situation = situation;
        this.task = task;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getBranche() {
        return branche;
    }

    public void setBranche(String branche) {
        this.branche = branche;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}