package validator;

import java.util.List;
import java.util.Map;

import play.libs.F.Promise;

public interface JsonInputValidator<T> {
	Promise<Map<String, List<String>>> validate(final T input);
}
