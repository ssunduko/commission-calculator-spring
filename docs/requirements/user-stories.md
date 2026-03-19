# User Stories — Commission Calculator (`com.chapman.edu.commissions.springboot`)

---

## 1. Authentication & Login

### US-1.1: User Login

| Field | Detail |
|-------|--------|
| **User Story** | As a **system user**, I want to log in with my username and password and receive a JWT token, so that I can securely access protected API endpoints. |
| **Priority** | Must |
| **Story Points** | 3 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a registered user with valid credentials, **When** they submit a POST to `/api/auth/login` with username and password, **Then** a 200 response is returned containing a JWT token, tokenType "Bearer", username, and roles array. |
| AC2 | **Given** a user provides incorrect credentials, **When** they submit a login request, **Then** a 401 response is returned with a generic error message that does not reveal whether the username or password was wrong. |
| AC3 | **Given** a deactivated user with valid credentials, **When** they attempt to log in, **Then** a 401 response is returned and no token is issued. |
| AC4 | **Given** a valid JWT token, **When** the user includes it as `Authorization: Bearer <token>` on subsequent requests, **Then** the request is authenticated and the user's roles are loaded from the token claims. |

---

### US-1.2: Token Expiration

| Field | Detail |
|-------|--------|
| **User Story** | As a **security administrator**, I want JWT tokens to expire after 24 hours, so that compromised tokens have a limited window of validity. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a token issued 24+ hours ago, **When** the user makes an API request with that token, **Then** a 401 response is returned indicating the token is expired. |
| AC2 | **Given** a token issued within the last 24 hours, **When** the user makes an API request, **Then** the request is processed normally. |
| AC3 | **Given** the `app.jwt.expirationMs` property is configured, **When** tokens are generated, **Then** they use the configured expiration value. |

---

### US-1.3: Public Endpoints

| Field | Detail |
|-------|--------|
| **User Story** | As an **unauthenticated visitor**, I want to access the health check, Swagger UI, and login endpoints without a token, so that I can verify the system is running and explore the API documentation. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** no Authorization header is provided, **When** a GET request is made to `/api/health`, **Then** a 200 response is returned with status "UP", application name, timestamp, and Java version. |
| AC2 | **Given** no Authorization header is provided, **When** a GET request is made to `/swagger-ui/`, **Then** the Swagger UI page loads successfully. |
| AC3 | **Given** no Authorization header is provided, **When** a GET request is made to any other `/api/**` endpoint, **Then** a 401 response is returned. |

---

## 2. User Management

### US-2.1: Create User

| Field | Detail |
|-------|--------|
| **User Story** | As a **system administrator**, I want to create new user accounts with specified roles, so that team members can access the commission system with appropriate permissions. |
| **Priority** | Must |
| **Story Points** | 3 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an admin is authenticated with SYSTEM_ADMIN role, **When** they submit a POST to `/api/users` with valid user details, **Then** a 201 response is returned with the created user including a generated ID and `active=true`. |
| AC2 | **Given** no roles are specified in the request, **When** the user is created, **Then** the default role `SALES_REP` is assigned. |
| AC3 | **Given** a username that already exists, **When** the admin attempts to create a user with that username, **Then** a 422 response is returned with a duplicate username error message. |
| AC4 | **Given** invalid input (username < 3 chars, invalid email, password < 6 chars), **When** the request is submitted, **Then** a 400 response is returned with field-level validation errors. |
| AC5 | **Given** a user with SALES_REP or SALES_MANAGER role, **When** they attempt to create a user, **Then** a 403 response is returned. |

---

### US-2.2: List and Filter Users

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to list users and filter by role, so that I can find members of my team. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a user with SALES_MANAGER or SYSTEM_ADMIN role, **When** they send a GET to `/api/users`, **Then** all users are returned in the response. |
| AC2 | **Given** the `role` query parameter is set to `SALES_REP`, **When** the request is made, **Then** only users with the SALES_REP role are returned. |
| AC3 | **Given** a user with SALES_REP role, **When** they attempt to list users, **Then** a 403 response is returned. |

---

### US-2.3: View User Details

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to view a specific user's profile by ID, so that I can see their department, territory, and role information. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a valid user ID, **When** a GET request is made to `/api/users/{id}`, **Then** the user's full profile is returned including id, username, email, firstName, lastName, roles, active status, department, and territory. |
| AC2 | **Given** a non-existent user ID, **When** the request is made, **Then** a 404 response is returned with message "User not found with id: '{id}'". |

---

### US-2.4: Deactivate User

