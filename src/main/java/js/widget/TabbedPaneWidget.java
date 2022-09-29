package js.widget;

import static js.base.Tools.*;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class TabbedPaneWidget extends Widget implements ChangeListener {

  public TabbedPaneWidget(WidgetListener listener, String key) {
    setId(key);
    JTabbedPane component = new JTabbedPane();
    component.addChangeListener(this);
    setComponent(component);
    registerListener(listener);
  }

  public void add(String tabNameExpr, JComponent component) {
    mTabNames.add(tabNameExpr);
    String lastDisplayName = last(mTabNames.displayNames());
    tabbedPane().add(lastDisplayName, component);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    // storeValueToStateMap(manager().stateMap(), mTabNames.getSymbolicName(tabbedPane().getSelectedIndex()));
    notifyListener();
  }

  private JTabbedPane tabbedPane() {
    return swingComponent();
  }

  private SymbolicNameSet mTabNames = new SymbolicNameSet();

  @Override
  public Integer readValue() {
    // I used to be storing the selected tab by its symbolic name, but for now let's not bother
    return tabbedPane().getSelectedIndex();
  }

  @Override
  public void writeValue(Object v) {
    Number n = (Number) v;
    tabbedPane().setSelectedIndex(n.intValue());
  }
}