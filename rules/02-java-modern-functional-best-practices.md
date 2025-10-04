# Modern Functional Java Best Practices

> **Version**: 1.0.0
> **Last Updated**: 2025-10-04
> **Scope**: Functional Java, Modern Java Features (Java 17+)
> **Applies To**: All Java codebases

## Overview

This rules file defines best practices for writing modern, functional Java code that leverages features from Java 8 onwards (Records, Sealed Classes, Pattern Matching, Streams, Optionals, etc.). These rules promote immutability, pure functions, declarative code, and type safety to produce maintainable, testable, and expressive Java code.

---

## Core Principles

### Principle 1: Immutability First

**Intent**: Favor immutable data structures to reduce bugs, improve thread safety, and make code easier to reason about.

**Rules**:
- Use `record` types for data carriers instead of traditional classes with getters/setters
- Declare all fields as `final` when mutable state is unavoidable
- Use immutable collections from `List.of()`, `Map.of()`, `Set.of()` instead of mutable variants
- Never expose mutable internal state directly

**Example**:
```java
// Good: Immutable record with all benefits built-in
public record Customer(String id, String name, LocalDate registrationDate) {}

// Good: Immutable collection
List<String> tags = List.of("java", "functional", "modern");

// Bad: Mutable JavaBean with setters
public class Customer {
    private String id;
    private String name;

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}

// Bad: Exposing mutable collection
public List<String> getTags() {
    return tags; // caller can modify internal state
}
```

### Principle 2: Pure Functions and Side-Effect Isolation

**Intent**: Separate pure business logic from side-effectful operations to improve testability and predictability.

**Rules**:
- Functions should not modify external state or depend on mutable state
- I/O operations, database calls, and external API calls should be isolated at boundaries
- Use functional composition to build complex operations from simple pure functions
- Mark side-effectful methods clearly in naming (e.g., `saveToDatabase`, `sendNotification`)

**Example**:
```java
// Good: Pure function - deterministic, testable
public BigDecimal calculateDiscount(Order order, Customer customer) {
    return order.total()
        .multiply(customer.discountRate())
        .setScale(2, RoundingMode.HALF_UP);
}

// Good: Side effects isolated and explicit
public void processOrder(Order order) {
    var discountedTotal = calculateDiscount(order, customer);
    saveToDatabase(order.withTotal(discountedTotal));
}

// Bad: Mixed concerns - calculation and side effects together
public BigDecimal calculateAndSaveDiscount(Order order) {
    var discount = order.total().multiply(0.1); // calculation
    database.save(discount); // side effect!
    return discount;
}
```

### Principle 3: Declarative Over Imperative

**Intent**: Express "what" the code does rather than "how" it does it, using Streams, Optionals, and functional patterns.

**Rules**:
- Use Streams API instead of explicit loops when transforming collections
- Chain operations declaratively using `map`, `filter`, `flatMap`, `reduce`
- Use method references (`::`) instead of lambda expressions when possible
- Use `Optional` to represent absence of value instead of null checks

**Example**:
```java
// Good: Declarative stream pipeline
List<String> activeCustomerNames = customers.stream()
    .filter(Customer::isActive)
    .map(Customer::name)
    .sorted()
    .toList();

// Good: Optional chaining
String displayName = Optional.ofNullable(customer.nickname())
    .orElseGet(customer::fullName);

// Bad: Imperative with explicit loops
List<String> activeCustomerNames = new ArrayList<>();
for (Customer customer : customers) {
    if (customer.isActive()) {
        activeCustomerNames.add(customer.getName());
    }
}
Collections.sort(activeCustomerNames);

// Bad: Null checks instead of Optional
String displayName;
if (customer.getNickname() != null) {
    displayName = customer.getNickname();
} else {
    displayName = customer.getFullName();
}
```

### Principle 4: Type Safety and Exhaustiveness

**Intent**: Use the type system to prevent runtime errors and make illegal states unrepresentable.

**Rules**:
- Use sealed classes/interfaces with pattern matching for domain modeling
- Use enums for fixed sets of values
- Prefer pattern matching over `instanceof` chains
- Use specific types over primitive obsession (e.g., custom types for IDs, Money)

**Example**:
```java
// Good: Sealed interface with exhaustive pattern matching
public sealed interface PaymentMethod permits CreditCard, BankTransfer, Crypto {
    record CreditCard(String cardNumber, String cvv) implements PaymentMethod {}
    record BankTransfer(String iban, String bic) implements PaymentMethod {}
    record Crypto(String walletAddress, String currency) implements PaymentMethod {}
}

public String processPayment(PaymentMethod payment) {
    return switch (payment) {
        case CreditCard(var number, var cvv) ->
            "Processing credit card: " + maskCard(number);
        case BankTransfer(var iban, var bic) ->
            "Processing bank transfer to: " + iban;
        case Crypto(var wallet, var currency) ->
            "Processing crypto payment: " + currency;
    }; // compiler ensures exhaustiveness
}

// Bad: Loose type hierarchy with instanceof
public String processPayment(Object payment) {
    if (payment instanceof CreditCard) {
        return "Processing credit card";
    } else if (payment instanceof BankTransfer) {
        return "Processing bank transfer";
    }
    // easy to forget a case!
    return "Unknown";
}
```

