package js.widget;

import static js.widget.SwingUtils.*;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

class TextWidget extends Widget implements DocumentListener {
  public TextWidget(WidgetListener listener, String key, String defaultValue, int lineCount, boolean editable,
      int fontSize, boolean monospaced, float minWidthEm, float minHeightEm) {
    setId(key);
    JComponent container;
    JTextComponent textComponent;
    if (lineCount > 1) {
      JTextArea textArea = new JTextArea(lineCount, 40);
      textArea.setLineWrap(true);
      textComponent = textArea;
      container = new JScrollPane(textArea);

    } else {
      JTextField textField = new JTextField();
      textComponent = textField;
      container = textComponent;
    }
    textComponent.setEditable(editable);
    textComponent.setText(defaultValue);
    if (editable) {
      registerListener(listener);
      textComponent.getDocument().addDocumentListener(this);
    }
    Font font = WidgetManager.getFont(monospaced, fontSize);
    textComponent.setFont(font);

    applyMinDimensions(container, font, minWidthEm, minHeightEm);
    mTextComponent = textComponent;
    setComponent(container);
  }

  public JTextComponent textComponent() {
    return mTextComponent;
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
  public void insertUpdate(DocumentEvent e) {
    commonUpdateHandler();
  }

  @Override
  public String readValue() {
    return getText();
  }

  @Override
  public void writeValue(Object v) {
    setText((String) v);
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    commonUpdateHandler();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    commonUpdateHandler();
  }

  private void commonUpdateHandler() {
    //      if (key() != null) {
    //        storeValueToStateMap(manager().stateMap(), textComponent().getText());
    //      }
    notifyListener();
  }

  private JTextComponent mTextComponent;
}