# Feature Spec Template — Vertical Slice Architecture

Use this template to define a new feature. AI agents (Claude Code, Copilot) read this spec and generate the complete vertical slice.

---

## Feature: {Feature Name}

### Intent

{One sentence describing what this feature does and why it exists.}

### Domain Entity

**Entity:** `{EntityName}`
**Table:** `{table_name}`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | @Id, auto-generated | Primary key |
| {field} | {Type} | {nullable, unique, etc.} | {description} |
| status | {StatusEnum} | @Enumerated(STRING) | Lifecycle state |
| createdDate | LocalDate | not null | Auto-set on creation |
| lastModifiedDate | LocalDate | nullable | Updated on changes |

### Status Lifecycle (if applicable)

```
{STATUS_A} → {STATUS_B} → {STATUS_C}
                ↓
            {STATUS_D}
```

Enum values: `{STATUS_A}("Display A"), {STATUS_B}("Display B"), ...`

### Operations

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| create{Entity} | Create{Entity}Request | {Entity}Response | Create a new entity |
| get{Entity} | String id | {Entity}Response | Get entity by ID |
| getAll{Entities} | — | List<{Entity}Response> | List all entities |
| get{Entities}By{Filter} | String filterId | List<{Entity}Response> | Filter by criteria |
| update{Entity} | String id, Update{Entity}Request | {Entity}Response | Update entity fields |
| delete{Entity} | String id | void | Delete entity |

### Validation Rules

- {field}: {validation rule, e.g., "Required, must not be blank"}
- {field}: {validation rule, e.g., "Must be greater than zero"}

### REST Endpoints

| Method | Path | Operation |
|--------|------|-----------|
| POST | /api/{entities} | create |
| GET | /api/{entities}/{id} | getById |
| GET | /api/{entities} | getAll |
| GET | /api/{entities}?{filter}={value} | getByFilter |
| PUT | /api/{entities}/{id} | update |
| DELETE | /api/{entities}/{id} | delete |

### MCP Tools

All operations above exposed as @Tool with descriptive names and descriptions.
Tool naming convention: `{operationName}` (camelCase matching the service method).

### MCP Resources (if applicable)

| URI | Description |
|-----|-------------|
| {entities}://all | All entities |
| {entities}://{filter} | Filtered view |

### MCP Prompts (if applicable)

| Name | Description | Parameters |
|------|-------------|------------|
| {workflow-name} | {what the prompt does} | {param1}, {param2} |

### Cross-Feature Dependencies

- {Depends on: FeatureX repository for Y lookup}
- {Or: None — fully self-contained}

### Files to Generate

```
features/{feature-name}/
  {Entity}.java                     ← JPA entity
  {Entity}Status.java               ← Lifecycle enum (if needed)
  {Entity}Repository.java           ← extends JpaRepository
  Create{Entity}Request.java        ← Java record + validate()
  Update{Entity}Request.java        ← Java record (nullable fields)
  {Entity}Response.java             ← Java record + from() factory
  {Entity}Service.java              ← @Service, business logic
  {Entity}Controller.java           ← @RestController
```

Plus: Add @Tool methods to `infrastructure/mcp/McpCommissionTools.java`

---

## Example: Completed Feature Spec

### Feature: Product Catalog

### Intent

Manage the catalog of products that can be attached to deals for commission calculation.

### Domain Entity

**Entity:** `Product`
**Table:** `products`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | @Id, auto-generated | Primary key |
| name | String | not null | Product name |
| sku | String | not null, unique | Stock keeping unit |
| category | String | nullable | Product category |
| basePrice | BigDecimal | not null, precision(19,2) | List price |
| commissionEligible | boolean | not null, default true | Eligible for commission |
| status | ProductStatus | @Enumerated(STRING) | Active/Discontinued |
| createdDate | LocalDate | not null | Auto-set on creation |

### Status Lifecycle

```
ACTIVE → DISCONTINUED
```

### Operations

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| createProduct | CreateProductRequest | ProductResponse | Add a new product |
| getProduct | String id | ProductResponse | Get product by ID |
| getAllProducts | — | List<ProductResponse> | List all products |
| getProductsByCategory | String category | List<ProductResponse> | Filter by category |
| updateProduct | String id, UpdateProductRequest | ProductResponse | Update product info |
| deleteProduct | String id | void | Remove a product |

### Validation Rules

- name: Required, must not be blank
- sku: Required, must not be blank, 3-20 characters
- basePrice: Required, must be greater than zero

### Files to Generate

```
features/products/
  Product.java
  ProductStatus.java
  ProductRepository.java
  CreateProductRequest.java
  UpdateProductRequest.java
  ProductResponse.java
  ProductService.java
  ProductController.java
```