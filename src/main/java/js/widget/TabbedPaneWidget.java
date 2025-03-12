package js.widget;

import static js.base.Tools.*;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TabbedPaneWidget extends Widget implements ChangeListener {

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
    notifyListener();
    notifyApp();
  }

  private JTabbedPane tabbedPane() {
    return swingComponent();
  }

  @Override
  public String readValue() {
    int index = tabbedPane().getSelectedIndex();
    return mTabNames.getSymbolicName(index);
  }

  @Override
  public void writeValue(Object v) {
    String n = (String) v;
    tabbedPane().setSelectedIndex(mTabNames.getSymbolicIndex(n));
  }

  private SymbolicNameSet mTabNames = new SymbolicNameSet();

}