package validator;

import java.util.List;

@Constrained
public class BookingForm {

  @NotNull
  @Size(min = 1, max = 5)
  final List<@NotNull GuestForm> guests;
  @NotNull
  final List<@AnyOf({"TV", "Kitchen"}) String> amenities;
  @NotNull
  @AnyOf({"House", "Hostel"})
  final String propertyType;
  @NotNull
  final Unrelated unrelated;

  public BookingForm(List<GuestForm> guests, List<String> amenities, String propertyType,
      Unrelated unrelated) {
    this.guests = guests;
    this.amenities = amenities;
    this.propertyType = propertyType;
    this.unrelated = unrelated;
  }

  public List<GuestForm> getGuests() {
    return guests;
  }

  public List<String> getAmenities() {
    return amenities;
  }

  public String getPropertyType() {
    return propertyType;
  }

}
