package js.widget;

import static js.base.Tools.*;

import javax.swing.JButton;

class ButtonWidget extends Widget {

  public ButtonWidget(WidgetListener listener, String id, String label) {
    loadTools();
    setId(id);
    JButton component = new JButton(label);
    component.addActionListener((e) ->
    {
      notifyListener();
      notifyApp();
    });
    setComponent(component);
    registerListener(listener);
  }

  @Override
  public void setEnabled(boolean enabled) {
    swingComponent().setEnabled(enabled);
  }

  @Override
  public String readValue() {
    return jButton().getText();
  }

  @Override
  public void writeValue(Object v) {
    String label = (String) v;
    jButton().setText(label);
  }

  private JButton jButton() {
    return swingComponent();
  }
}