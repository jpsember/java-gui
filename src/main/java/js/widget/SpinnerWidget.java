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

    {
      var editor = new JButton("editor");
      editor.addActionListener((e) -> {
        pr("action listener:", e);
        var d = new ModalWidgetValueEditor( this);
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        todo("bring up a modal dialog box");
      });
      c.setEditor(editor);
      c.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          pr("Spinner state changed");
          SpinnerModel model = c.getModel();
          var x = String.format("%d", model.getValue());
          editor.setText("value{   " + x + "   }");
        }
      });
    }


    model.addChangeListener(this);
    setComponent(c);
    registerListener(listener);
  }


  private void updateDisplayValue() {
//    if (mDisplay == null)
//      return;
//    int numValue = getSlider().getModel().getValue();
//    String value = mStepper.formatNumber(mStepper.fromInternalUnits(numValue));
//    mDisplay.setText(value);
  }


  @Override
  public void stateChanged(ChangeEvent e) {

    updateDisplayValue();

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
    updateDisplayValue();
    notifyListener();
  }

  private JSpinner spinner() {
    return swingComponent();
  }

  private NumericStepper mStepper;
}