package js.widget;

import static js.base.Tools.*;

import javax.swing.JCheckBox;

class ToggleButtonWidget extends Widget {

  public ToggleButtonWidget(WidgetListener listener, String key, String label, boolean defaultValue) {
    loadTools();
    setId(key);
    registerListener(listener);
    JCheckBox component = new JCheckBox(label, defaultValue);
    setComponent(component);
    component.addActionListener((x) ->
    {
      notifyApp();
      notifyListener();
    });
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