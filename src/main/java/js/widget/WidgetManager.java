/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.*;
import javax.swing.border.CompoundBorder;

import js.base.BaseObject;
import js.data.DataUtil;
import js.geometry.IPoint;
import js.geometry.MyMath;
import js.json.JSMap;
import js.parsing.RegExp;

import static js.base.Tools.*;
import static js.widget.SwingUtils.*;

/**
 * A collection of Widgets, with utilities to compose them onto GUI components
 */
public final class WidgetManager extends BaseObject {

  public void dump() {
    for (var ent : mWidgetMap.entrySet()) {
      var w = ent.getValue();
      pr(ent.getKey(), "=>", w.getClass());
    }

  }

  /**
   * Determine if widget events should be propagated to listeners. False while
   * user interface is still being constructed
   */
  public boolean active() {
    return mActive;
  }

  public void setActive(boolean state) {
    log("set active:", state);
    if (mActive == state)
      log("...unchanged!");
    mActive = state;
  }

  public boolean exists(String id) {
    return (find(id) != null);
  }

  public Widget get(String id) {
    Widget w = find(id);
    if (w == null)
      badState("Can't find widget with id:", id, INDENT, mWidgetMap.keySet());
    return w;
  }

  private Widget find(String id) {
    return mWidgetMap.get(id);
  }

  // ------------------------------------------------------------------
  // Accessing widget values
  // ------------------------------------------------------------------

  /**
   * Set widgets' values. Used to restore app widgets to a previously saved
   * state
   */
  public void setWidgetValues(JSMap map) {
    for (Map.Entry<String, Object> entry : map.wrappedMap().entrySet()) {
      String id = entry.getKey();
      if (!exists(id))
        continue;
      get(id).writeValue(entry.getValue());
    }
  }

  /**
   * Read widgets' values. Doesn't include widgets that have no ids, or whose
   * ids start with "."
   */
  public JSMap readWidgetValues() {
    JSMap m = map();
    for (Map.Entry<String, Widget> ent : mWidgetMap.entrySet()) {
      String id = ent.getKey();
      if (id.charAt(0) == '.')
        continue;
      Widget g = ent.getValue();
      Object v = g.readValue();
      if (v != null)
        m.putUnsafe(id, v);
    }
    return m;
  }

  /**
   * Get value of string-valued widget
   */
  public String vs(String id) {
    return (String) get(id).readValue();
  }

  /**
   * Set value of string-valued widget
   */
  public void sets(String id, String v) {
    get(id).writeValue(v);
  }

  /**
   * Get value of boolean-valued widget
   */
  public boolean vb(String id) {
    Boolean result = null;
    Widget g = get(id);
    if (g != null)
      result = (Boolean) g.readValue();
    if (result == null)
      result = false;
    return result;
  }

  /**
   * Set value of boolean-valued widget
   */
  public boolean setb(String id, boolean boolvalue) {
    get(id).writeValue(boolvalue);
    return boolvalue;
  }

  /**
   * Toggle value of boolean-valued widget
   * 
   * @return new value
   */
  public boolean toggle(String id) {
    return setb(id, !vb(id));
  }

  /**
   * Get value of integer-valued widget
   */
  public int vi(String id) {
    return numValue(id).intValue();
  }

  /**
   * Set value of integer-valued widget
   */
  public int seti(String id, int v) {
    get(id).writeValue(v);
    return v;
  }

  /**
   * Get value of double-valued widget
   */
  public double vd(String id) {
    return numValue(id).doubleValue();
  }

  /**
   * Set value of double-valued widget
   */
  public double setd(String id, double v) {
    get(id).writeValue(v);
    return v;
  }

  /**
   * Get value of float-valued widget
   */
  public float vf(String id) {
    return numValue(id).floatValue();
  }

  /**
   * Set value of float-valued widget
   */
  public double setf(String id, float v) {
    return setd(id, v);
  }

  private Number numValue(String id) {
    return (Number) get(id).readValue();
  }

  // ---------------------------------------------------------------------
  // Composing
  // ---------------------------------------------------------------------

  static final int SIZE_DEFAULT = 0;
  static final int SIZE_TINY = 1;
  static final int SIZE_SMALL = 2;
  static final int SIZE_LARGE = 3;
  static final int SIZE_HUGE = 4;
  static final int SIZE_MEDIUM = 5;

