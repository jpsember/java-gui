package js.widget;

import static js.base.Tools.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class SpinnerWidget extends Widget implements ChangeListener {
  public SpinnerWidget(WidgetListener listener, String key, boolean floatsFlag, Number defaultValue,
                       Number minimum, Number maximum, Number stepSize) {
    setId(key);
    checkState(!floatsFlag, "float not supported");
    mStepper = new NumericStepper(floatsFlag, defaultValue, minimum, maximum, stepSize);
    var model = new SpinnerNumberModel(mStepper.def().intValue(), mStepper.min().intValue(),
        mStepper.max().intValue(), mStepper.step().intValue());
    var c = new JSpinner(model);

    // Change the editor to a button that brings up a modal dialog to modify the value
    var editor = new JButton("editor");
    editor.addActionListener((e) -> {

      var d = new ModalWidgetValueEditor(
          new ModalEditorValueAccessor() {
            @Override
            public Object readValue() {
              var sw = SpinnerWidget.this;
              return sw.readValue();
            }

            @Override
            public void writeValue(Object value) {
              var sw = SpinnerWidget.this;
              sw.writeValue(value);
            }

            @Override
            public String encodeValueToString(Object value) {
              var n = (Number) value;
              return Integer.toString(n.intValue());
            }

            @Override
            public Object parseValueFromString(String string) {
              try {
                return Integer.valueOf(string);
              } catch (NumberFormatException e) {
                return null;
              }
            }
          });
      d.pack();
      d.setLocationRelativeTo(null);
      d.setVisible(true);
    });
    c.setEditor(editor);

    c.addChangeListener(e -> editor.setText(c.getValue().toString()));

    model.addChangeListener(this);
    setComponent(c);
    registerListener(listener);
  }


  @Override
  public void stateChanged(ChangeEvent e) {
    notifyListener();
    notifyApp();
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


  @Override
  public void setValue(Number number) {
    var internalValue = mStepper.toInternalUnits(number);
    spinner().getModel().setValue(internalValue);
    notifyListener();
  }

  private JSpinner spinner() {
    return swingComponent();
  }

  private NumericStepper mStepper;
}