# Temporary Job Provider App - README

## Introduction

Hi! My name is **Prajwal Rode**, a Web Development enthusiast with a strong foundation in Data Structures and Algorithms (DSA) and over **700+ problems solved**.  

**Tech Stack:**  
- Backend: Spring, Spring Boot, Spring Security, Node.js, Express.js  
- Database: PostgreSQL, MongoDB  
- Languages: JavaScript, TypeScript, Java, C++, HTML, CSS  

**Contact:**  
- **Email:** prajwalrode31@gmail.com  
- **Mobile:** +91-9130148301  

---

## Project Overview

This project is a **temporary job provider app** where:  
- **Wishmaker** represents a job provider who posts job openings (**wishes**).  
- **Genie** represents a user who applies for these jobs (**wishes**).  

### Key Features
1. **Role-Based Access Control (RBAC):**  
   - Two roles: **Wishmaker** and **Genie**.  
   - Specific endpoints can only be accessed by specific roles, ensuring proper security.  
   - Example:  
     - Endpoints like `/getGenie`, `/getAllWishByGenieEmail`, and `/acceptWish` can only be accessed by users with the **GENIE** role.  
   - This is achieved using `.hasRole()` in the security configuration:  
     ```java
     .requestMatchers("/getGenie", "/getAllWishByGenieEmail", "/acceptWish").hasRole("GENIE")
     ```

2. **User-Specific Data Access:**  
   - Ensured using method-level security with `@PreAuthorize`.  
   - Example:  
     ```java
     @PreAuthorize("#email == authentication.principal.claims['sub']")
     ```
     This ensures that only the logged-in user can access their own details.

3. **JWT-Based Statelessness:**  
   - Uses **JSON Web Tokens (JWT)** to ensure a stateless architecture.  
   - JWT tokens are self-contained, meaning they include all the necessary user information to validate requests directly on the resource server.  
   - No need to repeatedly query the authorization server to verify user existence.  

4. **OAuth2 with OpenID Connect:**  
   - Implements the **Authorization Code Grant Type** flow between the client, resource server, and authorization server.  
   - After successful login, the authorization server sends a JWT token to the client, which can be used to access role-specific endpoints on the resource server.

---

## 📜 Project Flow  

### 1️⃣ Registration  
- Users register as either **Wishmaker** or **Genie**.  
- These APIs are public and do not require authentication.  

### 2️⃣ Login  
- On login, the **Authorization Code Grant Flow** is triggered:  
  1. The Authorization Server verifies user credentials.  
  2. If valid, it generates a **JWT token** and sends it to the client (Postman).  

### 3️⃣ Role-Based API Access  
- The client includes the JWT token in the `Authorization` header for resource server requests.  
- The **Resource Server**:  
  1. Validates the JWT token (checks tampering, expiry, etc.).  
  2. Extracts user roles and checks permissions for the requested endpoint.  

---

## How to Run Locally

### Prerequisites
1. **Java Development Kit (JDK)** installed on your system.
2. **IntelliJ IDEA** or **Eclipse IDE** (IntelliJ recommended).
3. **PostgreSQL** installed and running.
4. **Postman** installed for API testing.

---

### Steps to Run

1. **Download the Source Code**  
   - Clone or download the zip files for **AuthServer** and **ResServer** from their respective GitHub repositories.
   - Extract the zip files and open each folder as a separate project in your IDE (IntelliJ recommended).

