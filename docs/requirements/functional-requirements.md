# Functional Requirements — Commission Calculator (`com.chapman.edu.commissions.springboot`)

---

## 1. User Authentication & Security

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-AUTH-001 | The system shall authenticate users via `POST /api/auth/login` accepting username and password, returning a signed JWT token with user roles embedded as claims. | Must | None | Verify valid credentials return 200 with JWT token, tokenType "Bearer", username, and roles array. Verify invalid credentials return 401. |
| REQ-AUTH-002 | The system shall validate a Bearer JWT token on every request to protected `/api/**` endpoints via the `JwtAuthenticationFilter`, extracting the username and loading authorities from the token claims. | Must | REQ-AUTH-001 | Verify requests without Authorization header return 401. Verify requests with valid Bearer token succeed. Verify expired tokens return 401. |
| REQ-AUTH-003 | JWT tokens shall expire after 24 hours (configurable via `app.jwt.expirationMs`). Expired tokens shall be rejected with a 401 response. | Must | REQ-AUTH-001 | Verify token created 24+ hours ago is rejected. Verify token within expiration window is accepted. |
| REQ-AUTH-004 | The system shall enforce stateless session management for API endpoints. No HTTP session shall be created or maintained between requests. | Must | REQ-AUTH-002 | Verify no `JSESSIONID` cookie is set on API responses. Verify each request is independently authenticated via JWT. |
| REQ-AUTH-005 | The system shall hash all user passwords using BCrypt before storage. Plaintext passwords shall never be persisted or returned in API responses. | Must | None | Verify stored password starts with `$2a$` BCrypt prefix. Verify no API response contains `password` or `passwordHash` fields. |
| REQ-AUTH-006 | The following endpoints shall be publicly accessible without authentication: `POST /api/auth/login`, `GET /api/health`, Swagger UI (`/swagger-ui/**`), and OpenAPI docs (`/api-docs/**`). | Must | None | Verify each public endpoint returns 200 without an Authorization header. Verify other `/api/**` endpoints return 401 without a token. |
| REQ-AUTH-007 | The system shall disable CSRF protection for all API endpoints since token-based authentication is used instead of cookie-based sessions. | Must | REQ-AUTH-004 | Verify POST/PATCH/DELETE API requests succeed without a CSRF token. |
| REQ-AUTH-008 | Inactive users (where `active=false`) shall be rejected during authentication even if credentials are valid. | Must | REQ-AUTH-005 | Verify deactivated user receives 401 on login attempt. Verify reactivated user can log in again. |

---

## 2. Role-Based Access Control (RBAC)

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-RBAC-001 | The system shall support four user roles: `SALES_REP`, `SALES_MANAGER`, `FINANCE_ADMIN`, and `SYSTEM_ADMIN`. Each user may have one or more roles. | Must | REQ-AUTH-001 | Verify users can be created with each role. Verify users can hold multiple roles simultaneously. |
| REQ-RBAC-002 | Endpoints under `/api/users/**` shall require `SYSTEM_ADMIN` or `SALES_MANAGER` role for read access. `POST`, `PATCH`, and `DELETE` user operations shall require `SYSTEM_ADMIN` role. | Must | REQ-RBAC-001 | Verify `SALES_REP` receives 403 on `GET /api/users`. Verify `SALES_MANAGER` can list users but receives 403 on `POST /api/users`. Verify `SYSTEM_ADMIN` can perform all user operations. |
| REQ-RBAC-003 | `PATCH /api/calculations/{id}/approve` shall require `SALES_MANAGER`, `FINANCE_ADMIN`, or `SYSTEM_ADMIN` role. `SALES_REP` users shall not be able to approve calculations. | Must | REQ-RBAC-001 | Verify `SALES_REP` receives 403 on approve. Verify `SALES_MANAGER`, `FINANCE_ADMIN`, and `SYSTEM_ADMIN` each receive 200. |
| REQ-RBAC-004 | `PATCH /api/calculations/{id}/pay` shall require `FINANCE_ADMIN` or `SYSTEM_ADMIN` role. `SALES_REP` and `SALES_MANAGER` users shall not be able to mark calculations as paid. | Must | REQ-RBAC-001 | Verify `SALES_REP` and `SALES_MANAGER` receive 403 on pay. Verify `FINANCE_ADMIN` and `SYSTEM_ADMIN` each receive 200. |
| REQ-RBAC-005 | Endpoints under `/api/admin/**` shall require `SYSTEM_ADMIN` role exclusively. | Must | REQ-RBAC-001 | Verify non-admin roles receive 403 on admin endpoints. |
| REQ-RBAC-006 | All other authenticated `/api/**` endpoints (deals, plans, calculations read, disputes) shall be accessible to any user with a valid JWT token regardless of role. | Must | REQ-AUTH-002 | Verify `SALES_REP` can create deals, view plans, create disputes, and list calculations. |