  static final int ALIGNMENT_DEFAULT = -1;
  static final int ALIGNMENT_LEFT = SwingConstants.LEFT;
  static final int ALIGNMENT_CENTER = SwingConstants.CENTER;
  static final int ALIGNMENT_RIGHT = SwingConstants.RIGHT;

  /**
   * <pre>
   *
   * Set the number of columns, and which ones can grow, for the next view in
   * the hierarchy. The columns expression is a string of column expressions,
   * which may be one of:
   * 
   *     "."   a column with weight zero
   *     "x"   a column with weight 100
   *     "\d+" column with integer weight
   * 
   * Spaces are ignored, except to separate integer weights from each other.
   * </pre>
   */
  public final WidgetManager columns(String columnsExpr) {
    checkState(mPendingColumnWeights == null, "previous column weights were never used");

    List<Integer> columnSizes = arrayList();
    for (String word : split(columnsExpr, ' ')) {
      if (RegExp.patternMatchesString("\\d+", word)) {
        columnSizes.add(Integer.parseInt(word));
      } else {
        for (int i = 0; i < word.length(); i++) {
          char c = columnsExpr.charAt(i);
          int size;
          if (c == '.') {
            size = 0;
          } else if (c == 'x') {
            size = 100;
          } else {
            throw new IllegalArgumentException(columnsExpr);
          }
          columnSizes.add(size);
        }
      }
    }
    mPendingColumnWeights = DataUtil.intArray(columnSizes);
    return this;
  }

  /**
   * Make next component added occupy remaining columns in its row
   */
  public final WidgetManager spanx() {
    mSpanXCount = -1;
    return this;
  }

  /**
   * Make next component added occupy some number of columns in its row
   */
  public WidgetManager spanx(int count) {
    checkArgument(count > 0);
    mSpanXCount = count;
    return this;
  }

  /**
   * Skip a single cell
   */
  public WidgetManager skip() {
    add(wrap(null));
    return this;
  }

  /**
   * Skip one or more cells
   */
  public WidgetManager skip(int count) {
    spanx(count);
    add(wrap(null));
    return this;
  }

  /**
   * Set pending component, and the column it occupies, as 'growable'. This can
   * also be accomplished by using an 'x' when declaring the columns.
   * <p>
   * Calls growX(100)...
   */
  public WidgetManager growX() {
    return growX(100);
  }

  /**
   * Set pending component, and the column it occupies, as 'growable'. This can
   * also be accomplished by using an 'x' when declaring the columns.
   * <p>
   * Calls growY(100)...
   */
  public WidgetManager growY() {
    return growY(100);
  }

  /**
   * Set pending component's horizontal weight to a value > 0 (if it is already
   * less than this value)
   */
  public WidgetManager growX(int weight) {
    mGrowXFlag = Math.max(mGrowXFlag, weight);
    return this;
  }

  /**
   * Set pending component's vertical weight to a value > 0 (if it is already
   * less than this value)
   */
  public WidgetManager growY(int weight) {
    mGrowYFlag = Math.max(mGrowYFlag, weight);
    return this;
  }

  /**
   * Specify the component to use for the next open() call, instead of
   * generating one
   */
  public WidgetManager setPendingContainer(JComponent component) {
    checkState(mPanelStack.isEmpty(), "current panel stack isn't empty");
    mPendingContainer = component;
    return this;
  }

  public WidgetManager tooltip(String tip) {
    if (mTooltip != null)
      alert("Tooltip was ignored:", mTooltip);
    mTooltip = tip;
    return this;
  }

  private WidgetManager setPendingSize(int value) {
    mPendingSize = value;
    return this;
  }

  private WidgetManager setPendingAlignment(int value) {
    mPendingAlignment = value;
    return this;
  }

  public WidgetManager small() {
    return setPendingSize(SIZE_SMALL);
  }

  public WidgetManager large() {
    return setPendingSize(SIZE_LARGE);
  }

  public WidgetManager medium() {
    return setPendingSize(SIZE_MEDIUM);
  }

  public WidgetManager tiny() {
    return setPendingSize(SIZE_TINY);
  }

