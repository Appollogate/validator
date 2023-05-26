package validator;

// No @Constrained annotation -> don't check this class
class Unrelated {

  @Positive
  final int x;

  public Unrelated(@Positive int x) {
    this.x = x;
  }

}
