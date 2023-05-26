package validator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyValidatorTest {

  GuestForm guestform;
  Validator val;

  @BeforeEach
  void setUp() {
    val = new MyValidator();
  }

  @AfterEach
  void tearDown() {
    val = null;
  }

  // GuestForm with correct values
  @Test
  void validateCorrectGuest() {
    guestform = new GuestForm("Daria", "Skrypina", 19);
    Set<ValidationError> errors = val.validate(guestform);
    assertEquals(errors.size(), 0);
  }

  // GuestForm with incorrect values
  @Test
  void validateWrongGuest() {
    GuestForm wrongGuestForm = new GuestForm("", null, -19);
    Set<ValidationError> validationErrors = val.validate(wrongGuestForm);
    assertTrue(validationErrors.stream().anyMatch(x -> x.getMessage().equals("Must not be blank")));
    assertTrue(validationErrors.stream()
        .anyMatch(x -> x.getMessage().equals("Value must be in range between 0 and 200")));
    assertTrue(validationErrors.stream().anyMatch(x -> x.getMessage().equals("Must not be null")));
    assertTrue(validationErrors.stream().anyMatch(x -> x.getPath().equals("lastName")));
    assertTrue(validationErrors.stream().anyMatch(x -> x.getPath().equals("firstName")));
    assertTrue(validationErrors.stream().anyMatch(x -> x.getPath().equals("age")));
    assertTrue(validationErrors.stream().anyMatch(
        x -> (x.getFailedValue() instanceof Number)
            && ((Integer) x.getFailedValue() == wrongGuestForm.getAge())));
    assertTrue(validationErrors.stream().anyMatch(
        x -> (x.getFailedValue() instanceof String) &&
            x.getFailedValue().equals(wrongGuestForm.getFirstName())));
    assertTrue(validationErrors.stream()
        .anyMatch(x -> x.getFailedValue() == wrongGuestForm.getLastName()));
  }

  // Example from the task description
  @Test
  void validateBookingForm1() {
    List<GuestForm> guests = List.of(
        new GuestForm(null, "Def", 21),
        new GuestForm("", "Ijk", -3));
    Unrelated unrelated = new Unrelated(-1);
    BookingForm bookingForm = new BookingForm(
        guests,
        List.of("TV", "Piano"),
        "Apartment",
        unrelated);
    Set<ValidationError> errors = val.validate(bookingForm);
    assertEquals(5, errors.size());
    // Check paths to failed values
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("guests[0].firstName")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("guests[1].age")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("guests[1].firstName")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("amenities[1]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("propertyType")));
    // Check failed values
    assertTrue(errors.stream()
        .anyMatch(x -> x.getFailedValue() == bookingForm.getGuests().get(0).getFirstName()));
    assertTrue(errors.stream().anyMatch(x -> x.getFailedValue() instanceof Number
        && ((Number) x.getFailedValue()).longValue() == bookingForm.getGuests().get(1).getAge()));
    assertTrue(errors.stream().anyMatch(x -> x.getFailedValue() instanceof String
        && x.getFailedValue().equals(bookingForm.getGuests().get(1).getFirstName())));
    assertTrue(errors.stream().anyMatch(x -> x.getFailedValue() instanceof String
        && x.getFailedValue().equals(bookingForm.getGuests().get(1).getFirstName())));
    assertTrue(errors.stream().anyMatch(x -> x.getFailedValue() instanceof String
        && x.getFailedValue().equals(bookingForm.getAmenities().get(1))));
    assertTrue(errors.stream().anyMatch(x -> x.getFailedValue() instanceof String
        && x.getFailedValue().equals(bookingForm.getPropertyType())));
  }

  // Another booking form example
  @Test
  void validateBookingForm2() {
    List<GuestForm> guests = new ArrayList<>();
    Unrelated unrelated = new Unrelated(-1);
    BookingForm bookingForm = new BookingForm(
        guests,
        null,
        "House",
        unrelated);
    Set<ValidationError> errors = val.validate(bookingForm);
    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("guests")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("amenities")));
  }

  // Correct booking form example
  @Test
  void validateBookingForm3() {
    List<GuestForm> guests = List.of(
        new GuestForm("Darya", "Skrypina", 19),
        new GuestForm("Tony", "Stark", 36));
    Unrelated unrelated = new Unrelated(-1);
    BookingForm bookingForm = new BookingForm(
        guests,
        List.of("TV", "Kitchen"),
        "Hostel",
        unrelated);
    Set<ValidationError> errors = val.validate(bookingForm);
    assertEquals(0, errors.size());
  }

  // Booking form with null/zero-initialized fields
  @Test
  void validateBookingForm4() {
    List<GuestForm> guests = List.of(
        new GuestForm(null, null, 0));
    BookingForm bookingForm = new BookingForm(
        guests,
        null,
        null,
        null);
    Set<ValidationError> errors = val.validate(bookingForm);
    assertEquals(5, errors.size());
  }

  // Trying to validate a class without a @Constrained annotation
  @Test
  void validateNoConstrained() {
    Unrelated clazz = new Unrelated(5);
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String message = MyValidator.NO_CONSTRAINED_MSG;
      assertEquals(message, ve.getMessage());
    }
  }

  @Test
  void validateAllAnnotations() {
    List<String> longList = Arrays.asList("a", "b", "c", "d", "e", "f");
    Related related = new Related(-4, 5, "   ", "", null, longList, -10, "Beep");
    Set<ValidationError> errors = val.validate(related);
    assertEquals(8, errors.size());
  }

  @Test
  void validateNullObject() {
    try {
      val.validate(null);
    } catch (ValidationException ve) {
      String message = MyValidator.OBJ_NULL_MSG;
      assertEquals(message, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated1() {
    @Constrained
    class Temp {

      @AnyOf({"A", "B"})
      final Integer x;

      Temp(Integer x) {
        this.x = x;
      }
    }

    Temp clazz = new Temp(3);
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @AnyOf. Use with String only. Expected String, actual: class java.lang.Integer";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated2() {
    @Constrained
    class Temp {

      @InRange(min = 4, max = -1)
      final Integer x;

      Temp(Integer x) {
        this.x = x;
      }
    }

    Temp clazz = new Temp(3);
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @InRange. min parameter must be <= max parameter. Min = 4, Max = -1.";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated3() {
    @Constrained
    class Temp {

      @InRange(min = 1, max = 10)
      final String s;

      Temp(String s) {
        this.s = s;
      }
    }

    Temp clazz = new Temp("Hello");
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @InRange. Use with whole numbers only. "
          + "Expected Byte/Short/Int/Long, actual: class java.lang.String";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated4() {
    @Constrained
    class Temp {

      @Size(min = 4, max = -1)
      final String s;

      Temp(String s) {
        this.s = s;
      }
    }

    Temp clazz = new Temp("Hello");
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @Size. min parameter must be <= max parameter. Min = 4, Max = -1.";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated5() {
    @Constrained
    class Temp {

      @Size(min = 1, max = 4)
      final Integer x;

      Temp(Integer x) {
        this.x = x;
      }
    }

    Temp clazz = new Temp(3);
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s =
          "ERROR: Incorrect use of @Size. Use with List<T>, Set<T>, Map<K, V> and String only. "
              + "Expected List/Set/Map/String, actual: class java.lang.Integer";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated6() {
    @Constrained
    class Temp {

      @Negative
      final String s;

      Temp(String s) {
        this.s = s;
      }
    }

    Temp clazz = new Temp("Hello");
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @Negative. Use with whole numbers only. "
          + "Expected Byte/Short/Int/Long, actual: class java.lang.String";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated7() {
    @Constrained
    class Temp {

      @NotBlank
      final Integer x;

      Temp(Integer x) {
        this.x = x;
      }
    }

    Temp clazz = new Temp(3);
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @NotBlank. Use with String only. "
          + "Expected String, actual: class java.lang.Integer";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrongAnnotated8() {
    @Constrained
    class Temp {

      @NotEmpty
      final Integer x;

      Temp(Integer x) {
        this.x = x;
      }
    }

    Temp clazz = new Temp(3);
    try {
      val.validate(clazz);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @NotEmpty. "
          + "Use with List<T>, Set<T>, Map<K, V> and String only. "
          + "Expected List/Set/Map/String, actual: class java.lang.Integer";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateWrapper() {
    GuestForm guestForm = new GuestForm("Darya", "Skrypina", -19);
    Wrapper wrapper = new Wrapper(-25, guestForm);
    Set<ValidationError> errors = val.validate(wrapper);
    assertEquals(2, errors.size());
  }

  @Test
  void validateNullFields() {
    Related related = new Related(null, null, null, null, null, null, null, null);
    Set<ValidationError> errors = val.validate(related);
    assertEquals(1, errors.size());
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("legend")));
  }

  @Test
  void validateContradictory() {
    @Constrained
    class MyClass {

      @Positive
      @Negative
      final int x;

      MyClass(int x) {
        this.x = x;
      }
    }
    MyClass mc1 = new MyClass(0);
    MyClass mc2 = new MyClass(12);
    Set<ValidationError> errors1 = val.validate(mc1);
    Set<ValidationError> errors2 = val.validate(mc2);
    // 2 errors, since 0 is neither positive nor negative
    assertEquals(2, errors1.size());
    // 1 error, since 12 is positive, but not negative
    assertEquals(1, errors2.size());
  }

  @Test
  void validateListAnnotations() {
    @Constrained
    class MyList {

      final List<@NotBlank String> list;

      MyList(List<String> list) {
        this.list = list;
      }
    }

    List<String> list = Arrays.asList("a", "", "c", "");
    MyList ml = new MyList(list);
    Set<ValidationError> errors = val.validate(ml);
    assertEquals(2, errors.size());
  }

  @Test
  void validateListWrongAnnotation() {
    @Constrained
    class MyList {

      final List<@Positive String> list;

      MyList(List<String> list) {
        this.list = list;
      }
    }
    List<String> list = Arrays.asList("a", "", "c", "");
    MyList ml = new MyList(list);
    try {
      val.validate(ml);
    } catch (ValidationException ve) {
      String s = "ERROR: Incorrect use of @Positive. Use with whole numbers only. Expected Byte/Short/Int/Long, actual: java.lang.String";
      assertEquals(s, ve.getMessage());
    }
  }

  @Test
  void validateCollections() {
    @Constrained
    class MyClass {

      @NotEmpty
      @Size(min = 0, max = 1)
      final List<String> list;

      @NotEmpty
      @Size(min = 0, max = 1)
      final Set<String> set;

      @NotEmpty
      @Size(min = 0, max = 1)
      final Map<String, String> map;

      @NotEmpty
      @Size(min = 0, max = 1)
      final String str;

      public MyClass(List<String> list, Set<String> set, Map<String, String> map, String str) {
        this.list = list;
        this.set = set;
        this.map = map;
        this.str = str;
      }
    }
    List<String> myList = Arrays.asList("a", "b", "c");
    Set<String> MySet = Set.of("a", "b", "c");
    Map<String, String> myMap = Map.of("a", "A", "b", "B");
    String myStr = "string";
    MyClass mc = new MyClass(myList, MySet, myMap, myStr);
    Set<ValidationError> errors = val.validate(mc);
    assertEquals(4, errors.size());
  }

  @Test
  void validateListConstrained() {
    @Constrained
    class MyList {

      final List<GuestForm> forms;

      MyList(List<GuestForm> list) {
        this.forms = list;
      }
    }
    GuestForm gf1 = new GuestForm("Mark", "Brown", 34);
    GuestForm gf2 = new GuestForm(null, "Ford", 57);
    GuestForm gf3 = new GuestForm("Peter", "   ", 22);
    GuestForm gf4 = new GuestForm("Sherlock", "Holmes", -28);
    MyList ml = new MyList(Arrays.asList(gf1, gf2, gf3, gf4));
    Set<ValidationError> errors = val.validate(ml);
    assertEquals(3, errors.size());
  }

  @Test
  void validateNestedList() {
    @Constrained
    class Temp {

      final List<@NotEmpty List<@NotNull @NotBlank String>> list;

      public Temp(
          List<List<String>> list) {
        this.list = list;
      }
    }
    List<List<String>> myList = Arrays
        .asList(Arrays.asList("a", "   "),
            Arrays.asList("", "d", "  "),
            Collections.emptyList());
    Temp temp = new Temp(myList);
    Set<ValidationError> errors = val.validate(temp);
    assertEquals(4, errors.size());
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[0][1]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[1][0]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[1][2]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[2]")));
  }

  @Test
  void validateNestedList2() {
    @Constrained
    class Temp {

      final List<@NotEmpty List<@NotEmpty List<@NotNull @NotBlank String>>> list;

      public Temp(
          List<List<List<String>>> list) {
        this.list = list;
      }
    }
    List<List<List<String>>> list = List.of(
        List.of(
            List.of("a, b"),
            List.of("c", "d", "")
        ),
        List.of(
            List.of("", "B"),
            List.of("C", "", "E")
        )
    );
    Temp temp = new Temp(list);
    Set<ValidationError> errors = val.validate(temp);
    assertEquals(3, errors.size());
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[0][1][2]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[1][0][0]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[1][1][1]")));
  }

  @Test
  void validateListOfNulls() {
    @Constrained
    class Temp {

      final List<@NotNull String> list;

      public Temp() {
        list = new ArrayList<>();
        list.add(null);
        list.add(null);
      }
    }
    Temp temp = new Temp();
    Set<ValidationError> errors = val.validate(temp);
    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[0]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[1]")));
  }


  @Test
  void validateListOfNulls2() {
    @Constrained
    class Temp {

      final List<@NotNull List<@NotNull Integer>> list;

      public Temp() {
        List<Integer> list1 = new ArrayList<>();
        list1.add(5);
        list1.add(6);
        list1.add(null);
        list = new ArrayList<>();
        list.add(Arrays.asList(1, 2, 3));
        list.add(list1);
        list.add(null);
      }
    }
    Temp temp = new Temp();
    Set<ValidationError> errors = val.validate(temp);
    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[2]")));
    assertTrue(errors.stream().anyMatch(x -> x.getPath().equals("list[1][2]")));
  }
}