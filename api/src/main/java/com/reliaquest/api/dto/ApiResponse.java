package com.reliaquest.api.dto;

import java.util.List;

public class ApiResponse {
    public record Employee(
            String id,
            String employee_name,
            int employee_salary,
            int employee_age,
            String employee_title,
            String employee_email) {}
    // public record SingleRecordResponse(Employee data, String status) {}
    public record EmployeeListResponse(List<Employee> data, String status) {}

    public record DeleteRecordResponse(String data, String status) {}

    public record EmployeeResponse(Employee data, String status) {}
}
