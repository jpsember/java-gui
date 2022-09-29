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

import java.awt.BorderLayout;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import js.base.BaseObject;
import js.data.DataUtil;
import js.geometry.IPoint;
import js.geometry.MyMath;
import js.guiapp.GUIApp;
import js.guiapp.UserEvent;
import js.json.JSMap;
import js.parsing.RegExp;

import static js.base.Tools.*;
import static js.widget.SwingUtils.*;

/**
 * A collection of Widgets, with utilities to compose them onto GUI components
 */
public final class WidgetManager extends BaseObject {

  public static final int SIZE_DEFAULT = 0;
  public static final int SIZE_TINY = 1;
  public static final int SIZE_SMALL = 2;
  public static final int SIZE_LARGE = 3;
  public static final int SIZE_HUGE = 4;
  public static final int SIZE_MEDIUM = 5;

  public static final int ALIGNMENT_DEFAULT = -1;
  public static final int ALIGNMENT_LEFT = SwingConstants.LEFT;
  public static final int ALIGNMENT_CENTER = SwingConstants.CENTER;
  public static final int ALIGNMENT_RIGHT = SwingConstants.RIGHT;

  /**
   * Determine if widget events should be propagated to listeners (including the
   * project or script record of gadget values). False while user interface is
   * still being constructed
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

  public Number numValue(String id) {
    return (Number) get(id).readValue();
  }

  /**
   * Get value of boolean-valued gadget
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
   */
  public void setValue(String id, double v) {
    get(id).writeValue(v);
  }

  /**
   * Get value of string-valued gadget
   */
  public String stringValue(String id) {
    return (String) (get(id).readValue());
  }

  /**
   * Set value of string-valued gadget
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
    Widget w = find(id);
    if (w == null)
      badState("Can't find widget with id:", id);
    return w;
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
   * Set gadget values according to a JSMap
   */
  public void writeGadgetValues(JSMap map) {
    for (Map.Entry<String, Object> entry : map.wrappedMap().entrySet()) {
      String id = entry.getKey();
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
        m.putUnsafe(ent.getKey(), v);
    }
    return m;
  }

  private SortedMap<String, Widget> mGadgetMap = treeMap();
  private boolean mActive;

  // ---------------------------------------------------------------------
  // Composing
  // ---------------------------------------------------------------------

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

  public WidgetManager fixedWidth(float ems) {
    mPendingFixedWidthEm = ems;
    return this;
  }

