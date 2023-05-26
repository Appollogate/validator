package validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MyValidator implements Validator {

  public MyValidator() {
    errors = new HashSet<>();
    errorMessage = new StringBuilder();
    path = new LinkedList<>();
  }

  @Override
  public Set<ValidationError> validate(Object object) {
    if (object == null) {
      throw new ValidationException(OBJ_NULL_MSG);
    }
    // Renew the set of mistakes
    errors = new HashSet<>();
    //Check if object's class has @Constrained annotation, otherwise throw an exception.
    //This only fires on user's call of validate().
    if (!object.getClass().isAnnotationPresent(Constrained.class)) {
      throw new ValidationException(NO_CONSTRAINED_MSG);
    }
    validateRecursive(object);
    return errors;
  }

  /**
   * Validates an object. Can be called recursively without consequences.
   *
   * @param object object to be inspected.
   */
  private void validateRecursive(Object object) {
    // Get all fields of this object
    Field[] declaredFields = object.getClass().getDeclaredFields();
    // Check each field separately
    for (Field field : declaredFields) {
      errors.addAll(validateField(field, object));
    }
  }

  /**
   * Check the annotations of the field and determines if they are applied correctly. Also, checks
   * if the field value meets the annotation's rules. Also, checks the insides of the field if its
   * value is an object whose class has a @Constrained annotation. Also, if the field is a list,
   * checks its elements accordingly.
   *
   * @param field the field to be checked.
   * @param owner object where the field resides.
   * @return a set of found validation errors from this field.
   */
  private Set<ValidationError> validateField(Field field, Object owner) {
    // Inner classes contain a field called 'this$0', which holds
    // the reference to outer class. We don't need to check this field,
    // or any other synthetic field (created by the compiler).
    if (field.isSynthetic()) {
      return new HashSet<>();
    }
    // Make a set for all errors belonging to this particular field
    Set<ValidationError> fieldErrors = new HashSet<>();
    // Check the annotations before field itself.
    checkFieldAnnotations(field, owner);
    // Get value of field.
    Object fieldValue = null;
    try {
      fieldValue = field.get(owner);
    } catch (IllegalAccessException e) {/*Exception is impossible since field is set accessible explicitly.*/}
    // Check if the field is an object of a @Constrained class.
    // If so, check its fields as well.
    validateInner(fieldValue, field.getName());
    // Check if the field is a List<T>. If so, check what's inside
    if (fieldValue instanceof List<?>) {
      validateListField(field, (List<?>) fieldValue);
    }
    return fieldErrors;
  }

  /**
   * Check the annotations before the field itself.
   *
   * @param field field that needs to be checked.
   */
  private void checkFieldAnnotations(Field field, Object owner) {
    // Make field accessible.
    field.setAccessible(true);
    Checker fieldChecker = new Checker(field, owner);
    // Get all annotations of field
    Annotation[] declaredAnnotations = field.getAnnotatedType().getAnnotations();
    // Check all annotations of current field
    for (Annotation ann : declaredAnnotations) {
      if (fieldChecker.isAnnotationInvalid(ann, errorMessage)) {
        // Since field value doesn't follow the annotation's rules,
        // create a ValidationError and add it to the set.
        // Get name of failed field
        String fieldName = field.getName();
        // Add it to the path
        path.add(fieldName);
        // Get value of failed field
        Object failedValue = null;
        try {
          failedValue = field.get(owner);
        } catch (IllegalAccessException e) {
          // Underlying field is always accessible because
          // we made it so with field.setAccessible(true).
        }
        errors.add(ErrorCreator.createError(failedValue, errorMessage.toString(), path));
        // Remove failed field name from path
        path.remove(path.size() - 1);
      }
      errorMessage.setLength(0);
    }
  }

  /**
   * If the field is an object of a class marked with @Constrained, checks the fields inside it.
   *
   * @param fieldValue value of the field to be checked.
   * @param fieldName  name of the field.
   */
  private void validateInner(Object fieldValue, String fieldName) {
    // Check if the field value is not null and is an object of class with @Constrained annotation
    if (fieldValue != null && fieldValue.getClass().isAnnotationPresent(Constrained.class)) {
      // If we got here, it means we are going deeper ->
      // we need to remember the path to newly found objects if they are invalid
      ++level;
      path.add(fieldName + ".");
      // Recursively check the inner object.
      validateRecursive(fieldValue);
      // After we exit recursion, clean up the path
      if (level > 0 && path.size() > 0) {
        path.remove(path.size() - 1);
        --level;
      }
    }
  }

  /**
   * Checks the elements inside a list field.
   *
   * @param listField a list field.
   * @param list      value of the list field.
   */
  private void validateListField(Field listField, List<?> list) {
    if (list != null) {
      // Get annotated type of list field
      AnnotatedType type = ((AnnotatedParameterizedType) listField.getAnnotatedType())
          .getAnnotatedActualTypeArguments()[0];
      // Validate list field in a recursive method
      validateListRecursive(type, list, listField.getName());
    }
  }

  /**
   * Validates a list. Can be called recursively.
   *
   * @param type     Annotated type of the list.
   * @param list     the list itself.
   * @param listName name of the list.
   */
  private void validateListRecursive(AnnotatedType type, List<?> list, String listName) {
    // No need to check the list if its length is 0
    if (list != null && list.size() > 0) {
      // Get annotations before list parameter type
      Annotation[] annotations = type.getAnnotations();
      // Get parameter type of list elements
      Class<?> listType = null;
      for (var elem : list) {
        if (elem != null) {
          listType = elem.getClass();
          break;
        }
      }
      // If listType is still null, that means that all elements are null
      if (listType == null) {
        checkListTypeAnnotation(annotations, list, listName, "Unknown type");
        return;
      }
      // 1. Check the annotations before the parameter type and validate all contents accordingly
      // example: List<@NotBlank String> list
      checkListTypeAnnotation(annotations, list, listName, listType.getTypeName());
      // 2. Check the contents of the list if their type is marked with @Constrained
      // example: List<GuestForm> forms
      checkListElemsIfConstrained(listType, list, listName);
      // 3. If the element of the list is a list itself, enter recursion.
      for (int i = 0; i < list.size(); ++i) {
        if (list.get(i) instanceof List<?>) {
          // Get the annotated type of inner list
          AnnotatedType newType = ((AnnotatedParameterizedType) type)
              .getAnnotatedActualTypeArguments()[0];
          String newListName = listName + "[" + i + "]";
          validateListRecursive(newType, (List<?>) list.get(i), newListName);
        }
      }
    }
  }

  /**
   * Checks the values inside a list according to the rules set by annotations.
   *
   * @param annotations  annotations that are applied to each element of the list.
   * @param list         the list itself.
   * @param listName     name of the list.
   * @param listTypeName name of the type of list elements.
   */
  private void checkListTypeAnnotation(Annotation[] annotations, List<?> list, String listName,
      String listTypeName) {
    Checker checker;
    for (Annotation ann : annotations) {
      for (int i = 0; i < list.size(); ++i) {
        // Make a new checker for each value
        checker = new Checker(list.get(i), listTypeName);
        if (checker.isAnnotationInvalid(ann, errorMessage)) {
          // Add list name to path + index of failed value
          path.add(listName + "[" + i + "]");
          // If the value in a list element doesn't follow annotation rules,
          // create a corresponding Validation error and add it to the set.
          errors.add(ErrorCreator.createError(list.get(i), errorMessage.toString(), path));
          // Remove name of list field from path
          path.remove(path.size() - 1);
        }
        errorMessage.setLength(0);
      }
    }
  }

  /**
   * Checks the insides of list elements if they belong to a type annotated by @Constrained.
   *
   * @param listType type of list elements.
   * @param list     the list itself.
   * @param listName the name of the list.
   */
  private void checkListElemsIfConstrained(Class<?> listType, List<?> list, String listName) {
    // Check if the class has @Constrained annotation.
    // If so, validate each element of list
    if (listType.isAnnotationPresent(Constrained.class)) {
      for (int i = 0; i < list.size(); ++i) {
        if (list.get(i) != null) {
          String name = listName + "[" + i + "]";
          validateInner(list.get(i), name);
        }
      }
    }
  }

  // Level of recursion.
  private int level = 0;
  // A reference to the validation error message that can be updated.
  private final StringBuilder errorMessage;
  // A list of string that form the path to a given field.
  private final List<String> path;
  // A set of validation errors collected from received object.
  private HashSet<ValidationError> errors;

  static final String NO_CONSTRAINED_MSG = "ERROR: no @Constrained annotation on given object";
  static final String OBJ_NULL_MSG = "ERROR: cannot validate null object.";

}