| Field | Detail |
|-------|--------|
| **User Story** | As a **system administrator**, I want to deactivate a user account, so that former employees can no longer access the system without permanently deleting their records. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an admin sends a PATCH to `/api/users/{id}/deactivate`, **When** the request is processed, **Then** the user's `active` flag is set to `false` and the updated user is returned. |
| AC2 | **Given** a deactivated user, **When** they attempt to log in with valid credentials, **Then** authentication is rejected with a 401 response. |
| AC3 | **Given** a user without SYSTEM_ADMIN role, **When** they attempt to deactivate a user, **Then** a 403 response is returned. |

---

### US-2.5: Delete User

| Field | Detail |
|-------|--------|
| **User Story** | As a **system administrator**, I want to permanently delete a user account, so that I can remove test accounts or comply with data removal requests. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an admin sends a DELETE to `/api/users/{id}`, **When** the user exists, **Then** a 204 response is returned and the user is permanently removed. |
| AC2 | **Given** the user has been deleted, **When** a GET is made to `/api/users/{id}`, **Then** a 404 response is returned. |
| AC3 | **Given** a non-existent user ID, **When** the delete request is made, **Then** a 404 response is returned. |

---

## 3. Deal Management

### US-3.1: Create Deal

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales representative**, I want to create a new deal with a title, value, and my sales rep ID, so that I can track my sales pipeline and eventually earn commissions. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user submits a POST to `/api/deals` with title, value, and salesRepId, **When** the request is valid, **Then** a 201 response is returned with the deal in `OPEN` status, a generated ID, and `createdDate` set to today. |
| AC2 | **Given** a title shorter than 3 characters or longer than 100, **When** the request is submitted, **Then** a 400 response is returned with a validation error on the title field. |
| AC3 | **Given** a value of 0 or negative, **When** the request is submitted, **Then** a 400 response is returned with a validation error indicating minimum value is 0.01. |

---

### US-3.2: Update Deal Status

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales representative**, I want to update my deal's status to WON, LOST, or CANCELLED, so that the system reflects the current state of each deal in my pipeline. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a deal in OPEN status, **When** a PATCH is sent to `/api/deals/{id}/status?status=WON`, **Then** the deal status changes to WON and `lastModifiedDate` is updated. |
| AC2 | **Given** a deal in OPEN status, **When** status is changed to LOST or CANCELLED, **Then** the transition succeeds. |
| AC3 | **Given** a deal in WON, LOST, or CANCELLED status, **When** a status change is attempted, **Then** the request is rejected because these are terminal states. |

---

### US-3.3: List and Filter Deals

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to list deals and filter by status or sales rep, so that I can monitor my team's pipeline and identify won deals ready for commission calculation. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user sends a GET to `/api/deals`, **When** no filters are provided, **Then** all deals are returned. |
| AC2 | **Given** the `status=WON` query parameter, **When** the request is made, **Then** only deals with WON status are returned. |
| AC3 | **Given** the `salesRepId=user-003` query parameter, **When** the request is made, **Then** only deals belonging to that sales rep are returned. |
| AC4 | **Given** a specific deal ID, **When** a GET is made to `/api/deals/{id}`, **Then** the full deal details including products list are returned. |

---

### US-3.4: Delete Deal

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales representative**, I want to delete a deal that was created in error, so that my pipeline accurately reflects real opportunities. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user sends a DELETE to `/api/deals/{id}`, **When** the deal exists, **Then** a 204 response is returned and the deal is permanently removed. |
| AC2 | **Given** a non-existent deal ID, **When** the delete request is made, **Then** a 404 response is returned. |

---

## 4. Commission Plan Management

### US-4.1: Create Commission Plan

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want to create a new commission plan with a name, currency, and effective date range, so that I can define how commissions will be calculated for upcoming periods. |
| **Priority** | Must |
| **Story Points** | 3 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user submits a POST to `/api/plans` with name, currencyCode, and effectiveStartDate, **When** the request is valid, **Then** a 201 response is returned with the plan in `DRAFT` status. |
| AC2 | **Given** a currencyCode that is not exactly 3 characters, **When** the request is submitted, **Then** a 400 response is returned with a validation error. |
| AC3 | **Given** an effectiveEndDate that is before effectiveStartDate, **When** the request is submitted, **Then** a 422 response is returned indicating the date range is invalid. |

---

### US-4.2: Activate Commission Plan

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want to activate a draft commission plan, so that it becomes available for commission calculations. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a plan in DRAFT status, **When** a PATCH is sent to `/api/plans/{id}/activate`, **Then** the plan status changes to ACTIVE. |
| AC2 | **Given** a plan already in ACTIVE status, **When** activation is attempted, **Then** a 422 response is returned stating only DRAFT plans can be activated. |
| AC3 | **Given** a plan in ARCHIVED status, **When** activation is attempted, **Then** a 422 response is returned. |

