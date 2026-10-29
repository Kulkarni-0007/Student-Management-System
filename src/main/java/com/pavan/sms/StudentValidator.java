package com.pavan.sms;

import java.util.regex.Pattern;

/** Input validation so bad data never reaches the database. */
public final class StudentValidator {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-\\s]{7,15}$");

    private StudentValidator() {}

    public static void validate(Student s) throws ValidationException {
        if (isBlank(s.getRollNo())) throw new ValidationException("Roll number is required.");
        if (isBlank(s.getName())) throw new ValidationException("Name is required.");
        if (isBlank(s.getDepartment())) throw new ValidationException("Department is required.");
        if (!isBlank(s.getEmail()) && !EMAIL.matcher(s.getEmail().trim()).matches())
            throw new ValidationException("Email format is invalid.");
        if (!isBlank(s.getPhone()) && !PHONE.matcher(s.getPhone().trim()).matches())
            throw new ValidationException("Phone must be 7-15 digits (may include + - or spaces).");
        if (s.getYear() < 1 || s.getYear() > 4)
            throw new ValidationException("Year must be between 1 and 4.");
        if (s.getCgpa() < 0 || s.getCgpa() > 10)
            throw new ValidationException("CGPA must be between 0 and 10.");
    }

    private static boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
}
