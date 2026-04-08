# 💸 Expense Tracker API

A production-ready RESTful API for tracking personal expenses, built with **Java 17**, **Spring Boot 4**, and **Amazon DynamoDB** as the primary data store. Designed with scalability and cloud-native principles in mind.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4 (Spring Web MVC) |
| Database | Amazon DynamoDB (AWS SDK v2) |
| ORM / Mapping | DynamoDB Enhanced Client (High-Level API) |
| Validation | Spring Boot Validation (Jakarta Bean Validation) |
| Boilerplate Reduction | Lombok |
| Environment Config | dotenv-java |
| Build Tool | Maven |

---

## ☁️ Why DynamoDB?

Rather than reaching for a traditional relational database, this project uses **Amazon DynamoDB** — a fully managed, serverless NoSQL database built for single-digit millisecond performance at any scale. Here's why it fits this use case:

- **Schemaless & flexible** — expense records can carry varying attributes (tags, currency, notes) without costly schema migrations.
- **Pay-per-request billing** — ideal for bursty or unpredictable workloads where you only pay for what you use.
- **Built-in scalability** — no connection pooling, no vertical scaling, no manual sharding.
- **AWS SDK v2 Enhanced Client** — the `DynamoDbEnhancedClient` provides a type-safe, annotation-driven mapping layer that eliminates boilerplate `AttributeValue` manipulation.

### DynamoDB Data Model

Expenses are stored using a composite primary key strategy for efficient access patterns:

```
Table: Expenses
├── Partition Key (PK): userId       → isolates data per user
└── Sort Key (SK):      expenseId    → enables range queries (by date, amount, etc.)
```

This design allows queries like:
- *"Get all expenses for user X"* → Query by PK
- *"Get expenses for user X in a date range"* → Query by PK + SK begins_with or between

---

## 📁 Project Structure

```
expense-tracker/
├── src/
│   └── main/
│       └── java/com/jamel/expense_tracker/
│           ├── ExpenseTrackerApplication.java   # Entry point
│           ├── controller/                      # REST controllers
│           ├── service/                         # Business logic
│           ├── repository/                      # DynamoDB data access layer
│           ├── model/                           # Entity / DTO classes
│           └── config/                          # AWS + DynamoDB client config
├── .env                                         # AWS credentials (gitignored)
├── pom.xml
└── README.md
```

---

## ⚙️ Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- An AWS account with DynamoDB access (or LocalStack for local development)

### 1. Clone the repository

```bash
git clone https://github.com/Zamoula/expense-tracker.git
cd expense-tracker
```

### 2. Configure environment variables

Create a `.env` file in the project root:

```env
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
DYNAMODB_TABLE_NAME=Expenses
```

> Credentials are loaded at runtime via `dotenv-java` — no hardcoded secrets.

### 3. Create the DynamoDB table

You can create the table via the AWS CLI:

```bash
aws dynamodb create-table \
  --table-name Expenses \
  --attribute-definitions \
      AttributeName=userId,AttributeType=S \
      AttributeName=expenseId,AttributeType=S \
  --key-schema \
      AttributeName=userId,KeyType=HASH \
      AttributeName=expenseId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST
```

### 4. Build and run

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/expenses/{userId}` | Retrieve all expenses for a user |
| `GET` | `/api/expenses/{userId}/{expenseId}` | Retrieve a specific expense |
| `POST` | `/api/expenses` | Create a new expense |
| `PUT` | `/api/expenses/{userId}/{expenseId}` | Update an existing expense |
| `DELETE` | `/api/expenses/{userId}/{expenseId}` | Delete an expense |

---

## 🛠️ DynamoDB Integration Details

The project uses the **AWS SDK for Java v2** with the **Enhanced Client** for type-safe DynamoDB interactions:

```java
// High-level Enhanced Client — annotation-driven, no raw AttributeValue handling
DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
    .dynamoDbClient(dynamoDbClient)
    .build();

DynamoDbTable<Expense> expenseTable = enhancedClient
    .table("Expenses", TableSchema.fromBean(Expense.class));
```

Entity mapping uses DynamoDB annotations:

```java
@DynamoDbBean
public class Expense {

    @DynamoDbPartitionKey
    private String userId;

    @DynamoDbSortKey
    private String expenseId;

    private String category;
    private Double amount;
    private String description;
    private String date;
}
```

---

## 🔒 Security & Best Practices

- AWS credentials are **never hardcoded** — managed via `.env` and excluded from version control.
- Input validation enforced at the controller layer using `@Valid` and Jakarta Bean Validation annotations.
- `.env` is listed in `.gitignore` to prevent accidental credential exposure.

---

## 📌 Future Improvements

- [ ] Add authentication with AWS Cognito or Spring Security + JWT
- [ ] Implement a GSI (Global Secondary Index) for querying expenses by category or date
- [ ] Add pagination support using DynamoDB's `LastEvaluatedKey`
- [ ] Deploy to AWS Lambda + API Gateway for a fully serverless architecture
- [ ] Add Docker + LocalStack support for local DynamoDB development

---

## 👤 Author

**Zamoula** — [@Zamoula](https://github.com/Zamoula)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