---

### US-4.3: Archive Commission Plan

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want to archive an active commission plan, so that it is no longer used for new calculations while preserving historical records. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an active plan, **When** a PATCH is sent to `/api/plans/{id}/archive`, **Then** the plan status changes to ARCHIVED. |
| AC2 | **Given** an archived plan, **When** it is referenced in a new commission calculation, **Then** the calculation is rejected because only ACTIVE plans are allowed. |

---

### US-4.4: List Active Plans

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to view only active commission plans whose effective dates include today, so that I know which plans currently apply to my team's deals. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user sends a GET to `/api/plans/active`, **When** active plans exist with today within their effective date range, **Then** only those plans are returned. |
| AC2 | **Given** no active plans cover today's date, **When** the request is made, **Then** an empty list is returned. |
| AC3 | **Given** plans in DRAFT or ARCHIVED status, **When** the active plans endpoint is called, **Then** those plans are excluded from the results. |

---

### US-4.5: Delete Commission Plan

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want to delete a draft or archived plan, so that I can clean up plans that are no longer needed. |
| **Priority** | Should |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a plan in DRAFT status, **When** a DELETE is sent to `/api/plans/{id}`, **Then** a 204 response is returned and the plan is removed. |
| AC2 | **Given** a plan in ACTIVE status, **When** deletion is attempted, **Then** the request is rejected because active plans must be archived before deletion. |

---

### US-4.6: Plan Composition

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want commission plans to contain rules (standard, accelerator, bonus, special), tiers (value brackets with rates), and bonus rules (fixed or percentage-based), so that I can model complex commission structures. |
| **Priority** | Must |
| **Story Points** | 5 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a plan with commission rules, **When** the plan is retrieved, **Then** the response includes `ruleCount` reflecting the number of configured rules. |
| AC2 | **Given** a plan with tiers defining value brackets (e.g., Bronze 0-25K at 5%, Silver 25K-50K at 8%), **When** the plan is retrieved, **Then** the response includes `tierCount` reflecting the configured tiers. |
| AC3 | **Given** a plan with bonus rules (fixed amount and percentage-based), **When** the plan is retrieved, **Then** the response includes `bonusCount` reflecting the configured bonuses. |
| AC4 | **Given** bonus rules with start and end dates, **When** a commission is calculated, **Then** only bonuses whose date range includes the calculation date are applied. |

---

## 5. Commission Calculation

### US-5.1: Calculate Commission

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to calculate commission for a won deal using an active plan, so that the sales representative knows how much they earned. |
| **Priority** | Must |
| **Story Points** | 5 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a WON deal and an ACTIVE plan, **When** a POST is sent to `/api/calculations` with dealId, planId, and calculatedBy, **Then** a 201 response is returned with status `CALCULATED`, baseCommission, grossCommission, and netCommission populated. |
| AC2 | **Given** a deal that is not in WON status, **When** a calculation is attempted, **Then** a 422 response is returned stating "Can only calculate commission for WON deals". |
| AC3 | **Given** a plan that is not in ACTIVE status, **When** a calculation is attempted, **Then** a 422 response is returned stating the plan must be active. |
| AC4 | **Given** a plan with a STANDARD rule at 10% rate and a deal worth $45,000, **When** the commission is calculated, **Then** the baseCommission is $4,500.00 (rounded to 2 decimal places, HALF_UP). |
| AC5 | **Given** a plan with no STANDARD rule, **When** the commission is calculated, **Then** a default rate of 10% is applied. |

---

### US-5.2: Apply Bonuses to Calculation

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want active bonus rules on the plan to be automatically applied during commission calculation, so that sales reps receive their full entitled compensation. |
| **Priority** | Must |
| **Story Points** | 3 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a plan with a percentage-based bonus of 10%, **When** commission is calculated with a base of $4,500, **Then** a $450 bonus is added and grossCommission equals $4,950. |
| AC2 | **Given** a plan with a fixed $500 SPIF bonus, **When** commission is calculated, **Then** exactly $500 is added to the grossCommission. |
| AC3 | **Given** a bonus with an expired date range, **When** commission is calculated today, **Then** the expired bonus is not applied. |
| AC4 | **Given** multiple active bonuses, **When** commission is calculated, **Then** all applicable bonuses are summed and grossCommission = baseCommission + total bonuses. |

---

