package validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Size of annotated object must be within [min, max]. Applies to List<T>, Set<T>, Map<K, V>,
 * String.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE})
public @interface Size {

  int min();

  int max();
}
