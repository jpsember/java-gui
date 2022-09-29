package js.widget;

import javax.swing.JCheckBox;

class ToggleButtonWidget extends Widget {
  public ToggleButtonWidget(WidgetListener listener, String key, String label, boolean defaultValue) {
    setId(key);
    JCheckBox component = new JCheckBox(label, defaultValue);
    setComponent(component);

    if (listener != null) {
      registerListener(listener);
      component.addActionListener(this);
    }
  }

  @Override
  public boolean isChecked() {
    JCheckBox component = swingComponent();
    return component.isSelected();
  }

  @Override
  public void doClick() {
    JCheckBox component = swingComponent();
    component.doClick();
  }

  @Override
  public void setChecked(boolean state) {
    JCheckBox component = swingComponent();
    component.setSelected(state);
  }

  @Override
  public Boolean readValue() {
    return isChecked();
  }

  @Override
  public void writeValue(Object v) {
    setChecked((Boolean) v);
  }
}