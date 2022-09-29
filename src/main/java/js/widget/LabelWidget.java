package js.widget;

import java.awt.Font;

import javax.swing.JLabel;

class LabelWidget extends Widget {

  public LabelWidget(String key, int gravity, int lineCount, String text, int fontSize, boolean monospaced,
      int alignment) {
    setId(key);
    JLabel label = new JLabel(text);
    if (fontSize == WidgetManager.SIZE_DEFAULT)
      fontSize = WidgetManager.SIZE_SMALL;
    Font font = WidgetManager.getFont(monospaced, fontSize);
    label.setFont(font);
    if (alignment == WidgetManager.ALIGNMENT_DEFAULT)
      alignment = WidgetManager.ALIGNMENT_RIGHT;
    label.setHorizontalAlignment(alignment);
    setComponent(label);
  }

  private JLabel textComponent() {
    return swingComponent();
  }

  @Override
  public void setText(String text) {
    textComponent().setText(text);
  }

  @Override
  public String getText() {
    return textComponent().getText();
  }

  @Override
  public String readValue() {
    return getText();
  }

  @Override
  public void writeValue(Object v) {
    setText((String) v);
  }
}