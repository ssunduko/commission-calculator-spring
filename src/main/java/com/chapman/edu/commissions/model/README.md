# Sales Commission Management System

## Overview
This directory contains the domain model classes for a Sales Commission Management System. The system is designed to calculate and manage sales commissions for organizations with complex commission structures, including tiered commissions, bonuses, and accelerators.

## Model Classes

### Core Entities
- **User.java**: Represents a user in the system with attributes like name, email, and role.
- **UserRole.java**: Enum defining the possible roles a user can have (e.g., Sales Representative, Manager, Admin).
- **Deal.java**: Represents a sales deal with information about the customer, products, amount, and status.
- **DealProduct.java**: Represents a product included in a deal, with quantity and price information.
- **DealStatus.java**: Enum defining the possible statuses of a deal (e.g., Draft, Submitted, Approved).

### Commission Structure
- **CommissionPlan.java**: Defines a commission plan with rules for calculating commissions.
- **PlanStatus.java**: Enum defining the possible statuses of a commission plan (e.g., Active, Inactive).
- **CommissionRule.java**: Defines a rule for calculating commissions based on specific conditions.
- **RuleCondition.java**: Defines conditions that must be met for a commission rule to apply.
- **CommissionTier.java**: Defines tiers for commission rates based on sales volume or other metrics.
- **BonusRule.java**: Defines rules for calculating bonuses beyond standard commissions.

### Calculation Results
- **CommissionCalculation.java**: Represents the result of a commission calculation for a deal.
- **BonusCalculation.java**: Represents the result of a bonus calculation.
- **AcceleratorCalculation.java**: Represents the result of an accelerator calculation (increased commission rates for exceeding targets).

### Dispute Management
- **Dispute.java**: Represents a dispute raised by a user regarding a commission calculation.
- **DisputeComment.java**: Represents comments added to a dispute during the resolution process.
- **DisputeStatus.java**: Enum defining the possible statuses of a dispute (e.g., Open, Under Review, Resolved).

## How It Works

1. **Deal Creation**: Sales representatives create deals in the system, specifying customer information, products, quantities, and prices.

2. **Commission Calculation**: When a deal is approved, the system calculates commissions based on:
   - The applicable commission plan
   - Commission rules and conditions
   - Tiered commission rates
   - Bonus rules
   - Accelerator conditions

3. **Commission Review**: Sales representatives and managers can review calculated commissions.

4. **Dispute Management**: If there's a disagreement about a commission calculation, users can raise disputes, which go through a resolution process.

## Example Scenario

A sales representative closes a deal worth $100,000. The commission plan has the following structure:
- Base commission rate: 5% for deals up to $50,000
- Tier 2 commission rate: 7% for the portion of deals between $50,000 and $150,000
- Bonus: $1,000 for deals closed within the quarter
- Accelerator: Additional 2% for representatives who exceed their quarterly target

The system would calculate:
- Base commission: $50,000 × 5% = $2,500
- Tier 2 commission: $50,000 × 7% = $3,500
- Total commission: $6,000
- Potential bonus: $1,000 (if closed within the quarter)
- Potential accelerator: $100,000 × 2% = $2,000 (if quarterly target exceeded)

## Integration Points

This model integrates with:
- User authentication and authorization systems
- Product and pricing catalogs
- Customer relationship management (CRM) systems
- Payment and accounting systems

## Design Considerations

The model is designed with flexibility in mind to accommodate various commission structures across different industries and organizations. It follows object-oriented design principles and separates concerns appropriately to allow for easy extension and modification.