---

## 3. User Management

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-USER-001 | `POST /api/users` shall create a new user with the provided username, email, firstName, lastName, password, and optional roles, department, territory, and managerId. New users shall be active by default. | Must | REQ-RBAC-002 | Verify user creation returns 201 with all provided fields. Verify `active` defaults to `true`. |
| REQ-USER-002 | The system shall enforce unique usernames. Attempting to create a user with a duplicate username shall return HTTP 422 with a `BusinessValidationException`. | Must | REQ-USER-001 | Verify creating two users with the same username returns 422 on the second attempt. |
| REQ-USER-003 | If no roles are specified during user creation, the system shall assign the default role `SALES_REP`. | Must | REQ-USER-001 | Verify user created without roles has `SALES_REP` in the response roles array. |
| REQ-USER-004 | User creation shall validate: username (3-50 chars), email (valid format), firstName (not blank), lastName (not blank), and password (minimum 6 characters). Invalid input shall return HTTP 400 with field-level validation errors. | Must | REQ-USER-001 | Verify each constraint violation returns 400 with the field name and error message in `validationErrors`. |
| REQ-USER-005 | `GET /api/users` shall return all users. When the `role` query parameter is provided, only users with that role shall be returned. | Must | REQ-RBAC-002 | Verify unfiltered request returns all users. Verify `?role=SALES_REP` returns only sales reps. |
| REQ-USER-006 | `GET /api/users/{id}` shall return the user with the specified ID, or HTTP 404 if the user does not exist. | Must | REQ-RBAC-002 | Verify existing user returns 200 with correct data. Verify non-existent ID returns 404. |
| REQ-USER-007 | `PATCH /api/users/{id}/deactivate` shall set the user's `active` flag to `false`. Deactivated users shall be unable to authenticate (REQ-AUTH-008). | Must | REQ-RBAC-002 | Verify deactivated user has `active=false` in response. Verify subsequent login attempt fails. |
| REQ-USER-008 | `DELETE /api/users/{id}` shall permanently remove the user record. Returns HTTP 204 on success, HTTP 404 if user does not exist. | Must | REQ-RBAC-002 | Verify deletion returns 204. Verify subsequent `GET /api/users/{id}` returns 404. |

---

## 4. Deal Management

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-DEAL-001 | `POST /api/deals` shall create a new deal with the provided title, value, and salesRepId. New deals shall have status `OPEN` and `createdDate` set to the current date. | Must | REQ-AUTH-002 | Verify deal creation returns 201 with status `OPEN` and a generated ID. |
| REQ-DEAL-002 | Deal creation shall validate: title (3-100 chars, not blank), value (decimal >= 0.01, not null), and salesRepId (not blank). Invalid input shall return HTTP 400. | Must | REQ-DEAL-001 | Verify title "ab" returns 400. Verify value 0.00 returns 400. Verify blank salesRepId returns 400. |
| REQ-DEAL-003 | `PATCH /api/deals/{id}/status` shall update the deal's status to the provided value and set `lastModifiedDate` to the current timestamp. | Must | REQ-DEAL-001 | Verify status changes from OPEN to WON. Verify `lastModifiedDate` is updated. |
| REQ-DEAL-004 | The system shall enforce the following deal status transitions: OPEN may transition to WON, LOST, or CANCELLED. WON, LOST, and CANCELLED are terminal states and shall not allow further transitions. | Must | REQ-DEAL-003 | Verify OPEN→WON succeeds. Verify OPEN→LOST succeeds. Verify WON→OPEN returns error. Verify CANCELLED→OPEN returns error. |
| REQ-DEAL-005 | `GET /api/deals` shall return all deals. When `status` query parameter is provided, only deals with that status shall be returned. When `salesRepId` is provided, only that rep's deals shall be returned. | Must | REQ-AUTH-002 | Verify unfiltered returns all deals. Verify `?status=WON` returns only won deals. Verify `?salesRepId=user-003` returns only that rep's deals. |
| REQ-DEAL-006 | `GET /api/deals/{id}` shall return the deal with the specified ID including its products list, or HTTP 404 if the deal does not exist. | Must | REQ-AUTH-002 | Verify existing deal returns 200 with products array. Verify non-existent ID returns 404. |
| REQ-DEAL-007 | `DELETE /api/deals/{id}` shall permanently remove the deal record. Returns HTTP 204 on success, HTTP 404 if deal does not exist. | Must | REQ-AUTH-002 | Verify deletion returns 204. Verify subsequent GET returns 404. |

