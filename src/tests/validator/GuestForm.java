package validator;

@Constrained
public class GuestForm {

  @NotNull
  @NotBlank
  private final String firstName;
  @NotNull
  @NotBlank
  private final String lastName;
  @InRange(min = 0, max = 200)
  private final int age;

  public GuestForm(String firstName, String lastName, int age) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public int getAge() {
    return age;
  }

}
