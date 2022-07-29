package js.gui.example;

import static js.base.Tools.*;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;

import js.app.AppOper;
import js.data.IntArray;
import js.guiapp.GUIApp;
import js.guiapp.KeyboardShortcutManager;
import js.guiapp.OurAppFrame;
import js.guiapp.UserEvent;
import js.guiapp.UserEventManager;
import js.guiapp.UserOperation;
import js.system.SystemUtil;

public class SampleApp extends GUIApp {

  

  public static void main(String[] args) {
    new SampleApp().startApplication(args);
  }

  @Override
  protected AppOper constructAppOper() {
    return new OurOper();
  }

  @Override
  public String getVersion() {
   return "9.9";
  }

  
  
  
  // ------------------------------------------------------------------
  // AppOper implementation
  // ------------------------------------------------------------------
  private class OurOper extends AppOper {

    @Override
    public String userCommand() {
      return null;
    }
//
//    @Override
//    public ScreditConfig defaultArgs() {
//      return ScreditConfig.DEFAULT_INSTANCE;
//    }

    @Override
    public void perform() {
      todo("add support for dev mode, killing other instances");
//      if (cmdLineArgs().hasNextArg()) {
//        mStartProjectFile = new File(cmdLineArgs().nextArg());
//        log(DASHES, "set start project:", INDENT, mStartProjectFile, VERT_SP);
//      }
//      if (cmdLineArgs().hasNextArg())
//        throw badArg("Unexpected argument(s):", cmdLineArgs().peekNextArg());
//      if (devMode()) {
//        SystemUtil.killProcesses("js.scredit");
//        SystemUtil.killAfterDelay("js.scredit");
//      }
      startGUI(() -> createAndShowGUI());
    }

//    @Override
//    protected List<Object> getAdditionalArgs() {
//      return arrayList("[<project directory>]");
//    }

    @Override
    protected String getHelpDescription() {
      return "Sample GUIApp implementation";
    }
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
 
  protected void createAndShowGUI() {
    mUserEventManager = new UserEventManager(new DefaultOper() );
   //mUserEventManager.setListener(this::processUserEvent);
    mKeyboardShortcutManager = new KeyboardShortcutManager(this.getClass());
    createFrame();
//    openAppropriateProject();
//    startPeriodicBackgroundTask();
  }
  
  
  
  // TODO: refactor to make this private
  public UserEventManager mUserEventManager;
  // TODO: refactor to make this private
 public KeyboardShortcutManager mKeyboardShortcutManager;

  
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 

 private void createFrame() {
   mFrame = new OurAppFrame();

   JFrame jFrame = mFrame.frame();
//
//   // Handle close window requests ourselves
//   //
//   jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//   jFrame.addWindowListener(new WindowAdapter() {
//     @Override
//     public void windowClosing(WindowEvent e) {
//       if (requestWindowClose()) {
//         closeProject();
//         jFrame.setVisible(false);
//         jFrame.dispose();
//         mFrame = null;
//       }
//     }
//   });
   jFrame.setVisible(true);
 }

 private OurAppFrame mFrame;

  
}









class DefaultOper extends UserOperation  {

  public DefaultOper() {
  }




}