  public WidgetManager huge() {
    return setPendingSize(SIZE_HUGE);
  }

  public WidgetManager left() {
    return setPendingAlignment(ALIGNMENT_LEFT);
  }

  public WidgetManager right() {
    return setPendingAlignment(ALIGNMENT_RIGHT);
  }

  public WidgetManager center() {
    return setPendingAlignment(ALIGNMENT_CENTER);
  }

  /**
   * Have next widget use a monospaced font
   */
  public WidgetManager monospaced() {
    mPendingMonospaced = true;
    return this;
  }

  public WidgetManager minWidth(float ems) {
    mPendingMinWidthEm = ems;
    return this;
  }

  public WidgetManager minHeight(float ems) {
    mPendingMinHeightEm = ems;
    return this;
  }

  public WidgetManager gravity(int gravity) {
    mPendingGravity = gravity;
    return this;
  }

  public WidgetManager editable() {
    mEditableFlag = true;
    return this;
  }

  public WidgetManager lineCount(int numLines) {
    mLineCount = numLines;
    checkArgument(numLines > 0);
    return this;
  }

  public WidgetManager addLabel() {
    return addLabel(null);
  }

  /**
   * Specify listener to add to next widget
   */
  public WidgetManager listener(WidgetListener listener) {
    checkState(mPendingListener == null, "already a pending listener");
    mPendingListener = listener;
    return this;
  }

  /**
   * Specify listener to add to following widgets. Must be balanced by call to
   * popListener()
   */
  public WidgetManager pushListener(WidgetListener listener) {
    checkState(mPendingListener == null, "already a pending listener");
    mListenerStack.add(listener);
    return this;
  }

  public WidgetManager popListener() {
    checkState(mPendingListener == null, "already a pending listener");
    checkState(!mListenerStack.isEmpty(), "listener stack underflow");
    pop(mListenerStack);
    return this;
  }

  public WidgetManager floats() {
    mPendingFloatingPoint = true;
    return this;
  }

  public WidgetManager min(double value) {
    floats();
    mPendingMinValue = value;
    return this;
  }

  public WidgetManager min(int value) {
    mPendingMinValue = value;
    return this;
  }

  public WidgetManager max(double value) {
    floats();
    mPendingMaxValue = value;
    return this;
  }

  public WidgetManager max(int value) {
    mPendingMaxValue = value;
    return this;
  }

  /**
   * Set default value for next boolean-valued control
   */
  public WidgetManager defaultVal(boolean value) {
    mPendingBooleanDefaultValue = value;
    return this;
  }

  public WidgetManager defaultVal(String value) {
    mPendingStringDefaultValue = value;
    return this;
  }

  public WidgetManager label(String value) {
    mPendingLabel = value;
    return this;
  }

  /**
   * Set default value for next double-valued control
   */
  public WidgetManager defaultVal(double value) {
    floats();
    mPendingDefaultValue = value;
    return this;
  }

  /**
   * Set default value for next integer-valued control
   */
  public WidgetManager defaultVal(int value) {
    mPendingDefaultValue = value;
    return this;
  }

  /**
   * Include auxilliary widget with this one; e.g., numeric display with slider,
   * or time stamp with log
   */
  public WidgetManager withDisplay() {
    mPendingWithDisplay = true;
    return this;
  }

  public WidgetManager stepSize(double value) {
    floats();
    mPendingStepSize = value;
    return this;
  }

  public WidgetManager stepSize(int value) {
    mPendingStepSize = value;
    return this;
  }

  /**
   * Append some choices for the next ComboBox
   */
  public WidgetManager choices(String... choices) {
    for (String choiceExpr : choices) {
      if (mComboChoices == null)
        mComboChoices = new SymbolicNameSet();
      mComboChoices.add(choiceExpr);
    }
    return this;
  }

  public boolean consumePendingBooleanDefaultValue() {
    boolean v = nullToFalse(mPendingBooleanDefaultValue);
    mPendingBooleanDefaultValue = null;
    return v;
  }

  public boolean consumePendingFloatingPoint() {
    boolean v = nullToFalse(mPendingFloatingPoint);
    mPendingFloatingPoint = null;
    return v;
  }

  public boolean consumePendingWithDisplay() {
    boolean v = nullToFalse(mPendingWithDisplay);
    mPendingWithDisplay = null;
    return v;
  }