2. **Configure the Database**  
   - Navigate to the `application.properties` file under the `src/main/resources` directory in both projects.  
   - Update the PostgreSQL database configurations for each project:
     - `spring.datasource.url` (database URL)
     - `spring.datasource.username` (your PostgreSQL username)
     - `spring.datasource.password` (your PostgreSQL password)
   - Example:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```
   - Ensure the database names are **same** for **AuthServer** and **ResServer**, if required.

3. **Run the Projects**  
   - Before running , you must have a Database created with the same Database name , you provided.
   - Start the **AuthServer** application on port `9000`.
   - Start the **ResServer** application on port `8080`.

4. **Verify Table Creation**  
   - Check your PostgreSQL database to ensure the required tables are created automatically. A total of **5 tables** should be generated.

5. **Test APIs with Postman**  
   - Download the Postman collection from the [Postman Collection](https://github.com/prajwal3641/Role-Based-Access-Control-Application/blob/main/Genie%20Projects.postman_collection.json).
   - Import the collection into Postman for easy access to all API endpoints.  
   - Start by registering the two main entities:
     - **Genie**
     - **Wishmaker**  
   - The registration endpoints are not role-based and have public access. Simply send the corresponding requests from Postman to add entries to the database.

6. **Verify Database Changes**  
   - Check the `Genie` and `Wishmaker` tables in your database to confirm the changes are reflected after the registration requests.

---

### PUBLIC API Endpoints Overview

| **API**                | **Method** | **Description**                      | **Endpoint**                      |
|-------------------------|------------|--------------------------------------|-----------------------------------|
| Register Genie          | `POST`     | Registers a new Genie               | `/registerGenie`            |
| Register Wishmaker      | `POST`     | Registers a new Wishmaker           | `/registerWishmaker`        |


---
---

## How to Test Role-Based APIs

Some APIs in this project are role-based, meaning they require the right user credentials and a valid JWT token. Follow these steps to test such APIs in Postman:

### Steps for Authentication and Authorization

1. **Set Up Authorization in Postman**  
   - Open the desired API endpoint in Postman.
   - Go to the **Authorization** tab.
   - Select **OAuth 2.0** from the "Type" dropdown.
   - Fill in the required details as shown below
      - Token Name :- AccessToken
      - Grant Type :- Authorization Code
      - Callback URL :- https://oauth.pstmn.io/v1/callback
      - Click on Authorize using browser
      - Auth URL :- http://localhost:9000/oauth2/authorize
      - Access Token URL :- http://localhost:9000/oauth2/token
      - Client ID :- GenieAuthCodeClient
      - Client Secret :- Qw3rTy6UjMnB9zXcV2pL0sKjHn5TxQqB
      - Scope :- openid email
      - State :- 2fc61105-40ba-4b5f-8983-ec764ec4e437
      - Client Authentication :- Send client credentials in body


2. **Get a New Access Token**  
   - Click on **Get New Access Token**.
   - You will be redirected to the login page hosted by the **AuthServer**.
   - Enter the credentials for the specific role required by the endpoint:
     - Use **Genie credentials** for Genie-specific endpoints.
     - Use **Wishmaker credentials** for Wishmaker-specific endpoints.
   - After login, you will be redirected back to Postman.

3. **Use the Access Token**  
   - Click on **Proceed** and then click **Use Token** to attach the JWT token to your request.

4. **Send the API Request**  
   - Now, your request is ready to be sent to the **ResServer** with a valid JWT token.

5. **Handle Expired Tokens**  
   - If your token expires, go to the "Current Token" section in Postman and click on **Refresh Token** to get a new token.
   - If tokens are not refreshing , then you have to login again !!

### Important Notes

- Follow the steps above for every endpoint based on the required user role.  
- Ensure you log in as the correct user for each endpoint (e.g., Genie or Wishmaker).  
- If a token is invalid or missing, the server will respond with an appropriate error message (e.g., **401 Unauthorized**).
- Also if wrong user is accessing the endpoint , then it will get **403 forbidden error**.

---

## API Endpoints and Their Functionality

Below is the list of APIs available in the project, categorized by functionality and access control.

| **Endpoint**                        | **Description**                                                                                 | **Input**                            | **Output**                  | **Access Control**                                                                 |
|-------------------------------------|-------------------------------------------------------------------------------------------------|--------------------------------------|--------------------------------|-----------------------------------------------------------------------------------|
| `/registerWishmaker`                | Registers a Wishmaker.                                                                          | Wishmaker details in request body.   | Success message as a string.  | Public API. Anyone can register as a Wishmaker.                                  |
| `/registerGenie`                    | Registers a Genie.                                                                              | Genie details in request body.       | Success message as a string.  | Public API. Anyone can register as a Genie.                                      |
| `/getWishmaker`                     | Retrieves Wishmaker details using their email.                                                  | Email as a request parameter.        | JSON object of Wishmaker.    | Only accessible by role **Wishmaker**, and only the logged-in user's details.    |
| `/getGenie`                         | Retrieves Genie details using their email.                                                      | Email as a request parameter.        | JSON object of Genie.        | Only accessible by role **Genie**, and only the logged-in user's details.        |
| `/getAllWishByGenieEmail`           | Fetches all wishes applied for by a Genie.                                                      | Email as a request parameter.        | List of Wish objects.        | Only accessible by role **Genie**, and only the logged-in user's data.           |
| `/getAllWishesByWishmakerEmail`     | Fetches all wishes created by a Wishmaker.                                                      | Email as a request parameter.        | List of Wish objects.        | Only accessible by role **Wishmaker**, and only the logged-in user's data.       |
| `/acceptWish`                       | Allows a Genie to apply for a Wish.                                                             | Wish ID as a request parameter.      | JSON object of the Wish.      | Only accessible by role **Genie** for the logged-in user.                        |
| `/insertWish`                       | Allows a Wishmaker to register a new Wish.                                                      | Wish details in request body.        | Success message as a string.  | Only accessible by role **Wishmaker**.                                           |

---

 



### Notes

- All schemas are automatically created by the application, so no manual SQL scripts are required.  
- Make sure both applications are connected to the correct database before running the API tests.  
- For any additional configurations, refer to the `application.properties` file.