### US-5.3: Approve Commission

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to approve a calculated commission, so that it advances toward payout and the sales rep has confidence in the amount. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a calculation in CALCULATED status, **When** a PATCH is sent to `/api/calculations/{id}/approve` by a SALES_MANAGER, **Then** the status changes to APPROVED. |
| AC2 | **Given** a calculation not in CALCULATED status (e.g., PAID or APPROVED), **When** approval is attempted, **Then** the request is rejected. |
| AC3 | **Given** a user with SALES_REP role, **When** they attempt to approve a calculation, **Then** a 403 response is returned. |
| AC4 | **Given** users with FINANCE_ADMIN or SYSTEM_ADMIN roles, **When** they approve a calculation, **Then** the approval succeeds. |

---

### US-5.4: Mark Commission as Paid

| Field | Detail |
|-------|--------|
| **User Story** | As a **finance administrator**, I want to mark an approved commission as paid and record the payout date, so that the system accurately reflects completed payouts. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a calculation in APPROVED status, **When** a PATCH is sent to `/api/calculations/{id}/pay` by a FINANCE_ADMIN, **Then** the status changes to PAID and `payoutDate` is set to today's date. |
| AC2 | **Given** a calculation not in APPROVED status, **When** payment is attempted, **Then** the request is rejected. |
| AC3 | **Given** a user with SALES_REP or SALES_MANAGER role, **When** they attempt to mark as paid, **Then** a 403 response is returned. |

---

### US-5.5: List and Filter Calculations

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales representative**, I want to view my commission calculations and filter by deal, so that I can track my earnings across all my won deals. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user sends a GET to `/api/calculations`, **When** no filters are provided, **Then** all calculations are returned. |
| AC2 | **Given** the `dealId` query parameter, **When** the request is made, **Then** only calculations for that deal are returned. |
| AC3 | **Given** the `salesRepId` query parameter, **When** the request is made, **Then** only that rep's calculations are returned. |
| AC4 | **Given** a specific calculation ID, **When** a GET is made to `/api/calculations/{id}`, **Then** the full calculation details are returned including bonusCount and acceleratorCount. |

---

## 6. Dispute Management

### US-6.1: File a Dispute

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales representative**, I want to file a dispute against a commission calculation I believe is incorrect, so that my concern is formally recorded and reviewed. |
| **Priority** | Must |
| **Story Points** | 3 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user submits a POST to `/api/disputes` with calculationId, salesRepId, title, and description, **When** the request is valid, **Then** a 201 response is returned with the dispute in `INITIATED` status. |
| AC2 | **Given** a title shorter than 5 characters, **When** the request is submitted, **Then** a 400 response is returned with a validation error. |
| AC3 | **Given** a description shorter than 10 characters, **When** the request is submitted, **Then** a 400 response is returned with a validation error. |

---

### US-6.2: Resolve a Dispute

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to resolve a dispute with a resolution explanation, so that the sales rep receives a formal response and the dispute is closed. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a dispute not in RESOLVED or CANCELLED status, **When** a PATCH is sent to `/api/disputes/{id}/resolve` with `resolution` and `resolvedBy` parameters, **Then** the dispute status changes to RESOLVED, `resolvedDate` is set, and a system comment is added. |
| AC2 | **Given** a dispute already in RESOLVED status, **When** resolution is attempted again, **Then** a 422 response is returned. |
| AC3 | **Given** a dispute in CANCELLED status, **When** resolution is attempted, **Then** a 422 response is returned. |

---

### US-6.3: Escalate a Dispute

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to escalate a dispute to higher authority when I cannot resolve it myself, so that complex cases receive appropriate attention. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a non-escalated dispute, **When** a PATCH is sent to `/api/disputes/{id}/escalate`, **Then** the dispute `escalated` flag is set to `true`, status changes to ESCALATED, and a system comment is added. |
| AC2 | **Given** a dispute already escalated, **When** escalation is attempted again, **Then** a 422 response is returned. |

---

### US-6.4: List and Filter Disputes

| Field | Detail |
|-------|--------|
| **User Story** | As a **sales manager**, I want to list disputes and filter by status or sales rep, so that I can prioritize which disputes need my attention. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an authenticated user sends a GET to `/api/disputes`, **When** no filters are provided, **Then** all disputes are returned. |
| AC2 | **Given** the `status=INITIATED` query parameter, **When** the request is made, **Then** only disputes with INITIATED status are returned. |
| AC3 | **Given** the `salesRepId` query parameter, **When** the request is made, **Then** only disputes filed by that sales rep are returned. |
| AC4 | **Given** a specific dispute ID, **When** a GET is made to `/api/disputes/{id}`, **Then** the full dispute details are returned including commentCount, escalated flag, and resolution if resolved. |

---

