package com.example.findany.model;

public class ModelClassStudent {
    private String FullName;
    private String year;
    private String branch;
    private String ImageUrl;
    private String RegNo;
    private String description;
    private String project;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    private String skills;

    public ModelClassStudent(String name, String year, String branch, String imageUrl, String Regno,String skills,String project) {
        this.FullName = name;
        this.year = year;
        this.branch = branch;
        this.ImageUrl = imageUrl;
        this.RegNo = Regno;
        this.skills=skills;
        this.project=project;
    }

    public String getName() {
        return FullName;
    }

    public String getRegno() {
        return RegNo;
    }

    public String getYear() {
        return year;
    }

    public String getBranch() {
        return branch;
    }

    public String getImageUrl() {
        return ImageUrl;
    }
}
