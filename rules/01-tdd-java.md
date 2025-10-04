# Test-Driven Development (TDD) for Java Rules

> **Version**: 1.0.0
> **Last Updated**: 2025-10-04
> **Scope**: Test-Driven Development Methodology for Java
> **Applies To**: All Java development within this project

## Overview

This document defines strict Test-Driven Development (TDD) practices for Java development. TDD is mandatory to ensure code quality, maintainability, and confidence in refactoring. These rules enforce the Red-Green-Refactor cycle and ensure that production code is only written in response to failing tests.

---

## Core Principles

### Principle 1: Red-Green-Refactor Cycle

**Intent**: Ensure all production code is driven by tests, preventing untested code from entering the codebase

**Rules**:
- Write a failing test before writing any production code
- Write only enough production code to make the failing test pass
- Refactor only after tests are green
- Never skip the Red phase (verify the test actually fails)
- Run tests after every code change

**Example**:
```java
// Good: Test written first
@Test
void shouldCalculateTotalPrice() {
    Order order = new Order();
    order.addItem(new Item("Product", 10.0), 2);

    assertThat(order.getTotalPrice()).isEqualTo(20.0);
}

// Then implement minimal code to pass
public class Order {
    private List<OrderLine> lines = new ArrayList<>();

    public void addItem(Item item, int quantity) {
        lines.add(new OrderLine(item, quantity));
    }

    public double getTotalPrice() {
        return lines.stream()
            .mapToDouble(line -> line.getItem().getPrice() * line.getQuantity())
            .sum();
    }
}

// Bad: Production code written first without a failing test
public class Order {
    // Code written without a driving test
    public double getTotalPrice() {
        // Implementation without test coverage
    }
}
```

### Principle 2: Test Independence

**Intent**: Each test should run in isolation without dependencies on other tests or execution order

**Rules**:
- Tests must not share mutable state
- Use `@BeforeEach` for test setup, not static fields
- Each test should create its own test data
- Tests should be runnable in any order
- Clean up resources in `@AfterEach` if necessary

**Example**:
```java
// Good: Independent test setup
class OrderServiceTest {
    private OrderService service;
    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        service = new OrderService(repository);
    }

    @Test
    void shouldCreateOrder() {
        Order order = new Order();
        service.create(order);
        verify(repository).save(order);
    }
}

// Bad: Shared mutable state
class OrderServiceTest {
    private static Order sharedOrder = new Order(); // Anti-pattern

    @Test
    void testA() {
        sharedOrder.addItem(new Item("A", 10.0), 1);
        // Test depends on sharedOrder state
    }

    @Test
    void testB() {
        // This test is affected by testA's modifications
        assertThat(sharedOrder.getItems()).isEmpty(); // May fail
    }
}
```

---

## Mandatory Requirements

> These rules MUST be followed without exception

- [ ] **REQ-001**: Every public method in production code must have at least one test
  - **Rationale**: Ensures all code paths have test coverage and are intentionally designed
  - **Validation**: Run code coverage tools (JaCoCo); minimum 80% line coverage, 70% branch coverage

- [ ] **REQ-002**: Tests must be written BEFORE production code (Red phase is mandatory)
  - **Rationale**: Prevents "testing to the implementation" and ensures testability by design
  - **Validation**: Code review must verify commit history shows test-first approach; tests should fail initially

- [ ] **REQ-003**: Production code changes without corresponding test changes are prohibited
  - **Rationale**: Maintains test coverage and prevents regression
  - **Validation**: Pull requests must include both test and production code changes

- [ ] **REQ-004**: All tests must pass before committing code
  - **Rationale**: Broken tests indicate incomplete work or regressions
  - **Validation**: CI/CD pipeline must run full test suite; pre-commit hooks should run tests

- [ ] **REQ-005**: Test names must clearly describe the behavior being tested
  - **Rationale**: Tests serve as living documentation of system behavior
  - **Validation**: Test names follow pattern: `should[ExpectedBehavior]When[Condition]` or Given-When-Then format

---

## Best Practices

> These rules SHOULD be followed unless there's a documented reason not to

### Category 1: Test Structure

1. **Use Arrange-Act-Assert (AAA) Pattern**
   - What: Structure tests with clear sections for setup, execution, and verification
   - Why: Improves test readability and maintainability
   - When: For all unit and integration tests

2. **One Assertion Concept Per Test**
   - What: Each test should verify one logical concept (may have multiple assertions for that concept)
   - Why: Makes test failures easier to diagnose
   - When: For all tests except acceptance tests

3. **Use Test Fixtures Appropriately**
   - What: Extract common test data setup into `@BeforeEach` only when truly shared
   - Why: Reduces duplication while maintaining test clarity
   - When: When 3+ tests need identical setup

### Category 2: Test Naming and Organization

1. **Follow Naming Conventions**
   - What: Use descriptive names like `shouldThrowExceptionWhenOrderIsEmpty()`
   - Why: Tests become self-documenting
   - When: Always

2. **Organize Tests by Feature**
   - What: Group related tests using nested test classes with `@Nested`
   - Why: Improves test organization and readability
   - When: When testing classes with multiple responsibilities

