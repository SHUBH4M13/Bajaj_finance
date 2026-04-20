package com.Bajaj.bajaj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... args) {

        try {
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", "John Doe");
            requestBody.put("regNo", "REG12347");
            requestBody.put("email", "john@example.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(generateUrl, request, Map.class);

            System.out.println("Generate Webhook Response: " + response.getBody());

            Map<String, String> responseMap = response.getBody();

            String webhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
            String accessToken = responseMap.get("accessToken");

            System.out.println("TOKEN: " + accessToken);

            String sqlQuery = "SELECT p.AMOUNT AS SALARY, " +
                    "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                    "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                    "d.DEPARTMENT_NAME " +
                    "FROM PAYMENTS p " +
                    "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                    "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                    "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                    "AND p.AMOUNT = (SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) <> 1);";

            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);

            // ✅ FIX: NO "Bearer"
            submitHeaders.set("Authorization", accessToken);

            Map<String, String> finalBody = new HashMap<>();
            finalBody.put("finalQuery", sqlQuery);

            HttpEntity<Map<String, String>> submitRequest =
                    new HttpEntity<>(finalBody, submitHeaders);

            ResponseEntity<String> submitResponse =
                    restTemplate.exchange(
                            webhookUrl,
                            HttpMethod.POST,
                            submitRequest,
                            String.class
                    );

            System.out.println("Submission Response:");
            System.out.println(submitResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}