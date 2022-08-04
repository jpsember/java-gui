package js.guiapp;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.geometry.IPoint;
import js.geometry.IRect;

public class FrameWrapper extends BaseObject {

  public FrameWrapper() {
    loadTools();
    JFrame frame = new JFrame();
    // Set a distinctive background color in case no other components are added
    // (e.g. if no project is open)
    frame.getContentPane().setBackground(Color.gray);
    mFrame = frame;
    setBounds(IRect.DEFAULT_INSTANCE);
  }

  public JFrame frame() {
    return mFrame;
  }

  public IRect bounds() {
    return new IRect(frame().getBounds());
  }

  public void setBounds(IRect bounds) {
    if (bounds.minDim() < 40) {
      IPoint screenSize = new IPoint(Toolkit.getDefaultToolkit().getScreenSize());
      IPoint defaultSize = screenSize.scaledBy(.9f);
      bounds = new IRect((screenSize.x - defaultSize.x) / 2, (screenSize.y - defaultSize.y) / 2,
          defaultSize.x, defaultSize.y);
      if (alert("setting bounds smaller"))
        bounds = IRect.withLocAndSize(IPoint.with(300, 150), IPoint.with(1500,800));
    }
    frame().setBounds(bounds.toRectangle());
  }

  private JFrame mFrame;
}