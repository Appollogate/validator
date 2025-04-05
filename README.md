# Validator

> 📝 Archived 2023 homework. No longer maintained.

---
**Validator** is a library that simplifies data validation in domain models by introducing declarative checks via custom
annotations.

## How To Use

```java

@Constrained
class User {
    @NotNull
    @NotBlank
    String firstName;

    @NotNull
    @NotBlank
    String lastName;

    @NotNull
    @NotBlank
    String email;

    @InRange(min = 18, max = 100)
    int age;

    @AnyOf({"Russian", "English"})
    String language;
}
```

```java
class Application {
    public static void main(String[] args) {
        User user = new User();
        user.firstName = null;
        user.lastName = "";
        user.email = "example@gmail.com";
        user.age = 6;
        user.language = "French";

        Validator validator = new MyValidator();
        Set<ValidationError> errors = validator.validate(user);
        for (var error : errors) {
            System.out.printf("Validation failed!\nAffected field: %s\nValue: %s\nReason: %s\n%n",
                    error.getPath(), error.getFailedValue(), error.getMessage());
        }
    }
}
```

### Output:

```
Validation failed!
Affected field: language
Value: French
Reason: Must be one of 'Russian', 'English'

Validation failed!
Affected field: age
Value: 6
Reason: Value must be in range between 18 and 100

Validation failed!
Affected field: lastName
Value:    
Reason: Must not be blank

Validation failed!
Affected field: firstName
Value: null
Reason: Must not be null
```

## Supported Annotations

| Annotation   | Description                                                                                                        | Supported types                                    | Params             |
|--------------|--------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|--------------------|
| @NotNull     | Value must not be null                                                                                             | Any reference type                                 | -                  |
| @Positive    | Value must be positive (>0)                                                                                        | byte, short, int, long, Byte, Short, Integer, Long | -                  |
| @Negative    | Value must be negative (<0)                                                                                        | byte, short, int, long, Byte, Short, Integer, Long | -                  |
| @NotBlank    | see [String.isBlank](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#isBlank()) | String                                             | -                  |
| @NotEmpty    | Value is not empty                                                                                                 | List<T>, Set<T>, Map<K,V>, String                  | -                  |
| @Size        | Size must be in interval [min, max]                                                                                | List<T>, Set<T>, Map<K,V>, String                  | int min, int max   |
| @InRange     | Value must be in interval [min, max]                                                                               | byte, short, int, long, Byte, Short, Integer, Long | long min, long max |
| @AnyOf       | Value must exist in a given array                                                                                  | String                                             | String[] value     |
| @Constrained | The annotated type is subject to validation                                                                        | Any reference type                                 | -                  |