---

## 5. Commission Plan Management

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-PLAN-001 | `POST /api/plans` shall create a new commission plan with the provided name, currencyCode, effectiveStartDate, and optional effectiveEndDate and createdBy. New plans shall have status `DRAFT`. | Must | REQ-AUTH-002 | Verify plan creation returns 201 with status `DRAFT`. |
| REQ-PLAN-002 | Plan creation shall validate: name (3-100 chars), currencyCode (exactly 3 chars), and effectiveStartDate (not null). If effectiveEndDate is provided, it must be after effectiveStartDate; otherwise the system shall return HTTP 422. | Must | REQ-PLAN-001 | Verify name "ab" returns 400. Verify currencyCode "US" returns 400. Verify endDate before startDate returns 422. |
| REQ-PLAN-003 | `PATCH /api/plans/{id}/activate` shall transition a plan from `DRAFT` to `ACTIVE` status. If the plan is not in `DRAFT` status, the system shall return HTTP 422 with a `BusinessValidationException`. | Must | REQ-PLAN-001 | Verify DRAFT plan activates successfully. Verify ACTIVE plan returns 422 on activate. Verify ARCHIVED plan returns 422 on activate. |
| REQ-PLAN-004 | `PATCH /api/plans/{id}/archive` shall transition a plan to `ARCHIVED` status. Archived plans shall not be usable for new commission calculations. | Must | REQ-PLAN-001 | Verify active plan archives successfully. Verify archived plan cannot be used in `POST /api/calculations`. |
| REQ-PLAN-005 | `GET /api/plans/active` shall return only plans with status `ACTIVE` whose effective date range includes the current date. | Must | REQ-PLAN-003 | Verify only ACTIVE plans with valid date ranges are returned. Verify DRAFT and ARCHIVED plans are excluded. |
| REQ-PLAN-006 | `DELETE /api/plans/{id}` shall not be permitted on plans with `ACTIVE` status. Attempting to delete an active plan shall return an error. DRAFT and ARCHIVED plans may be deleted. | Must | REQ-PLAN-003 | Verify deleting ACTIVE plan returns error. Verify deleting DRAFT plan returns 204. |
| REQ-PLAN-007 | Each commission plan shall support composition of rules (with types STANDARD, ACCELERATOR, BONUS, SPECIAL), tiers (min/max value brackets with commission rates), and bonus rules (FIXED, SPIF, ACCELERATOR, QUOTA_ACHIEVEMENT, TEAM_PERFORMANCE, SPECIAL_INCENTIVE). | Must | REQ-PLAN-001 | Verify plan response includes `ruleCount`, `tierCount`, and `bonusCount` reflecting the configured items. |

---

