package com.pavan.sms;

/** A student record. */
public class Student {
    private int id;               // 0 = not yet persisted
    private String rollNo;
    private String name;
    private String email;
    private String phone;
    private String department;
    private int year;             // 1..4
    private double cgpa;          // 0..10

    public Student() {}

    public Student(int id, String rollNo, String name, String email, String phone,
                   String department, int year, double cgpa) {
        this.id = id; this.rollNo = rollNo; this.name = name; this.email = email;
        this.phone = phone; this.department = department; this.year = year; this.cgpa = cgpa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
}
