package validator;

@Constrained
class Wrapper {

  @Positive
  Integer x;
  GuestForm guestForm;

  public Wrapper(Integer x, GuestForm guestForm) {
    this.x = x;
    this.guestForm = guestForm;
  }
}
