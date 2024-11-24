package com.reliaquest.api.controller;

import com.reliaquest.api.dto.ApiResponse;
import com.reliaquest.api.dto.EmpRequestDto;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/employee")
public class EmployeeController implements IEmployeeController<ApiResponse.Employee, EmpRequestDto> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    EmployeeService employeeService;

    @Override
    public ResponseEntity<List<ApiResponse.Employee>> getAllEmployees() {
        logger.info("GET all employees.");
        List<ApiResponse.Employee> employeeList = employeeService.getAllEmployeeList();
        return !employeeList.isEmpty()
                ? ResponseEntity.ok(employeeList)
                : ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ApiResponse.Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        logger.info("GET employees by name {}", searchString);
        List<ApiResponse.Employee> employeeList = employeeService.getEmployeeByName(searchString);
        return !employeeList.isEmpty()
                ? ResponseEntity.ok(employeeList)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<ApiResponse.Employee> getEmployeeById(@PathVariable String id) {
        logger.info("GET employee for id {}", id);
        ApiResponse.Employee employee = employeeService.getEmployeeById(id);
        return employee != null
                ? ResponseEntity.ok(employee)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        logger.info("GET highest salary of employees");
        Integer maxSalary = employeeService.getMaxSal();
        return maxSalary != null
                ? ResponseEntity.ok(maxSalary)
                : ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        logger.info("GET names of top 10 highest paid employees");
        List<String> employeeList = employeeService.getTenTopSalaryEmpList();
        return !employeeList.isEmpty()
                ? ResponseEntity.ok(employeeList)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<ApiResponse.Employee> createEmployee(@RequestBody @Valid EmpRequestDto employeeInput) {
        logger.info("POST new employee {}", employeeInput.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.addNewEmployee(employeeInput));
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        logger.info("DELETE employee by id {}", id);
        String employeeName = employeeService.deleteEmployeeById(id);
        return employeeName != null
                ? ResponseEntity.status(HttpStatus.ACCEPTED).body(employeeName)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