  private WidgetListener consumePendingListener() {
    WidgetListener listener = mPendingListener;
    mPendingListener = null;
    if (listener == null && !mListenerStack.isEmpty()) {
      listener = last(mListenerStack);
    }
    return listener;
  }

  private String consumePendingLabel(boolean required) {
    String lbl = mPendingLabel;
    mPendingLabel = null;
    if (nullOrEmpty(lbl))
      badState("missing label");
    return lbl;
  }

  private String consumePendingStringDefaultValue() {
    String s = mPendingStringDefaultValue;
    mPendingStringDefaultValue = null;
    return s;
  }

  private Number consumePendingMinValue() {
    Number n = mPendingMinValue;
    mPendingMinValue = null;
    return n;
  }

  private Number consumePendingMaxValue() {
    Number n = mPendingMaxValue;
    mPendingMaxValue = null;
    return n;
  }

  private Number consumePendingDefaultValue() {
    Number n = mPendingDefaultValue;
    mPendingDefaultValue = null;
    return n;
  }

  private Number consumePendingStepSize() {
    Number n = mPendingStepSize;
    mPendingStepSize = null;
    return n;
  }

  private String consumePendingTabTitle(Object component) {
    if (mPendingTabTitle == null)
      throw badState("no tab name specified for:", component);
    String tabNameExpression = mPendingTabTitle;
    mPendingTabTitle = null;
    return tabNameExpression;
  }

  private void verifyUsed(Object value, String name) {
    if (value == null)
      return;
    String dispName = chompPrefix(name.trim(), "m");
    dispName = DataUtil.convertCamelCaseToUnderscores(dispName);
    throw badState("unused value:", dispName);
  }

  private void clearPendingComponentFields() {
    // If some values were not used, issue warnings
    verifyUsed(mPendingContainer, "pending container");
    verifyUsed(mPendingColumnWeights, "pending column weights");
    verifyUsed(mComboChoices, "pending combo choices");
    verifyUsed(mPendingMinValue, "mPendingMinValue");
    verifyUsed(mPendingMaxValue, "mPendingMaxValue");
    verifyUsed(mPendingDefaultValue, "mPendingDefaultValue");
    verifyUsed(mPendingBooleanDefaultValue, "mPendingBooleanDefaultValue");
    verifyUsed(mPendingStringDefaultValue, "mPendingStringDefaultValue");
    verifyUsed(mPendingLabel, "mPendingLabel ");
    verifyUsed(mPendingStepSize, "mPendingStepSize");
    verifyUsed(mPendingFloatingPoint, "mPendingFloatingPoint");
    verifyUsed(mPendingWithDisplay, "mPendingWithDisplay");

    mPendingContainer = null;
    mPendingColumnWeights = null;
    mSpanXCount = 0;
    mGrowXFlag = mGrowYFlag = 0;
    mPendingSize = SIZE_DEFAULT;
    mPendingAlignment = ALIGNMENT_DEFAULT;
    mPendingGravity = 0;
    mPendingMinWidthEm = 0;
    mPendingMinHeightEm = 0;
    mPendingMonospaced = false;
    mEditableFlag = false;
    mLineCount = 0;
    mComboChoices = null;
    mPendingMinValue = null;
    mPendingMaxValue = null;
    mPendingDefaultValue = null;
    mPendingBooleanDefaultValue = null;
    mPendingStringDefaultValue = null;
    mPendingLabel = null;
    mPendingStepSize = null;
    mPendingWithDisplay = null;
    mPendingFloatingPoint = null;
  }

  // ------------------------------------------------------------------
  // Layout logic
  // ------------------------------------------------------------------

  private int[] mPendingColumnWeights;

  /**
   * Call widget listener, setting up event source beforehand
   */
  public void notifyWidgetListener(Widget widget, WidgetListener listener) {
    if (!active())
      return;
    Widget previousListener = mListenerWidget;
    try {
      mLastWidgetEventTime = System.currentTimeMillis();
      mListenerWidget = widget;
      listener.widgetEvent(widget.id());
    } finally {
      mListenerWidget = previousListener;
    }
  }