3. **Use Display Names for Complex Scenarios**
   - What: Add `@DisplayName("description")` for business-critical scenarios
   - Why: Provides clear documentation in test reports
   - When: For acceptance tests and complex business rules

### Category 3: Test Types and Coverage

1. **Write Unit Tests for All Business Logic**
   - What: Test individual classes in isolation using mocks for dependencies
   - Why: Fast feedback, pinpoint failures
   - When: For all service classes, domain models, and utilities

2. **Write Integration Tests for Infrastructure**
   - What: Test database interactions, API clients, and external integrations
   - Why: Verifies system components work together correctly
   - When: For repositories, external service clients, and configuration

3. **Maintain Fast Test Execution**
   - What: Unit tests should complete in milliseconds; full suite under 5 minutes
   - Why: Enables frequent test execution during development
   - When: Always; use test categorization to separate slow tests

---

## Anti-Patterns

> Common mistakes to avoid

| Anti-Pattern | Why It's Problematic | Better Alternative |
|-------------|---------------------|-------------------|
| Writing tests after production code | Leads to tests that match implementation, not requirements; misses design issues | Always write failing test first (Red phase) |
| Testing private methods | Couples tests to implementation details; breaks on refactoring | Test public API; private methods are tested indirectly |
| Large test setups in `@BeforeAll` | Creates shared state and test dependencies | Use `@BeforeEach` or test data builders |
| Assertions without messages | Failures are hard to diagnose | Use descriptive assertion messages or AssertJ fluent API |
| Ignoring or commenting out failing tests | Hides real issues; degrades test suite value | Fix the test or remove it; use `@Disabled` only with ticket reference |
| Testing framework code or getters/setters | Wastes time testing third-party code | Focus on business logic and behavior |
| Mocking everything | Leads to testing mocks, not real behavior | Use real objects for value objects and domain models |
| Catching exceptions in tests | Hides test failures | Use `assertThrows()` to verify expected exceptions |

---

## Decision Framework

When you need to make decisions about testing, use this framework:

1. **Does this code contain business logic or behavior?**
   - If YES → Write a unit test with mocked dependencies
   - If NO → Consider if testing adds value (e.g., simple DTOs may not need tests)

2. **Does this code interact with external systems (DB, API, filesystem)?**
   - If YES → Write an integration test with real or embedded dependencies
   - If NO → Unit test is sufficient

3. **Is this a new feature or bug fix?**
   - If YES → Write a failing test that reproduces the requirement or bug first
   - If NO → Still verify existing tests cover the code path

4. **Can I test this through the public API?**
   - If YES → Do so; avoid testing private methods
   - If NO → Reconsider the design; difficulty testing often indicates poor design

5. **Does this test run in under 100ms?**
   - If YES → Mark as unit test; run with every build
   - If NO → Mark as integration test; consider optimization or categorization

---

## Exceptions & Context

### When to Deviate

These rules may be relaxed in the following circumstances:

- **Prototyping/Spike Work**: During time-boxed exploration to validate technical approach (must be rewritten with TDD before merging)
- **Generated Code**: For code generated by tools (Lombok, MapStruct) that is well-tested by the framework
- **Simple DTOs**: For pure data classes with only getters/setters and no logic
- **Performance Optimization**: When optimizing existing well-tested code, tests may temporarily be adjusted after implementation

### Documentation Requirements

When deviating from these rules, document:
- The specific rule being broken
- The reason for the exception
- The alternative approach taken
- Reference to team discussion or approval (if required)
- Add `// TDD-EXCEPTION: [reason]` comment in code

---

## References

- Kent Beck - "Test-Driven Development: By Example"
- Martin Fowler - "Refactoring: Improving the Design of Existing Code"
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- AssertJ Documentation: https://assertj.github.io/doc/
- Mockito Documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html

---

## Glossary

**Red-Green-Refactor**: The TDD cycle where you write a failing test (Red), implement code to pass (Green), then improve the code quality (Refactor)

**Test Double**: Generic term for any object that stands in for a real object in a test (includes mocks, stubs, fakes, spies)

**Unit Test**: Test that verifies a single unit of code (typically a class) in isolation from its dependencies

**Integration Test**: Test that verifies multiple components working together or interaction with external systems

**Test Fixture**: The fixed state used as a baseline for running tests (test data, mock objects, etc.)

**Arrange-Act-Assert (AAA)**: Test structure pattern that separates test setup, execution, and verification into distinct sections

**Code Coverage**: Metric measuring percentage of code executed by tests (line coverage, branch coverage, etc.)

---

## TL;DR

### Must Do ✓
- Write the test first, watch it fail, then implement
- Run tests after every change
- Maintain minimum 80% line coverage, 70% branch coverage
- Use descriptive test names that document behavior
- Keep tests fast, independent, and repeatable

### Never Do ✗
- Write production code without a failing test
- Commit code with failing tests
- Test private methods directly
- Share mutable state between tests
- Ignore or disable tests without documented reason

### Always Ask
- Have I written a failing test before implementing this?
- Does this test actually fail for the right reason?
- Can this test run independently in any order?
- Does this test describe the behavior, not the implementation?

### Key Takeaway
Test-Driven Development is not optional—it's the primary method for writing all production Java code, ensuring quality, maintainability, and confidence through the Red-Green-Refactor cycle.
