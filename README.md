# Multi-Tenant AI Knowledge & Decision Assistant

A production-ready multi-tenant SaaS MVP built with **Spring Boot 3.2**, **MySQL**, **Apache PDFBox**, and a **Hybrid AI Decision Engine**. This application allows organizations to create independent accounts, upload internal PDF policy documents, generate secure assistant widgets, and handle visitor queries using a combination of generative LLMs and System 1 structured decision models.

---

## 🏗️ 1. Architecture & System Design

The system implements a **Hybrid AI Architecture** that separates natural language processing from structured decision logic, preventing the anti-pattern of relying on a single raw LLM prompt.

### **System Architecture Diagram**
```text
+---------------------------------------------------------------------------------+
|                                FRONTEND TIER                                    |
|   +-----------------------+               +---------------------------------+   |
|   | Admin Dashboard       |               | Visitor Chat Widget             |   |
|   | (dashboard.html)      |               | (chat.html via Iframe Embed)    |   |
|   +-----------+-----------+               +---------------+-----------------+   |
+-----------------------------------------------------------|---------------------+
                                                            | HTTP POST /api/ask
                                                            v
+---------------------------------------------------------------------------------+
|                            SPRING BOOT 3.2 BACKEND                              |
|   +-------------------------------------------------------------------------+   |
|   |                       Tenant Validation Filter                          |   |
|   |         (Resolves Organization ID securely via Assistant Token)         |   |
|   +-----------------------------------+-------------------------------------+   |
|                                       |                                         |
|                                       v                                         |
|   +-------------------------------------------------------------------------+   |
|   |                        Hybrid AI Decision Pipeline                      |   |
|   |  1. Knowledge Retrieval: Fetch isolated chunks (Apache PDFBox)          |   |
|   |  2. Understanding & Decomposition: LLM breaks question into sub-tasks    |   |
|   |  3. System 1 Decision Engine: Parallel Boolean / Choice / Score models  |   |
|   |  4. Synthesis & Grounding: LLM generates final response with citations  |   |
|   +-----------------------------------+-------------------------------------+   |
|                                       |                                         |
+---------------------------------------|-----------------------------------------+
                                        v
+---------------------------------------------------------------------------------+
|                             MYSQL STORAGE TIER                                  |
|   +-------------------+  +------------------+  +----------------------------+   |
|   |   organizations   |  |    documents     |  |      document_chunks       |   |
|   +-------------------+  +------------------+  +----------------------------+   |
|   | id (PK)           |  | id (PK)          |  | id (PK)                    |   |
|   | name              |  | organization_id  |  | organization_id (FK/Index) |   |
|   | created_at        |  | file_path        |  | document_id (FK)           |   |
|   +-------------------+  | page_count       |  | chunk_text                 |   |
|                          +------------------+  +----------------------------+   |
+---------------------------------------------------------------------------------+
```

### **Data Flow for Visitor Queries (`POST /api/ask`)**
1. **Token Resolution:** The incoming request supplies an `assistant_token`. The backend resolves this securely to a specific `organization_id`.
2. **Knowledge Retrieval:** Queries the `document_chunks` table restricted strictly to that `organization_id`.
3. **Decomposition:** The LLM inspects the user question and extracted text chunks to isolate required variables (e.g., GPA, attendance, leave days).
4. **System 1 Decision Execution:** Evaluates independent conditions in parallel (Boolean eligibility checks, threshold comparisons, categorization).
5. **Synthesis:** Combines structured outputs and formulates a grounded natural-language response with exact page citations (e.g., `policy.pdf — Page 2`).

---

## 🚀 2. Local Setup Instructions

### **Prerequisites**
* Java Development Kit (JDK) 17 or higher.
* Maven 3.8+.
* MySQL Server 8.0+.

### **Step 1: Database Setup**
Log into MySQL and create the local database:
```sql
CREATE DATABASE ai_assistant_db;
USE ai_assistant_db;
```

### **Step 2: Configuration (`application.properties`)**
Configure your datasource and file upload directories in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai_assistant_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# File Upload Limits (Section 3 Compliance)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=uploads
```

### **Step 3: Build & Run**
Run the Spring Boot application using Maven:
```bash
mvn clean install
mvn spring-boot:run
```

The application will be live at **`http://localhost:8080/dashboard.html`**.

---

## 🔌 3. Major API Endpoints

### **A. Administrative Endpoints**
* **`POST /api/organizations`**
  * *Description:* Registers a new organization and initializes its default assistant token.
  * *Body:* `{ "name": "TechCorp University" }`
* **`POST /api/organizations/{orgId}/documents`**
  * *Description:* Uploads a PDF document (enforces max 10 PDFs, max 20 pages, max 10MB limits, extracts text via PDFBox, and chunks it).
  * *Form-Data:* `file` (Multipart PDF file)
* **`GET /api/organizations/{orgId}/documents`**
  * *Description:* Lists all documents uploaded by the specified organization.
* **`DELETE /api/organizations/{orgId}/documents/{docId}`**
  * *Description:* Deletes a specific document and its associated chunks.

### **B. Visitor & Assistant Endpoints**
* **`POST /api/ask`**
  * *Description:* Processes visitor questions through the hybrid retrieval and decision engine.
  * *Body:* `{ "token": "your-assistant-token", "question": "Am I eligible for a scholarship?" }`
  * *Response:* Returns structured answer, evaluation breakdown, and source citations (e.g., `policy.pdf — Page 2`).

---

## 🔒 4. Security & Multi-Tenant Isolation

* **Database-Level Isolation:** Every table (`documents`, `document_chunks`, `assistants`) enforces foreign-key constraints linked directly to `organization_id`.
* **Server-Side Token Validation:** Assistant tokens are checked on every incoming request. Cross-tenant queries are blocked at the repository layer.
* **Dashboard Protection:** Administrative endpoints and dashboard views are strictly separated from visitor chat widgets (`chat.html`), ensuring visitor tokens cannot expose admin controls or private files.

---

## 🧪 5. Test Data Suite (Section 18 Compliance)

* **Organization 1:** `TechCorp University` (PDFs: `techcorp_policy.pdf`, `techcorp_admissions.pdf`)
* **Organization 2:** `GlobalTech Solutions` (PDFs: `globaltech_hr.pdf`, `globaltech_benefits.pdf`)
* **Test Coverage:** Includes direct factual queries, procedural lookups, multi-condition eligibility checks, parallel System 1 decisions, and out-of-scope fallback queries (`UNANSWERABLE_OUT_OF_SCOPE`).