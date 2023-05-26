package validator;

import java.util.List;

@Constrained
public class Related {

  @Positive
  final Integer x;
  @Negative
  final Integer y;
  @NotBlank
  final String title;
  @NotEmpty
  final String author;
  @NotNull
  final String legend;
  @Size(min = 1, max = 5)
  final List<String> comments;
  @InRange(min = 0, max = 100)
  final Integer scale;
  @AnyOf({"Linear", "Box", "Heatmap", "Histogram", "Violin"})
  final String plot;

  public Related(Integer x, Integer y, String title, String author, String legend,
      List<String> comments, Integer scale, String plot) {
    this.x = x;
    this.y = y;
    this.title = title;
    this.author = author;
    this.legend = legend;
    this.comments = comments;
    this.scale = scale;
    this.plot = plot;
  }
}