## 7. Error Handling & API Consistency

### US-7.1: Consistent API Response Envelope

| Field | Detail |
|-------|--------|
| **User Story** | As an **API consumer**, I want all responses to follow a consistent envelope format with success flag, message, data, and timestamp, so that I can build reliable client integrations. |
| **Priority** | Must |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** any successful API response (2xx), **When** the response body is parsed, **Then** it contains `success: true`, a `message` string, the `data` payload, and an ISO 8601 `timestamp`. |
| AC2 | **Given** a validation error (400), **When** the response is returned, **Then** it contains field-level errors in a `validationErrors` map with field names as keys and error message arrays as values. |
| AC3 | **Given** a business rule violation (422), **When** the response is returned, **Then** it contains a descriptive error message explaining what went wrong and how to fix it. |

---

### US-7.2: Resource Not Found Handling

| Field | Detail |
|-------|--------|
| **User Story** | As an **API consumer**, I want to receive a clear 404 error with the resource type and ID when I request a non-existent resource, so that I can quickly diagnose integration issues. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** a request for a non-existent deal, **When** the response is returned, **Then** it is HTTP 404 with message format "Deal not found with id: '{id}'". |
| AC2 | **Given** a request for a non-existent user, plan, calculation, or dispute, **When** the response is returned, **Then** the same message pattern is used with the appropriate resource name. |

---

### US-7.3: Secure Error Messages

| Field | Detail |
|-------|--------|
| **User Story** | As a **security administrator**, I want error responses to never expose internal implementation details such as stack traces, class names, or SQL queries, so that attackers cannot gain insight into the system's internals. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** an unhandled server exception occurs, **When** the 500 response is returned, **Then** it contains a generic error message with no stack trace or internal class names. |
| AC2 | **Given** an authentication failure, **When** the 401 response is returned, **Then** it does not distinguish between incorrect username and incorrect password. |

---

## 8. Health, Observability & Developer Experience

### US-8.1: Health Check Endpoint

| Field | Detail |
|-------|--------|
| **User Story** | As a **DevOps engineer**, I want a health check endpoint that reports application status, so that I can configure load balancer health probes and monitoring alerts. |
| **Priority** | Must |
| **Story Points** | 1 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** the application is running, **When** a GET is made to `/api/health`, **Then** a 200 response is returned with `status: "UP"`, `application` name, current `timestamp`, and `javaVersion`. |
| AC2 | **Given** no authentication token, **When** the health check is called, **Then** it still returns 200 (public endpoint). |

---

### US-8.2: API Documentation

| Field | Detail |
|-------|--------|
| **User Story** | As an **API consumer**, I want interactive Swagger UI documentation and a machine-readable OpenAPI spec, so that I can explore and test the API without reading source code. |
| **Priority** | Should |
| **Story Points** | 2 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** the application is running, **When** a browser navigates to `/swagger-ui/`, **Then** the Swagger UI loads with all endpoints grouped by tag. |
| AC2 | **Given** the application is running, **When** a GET is made to `/api-docs`, **Then** a valid OpenAPI JSON spec is returned describing all endpoints, schemas, and security requirements. |
| AC3 | **Given** the Swagger UI, **When** a user provides a JWT token via the "Authorize" button, **Then** subsequent "Try it out" requests include the Bearer token. |

---

### US-8.3: Sample Data for Development

| Field | Detail |
|-------|--------|
| **User Story** | As a **developer**, I want the system to load sample data on startup including users, plans, deals, calculations, and disputes, so that I can immediately test and demo all features without manual setup. |
| **Priority** | Should |
| **Story Points** | 3 |

**Acceptance Criteria**

| # | Criteria |
|---|---------|
| AC1 | **Given** the application starts up, **When** the SampleDataLoader runs, **Then** 6 users are created (admin, manager, 3 reps, finance admin) with correct roles and departments. |
| AC2 | **Given** sample data is loaded, **When** logging in as admin/admin123, **Then** a valid JWT with SYSTEM_ADMIN role is returned. |
| AC3 | **Given** sample data is loaded, **When** listing plans, **Then** 3 plans exist (2 ACTIVE with tiers and bonuses, 1 DRAFT). |
| AC4 | **Given** sample data is loaded, **When** listing deals, **Then** 6 deals exist in various statuses (4 WON, 1 OPEN, 1 LOST). |
| AC5 | **Given** sample data is loaded, **When** listing calculations, **Then** 4 calculations exist in various statuses (APPROVED, CALCULATED, PAID, DISPUTED). |
| AC6 | **Given** sample data is loaded, **When** viewing the dispute, **Then** it contains both system and user comments. |
