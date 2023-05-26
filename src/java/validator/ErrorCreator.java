package validator;

import java.util.List;

class ErrorCreator {

  /**
   * Constructs a ValidationError.
   * @param failedValue object that failed validation.
   * @param errorMessage validation error message.
   * @param path path to failed value.
   * @return constructed validation error.
   */
  static ValidationError createError(Object failedValue,
      String errorMessage, List<String> path) {
    StringBuilder pathBuilder = new StringBuilder();
    // Build path to failed value by concatenating all Strings of list
    for (String pathNode : path) {
      pathBuilder.append(pathNode);
    }
    String pathToField = pathBuilder.toString();
    return new MyValidationError(errorMessage, pathToField, failedValue);
  }


}