## 6. Commission Calculation Engine

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-CALC-001 | `POST /api/calculations` shall accept a `dealId`, `planId`, and `calculatedBy`, validate preconditions, compute the commission, and return the result with status `CALCULATED`. | Must | REQ-DEAL-001, REQ-PLAN-001 | Verify successful calculation returns 201 with status `CALCULATED` and non-zero commission amounts. |
| REQ-CALC-002 | The system shall reject commission calculations where the referenced deal does not have status `WON`. Only WON deals shall earn commissions. The system shall return HTTP 422 with message indicating the current deal status. | Must | REQ-CALC-001 | Verify OPEN deal returns 422. Verify LOST deal returns 422. Verify CANCELLED deal returns 422. Verify WON deal succeeds. |
| REQ-CALC-003 | The system shall reject commission calculations where the referenced plan does not have status `ACTIVE`. Only ACTIVE plans shall be used for calculations. The system shall return HTTP 422. | Must | REQ-CALC-001 | Verify DRAFT plan returns 422. Verify ARCHIVED plan returns 422. Verify ACTIVE plan succeeds. |
| REQ-CALC-004 | Base commission shall be calculated as: `deal.value * plan.standardRule.rate / 100`, rounded to 2 decimal places using HALF_UP rounding. If no STANDARD rule exists on the plan, a default rate of 10% shall be used. | Must | REQ-CALC-001 | Verify $45,000 deal with 10% rate yields $4,500.00 base commission. Verify plan without STANDARD rule uses 10% default. |
| REQ-CALC-005 | For each bonus rule on the plan that is active on the calculation date: if `isPercentage` is true, the bonus amount shall be `baseCommission * bonus.amount / 100`; if false, the bonus amount shall be `bonus.amount` (fixed). | Must | REQ-CALC-004 | Verify percentage bonus of 10% on $4,500 base yields $450 bonus. Verify fixed $500 bonus adds exactly $500. Verify expired bonuses are excluded. |
| REQ-CALC-006 | Gross commission shall equal base commission plus the sum of all applicable bonuses. Net commission shall equal gross commission (no deductions in current implementation). | Must | REQ-CALC-005 | Verify grossCommission = baseCommission + sum(bonuses). Verify netCommission = grossCommission. |
| REQ-CALC-007 | `PATCH /api/calculations/{id}/approve` shall transition a calculation from `CALCULATED` to `APPROVED`. If the calculation is not in `CALCULATED` status, the request shall be rejected. | Must | REQ-CALC-001, REQ-RBAC-003 | Verify CALCULATED→APPROVED succeeds. Verify PAID→APPROVED returns error. Verify APPROVED→APPROVED returns error. |
| REQ-CALC-008 | `PATCH /api/calculations/{id}/pay` shall transition a calculation from `APPROVED` to `PAID` and set `payoutDate` to the current date. If the calculation is not in `APPROVED` status, the request shall be rejected. | Must | REQ-CALC-007, REQ-RBAC-004 | Verify APPROVED→PAID succeeds with payoutDate set. Verify CALCULATED→PAID returns error. |
| REQ-CALC-009 | `GET /api/calculations` shall support filtering by `salesRepId` and `dealId` query parameters. When both are omitted, all calculations shall be returned. | Must | REQ-AUTH-002 | Verify `?dealId=deal-001` returns only calculations for that deal. Verify `?salesRepId=user-003` returns only that rep's calculations. |
| REQ-CALC-010 | The calculation status lifecycle shall be: CALCULATED → APPROVED → PAID (happy path), CALCULATED → DISPUTED (if contested), and any status → CANCELLED. | Must | REQ-CALC-001 | Verify each valid transition succeeds. Verify invalid transitions (e.g., PAID→CALCULATED) are rejected. |

---

## 7. Dispute Management

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-DISP-001 | `POST /api/disputes` shall create a new dispute referencing a calculation, with the filing sales rep's ID, a title, and a description. New disputes shall have status `INITIATED`. | Must | REQ-CALC-001 | Verify dispute creation returns 201 with status `INITIATED` and the provided fields. |
| REQ-DISP-002 | Dispute creation shall validate: calculationId (not blank), salesRepId (not blank), title (5-200 chars), and description (10-2000 chars). Invalid input shall return HTTP 400. | Must | REQ-DISP-001 | Verify title under 5 chars returns 400. Verify description under 10 chars returns 400. |
| REQ-DISP-003 | `PATCH /api/disputes/{id}/resolve` shall accept `resolution` and `resolvedBy` query parameters, transition the dispute to `RESOLVED` status, set `resolvedDate` to the current timestamp, and add a system comment documenting the resolution. | Must | REQ-DISP-001 | Verify dispute transitions to RESOLVED. Verify `resolvedDate` is set. Verify system comment is added. |
| REQ-DISP-004 | The system shall not allow resolution of disputes that are already in `RESOLVED` or `CANCELLED` status. Attempting to resolve such disputes shall return HTTP 422. | Must | REQ-DISP-003 | Verify resolving a RESOLVED dispute returns 422. Verify resolving a CANCELLED dispute returns 422. |
| REQ-DISP-005 | `PATCH /api/disputes/{id}/escalate` shall set `isEscalated` to `true`, transition status to `ESCALATED`, and add a system comment. A dispute that is already escalated shall not be escalated again (return HTTP 422). | Must | REQ-DISP-001 | Verify escalation sets `escalated=true` and status `ESCALATED`. Verify re-escalation returns 422. |
| REQ-DISP-006 | The dispute status lifecycle shall support: INITIATED → UNDER_REVIEW → ADDITIONAL_INFO_REQUESTED → ESCALATED → APPROVED/REJECTED → RESOLVED → CANCELLED. | Must | REQ-DISP-001 | Verify each forward transition succeeds. |
| REQ-DISP-007 | `GET /api/disputes` shall support filtering by `salesRepId` and `status` query parameters. When both are omitted, all disputes shall be returned. | Must | REQ-AUTH-002 | Verify `?status=INITIATED` returns only initiated disputes. Verify `?salesRepId=user-003` returns only that rep's disputes. |
| REQ-DISP-008 | The dispute commenting system shall support system-generated comments (for escalations and resolutions) and user comments. Each comment shall record the author, timestamp, and whether it is a system comment. | Should | REQ-DISP-001 | Verify escalation adds a system comment. Verify resolution adds a system comment with resolution text. Verify `commentCount` in response reflects total comments. |