---

## Mandatory Requirements

> These rules MUST be followed without exception

- [ ] **REQ-001**: Use Java 17 or later as the minimum Java version
  - **Rationale**: Modern features (Records, Sealed Classes, Pattern Matching) require Java 17+
  - **Validation**: Check `pom.xml` or `build.gradle` for `java.version >= 17`

- [ ] **REQ-002**: Never return `null` from public methods; use `Optional<T>` instead
  - **Rationale**: Eliminates NullPointerException at call sites, makes absence explicit in type system
  - **Validation**: Static analysis with NullAway or SpotBugs, code review

- [ ] **REQ-003**: All collection-returning methods must return immutable collections or defensive copies
  - **Rationale**: Prevents external modification of internal state, ensures encapsulation
  - **Validation**: Use `List.of()`, `Set.of()`, `Map.of()`, or `Collections.unmodifiable*()` wrappers

- [ ] **REQ-004**: Use `record` for data transfer objects and value objects
  - **Rationale**: Eliminates boilerplate, provides immutability, equals/hashCode, toString by default
  - **Validation**: Code review - no JavaBeans for pure data carriers

---

## Best Practices

> These rules SHOULD be followed unless there's a documented reason not to

### Category 1: Functional Programming Patterns

1. **Prefer Function Composition**
   - What: Build complex operations by composing small, reusable functions
   - Why: Improves testability, reusability, and readability
   - When: When transforming data through multiple steps

2. **Use Higher-Order Functions**
   - What: Accept functions as parameters, return functions from functions
   - Why: Enables flexible, reusable abstractions
   - When: Building reusable utilities and domain operations

3. **Avoid Stateful Streams**
   - What: Don't use `forEach` with side effects; prefer terminal operations like `collect`, `reduce`
   - Why: Streams should be functional pipelines, not disguised loops
   - When: Always when using Streams API

### Category 2: Error Handling

1. **Use Result/Either Types for Expected Failures**
   - What: Model success/failure explicitly in return types instead of exceptions
   - Why: Makes error handling explicit, forces callers to handle errors
   - When: For business logic failures (validation, domain rules)

2. **Reserve Exceptions for Exceptional Conditions**
   - What: Use exceptions only for truly unexpected conditions (I/O errors, system failures)
   - Why: Exceptions are expensive and break functional flow
   - When: For infrastructure failures, not domain logic

3. **Leverage Try-with-Resources**
   - What: Always use try-with-resources for `AutoCloseable` resources
   - Why: Guarantees resource cleanup, even with exceptions
   - When: Working with files, streams, database connections

### Category 3: Stream Operations

1. **Keep Stream Pipelines Short and Readable**
   - What: Limit streams to 3-5 operations; extract complex logic to named methods
   - Why: Long pipelines become hard to debug and understand
   - When: Building stream transformations

2. **Prefer Collectors for Complex Aggregations**
   - What: Use `Collectors.groupingBy`, `partitioningBy`, `teeing` instead of manual loops
   - Why: Declarative, optimized, and more expressive
   - When: Grouping, partitioning, or aggregating collections

3. **Avoid Parallel Streams Unless Proven Beneficial**
   - What: Use sequential streams by default; benchmark before using `parallelStream()`
   - Why: Parallel streams have overhead; often slower for small datasets
   - When: Only after profiling shows benefit for large datasets

### Category 4: Modern Java Features

1. **Use Text Blocks for Multi-line Strings**
   - What: Use `"""` text blocks for SQL, JSON, HTML, etc.
   - Why: Improves readability, eliminates escape characters
   - When: Any multi-line string literal

2. **Leverage Pattern Matching**
   - What: Use pattern matching in `switch` expressions and `instanceof`
   - Why: More concise, type-safe, and expressive than traditional approaches
   - When: Type checks, sealed class discrimination

3. **Use Enhanced Switch Expressions**
   - What: Use `switch` expressions with `->` arrows instead of statements
   - Why: Returns values, enforces exhaustiveness, prevents fall-through bugs
   - When: Conditional logic with multiple branches

---

## Anti-Patterns

> Common mistakes to avoid

