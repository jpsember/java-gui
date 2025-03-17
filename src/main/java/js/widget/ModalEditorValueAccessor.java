package js.widget;

public interface ModalEditorValueAccessor {

  Object readValue();
  void writeValue(Object value);

  String encodeValueToString(Object value);

  Object parseValueFromString(String string);
}
