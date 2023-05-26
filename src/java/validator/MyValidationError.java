package validator;

public class MyValidationError implements ValidationError {

  public MyValidationError(String message, String path, Object failedValue) {
    this.message = message;
    this.path = path;
    this.failedValue = failedValue;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Object getFailedValue() {
    return failedValue;
  }

  private final String message;
  private final String path;
  private final Object failedValue;
}
