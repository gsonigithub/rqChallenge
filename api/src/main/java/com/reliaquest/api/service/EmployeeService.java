package com.reliaquest.api.service;

import com.reliaquest.api.client.ApiClient;
import com.reliaquest.api.dto.ApiDeleteRequestDto;
import com.reliaquest.api.dto.ApiResponse;
import com.reliaquest.api.dto.EmpRequestDto;
import com.reliaquest.api.exception.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final ApiClient apiClient;

    @Autowired
    public EmployeeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<ApiResponse.Employee> getAllEmployeeList() {
        List<ApiResponse.Employee> employeeList = apiClient.getAllEmployees().block();
        if (employeeList == null || employeeList.isEmpty()) {
            logger.info("Employee list is empty or unavailable.");
            return Collections.emptyList();
        } else {
            logger.info("Employee list fetched successfully.");
            return employeeList;
        }
    }

    public Integer getMaxSal() {
        return apiClient.getHighestSalaryOfEmployees().block();
    }

    public List<ApiResponse.Employee> getEmployeeByName(String searchString) {
        return apiClient.getEmployeesByNameSearch(searchString).block();
    }

    public ApiResponse.Employee addNewEmployee(EmpRequestDto dto) {
        return apiClient.createEmployee(dto).block();
    }

    public ApiResponse.Employee getEmployeeById(String id) {
        return apiClient.getEmployeeById(id).block();
    }

    public List<String> getTenTopSalaryEmpList() {
        return apiClient.getTop10HighestEarningEmployeeNames().block();
    }

    public String deleteEmployeeById(String id) {
        try {
            ApiResponse.Employee employee = apiClient.getEmployeeById(id).block();
            if (employee != null) {
                // fetch name from employee as client DELETE API accepts name
                String name = employee.employee_name();
                ApiResponse.DeleteRecordResponse apiResponse = apiClient
                        .deleteEmployeeByName(new ApiDeleteRequestDto(name))
                        .block();
                if (apiResponse != null) {
                    boolean deleteApiSuccess = Boolean.parseBoolean(apiResponse.data());
                    logger.info("Employee with id: {} deleted successfully.", id);
                    if (deleteApiSuccess) return employee.employee_name();
                }
                logger.error("Employee with id: {} deletion failed.", id);
            } else {
                logger.error("Employee with id: {} not found.", id);
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Employee with id: {} not found.", id);
        }
        return null;
    }
}