  /**
   * Get widget associated with listener event
   */
  public Widget eventSource() {
    checkNotNull(mListenerWidget, "no event source found");
    return mListenerWidget;
  }

  public long timeSinceLastEvent() {
    long currTime = System.currentTimeMillis();
    if (mLastWidgetEventTime == 0)
      mLastWidgetEventTime = currTime;
    return currTime - mLastWidgetEventTime;
  }

  public static String randomText(int maxLength, boolean withLinefeeds) {
    StringBuilder sb = new StringBuilder();
    Random r = ThreadLocalRandom.current();
    int len = (int) Math.abs(r.nextGaussian() * maxLength);
    while (sb.length() < len) {
      int wordSize = r.nextInt(8) + 2;
      if (withLinefeeds && r.nextInt(4) == 0)
        sb.append('\n');
      else
        sb.append(' ');
      String sample = "orhxxidfusuytelrcfdlordburswfxzjfjllppdsywgsw"
          + "kvukrammvxvsjzqwplxcpkoekiznlgsgjfonlugreiqvtvpjgrqotzu";
      int cursor = r.nextInt(sample.length() - wordSize);
      sb.append(sample.substring(cursor, cursor + wordSize));
    }
    return sb.toString().trim();
  }

  /**
   * Wrap a system-specific element within a Widget
   *
   * @param component
   *          component, or null to represent a gap in the layout
   */
  public Widget wrap(Object component) {
    if (component == null || component instanceof JComponent) {
      return new ComponentWidget((JComponent) component); //.setId(getAnonId());
    }
    if (component instanceof Widget)
      return (Widget) component;
    throw new IllegalArgumentException("cannot create Widget wrapper for: " + component);
  }

  public WidgetManager open() {
    return open("<no context>");
  }

  private void log2(Object... messages) {
    if (!verbose())
      return;
    String indent = tab();
    Object[] msg = insertStringToFront(indent, messages);
    log(msg);
  }

  private String tab() {
    if (!verbose())
      return "";
    String dots = "................................................................................";
    int len = mPanelStack.size() * 4;
    len = Math.min(len, dots.length());
    return "|" + dots.substring(0, len);
  }

  public WidgetManager openTabSet(String selectTabKey) {
    log2("openTabSet", selectTabKey);
    Grid grid = new Grid();
    grid.setContext(selectTabKey);
    grid.setWidget(new TabbedPaneWidget(consumePendingListener(), selectTabKey));
    add(grid.widget());
    mPanelStack.add(grid);
    log2("added grid to panel stack for tab set");
    return this;
  }

  /**
   * Open a new tab within the current TabSet
   * 
   * @param tabTitle
   *          label for tab; either "{id}", or, if the id is different than what
   *          should be displayed, "{id:display}"
   */
  public WidgetManager openTab(String tabTitle) {
    log2("openTab", tabTitle);
    mPendingTabTitle = tabTitle;
    open(tabTitle);
    return this;
  }

  public WidgetManager closeTab() {
    close("closeTab");
    return this;
  }

  public WidgetManager closeTabSet() {
    Grid parent = last(mPanelStack);
    if (!(parent.widget() instanceof TabbedPaneWidget))
      badState("attempt to close tab set, current is:", parent.widget().id());
    close("tab set");
    return this;
  }

  /**
   * Add a colored panel, for test purposes
   */
  public WidgetManager debPanel() {
    return add(wrap(colorPanel()));
  }

  /**
   * Create a child view and push onto stack
   */
  public WidgetManager open(String debugContext) {
    log2("open", debugContext);

    Grid grid = new Grid();
    grid.setContext(debugContext);

    {
      if (mPendingColumnWeights == null)
        columns("x");
      grid.setColumnSizes(mPendingColumnWeights);
      mPendingColumnWeights = null;

      JComponent panel;
      if (mPendingContainer != null) {
        panel = mPendingContainer;
        log2("pending container:", panel.getClass());
        mPendingContainer = null;
      } else {
        log2("constructing JPanel");
        panel = new JPanel();
        applyMinDimensions(panel, mPendingMinWidthEm, mPendingMinHeightEm);
      }
      panel.setLayout(buildLayout());
      addStandardBorderForSpacing(panel);
      grid.setWidget(wrap(panel));
    }
    add(grid.widget());
    mPanelStack.add(grid);
    log2("added grid to panel stack, its widget:", grid.widget().getClass());
    return this;
  }

