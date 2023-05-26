# Validator

**Validator** - библиотека, упрощающая валидацию данных в предметных моделях посредством введения декларативных проверок через аннотации.

## Пример использования

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

    User user = new User();
    user.firstName = null;
    user.lastName = "   ";
    user.email = "example@gmail.com";
    user.age = 6;
    user.language = "French";

    Validator validator = new MyValidator();
    Set<ValidationError> errors = validator.validate(user);
    for (var error: errors) {
      System.out.printf("Ошибка валидации!\nГде: %s\nЗначение: %s\nПричина: %s\n%n",
              error.getPath(), error.getFailedValue(), error.getMessage());
    }
```

### Вывод:
```
Ошибка валидации!
Где: language
Значение: French
Причина: Must be one of 'Russian', 'English'

Ошибка валидации!
Где: age
Значение: 6
Причина: Value must be in range between 18 and 100

Ошибка валидации!
Где: lastName
Значение:    
Причина: Must not be blank

Ошибка валидации!
Где: firstName
Значение: null
Причина: Must not be null
```

## Поддерживаемые аннотации
| Аннотация | Описание | Поддерживаемые типы | Параметры |
| --- | --- | --- | --- |
| @NotNull | Значение не должно быть null | Любой ссылочный тип | - |
| @Positive | Значение положительно | byte, short, int, long, Byte, Short, Integer, Long | - |
| @Negative | Значение отрицательно | byte, short, int, long, Byte, Short, Integer, Long | - |
| @NotBlank | см. [String.isBlank](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#isBlank()) | String | - |
| @NotEmpty | Значение не пустое | List<T>, Set<T>, Map<K,V>, String | - |
| @Size | Размер в диапазоне [min, max] | List<T>, Set<T>, Map<K,V>, String | int min, int max |
| @InRange | Значение в диапазоне [min, max] | byte, short, int, long, Byte, Short, Integer, Long | long min, long max |
| @AnyOf | Значение находится в массиве, указанном в аннотации | String | String[] value |
| @Constrained | Тип подвергается проверке | Любой ссылочный тип | - |
