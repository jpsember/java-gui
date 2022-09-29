package js.widget;

import static js.base.Tools.*;

import javax.swing.JButton;

class ButtonWidget extends Widget {

  public ButtonWidget(WidgetListener listener, String key, String label) {
    JButton component = new JButton(label);
    todo("add action listener");
    //      component.addActionListener(this);
    setComponent(component);
    registerListener(listener);
  }

  @Override
  public void setEnabled(boolean enabled) {
    swingComponent().setEnabled(enabled);
  }

  @Override
  public String readValue() {
    JButton b = swingComponent();
    return b.getText();
  }

  @Override
  public void writeValue(Object v) {
    throw notSupported();
  }
}