| Anti-Pattern | Why It's Problematic | Better Alternative |
|-------------|---------------------|-------------------|
| Using `null` instead of `Optional` | Causes NullPointerExceptions, hides intent | `Optional<T>` for possibly-absent values |
| Mutable JavaBeans for data carriers | Verbose, not thread-safe, error-prone | `record` types for immutability |
| `stream().forEach()` with side effects | Breaks functional paradigm, hard to parallelize | Use `collect()`, `reduce()`, or extract side effects |
| Primitive obsession (String for ID) | No type safety, easy to mix up parameters | Custom value types: `record CustomerId(String value)` |
| Checked exceptions in lambdas | Breaks functional composition, verbose | Wrap in unchecked or use Result types |
| Overusing `Optional` for fields | Serialization issues, overhead | Use `Optional` only for return types |
| `instanceof` chains instead of sealed classes | Not exhaustive, error-prone, verbose | Sealed classes with pattern matching |
| Stateful lambdas | Not thread-safe, breaks functional contract | Use pure functions with parameters |

---

## Decision Framework

When you need to make decisions about functional Java code, use this framework:

1. **Question 1**: Can this data structure be immutable?
   - If YES → Use `record` or immutable classes with `final` fields
   - If NO → Document why mutability is required, minimize scope

2. **Question 2**: Does this function have side effects?
   - If YES → Isolate at boundaries, name explicitly (e.g., `saveOrder`)
   - If NO → Keep pure, make static if possible, compose freely

3. **Question 3**: Can this be expressed declaratively with streams?
   - If YES → Use Stream API with `filter`, `map`, `collect`
   - If NO → Use explicit loop if clearer (simple iteration, early exit)

4. **Question 4**: Can this value be absent?
   - If YES → Return `Optional<T>`, never return `null`
   - If NO → Use non-nullable type, validate at boundaries

5. **Question 5**: Is this a closed set of types/values?
   - If YES → Use sealed classes/interfaces or enums
   - If NO → Use open inheritance or composition

---

## Exceptions & Context

### When to Deviate

These rules may be relaxed in the following circumstances:

- **Performance-Critical Code**: Immutability and streams may have overhead; profile first, optimize if needed
- **Legacy Integration**: When interfacing with legacy code requiring JavaBeans/mutable objects
- **Framework Requirements**: Some frameworks (JPA, JSON libraries) require no-arg constructors and setters
- **Prototyping**: During initial exploration, favor quick iteration over perfect functional style

### Documentation Requirements

When deviating from these rules, document:
- The specific rule being broken (e.g., "Using mutable collection for performance")
- The reason for the exception (e.g., "Profiling showed 40% performance gain")
- The alternative approach taken (e.g., "Using ArrayList with manual synchronization")
- Benchmark/profiling data supporting the decision (if performance-related)

---

## References

- [Java Records (JEP 395)](https://openjdk.org/jeps/395)
- [Sealed Classes (JEP 409)](https://openjdk.org/jeps/409)
- [Pattern Matching (JEP 441)](https://openjdk.org/jeps/441)
- [Effective Java, 3rd Edition by Joshua Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- [Functional Programming in Java by Venkat Subramaniam](https://pragprog.com/titles/vsjava8/functional-programming-in-java/)

---

## Glossary

**Pure Function**: A function that always returns the same output for the same input and has no side effects (no state modification, I/O, etc.)

**Immutability**: The property of an object whose state cannot be modified after construction

**Side Effect**: Any observable interaction with the outside world (I/O, state mutation, exceptions, etc.)

**Higher-Order Function**: A function that takes functions as parameters or returns a function

**Pattern Matching**: Language feature allowing type checking and deconstruction in a single operation

**Sealed Class**: A class/interface that restricts which classes can extend/implement it, enabling exhaustiveness checking

---

## TL;DR

### Must Do ✓
- Use `record` for data carriers and value objects
- Return `Optional<T>` instead of `null` for potentially absent values
- Make all data immutable by default (use `final`, immutable collections)
- Use Streams API declaratively for collection transformations
- Use sealed classes and pattern matching for type-safe domain modeling

### Never Do ✗
- Return `null` from public methods
- Use JavaBeans with getters/setters for data carriers
- Use `stream().forEach()` with side effects
- Mix business logic with side effects
- Use mutable collections in public APIs
- Use long `instanceof` chains instead of sealed classes

### Always Ask
- Can this be immutable?
- Does this function have side effects?
- Can this be expressed declaratively?
- Should this use Optional for absence?
- Is this a closed type hierarchy?

### Key Takeaway
Write Java that is immutable, declarative, and functional by default—leverage modern Java features (Records, Sealed Classes, Pattern Matching, Streams) to make illegal states unrepresentable and side effects explicit, resulting in code that is safer, more maintainable, and easier to reason about.