---

## 8. Error Handling & Validation

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-ERR-001 | All API responses shall be wrapped in the standard `ApiResponse<T>` envelope containing `success` (boolean), `message` (string), `data` (payload), and `timestamp` (ISO 8601 datetime). | Must | None | Verify every successful response includes all four fields. Verify `success=true` on 2xx responses. |
| REQ-ERR-002 | Bean validation failures (`@NotBlank`, `@Size`, `@Email`, `@DecimalMin`) shall return HTTP 400 with an `ApiErrorResponse` containing a `validationErrors` map of field names to error message arrays. | Must | None | Verify missing required field returns 400 with field name in `validationErrors`. Verify multiple violations on one request return all errors. |
| REQ-ERR-003 | Business rule violations (invalid state transitions, duplicate usernames, precondition failures) shall return HTTP 422 with a descriptive error message in the `BusinessValidationException`. | Must | None | Verify activating a non-DRAFT plan returns 422. Verify duplicate username returns 422. Verify calculating on non-WON deal returns 422. |
| REQ-ERR-004 | Resource not found errors shall return HTTP 404 with message format: `"{ResourceName} not found with {field}: '{value}'"`. | Must | None | Verify `GET /api/deals/nonexistent` returns 404 with message "Deal not found with id: 'nonexistent'". |
| REQ-ERR-005 | Authentication failures shall return HTTP 401 with a generic message. The system shall not disclose whether the username or password was incorrect. | Must | REQ-AUTH-001 | Verify invalid username returns 401. Verify invalid password returns same 401 message. |
| REQ-ERR-006 | Authorization failures (insufficient role) shall return HTTP 403 with message "You do not have permission to perform this action". | Must | REQ-RBAC-001 | Verify `SALES_REP` accessing `/api/users` returns 403 with the standard message. |
| REQ-ERR-007 | Unhandled server exceptions shall return HTTP 500 with a generic error message. Full stack traces shall be logged server-side but never exposed in API responses. | Must | None | Verify 500 response does not contain stack trace or internal class names. |

---

## 9. Health & Observability

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-OBS-001 | `GET /api/health` shall return application status including: `status` ("UP"), `application` (app name), `timestamp` (current datetime), and `javaVersion`. This endpoint shall not require authentication. | Must | None | Verify response contains all four fields. Verify `status` is "UP" when application is running. |
| REQ-OBS-002 | The system shall expose Swagger UI at `/swagger-ui/` and OpenAPI JSON specification at `/api-docs` without requiring authentication. | Should | None | Verify Swagger UI loads at `/swagger-ui/`. Verify `/api-docs` returns valid OpenAPI JSON. |
| REQ-OBS-003 | Spring Boot Actuator health and info endpoints shall be exposed at `/actuator/health` and `/actuator/info` for monitoring integration. | Should | None | Verify `/actuator/health` returns 200 with status "UP". |

---

## 10. Sample Data & Development Support

| Req ID | Requirement | Priority | Dependencies | Testable Criteria |
|--------|-------------|----------|--------------|-------------------|
| REQ-DEV-001 | The system shall load sample data on startup via `SampleDataLoader` including: 6 users (admin, manager, 3 reps, finance), 3 commission plans (2 active, 1 draft), 6 deals (4 won, 1 open, 1 lost), 4 calculations (various statuses), and 1 dispute with comments. | Should | All entity services | Verify `GET /api/users` returns 6 users after startup. Verify `GET /api/plans` returns 3 plans. Verify `GET /api/deals` returns 6 deals. |
| REQ-DEV-002 | The default admin account shall have username `admin`, password `admin123`, and role `SYSTEM_ADMIN`. | Should | REQ-AUTH-001 | Verify `POST /api/auth/login` with admin/admin123 returns a valid JWT with SYSTEM_ADMIN role. |
| REQ-DEV-003 | The H2 in-memory database console shall be accessible at `/h2-console` when `spring.h2.console.enabled=true`. | Should | None | Verify H2 console loads at `/h2-console`. |
| REQ-DEV-004 | The system shall run on port 8081 by default (configurable via `server.port`). | Should | None | Verify application starts and accepts requests on port 8081. |
