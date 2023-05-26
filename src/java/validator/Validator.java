package validator;

import java.util.Set;

public interface Validator {

  /**
   * Looks for errors in given object's fields based on annotations from this library.
   * @param object object to be checked.
   * @return set of discovered validation errors.
   */
  Set<ValidationError> validate(Object object);
}
