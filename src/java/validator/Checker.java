package validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Checker {

  Checker(Field field, Object owner) {
    exceptionMessageBuilder = new StringBuilder();
    this.fieldType = field.getType().toString();
    try {
      value = field.get(owner);
    } catch (IllegalAccessException e) {
      // Underlying field is always accessible because
      // we made it so with field.setAccessible(true).
    }
  }

  Checker(Object value, String typeName) {
    exceptionMessageBuilder = new StringBuilder();
    this.fieldType = typeName;
    this.value = value;
  }

  /**
   * Checks if the value follows the annotation's rules.
   *
   * @param annotation   Annotation that is applied to this value
   * @param errorMessage StringBuilder which contains the error message to be displayed in
   *                     ValidationError
   * @return true if the value doesn't follow annotation's rules, false otherwise
   */
  boolean isAnnotationInvalid(Annotation annotation, StringBuilder errorMessage) {
    if (annotation instanceof NotNull) {
      errorMessage.append(NOT_NULL_MESSAGE);
      return !checkNotNull();
    }
    if (annotation instanceof Positive) {
      errorMessage.append(POSITIVE_MESSAGE);
      return !checkPositive();
    }
    if (annotation instanceof Negative) {
      errorMessage.append(NEGATIVE_MESSAGE);
      return !checkNegative();
    }
    if (annotation instanceof NotBlank) {
      errorMessage.append(NOT_BLANK_MESSAGE);
      return !checkNotBlank();
    }
    if (annotation instanceof NotEmpty) {
      errorMessage.append(NOT_EMPTY_MESSAGE);
      return !checkNotEmpty();
    }
    if (annotation instanceof Size) {
      return !checkSize(annotation, errorMessage);
    }
    if (annotation instanceof InRange) {
      return !checkRange(annotation, errorMessage);
    }
    if (annotation instanceof AnyOf) {
      return !checkAnyOf(annotation, errorMessage);
    }
    // Given annotation is not from our library, so we don't check it.
    return false;
  }

  private boolean checkNotNull() {
    return value != null;
  }

  private boolean checkPositive() {
    // @Positive doesn't handle nulls
    if (value == null) {
      return true;
    }
    if (value instanceof Byte ||
        value instanceof Short ||
        value instanceof Integer ||
        value instanceof Long) {
      Number number = (Number) value;
      return number.longValue() > 0;
    }
    // If the value is not a whole number, throw an exception.
    exceptionMessageBuilder.append(POSITIVE_MISPLACEMENT).append(" ").
        append(EXPECTED_INTEGER).append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  private boolean checkNegative() {
    // @Negative doesn't handle nulls
    if (value == null) {
      return true;
    }
    if (value instanceof Byte ||
        value instanceof Short ||
        value instanceof Integer ||
        value instanceof Long) {
      Number number = (Number) value;
      return number.longValue() < 0;
    }
    // If it's not a whole number, throw an exception.
    exceptionMessageBuilder.append(NEGATIVE_MISPLACEMENT).append(" ").
        append(EXPECTED_INTEGER).append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  private boolean checkNotBlank() {
    // @NotBlank doesn't handle nulls
    if (value == null) {
      return true;
    }
    if (value instanceof String) {
      String text = (String) value;
      return !text.isBlank();
    }
    // If value isn't a String
    exceptionMessageBuilder.append(NOT_MISPLACEMENT).append(" ").
        append(EXPECTED_STRING).append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  private boolean checkNotEmpty() {
    // @NotEmpty doesn't handle nulls
    if (value == null) {
      return true;
    }
    if (value instanceof List<?>
        || value instanceof Set<?>) {
      Collection<?> collection = (Collection<?>) value;
      return !collection.isEmpty();
    }
    if (value instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>) value;
      return !map.isEmpty();
    }
    if (value instanceof String) {
      String str = (String) value;
      return !str.isEmpty();
    }
    // If it's not any of these types...
    exceptionMessageBuilder.append(NOT_EMPTY_MISPLACEMENT).append(" ").
        append(EXPECTED_COLLECTION).append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  private boolean checkSize(Annotation annotation, StringBuilder sb) {
    // @CheckSize doesn't handle nulls
    if (value == null) {
      return true;
    }
    // Size must be between min and max annotation parameters.
    Size sizeAnnotation = (Size) annotation;
    int min = sizeAnnotation.min();
    int max = sizeAnnotation.max();
    // Check if borders are set incorrectly. If so, throw an exception.
    if (min > max) {
      exceptionMessageBuilder.append(SIZE_BORDER_ERROR).append(" Min = ").append(min)
          .append(", Max = ")
          .append(max).append(".");
      throw new ValidationException(exceptionMessageBuilder.toString());
    }
    sb.append(SIZE_MESSAGE);
    sb.append(min);
    sb.append(" and ");
    sb.append(max);
    if (value instanceof List<?>
        || value instanceof Set<?>) {
      Collection<?> collection = (Collection<?>) value;
      return collection.size() >= min && collection.size() <= max;
    }
    if (value instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>) value;
      return map.size() >= min && map.size() <= max;
    }
    if (value instanceof String) {
      String str = (String) value;
      return str.length() >= min && str.length() <= max;
    }
    // If it's not any of these types, throw an exception.
    exceptionMessageBuilder.append(SIZE_MISPLACEMENT).append(" ").
        append(EXPECTED_COLLECTION).append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  private boolean checkRange(Annotation annotation, StringBuilder sb) {
    // @InRange doesn't handle nulls
    if (value == null) {
      return true;
    }
    // Range of value must be between min and max annotation parameters.
    InRange rangeAnnotation = (InRange) annotation;
    long min = rangeAnnotation.min();
    long max = rangeAnnotation.max();
    // Check if borders are set incorrectly. If so, throw an exception.
    if (min > max) {
      exceptionMessageBuilder.append(IN_RANGE_BORDER_ERROR).append(" Min = ").append(min)
          .append(", Max = ")
          .append(max).append(".");
      throw new ValidationException(exceptionMessageBuilder.toString());
    }
    sb.append(IN_RANGE_MESSAGE);
    sb.append(min);
    sb.append(" and ");
    sb.append(max);
    if (value instanceof Byte ||
        value instanceof Short ||
        value instanceof Integer ||
        value instanceof Long) {
      Number number = (Number) value;
      return number.longValue() >= min && number.longValue() <= max;
    }
    // If value isn't a whole number, throw an exception.
    exceptionMessageBuilder.append(IN_RANGE_MISPLACEMENT).append(" ").
        append(EXPECTED_INTEGER).append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  private boolean checkAnyOf(Annotation annotation, StringBuilder sb) {
    // @AnyOf doesn't handle nulls
    if (value == null) {
      return true;
    }
    // Value must be included in array in annotation parameter.
    AnyOf anyOfAnnotation = (AnyOf) annotation;
    String[] values = anyOfAnnotation.value();
    // Complete error message...
    sb.append(ANY_OF_MESSAGE);
    for (String val : values) {
      sb.append(" '");
      sb.append(val);
      sb.append("',");
    }
    sb.setLength(sb.length() - 1);
    if (value instanceof String) {
      String text = (String) value;
      return Arrays.asList(values).contains(text);
    }
    // If given field wasn't a String, throw an exception.
    exceptionMessageBuilder.append(ANY_OF_MISPLACEMENT).append(" ").append(EXPECTED_STRING)
        .append(fieldType);
    throw new ValidationException(exceptionMessageBuilder.toString());
  }

  // Value that needs to be checked according to the annotation
  private Object value;
  // Reference to a String used as a parameter for ValidationException constructor.
  private final StringBuilder exceptionMessageBuilder;
  // String that represents the type of this value.
  private final String fieldType;

  private static final String POSITIVE_MISPLACEMENT =
      "ERROR: Incorrect use of @Positive. Use with whole numbers only.";
  private static final String NEGATIVE_MISPLACEMENT =
      "ERROR: Incorrect use of @Negative. Use with whole numbers only.";
  private static final String NOT_MISPLACEMENT =
      "ERROR: Incorrect use of @NotBlank. Use with String only.";
  private static final String NOT_EMPTY_MISPLACEMENT =
      "ERROR: Incorrect use of @NotEmpty. Use with List<T>, Set<T>, Map<K, V> and String only.";
  private static final String SIZE_MISPLACEMENT =
      "ERROR: Incorrect use of @Size. Use with List<T>, Set<T>, Map<K, V> and String only.";
  private static final String SIZE_BORDER_ERROR =
      "ERROR: Incorrect use of @Size. min parameter must be <= max parameter.";
  private static final String IN_RANGE_MISPLACEMENT =
      "ERROR: Incorrect use of @InRange. Use with whole numbers only.";
  private static final String IN_RANGE_BORDER_ERROR =
      "ERROR: Incorrect use of @InRange. min parameter must be <= max parameter.";
  private static final String ANY_OF_MISPLACEMENT =
      "ERROR: Incorrect use of @AnyOf. Use with String only.";

  private static final String EXPECTED_STRING = "Expected String, actual: ";
  private static final String EXPECTED_COLLECTION = "Expected List/Set/Map/String, actual: ";
  private static final String EXPECTED_INTEGER = "Expected Byte/Short/Int/Long, actual: ";

  private static final String NOT_NULL_MESSAGE = "Must not be null";
  private static final String POSITIVE_MESSAGE = "Must be positive (more than 0)";
  private static final String NEGATIVE_MESSAGE = "Must be negative (less than 0)";
  private static final String NOT_BLANK_MESSAGE = "Must not be blank";
  private static final String NOT_EMPTY_MESSAGE = "Must not be empty";
  private static final String SIZE_MESSAGE = "Size must be in range between ";
  private static final String IN_RANGE_MESSAGE = "Value must be in range between ";
  private static final String ANY_OF_MESSAGE = "Must be one of";
}
