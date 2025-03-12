package js.widget;

import static js.base.Tools.*;

import javax.swing.JComboBox;

import js.data.DataUtil;

class ComboBoxWidget extends Widget {
  public ComboBoxWidget(WidgetListener listener, String key, SymbolicNameSet choices) {
    mChoices = checkNotNull(choices);
    setId(key);
    JComboBox<String> component = new JComboBox<>(DataUtil.toStringArray(mChoices.displayNames()));
    setComponent(component);
    registerListener(listener);
    component.addActionListener((e) -> {
      notifyListener();
      notifyApp();
    });
  }

  //    @Override
  //    public void actionPerformed(ActionEvent e) {
  //      JComboBox<String> component = swingComponent();
  //      String selectedItem = (String) component.getSelectedItem();
  //      storeValueToStateMap(manager().stateMap(), displayToInternal(selectedItem));
  //      super.actionPerformed(e);
  //    }

  @Override
  public void writeValue(Object v) {
    todo("!cbwidget: not finished");
  }

  @Override
  public Integer readValue() {
    JComboBox<String> component = swingComponent();
    return component.getSelectedIndex();
  }

  //    private String displayToInternal(String s) {
  //      return mChoices.displayToSymbolic(s);
  //    }

  private SymbolicNameSet mChoices;
}