package js.guiapp;

import java.io.*;
import java.util.List;

import js.file.Files;

import static js.base.Tools.*;

public class PathFilter extends javax.swing.filechooser.FileFilter implements FilenameFilter {

  public PathFilter(FilenameFilter f) {
    mFilter = f;
  }

  public PathFilter(String... extensions) {
    for (String ext : extensions)
      mExt.add(ext);
  }

  /**
   * Accept file?
   * 
   * @param dir
   *          File, or null
   * @param name
   *          String
   * @return boolean
   */
  public boolean accept(File dir, String name) {
    if (mFilter != null)
      return mFilter.accept(dir, name);

    File f = new File(name);

    boolean flag;
    if (f.isDirectory()) {
      flag = true;
    } else
      flag = accept(name);
    return flag;
  }

  public boolean accept(File file) {
    if (file.isDirectory())
      return true;
    return accept(file.getPath());
  }

  private boolean accept(String path) {
    String e = Files.getExtension(path);
    return mExt.contains(e);
  }

  @Override
  public String getDescription() {
    String ret = null;
    if (!mExt.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      int index = INIT_INDEX;
      for (String ext : mExt) {
        index++;
        if (index > 0)
          sb.append(", ");
        sb.append("*.");
        sb.append(ext);
      }
      ret = sb.toString();
    } else
      ret = "*** override PathFilter: getDescription ***";
    return ret;
  }

  /**
   * Construct a PathFilter from a FilenameFilter. If FilenameFilter is already
   * a PathFilter, just returns it.
   * 
   * @param filter
   *          : FilenameFilter
   * @return PathFilter
   */
  public static PathFilter construct(FilenameFilter filter) {
    PathFilter ret = null;

    if (filter instanceof PathFilter)
      ret = (PathFilter) filter;
    else
      ret = new PathFilter(filter);
    return ret;
  }

  private List<String> mExt = arrayList();

  private FilenameFilter mFilter;

}
