package js.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static js.base.Tools.*;

class ModalWidgetValueEditor extends JDialog
    implements ActionListener,
    PropertyChangeListener {


  /**
   * Creates the reusable dialog.
   */
  public ModalWidgetValueEditor(ModalEditorValueAccessor accessor) {
    super((Frame) null, true);

    mAccessor = accessor;
    setTitle("Value Editor");

    mTextField = new JTextField(10);
    mTextField.setText(
        mAccessor.encodeValueToString(accessor.readValue()));

    // Create an array of the text and components to be displayed.
    Object[] array = {mTextField};

    //Create an array specifying the number of dialog buttons
    //and their text.
    Object[] options = {mStrEnter, mStrCancel};

    //Create the JOptionPane.
    mOptionPane = new JOptionPane(array,
        JOptionPane.PLAIN_MESSAGE,
        JOptionPane.YES_NO_OPTION,
        null,
        options,
        options[0]);


    // Make this dialog display it.
    setContentPane(mOptionPane);

    // Handle window closing correctly.
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        /*
         * Instead of directly closing the window,
         * we're going to change the JOptionPane's
         * value property.
         */
        mOptionPane.setValue(new Integer(
            JOptionPane.CLOSED_OPTION));
      }
    });

    //Ensure the text field always gets the first focus.
    addComponentListener(new ComponentAdapter() {
      public void componentShown(ComponentEvent ce) {
        mTextField.requestFocusInWindow();
      }
    });

    //Register an event handler that puts the text into the option pane.
    mTextField.addActionListener(this);

    //Register an event handler that reacts to option pane state changes.
    mOptionPane.addPropertyChangeListener(this);
  }

  /**
   * This method handles events for the text field.
   */
  public void actionPerformed(ActionEvent e) {
    mOptionPane.setValue(mStrEnter);
  }

  /**
   * This method reacts to state changes in the option pane.
   */
  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();

    if (isVisible()
        && (e.getSource() == mOptionPane)
        && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
        JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
      Object value = mOptionPane.getValue();

      if (value == JOptionPane.UNINITIALIZED_VALUE) {
        return;
      }

      // Reset the JOptionPane's value.
      // If you don't do this, then if the user
      // presses the same button next time, no
      // property change event will be fired.
      mOptionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

      if (mStrEnter.equals(value)) {
        var typedText = mTextField.getText();
        var parsedValue = mAccessor.parseValueFromString(typedText);
        if (parsedValue != null) {
          clearAndHide();
          mAccessor.writeValue(parsedValue);
        }
      } else {
        clearAndHide();
      }
    }
  }

  /**
   * This method clears the dialog and hides it.
   */
  private void clearAndHide() {
    mTextField.setText(null);
    setVisible(false);
  }

  private JTextField mTextField;
  private JOptionPane mOptionPane;
  private String mStrEnter = "Enter";
  private String mStrCancel = "Cancel";
  private ModalEditorValueAccessor mAccessor;
}