  /**
   * Pop view from the stack
   */
  public WidgetManager close() {
    return close("<no context>");
  }

  /**
   * Pop view from the stack
   */
  public WidgetManager close(String debugContext) {
    log2("about to close", debugContext);

    Grid parent = pop(mPanelStack);
    if (verbose())
      log2("close", debugContext, compInfo(gridComponent(parent)));
    endRow();

    if (!(parent.widget() instanceof TabbedPaneWidget))
      assignViewsToGridLayout(parent);
    return this;
  }

  /**
   * Verify that no unused 'pending' arguments exist, calls are balanced, etc
   */
  public WidgetManager finish() {
    clearPendingComponentFields();
    if (!mPanelStack.isEmpty())
      badState("panel stack nonempty; size:", mPanelStack.size());
    if (!mListenerStack.isEmpty())
      badState("listener stack nonempty; size:", mListenerStack.size());
    return this;
  }

  /**
   * If current row is only partially complete, add space to its end
   */
  public WidgetManager endRow() {
    if (mPanelStack.isEmpty())
      return this;
    Grid parent = last(mPanelStack);
    if (parent.nextCellLocation().x != 0)
      spanx().addHorzSpace();
    return this;
  }

  public WidgetManager addText(String id) {
    TextWidget t = new TextWidget(consumePendingListener(), id, consumePendingStringDefaultValue(),
        mLineCount, mEditableFlag, mPendingSize, mPendingMonospaced, mPendingMinWidthEm, mPendingMinHeightEm);
    consumeTooltip(t);
    return add(t);
  }

  public WidgetManager addHeader(String text) {
    spanx();
    JLabel label = new JLabel(text);
    label.setBorder(
        new CompoundBorder(buildStandardBorderWithZeroBottom(), BorderFactory.createEtchedBorder()));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    add(wrap(label));
    return this;
  }

  /**
   * Add a horizontal space to occupy cell(s) in place of other widgets
   */
  public WidgetManager addHorzSpace() {
    add(wrap(new JPanel()));
    return this;
  }

  /**
   * Add a horizontal separator that visually separates components above from
   * below
   */
  public WidgetManager addHorzSep() {
    spanx();
    add(wrap(new JSeparator(JSeparator.HORIZONTAL)));
    return this;
  }

  /**
   * Add a vertical separator that visually separates components left from right
   */
  public WidgetManager addVertSep() {
    spanx();
    growY();
    add(wrap(new JSeparator(JSeparator.VERTICAL)));
    return this;
  }

  /**
   * Add a row that can stretch vertically to occupy the available space
   */
  public WidgetManager addVertGrow() {
    JComponent panel;
    if (verbose())
      panel = colorPanel();
    else
      panel = new JPanel();
    spanx().growY();
    add(wrap(panel));
    return this;
  }

  /**
   * Add widget to view hierarchy
   */
  public WidgetManager add(Widget widget) {
    String id = null;
    if (widget.hasId())
      id = widget.id();
    log2("add widget", id != null ? id : "<anon>");

    if (id != null) {
      if (exists(widget.id()))
        badState("attempt to add widget id:", widget.id(), "that already exists");
      mWidgetMap.put(id, widget);
    }
    JComponent tooltipOwner = widget.componentForTooltip();
    if (tooltipOwner != null)
      consumeTooltip(tooltipOwner);
    addView(widget);
    return this;
  }

  public WidgetManager addHidden(String id, Object defaultValue) {
    return add(new HiddenWidget(defaultValue).setId(id));
  }

  private void consumeTooltip(Widget widget) {
    consumeTooltip(widget.swingComponent());
  }

  private void consumeTooltip(JComponent component) {
    if (mTooltip != null) {
      // Don't consume the tooltip if the component is a label or panel; we will
      // assume it is targeted at some later component
      if (component instanceof JPanel || component instanceof JLabel)
        return;
      component.setToolTipText(mTooltip);
      mTooltip = null;
    }
  }

  /**
   * Add a component to the current panel. Process pending constraints
   */
  private WidgetManager addView(Widget widget) {
    consumeTooltip(widget);

    if (!mPanelStack.isEmpty())
      auxAddComponent(widget);

    clearPendingComponentFields();
    return this;
  }

