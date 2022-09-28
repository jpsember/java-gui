package js.widget;

import static js.base.Tools.*;

import java.util.Map;
import java.util.SortedMap;

import js.json.JSMap;

/**
 * A collection of Widgets
 */
public final class WidgetCollection {

  /**
   * Determine if widget events should be propagated to listeners (including the
   * project or script record of gadget values). False while user interface is
   * still being constructed
   */
  public boolean active() {
    return mActive;
  }

  public void setActive(boolean state) {
    mActive = state;
  }
  //
  //  /**
  //   * Enable each gadget in a list
  //   */
  //  public void setEnable(int[] idList, boolean state) {
  //    for (int i = 0; i < idList.length; i++) {
  //      setEnable(idList[i], state);
  //    }
  //  }
  //
  //  /**
  //   * Read enable state of gadget
  //   */
  //  public boolean enabled(int id) {
  //    Gadget c = get(id);
  //    return c.getComponent().isEnabled();
  //  }
  //
  //  /**
  //   * Set enable status of a gadget and its children
  //   * 
  //   * @param id
  //   *          : gadget id
  //   * @param state
  //   *          : true to enable, false to disable
  //   */
  //  public void setEnable(int id, boolean state) {
  //    Gadget c = get(id);
  //    c.getComponent().setEnabled(state);
  //  }

  public Number numValue(String id) {
    return (Number) get(id).readValue();
  }

  /**
   * Get value of boolean-valued gadget
   * 
   * @param id
   *          : id of gadget
   * @return value
   */
  public boolean booleanValue(String id) {
    Boolean result = null;
    Widget g = get(id);
    if (g != null)
      result = (Boolean) g.readValue();
    if (result == null)
      result = false;
    return result;
  }

  /**
   * Set value of integer-valued gadget
   */
  public void setValue(String id, int v) {
    get(id).writeValue(v);
  }

  /**
   * Set value of boolean-valued gadget
   */
  public void setValue(String id, boolean v) {
    get(id).writeValue(v);
  }

  /**
   * Set value of double-valued gadget
   * 
   * @param id
   *          int
   * @param v
   *          double
   */
  public void setValue(String id, double v) {
    get(id).writeValue(v);
  }

  /**
   * Get value of string-valued gadget
   * 
   * @param id
   *          : id of gadget
   * @return value
   */
  public String stringValue(String id) {
    return (String) (get(id).readValue());
  }

  /**
   * Set value of string-valued gadget
   * 
   * @param id
   *          int
   * @param v
   *          String
   */
  public void setValue(String id, String v) {
    get(id).writeValue(v);
  }

  private Widget find(String id) {
    return mGadgetMap.get(id);
  }

  public boolean exists(String id) {
    return (find(id) != null);
  }

  public Widget get(String id) {
    return find(id);
  }

  public void add(Widget c) {
    checkState(!exists(c.id()));
    mGadgetMap.put(c.id(), c);
  }

  public Widget addHidden(String id, Object defaultValue) {
    checkState(!exists(id));
    Widget g = new HiddenWidget(defaultValue).setId(id);
    add(g);
    return g;
  }

  /**
   * Get (integer) value of gadget
   */
  public int vi(String id) {
    return numValue(id).intValue();
  }

  /**
   * Set value of (integer-valued) gadget
   */
  public int seti(String id, int v) {
    setValue(id, v);
    return v;
  }

  /**
   * Get boolean value of gadget
   */
  public boolean vb(String id) {
    return booleanValue(id);
  }

  /**
   * Set boolean value of gadget
   */
  public boolean setb(String id, boolean boolvalue) {
    setValue(id, boolvalue);
    return boolvalue;
  }

  /**
   * Toggle boolean value of gadget
   * 
   * @return new value
   */
  public boolean toggle(String id) {
    return setb(id, !vb(id));
  }

  /**
   * Get (double) value of gadget
   */
  public double vd(String id) {
    return numValue(id).doubleValue();
  }

  public float vf(String id) {
    return ((Number) get(id).readValue()).floatValue();
  }

  /**
   * Set double value of gadget
   */
  public double setd(String id, double v) {
    setValue(id, v);
    return v;
  }

  /**
   * Set float value of gadget
   */
  public double set(String id, float v) {
    setValue(id, v);
    return v;
  }

  /**
   * Get (string) value of gadget
   */
  public String vs(String id) {
    return stringValue(id);
  }

  /**
   * Allocate another anonymous id
   */
  public String getAnonId() {
    return ""+mAnonIdBase++;
  }

  /**
   * Set gadget values according to a JSMap
   */
  public void writeGadgetValues(JSMap map) {
    for (Map.Entry<String, Object> entry : map.wrappedMap().entrySet()) {
      String id =  entry.getKey() ;
      if (!exists(id))
        continue;
      get(id).writeValue(entry.getValue());
    }
  }

  /**
   * Read gadget values into JSMap
   */
  public JSMap readGadgetValues() {
    JSMap m = map();
    for (Map.Entry<String, Widget> ent : mGadgetMap.entrySet()) {
      Widget g = ent.getValue();
      Object v = g.readValue();
      if (v != null)
        m.putUnsafe( ent.getKey(), v);
    }
    return m;
  }

  private SortedMap<String, Widget> mGadgetMap = treeMap();
  private int mAnonIdBase = 9500;
  private boolean mActive;
}
