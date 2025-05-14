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
 **/
package js.guiapp;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import js.file.Files;

import static js.base.Tools.*;

public final class SwingUtils {

  @Deprecated
  public static final boolean DEBUG_FILEBASED = false && alert("!DEBUG_FILEBASED is true");


  public static File displayOpenDirectoryFileRequester(File startDirOrNull, String prompt) {
    FileDialog fileChooser = new FileDialog((JFrame) null, prompt, FileDialog.LOAD);
    // See: https://coderanch.com/t/645553/java/JFileChooser-Mac-OSX
    System.setProperty("apple.awt.fileDialogForDirectories", "true");
    if (Files.nonEmpty(startDirOrNull))
      fileChooser.setDirectory(startDirOrNull.getPath());
    fileChooser.setVisible(true);
    String filename = fileChooser.getFile();
    String directory = fileChooser.getDirectory();
    fileChooser.setVisible(false);
    if (filename == null)
      return null;
    return new File(directory, filename);
  }

  public static File displayOpenFileRequester(File startDirOrNull, String prompt) {
    return displayOpenFileRequester(startDirOrNull, prompt, null);
  }


  public static File displayOpenFileRequester(File startDirOrNull, String prompt, FilenameFilter filter) {
    FileDialog fileChooser = new FileDialog((JFrame) null, prompt, FileDialog.LOAD);
    // I think prompts just won't show up on Mac.
    if (Files.nonEmpty(startDirOrNull))
      fileChooser.setDirectory(startDirOrNull.getPath());
    if (filter != null)
      fileChooser.setFilenameFilter(filter);
    fileChooser.setVisible(true);
    String filename = fileChooser.getFile();
    String directory = fileChooser.getDirectory();
    fileChooser.setVisible(false);
    if (filename == null)
      return null;
    return new File(directory, filename);
  }

// ----------------------------------------------------------------------------------------------


  public static FilenameFilter filenameFilterForExtension(final String ext, final String description) {
    return (dir, name) -> Files.getExtension(name).equals(ext);
  }

  // https://stackoverflow.com/questions/9796800/jfilechooser-vs-jdialog-vs-filedialog


  @Deprecated // Use displayOpenFileRequester instead
  public static File displayOpenFileChooser(File startDirOrNull, String prompt, FileFilter filter) {
    File output = null;
    File dir = Files.currentDirectory();
    var fc = new JFileChooser(dir);
    if (filter != null)
      fc.setFileFilter(filter);
    fc.setDialogTitle(prompt);
    int x = fc.showOpenDialog(null);
    if (x == JFileChooser.APPROVE_OPTION)
      output = fc.getSelectedFile();
    return output;
  }

  @Deprecated // Create a 'save' analogy to displayOpenFileRequester and use that instead
  public static File displaySaveFileChooser(File startFileOrNull, String prompt, FileFilter filter) {
    File output = null;
    File dir = Files.currentDirectory();
    var fc = new JFileChooser(dir);
    fc.setDialogTitle(prompt);
    if (filter != null)
      fc.setFileFilter(filter);
    var x = fc.showSaveDialog(null);
    if (x == JFileChooser.APPROVE_OPTION)
      output = fc.getSelectedFile();
    return output;
  }

}