  private void auxAddComponent(Widget widget) {
    JComponent component = widget.swingComponent();

    // If the parent grid's widget is a tabbed pane,
    // add the component to it

    Grid grid = last(mPanelStack);
    if (grid.widget() instanceof TabbedPaneWidget) {
      TabbedPaneWidget tabPane = grid.widget();
      String tabIdNameExpression = consumePendingTabTitle(component);
      log2("adding a tab with name:", tabIdNameExpression);
      tabPane.add(tabIdNameExpression, component);
      return;
    }

    GridCell cell = new GridCell();
    cell.view = widget;
    IPoint nextGridCellLocation = grid.nextCellLocation();
    cell.x = nextGridCellLocation.x;
    cell.y = nextGridCellLocation.y;

    // determine location and size, in cells, of component
    int cols = 1;
    if (mSpanXCount != 0) {
      int remainingCols = grid.numColumns() - cell.x;
      if (mSpanXCount < 0)
        cols = remainingCols;
      else {
        if (mSpanXCount > remainingCols)
          throw new IllegalStateException(
              "requested span of " + mSpanXCount + " yet only " + remainingCols + " remain");
        cols = mSpanXCount;
      }
    }
    cell.width = cols;

    cell.growX = mGrowXFlag;
    cell.growY = mGrowYFlag;

    // If any of the spanned columns have 'grow' flag set, set it for this component
    for (int i = cell.x; i < cell.x + cell.width; i++) {
      int colSize = grid.columnSizes()[i];
      cell.growX = Math.max(cell.growX, colSize);
    }

    // "paint" the cells this view occupies by storing a copy of the entry in each cell
    for (int i = 0; i < cols; i++)
      grid.addCell(cell);
  }

  private int mColorIndex;
  private static Color sColors[] = { Color.BLUE, Color.GREEN, Color.RED, Color.GRAY, Color.MAGENTA,
      Color.pink.darker(), Color.BLUE.darker(), Color.GREEN.darker(), Color.RED.darker(), Color.GRAY.darker(),
      Color.MAGENTA.darker(), };

