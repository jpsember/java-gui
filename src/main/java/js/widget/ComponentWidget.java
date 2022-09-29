package js.widget;

import static js.base.Tools.*;

import javax.swing.JComponent;

class ComponentWidget extends Widget {

  public ComponentWidget(JComponent component) {
    setComponent(component);
  }

  @Override
  public void setVisible(boolean visible) {
    swingComponent().setVisible(visible);
  }

  @Override
  public Object readValue() {
    return null;
  }

  @Override
  public void writeValue(Object v) {
    throw notSupported("write value;", this);
  }
}