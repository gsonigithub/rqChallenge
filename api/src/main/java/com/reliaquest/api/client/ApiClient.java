package com.reliaquest.api.client;

import com.reliaquest.api.dto.ApiDeleteRequestDto;
import com.reliaquest.api.dto.ApiResponse;
import com.reliaquest.api.dto.EmpRequestDto;
import com.reliaquest.api.exception.ClientException;
import com.reliaquest.api.exception.ResourceNotFoundException;
import com.reliaquest.api.exception.ServerException;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Component
public class ApiClient {

    private final WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    @Autowired
    public ApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8112/api/v1/employee")
                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    // Log the URL of the request
                    logger.debug("Requested API URL: {}", clientRequest.url());
                    return Mono.just(clientRequest);
                }))
                .build();
    }

    public ApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // Get all employees
    public Mono<List<ApiResponse.Employee>> getAllEmployees() {
        return webClient
                .get()
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(
                                new ResourceNotFoundException(HttpStatus.NOT_FOUND.value(), "Employee not found"));
                    }
                    return Mono.error(
                            new ClientException(clientResponse.statusCode().value(), "Client Error occurred."));
                })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(
                                new ServerException(clientResponse.statusCode().value(), "Server error occurred")))
                .bodyToMono(ApiResponse.EmployeeListResponse.class)
                .map(ApiResponse.EmployeeListResponse::data)
                .retryWhen(retryBackoffSpec());
    }

    // Search employees by name fragment
    public Mono<List<ApiResponse.Employee>> getEmployeesByNameSearch(String searchString) {
        return getAllEmployees().map(employees -> employees.stream()
                .filter(employee -> employee.employee_name().equalsIgnoreCase(searchString))
                .toList());
    }

    // Get a single employee by ID
    public Mono<ApiResponse.Employee> getEmployeeById(String id) {
        return webClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(ApiResponse.EmployeeResponse.class)
                .map(ApiResponse.EmployeeResponse::data)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        // Log the error or take some action if needed
                        logger.error("Employee with ID {} not found. ", id);
                        return Mono.error(new ResourceNotFoundException(
                                HttpStatus.NOT_FOUND.value(), "Employee with ID " + id + " not found."));
                    }
                    return Mono.error(e); // Propagate other errors
                })
                .retryWhen(retryBackoffSpec());
    }

    // Get the highest salary among all employees
    public Mono<Integer> getHighestSalaryOfEmployees() {
        return getAllEmployees().map(employees -> employees.stream()
                .map(ApiResponse.Employee::employee_salary)
                .max(Integer::compare)
                .orElseThrow(() -> new RuntimeException("No employees found")));
    }

    // Get the top 10 highest earning employees
    public Mono<List<String>> getTop10HighestEarningEmployeeNames() {
        return getAllEmployees().map(employees -> employees.stream()
                .sorted((e1, e2) -> Integer.compare(e2.employee_salary(), e1.employee_salary()))
                .limit(10)
                .map(ApiResponse.Employee::employee_name)
                .toList());
    }

    // Create a new employee
    public Mono<ApiResponse.Employee> createEmployee(EmpRequestDto employee) {
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(employee), EmpRequestDto.class)
                .retrieve()
                .bodyToMono(ApiResponse.EmployeeResponse.class)
                .map(ApiResponse.EmployeeResponse::data)
                .retryWhen(retryBackoffSpec());
    }

    // Delete an employee by ID
    public Mono<ApiResponse.DeleteRecordResponse> deleteEmployeeByName(ApiDeleteRequestDto name) {
        return webClient
                .method(HttpMethod.DELETE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(name), ApiDeleteRequestDto.class)
                .retrieve()
                .bodyToMono(ApiResponse.DeleteRecordResponse.class);
    }

    // Retry spec in case of rate limit response 429
    private RetryBackoffSpec retryBackoffSpec() {
        return Retry.backoff(5, Duration.ofSeconds(5)).filter(throwable -> {
            // Retry only for 403 responses
            if (throwable instanceof WebClientResponseException ex) {
                logger.error("API Response Code 429: Retrying... ");
                return ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            }
            return false;
        });
    }
}