  private JComponent colorPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(randomColor());
    return panel;
  }

  private Color randomColor() {
    return sColors[MyMath.myMod(mColorIndex++, sColors.length)];
  }

  private static String compInfo(Component c) {
    String s = c.getClass().getSimpleName();
    if (c instanceof JLabel) {
      s = s + " " + quoted(((JLabel) c).getText());
    }
    return s;
  }

  private static String quoted(String s) {
    if (s == null)
      return "<null>";
    return "\"" + s + "\"";
  }

  public WidgetManager addButton(String id) {
    ButtonWidget button = new ButtonWidget(consumePendingListener(), id, consumePendingLabel(true));
    return add(button);
  }

  private static boolean nullToFalse(Boolean value) {
    if (value == null)
      value = false;
    return value;
  }

  public WidgetManager addToggleButton(String id) {
    ToggleButtonWidget button = new ToggleButtonWidget(consumePendingListener(), id,
        consumePendingLabel(true), consumePendingBooleanDefaultValue());
    return add(button);
  }

  public WidgetManager addLabel(String id) {
    String text = consumePendingLabel(true);
    log2("addLabel", id, text);
    add(new LabelWidget(id, mPendingGravity, mLineCount, text, mPendingSize, mPendingMonospaced,
        mPendingAlignment));
    return this;
  }

  public WidgetManager addSpinner(String id) {
    SpinnerWidget spinner = new SpinnerWidget(consumePendingListener(), id, consumePendingFloatingPoint(),
        consumePendingDefaultValue(), consumePendingMinValue(), consumePendingMaxValue(),
        consumePendingStepSize());
    return add(spinner);
  }

  public WidgetManager addSlider(String id) {
    SliderWidget slider = new SliderWidget(consumePendingListener(), id, consumePendingFloatingPoint(),
        consumePendingDefaultValue(), consumePendingMinValue(), consumePendingMaxValue(),
        consumePendingWithDisplay());
    return add(slider);
  }

  public WidgetManager addChoiceBox(String id) {
    ComboBoxWidget c = new ComboBoxWidget(consumePendingListener(), id, mComboChoices);
    mComboChoices = null;
    add(c);
    return this;
  }

  public void showModalErrorDialog(String message) {
    JOptionPane.showMessageDialog(getApplicationFrame(), message, "Problem", JOptionPane.ERROR_MESSAGE);
  }

  public void showModalInfoDialog(String message) {
    JOptionPane.showMessageDialog(getApplicationFrame(), message, "Info", JOptionPane.INFORMATION_MESSAGE);
  }

  private Component getApplicationFrame() {
    todo("getApplicationFrame returning null for now");
    return null;
  }

  private List<Grid> mPanelStack = arrayList();

  // ------------------------------------------------------------------
  // Layout manager
  // ------------------------------------------------------------------

  private LayoutManager buildLayout() {
    return new GridBagLayout();
  }

  private static <T extends JComponent> T gridComponent(Grid grid) {
    Widget widget = grid.widget();
    return widget.swingComponent();
  }

  private void assignViewsToGridLayout(Grid grid) {
    grid.propagateGrowFlags();
    Widget containerWidget = grid.widget();
    JComponent container = containerWidget.swingComponent();

    int gridWidth = grid.numColumns();
    int gridHeight = grid.numRows();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        GridCell cell = grid.cellAt(gridX, gridY);
        if (cell.isEmpty())
          continue;

        // If cell's coordinates don't match our iteration coordinates, we've
        // already added this cell
        if (cell.x != gridX || cell.y != gridY)
          continue;

        GridBagConstraints gc = new GridBagConstraints();

        float weightX = cell.growX;
        gc.weightx = weightX;
        gc.gridx = cell.x;
        gc.gridwidth = cell.width;
        gc.weighty = cell.growY;
        gc.gridy = cell.y;
        gc.gridheight = 1;

        Widget widget = (Widget) cell.view;
        JComponent component = widget.swingComponent();
        // Padding widgets have no views
        if (component == null)
          continue;

        // Not using gc.anchor
        gc.fill = GridBagConstraints.BOTH;

        // Not using gravity
        container.add(widget.swingComponent(), gc);
      }
    }
  }

  static Font getFont(boolean monospaced, int widgetFontSize) {
    int fontSize;
    switch (widgetFontSize) {
    case SIZE_DEFAULT:
      fontSize = 16;
      break;
    case SIZE_MEDIUM:
      fontSize = 16;
      break;
    case SIZE_SMALL:
      fontSize = 12;
      break;
    case SIZE_LARGE:
      fontSize = 22;
      break;
    case SIZE_HUGE:
      fontSize = 28;
      break;
    default:
      alert("unsupported widget font size:", widgetFontSize);
      fontSize = 16;
      break;
    }

    Integer mapKey = fontSize + (monospaced ? 0 : 1000);

    Font f = sFontMap.get(mapKey);
    if (f == null) {
      if (monospaced)
        f = new Font("Monaco", Font.PLAIN, fontSize);
      else
        f = new Font("Lucida Grande", Font.PLAIN, fontSize);
      sFontMap.put(mapKey, f);
    }
    return f;
  }

  private static Map<Integer, Font> sFontMap = hashMap();

  private SortedMap<String, Widget> mWidgetMap = treeMap();
  private boolean mActive;

  private WidgetListener mPendingListener;
  private Widget mListenerWidget;

  private JComponent mPendingContainer;
  private String mPendingTabTitle;
  private int mSpanXCount;
  private int mGrowXFlag, mGrowYFlag;
  private int mPendingSize;
  private boolean mEditableFlag;
  private int mPendingGravity;
  private float mPendingMinWidthEm;
  private float mPendingMinHeightEm;
  private boolean mPendingMonospaced;
  private int mPendingAlignment;
  private int mLineCount;
  private SymbolicNameSet mComboChoices;
  private String mTooltip;
  private Number mPendingMinValue, mPendingMaxValue, mPendingDefaultValue, mPendingStepSize;
  private Boolean mPendingBooleanDefaultValue;
  private String mPendingStringDefaultValue;
  private String mPendingLabel;
  private Boolean mPendingFloatingPoint;
  private Boolean mPendingWithDisplay;
  private long mLastWidgetEventTime;
  private List<WidgetListener> mListenerStack = arrayList();
}