  public WidgetManager fixedHeight(float ems) {
    mPendingFixedHeightEm = ems;
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

  public WidgetManager scrollable() {
    mScrollableFlag = true;
    return this;
  }

  /**
   * Add a dummy item to prevent an editable text field from gaining focus
   * automatically (and showing the keyboard).
   * <p>
   * It will add a column-spanning row of zero height.
   */
  public WidgetManager suppressTextFocus() {
    return this;
  }

  public WidgetManager addLabel() {
    return addLabel(null, "");
  }

  public WidgetManager addLabel(String text) {
    return addLabel(null, text);
  }

  /**
   * Add a text field whose content is not persisted to the state map
   */
  public Widget addText() {
    return addText(null);
  }

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

  public WidgetManager listener(WidgetListener listener) {
    checkState(mPendingListener == null, "already a pending listener");
    mPendingListener = listener;
    return this;
  }

  public Widget addButton(String label) {
    return addButton(null, label);
  }

  // public abstract Widget addToggleButton(String key, String label);

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

  public WidgetManager defaultVal(double value) {
    floats();
    mPendingDefaultValue = value;
    return this;
  }

  public WidgetManager defaultVal(int value) {
    mPendingDefaultValue = value;
    return this;
  }

  /**
   * Include auxilliary widget with this one; e.g., numeric display with slider,
   * or time stamp with log
   */
  public WidgetManager withDisplay() {
    mPendingWithDisplayFlag = true;
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

  /**
   * If there's a pending WidgetListener, return it (and clear it)
   */
  public WidgetListener consumePendingListener() {
    WidgetListener listener = mPendingListener;
    mPendingListener = null;
    if (listener == null && !mListenerStack.isEmpty()) {
      listener = last(mListenerStack);
    }
    return listener;
  }

  private String consumePendingTabTitle(Object component) {
    String tabNameExpression = "?NAME?";
    if (mPendingTabTitle == null)
      die("no tab name specified for:", component);
    else {
      tabNameExpression = mPendingTabTitle;
      mPendingTabTitle = null;
    }
    return tabNameExpression;
  }

  private void clearPendingComponentFields() {
    mPendingContainer = null;
    mPendingColumnWeights = null;
    mSpanXCount = 0;
    mGrowXFlag = mGrowYFlag = 0;
    mPendingSize = SIZE_DEFAULT;
    mPendingAlignment = ALIGNMENT_DEFAULT;
    mPendingGravity = 0;
    mPendingMinWidthEm = 0;
    mPendingMinHeightEm = 0;
    mPendingFixedWidthEm = 0;
    mPendingFixedHeightEm = 0;
    mPendingMonospaced = false;
    mEditableFlag = false;
    mScrollableFlag = false;
    mLineCount = 0;
    mComboChoices = null;
    mPendingMinValue = null;
    mPendingMaxValue = null;
    mPendingDefaultValue = null;
    mPendingStepSize = null;
    mPendingWithDisplayFlag = false;
    mPendingFloatingPoint = false;
  }

  // ------------------------------------------------------------------
  // Layout logic
  // ------------------------------------------------------------------

  private int[] mPendingColumnWeights;
  /* private */ boolean mSuppressFocusFlag;

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
      listener.event();
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

  @Deprecated // prefer with context for now
  public Widget open() {
    return open("<no context>");
  }

  private void log2(Object... messages) {
    if (!verbose())
      return;
    String indent = tab();
    Object[] msg = insertStringToFront(indent, messages);
    log(msg);
  }

  public WidgetManager openTabSet(String selectTabKey) {
    log2("openTabSet", selectTabKey);
    Grid grid = new Grid();
    grid.setContext(selectTabKey);
    grid.setWidget(new OurTabbedPane(consumePendingListener(), selectTabKey));
    add(grid.widget());
    mPanelStack.add(grid);
    log2("added grid to panel stack for tab set");
    return this;
  }

  public WidgetManager openTab(String tabTitle) {
    todo("allow a symbolic value AND a human title, and store selected tab as a string");
    log2("openTab", tabTitle);
    mPendingTabTitle = tabTitle;
    open(tabTitle);
    return this;
  }

  public WidgetManager closeTab() {
    //    log2("closeTab");
    close("closeTab");
    return this;
  }

  public WidgetManager closeTabSet() {
    Grid parent = last(mPanelStack);
    if (!(parent.widget() instanceof OurTabbedPane))
      badState("attempt to close tab set, current is:", parent.widget().id());
    close("tab set");
    return this;
  }

  /**
   * Create a child view and push onto stack
   */
  public Widget open(String debugContext) {
    log2("open", debugContext);

    Grid grid = new Grid();
    grid.setContext(debugContext);

    {
      if (mPendingColumnWeights == null)
        columns("x");
      grid.setColumnSizes(mPendingColumnWeights);

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
    return grid.widget();
  }

  private String tab() {
    if (!verbose())
      return "";
    String dots = "................................................................................";
    int len = mPanelStack.size() * 4;
    len = Math.min(len, dots.length());
    return "|" + dots.substring(0, len);
  }

  /**
   * Pop view from the stack
   */
  @Deprecated // prefer with context for now
  public WidgetManager close() {
    return close("<no context>");
  }

  public WidgetManager close(String debugContext) {
    log2("about to close", debugContext);

    Grid parent = pop(mPanelStack);
    if (verbose())
      log2("close", debugContext, compInfo(gridComponent(parent)));
    endRow();
    //    if (verbose()) {
    //      log("close", compInfo(gridComponent(parent)));
    //      log("");
    //    }

    if (!(parent.widget() instanceof OurTabbedPane)) {
      assignViewsToGridLayout(parent);
    }

    //    if (mPanelStack.isEmpty()) {
    //      Widget widget = parent.widget();
    //      mOutermostView = widget.swingComponent();
    //      ensureListenerStackEmpty();
    //    }
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

  /**
   * Add a text field whose content is not persisted to the state map
   */
  public Widget addText(String key) {
    OurText t = new OurText(consumePendingListener(), key, mLineCount, mEditableFlag, mPendingSize,
        mPendingMonospaced, mPendingMinWidthEm, mPendingMinHeightEm);
    consumeTooltip(t);
    add(t);
    return t;
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
  public Widget add(Widget widget) {
    String id = null;
    if (widget.hasId())
      id = widget.id();
    log2("add widget", id != null ? id : "<anon>");

    if (id != null) {
      if (exists(widget.id()))
        badState("attempt to add widget id:", widget.id(), "that already exists");
      mGadgetMap.put(id, widget);
    }
    JComponent tooltipOwner = widget.componentForTooltip();
    if (tooltipOwner != null)
      consumeTooltip(tooltipOwner);
    addView(widget);
    return widget;
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
    if (grid.widget() instanceof OurTabbedPane) {
      OurTabbedPane tabPane = grid.widget();
      String tabName = consumePendingTabTitle(component);
      log2("adding a tab with name:", tabName);
      tabPane.add(tabName, component);
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

  public Widget addButton(String key, String label) {
    OurButton button = new OurButton(consumePendingListener(), key, label);
    add(button);
    return button;
  }

  public Widget addToggleButton(String key, String label, boolean defaultValue) {
    OurToggleButton button = new OurToggleButton(consumePendingListener(), key, label, defaultValue);
    add(button);
    todo("should we return WidgetManager consistently, instead of the widgets?");
    return button;
  }

  public WidgetManager addLabel(String key, String text) {
    log2("addLabel", key, text);
    add(new OurLabel(this, key, mPendingGravity, mLineCount, text, mPendingSize, mPendingMonospaced,
        mPendingAlignment));
    return this;
  }

  public Widget addSpinner(String key) {
    OurSpinner spinner = new OurSpinner(consumePendingListener(), key, mPendingFloatingPoint,
        mPendingDefaultValue, mPendingMinValue, mPendingMaxValue, mPendingStepSize);
    add(spinner);
    return spinner;
  }

  public Widget addSlider(String key) {
    OurSlider slider = new OurSlider(consumePendingListener(), key, mPendingFloatingPoint,
        mPendingDefaultValue, mPendingMinValue, mPendingMaxValue, mPendingWithDisplayFlag);
    add(slider);
    return slider;
  }

  public Widget addChoiceBox(String key) {
    OurComboBox c = new OurComboBox(consumePendingListener(), key, mComboChoices);
    add(c);
    return c;
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

  private static class ComponentWidget extends Widget {

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

  private static class OurTabbedPane extends Widget implements ChangeListener {

    public OurTabbedPane(WidgetListener listener, String key) {
      setId(key);
      todo("pass in listener, not widgetManager");
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

    @Override
    public void restoreValue(Object value) {
      if (value == null)
        value = "";
      String symbolicName = value.toString();
      int index = mTabNames.getSymbolicIndex(symbolicName);
      tabbedPane().setSelectedIndex(index);
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

  private static class OurButton extends Widget {

    public OurButton(WidgetListener listener, String key, String label) {
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

  private static class OurToggleButton extends Widget {
    public OurToggleButton(WidgetListener listener, String key, String label, boolean defaultValue) {
      setId(key);
      JCheckBox component = new JCheckBox(label, defaultValue);
      //     component.addActionListener(this);
      setComponent(component);
      registerListener(listener);
    }
    //
    //        @Override
    //        public void actionPerformed(ActionEvent e) {
    //          JCheckBox component = swingComponent();
    //          storeValueToStateMap(manager().stateMap(), component.isSelected());
    //          super.actionPerformed(e);
    //        }

    @Override
    public void restoreValue(Object value) {
      Boolean boolValue = (Boolean) value;
      if (boolValue == null)
        boolValue = false;
      setChecked(boolValue);
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

  private static class OurSpinner extends Widget implements ChangeListener {
    public OurSpinner(WidgetListener listener, String key, boolean floatsFlag, Number defaultValue,
        Number minimum, Number maximum, Number stepSize) {
      mStepper = new NumericStepper(floatsFlag, defaultValue, minimum, maximum, stepSize);
      checkState(mStepper.isInt(), "non-integer not supported");
      SpinnerModel model = new SpinnerNumberModel(mStepper.def().intValue(), mStepper.min().intValue(),
          mStepper.max().intValue(), mStepper.step().intValue());
      JSpinner component = new JSpinner(model);
      model.addChangeListener(this);
      setComponent(component);
      registerListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      notifyListener();
    }

    @Override
    public void restoreValue(Object value) {
      JSpinner component = swingComponent();
      Number number = (Number) value;
      if (number == null)
        number = mStepper.def();
      component.getModel().setValue(number.intValue());
    }

    @Override
    public Number readValue() {
      return (Number) spinner().getModel().getValue();
    }

    @Override
    public void writeValue(Object v) {
      Number number = (Number) v;
      setValue(number);
    }

    private JSpinner spinner() {
      return swingComponent();
    }

    private NumericStepper mStepper;
  }

  private static class OurSlider extends Widget implements ChangeListener {
    public OurSlider(WidgetListener listener, String key, boolean floatsFlag, Number defaultValue,
        Number minimum, Number maximum, boolean includesDisplay) {
      setId(key);
      mStepper = new NumericStepper(floatsFlag, defaultValue, minimum, maximum, null);
      JComponent component;
      JSlider slider = new JSlider(mStepper.internalMin(), mStepper.internalMax(), mStepper.internalVal());
      slider.addChangeListener(this);
      mSlider = slider;
      if (includesDisplay) {
        int maxValueStringLength = mStepper.maxDigits();
        mDisplay = new JTextField(maxValueStringLength);
        mDisplay.setEditable(false);
        mDisplay.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel container = new JPanel();
        // Make the container's layout a BorderLayout, with the slider grabbing the available space
        container.setLayout(new BorderLayout());
        container.add(slider, BorderLayout.CENTER);
        container.add(mDisplay, BorderLayout.EAST);
        component = container;
        updateDisplayValue();
      } else {
        component = slider;
      }
      setComponent(component);
      registerListener(listener);
    }

    @Override
    public JComponent componentForTooltip() {
      return getSlider();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      updateDisplayValue();
      GUIApp.sharedInstance().userEventManagerListener(UserEvent.widgetEvent(id()));
      notifyListener();
    }

    @Override
    public void restoreValue(Object value) {
      Number number = (Number) value;
      getSlider().getModel().setValue(mStepper.toInternalUnits(number));
    }

    private void updateDisplayValue() {
      if (mDisplay == null)
        return;
      int numValue = getSlider().getModel().getValue();
      String value = mStepper.fromInternalUnits(numValue).toString();
      mDisplay.setText(value);
    }

    public JSlider getSlider() {
      return mSlider;
    }

    @Override
    public void setValue(Number number) {
      getSlider().getModel().setValue(number.intValue());
      updateDisplayValue();
      notifyListener();
    }

    @Override
    public Number readValue() {
      return getSlider().getModel().getValue();
    }

    @Override
    public void writeValue(Object v) {
      Number number = (Number) v;
      setValue(number);
    }

    private NumericStepper mStepper;
    private JTextField mDisplay;
    private JSlider mSlider;
  }

  private static class OurLabel extends Widget {

    public OurLabel(WidgetManager manager, String key, int gravity, int lineCount, String text, int fontSize,
        boolean monospaced, int alignment) {
      todo("manager shouldn't be required");
      setId(key);
      JLabel label = new JLabel(text);
      if (fontSize == SIZE_DEFAULT)
        fontSize = SIZE_SMALL;
      Font font = getFont(monospaced, fontSize);
      label.setFont(font);
      if (alignment == ALIGNMENT_DEFAULT)
        alignment = ALIGNMENT_RIGHT;
      label.setHorizontalAlignment(alignment);
      setComponent(label);
    }

    private JLabel textComponent() {
      return swingComponent();
    }

    @Override
    public void setText(String text) {
      textComponent().setText(text);
    }

    @Override
    public String getText() {
      return textComponent().getText();
    }

    @Override
    public String readValue() {
      return getText();
    }

    @Override
    public void writeValue(Object v) {
      setText((String) v);
    }
  }

  private static class OurText extends Widget implements DocumentListener {
    public OurText(WidgetListener listener, String key, int lineCount, boolean editable, int fontSize,
        boolean monospaced, float minWidthEm, float minHeightEm) {
      todo("manager shouldn't be required");
      setId(key);
      JComponent container;
      JTextComponent textComponent;
      if (lineCount > 1) {
        JTextArea textArea = new JTextArea(lineCount, 40);
        textArea.setLineWrap(true);
        textComponent = textArea;
        container = new JScrollPane(textArea);
      } else {
        JTextField textField = new JTextField();
        textComponent = textField;
        container = textComponent;
      }
      textComponent.setEditable(editable);
      if (editable) {
        registerListener(listener);
        textComponent.getDocument().addDocumentListener(this);
      }
      Font font = getFont(monospaced, fontSize);
      textComponent.setFont(font);

      applyMinDimensions(container, font, minWidthEm, minHeightEm);
      mTextComponent = textComponent;
      setComponent(container);
    }

    @Override
    public void restoreValue(Object value) {
      String textValue = nullToEmpty((String) value);
      textComponent().setText(textValue);
    }

    public JTextComponent textComponent() {
      return mTextComponent;
    }

    @Override
    public void setText(String text) {
      textComponent().setText(text);
    }

    @Override
    public String getText() {
      return textComponent().getText();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      commonUpdateHandler();
    }

    @Override
    public String readValue() {
      return getText();
    }

    @Override
    public void writeValue(Object v) {
      setText((String) v);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      commonUpdateHandler();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      commonUpdateHandler();
    }

    private void commonUpdateHandler() {
      //      if (key() != null) {
      //        storeValueToStateMap(manager().stateMap(), textComponent().getText());
      //      }
      notifyListener();
    }

    private JTextComponent mTextComponent;
  }

  private static class OurComboBox extends Widget {
    public OurComboBox(WidgetListener listener, String key, SymbolicNameSet choices) {
      mChoices = checkNotNull(choices);
      JComboBox<String> component = new JComboBox<>(DataUtil.toStringArray(mChoices.displayNames()));
      todo("add action listener");
      //      component.addActionListener(this);
      setComponent(component);
      registerListener(listener);
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
      todo("not finished");
    }

    @Override
    public Integer readValue() {
      JComboBox<String> component = swingComponent();
      todo("not sure this is correct");
      return component.getSelectedIndex();
    }

    @Override
    public void restoreValue(Object value) {
      JComboBox<String> component = swingComponent();
      int index = mChoices.getSymbolicIndex((String) value);
      component.setSelectedIndex(index);
    }

    //    private String displayToInternal(String s) {
    //      return mChoices.displayToSymbolic(s);
    //    }

    private SymbolicNameSet mChoices;
  }

  private List<Grid> mPanelStack = arrayList();

  // ------------------------------------------------------------------
  // Layout manager
  // ------------------------------------------------------------------

  private LayoutManager buildLayout() {
    return new GridBagLayout();
  }

  /**
   * Add a colored panel, for test purposes
   */
  public Widget addPanel() {
    return add(wrap(colorPanel()));
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

  /**
   * Create a view, push onto stack; but don't add the view to the current
   * hierarchy
   */
  public Widget openFree() {
    throw die("not supported");
  }

  public static Font getFont(boolean monospaced, int widgetFontSize) {
    //unimp("use SwingUtils for this");
    int fontSize;
    switch (widgetFontSize) {
    case WidgetManager.SIZE_DEFAULT:
      fontSize = 16;
      break;
    case WidgetManager.SIZE_MEDIUM:
      fontSize = 16;
      break;
    case WidgetManager.SIZE_SMALL:
      fontSize = 12;
      break;
    case WidgetManager.SIZE_LARGE:
      fontSize = 22;
      break;
    case WidgetManager.SIZE_HUGE:
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

  private WidgetListener mPendingListener;
  private Widget mListenerWidget;

  private JComponent mPendingContainer;
  private String mPendingTabTitle;
  private int mSpanXCount;
  private int mGrowXFlag, mGrowYFlag;
  private int mPendingSize;
  private boolean mEditableFlag;
  /* private */ boolean mScrollableFlag;
  private int mPendingGravity;
  private float mPendingMinWidthEm;
  private float mPendingMinHeightEm;
  /* private */ float mPendingFixedWidthEm;
  /* private */ float mPendingFixedHeightEm;
  private boolean mPendingMonospaced;
  private int mPendingAlignment;
  private int mLineCount;
  private SymbolicNameSet mComboChoices;
  private String mTooltip;
  private Number mPendingMinValue, mPendingMaxValue, mPendingDefaultValue, mPendingStepSize;
  private boolean mPendingFloatingPoint;
  private boolean mPendingWithDisplayFlag;
  private long mLastWidgetEventTime;
  private List<WidgetListener> mListenerStack = arrayList();
}
