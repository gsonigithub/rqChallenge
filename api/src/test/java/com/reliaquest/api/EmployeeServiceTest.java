package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.ApiClient;
import com.reliaquest.api.dto.ApiDeleteRequestDto;
import com.reliaquest.api.dto.ApiResponse;
import com.reliaquest.api.dto.EmpRequestDto;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;

class EmployeeServiceTest {

    @Mock
    private ApiClient apiClient;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployeeList_success() {

        List<ApiResponse.Employee> employeeList =
                List.of(new ApiResponse.Employee("1", "Kanchhedi Lal", 100000, 20, "Developer", "kcl@example.com"));

        // Mock ApiClient to return the employee list
        when(apiClient.getAllEmployees()).thenReturn(Mono.just(employeeList));

        List<ApiResponse.Employee> result = employeeService.getAllEmployeeList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Kanchhedi Lal", result.get(0).employee_name());
        verify(apiClient, times(1)).getAllEmployees();
    }

    @Test
    void testGetAllEmployeeList_emptyResponse() {

        when(apiClient.getAllEmployees()).thenReturn(Mono.just(List.of()));

        List<ApiResponse.Employee> result = employeeService.getAllEmployeeList();

        assertTrue(result.isEmpty());
        verify(apiClient, times(1)).getAllEmployees();
    }

    @Test
    void testGetMaxSal() {

        int expectedSalary = 150000;
        when(apiClient.getHighestSalaryOfEmployees()).thenReturn(Mono.just(expectedSalary));

        Integer result = employeeService.getMaxSal();

        assertEquals(expectedSalary, result);
        verify(apiClient, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void testGetEmployeeByName_success() {

        List<ApiResponse.Employee> employees =
                List.of(new ApiResponse.Employee("1", "Baburao", 100000, 20, "Developer", "babu@example.com"));
        when(apiClient.getEmployeesByNameSearch("Baburao")).thenReturn(Mono.just(employees));

        List<ApiResponse.Employee> result = employeeService.getEmployeeByName("Baburao");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Baburao", result.get(0).employee_name());
        verify(apiClient, times(1)).getEmployeesByNameSearch("Baburao");
    }

    @Test
    void testAddNewEmployee_success() {

        EmpRequestDto dto = new EmpRequestDto("Ashish Mishra", 120000, 30, "Manager", "abc@companyemail.in");
        ApiResponse.Employee newEmployee =
                new ApiResponse.Employee("2", "Gaurav Soni", 120000, 23, "Manager", "gaurav@example.com");

        when(apiClient.createEmployee(dto)).thenReturn(Mono.just(newEmployee));

        ApiResponse.Employee result = employeeService.addNewEmployee(dto);

        assertNotNull(result);
        assertEquals("Gaurav Soni", result.employee_name());
        verify(apiClient, times(1)).createEmployee(dto);
    }

    @Test
    void testGetEmployeeById_success() {

        ApiResponse.Employee employee =
                new ApiResponse.Employee("1", "Rahul Dua", 100000, 44, "Developer", "rahul@example.com");
        when(apiClient.getEmployeeById("1")).thenReturn(Mono.just(employee));

        ApiResponse.Employee result = employeeService.getEmployeeById("1");

        assertNotNull(result);
        assertEquals("Rahul Dua", result.employee_name());
        verify(apiClient, times(1)).getEmployeeById("1");
    }

    @Test
    void testGetTenTopSalaryEmpList() {
        List<String> topEmployees = List.of("Dinesh R", "Ashish M", "Shantanu Singh");
        when(apiClient.getTop10HighestEarningEmployeeNames()).thenReturn(Mono.just(topEmployees));

        List<String> result = employeeService.getTenTopSalaryEmpList();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Dinesh R", result.get(0));
        verify(apiClient, times(1)).getTop10HighestEarningEmployeeNames();
    }

    @Test
    void testDeleteEmployeeById_success() {

        ApiResponse.Employee employee =
                new ApiResponse.Employee("1", "Ravindra", 100000, 34, "Developer", "ravi@example.com");
        ApiResponse.DeleteRecordResponse deleteResponse = new ApiResponse.DeleteRecordResponse("true", "success");

        when(apiClient.getEmployeeById("1")).thenReturn(Mono.just(employee));
        when(apiClient.deleteEmployeeByName(any(ApiDeleteRequestDto.class))).thenReturn(Mono.just(deleteResponse));

        String result = employeeService.deleteEmployeeById("1");

        assertEquals("Ravindra", result);
        verify(apiClient, times(1)).getEmployeeById("1");
        verify(apiClient, times(1)).deleteEmployeeByName(any(ApiDeleteRequestDto.class));
    }

    @Test
    void testDeleteEmployeeById_employeeNotFound() {

        when(apiClient.getEmployeeById("nonexistent-id")).thenReturn(Mono.empty());

        String result = employeeService.deleteEmployeeById("nonexistent-id");

        assertNull(result);
        verify(apiClient, times(1)).getEmployeeById("nonexistent-id");
    }

    @Test
    void testDeleteEmployeeById_failure() {

        ApiResponse.Employee employee =
                new ApiResponse.Employee("1", "Gaurav Soni", 100000, 22, "Developer", "gaurav@example.com");
        ApiResponse.DeleteRecordResponse deleteResponse = new ApiResponse.DeleteRecordResponse("false", "failed");

        when(apiClient.getEmployeeById("1")).thenReturn(Mono.just(employee));
        when(apiClient.deleteEmployeeByName(any(ApiDeleteRequestDto.class))).thenReturn(Mono.just(deleteResponse));

        String result = employeeService.deleteEmployeeById("1");

        assertNull(result);
        verify(apiClient, times(1)).getEmployeeById("1");
        verify(apiClient, times(1)).deleteEmployeeByName(any(ApiDeleteRequestDto.class));
    }
}
