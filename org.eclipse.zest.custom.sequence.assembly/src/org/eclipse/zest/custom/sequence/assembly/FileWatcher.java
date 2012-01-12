package org.eclipse.zest.custom.sequence.assembly;

import java.util.*;
import java.io.*;

public abstract class FileWatcher extends TimerTask {
  private long timeStamp;
  private File file;
  
  public FileWatcher( File file ) {
    this.file = file;
    this.timeStamp = file.lastModified();
  }

  public final void run() {
	long timeStamp = file.lastModified();
	
    if( this.timeStamp != timeStamp ) {
      this.timeStamp = timeStamp;
      onChange(file);
    }
  }

  protected abstract void onChange( File file );
}