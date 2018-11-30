package com.sirolf2009.sshfs;

import com.jcraft.jsch.SftpATTRS;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

@Data
@SuppressWarnings("all")
public class SSHFileAttributes implements BasicFileAttributes {
  private final SftpATTRS attrs;
  
  @Override
  public FileTime creationTime() {
    return this.lastModifiedTime();
  }
  
  @Override
  public Object fileKey() {
    return null;
  }
  
  @Override
  public boolean isDirectory() {
    return this.attrs.isDir();
  }
  
  @Override
  public boolean isOther() {
    return false;
  }
  
  @Override
  public boolean isRegularFile() {
    boolean _isDirectory = this.isDirectory();
    return (!_isDirectory);
  }
  
  @Override
  public boolean isSymbolicLink() {
    return false;
  }
  
  @Override
  public FileTime lastAccessTime() {
    return FileTime.from(this.attrs.getATime(), TimeUnit.SECONDS);
  }
  
  @Override
  public FileTime lastModifiedTime() {
    return FileTime.from(this.attrs.getMTime(), TimeUnit.SECONDS);
  }
  
  @Override
  public long size() {
    return this.attrs.getSize();
  }
  
  public SSHFileAttributes(final SftpATTRS attrs) {
    super();
    this.attrs = attrs;
  }
  
  @Override
  @Pure
  public int hashCode() {
    return 31 * 1 + ((this.attrs== null) ? 0 : this.attrs.hashCode());
  }
  
  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SSHFileAttributes other = (SSHFileAttributes) obj;
    if (this.attrs == null) {
      if (other.attrs != null)
        return false;
    } else if (!this.attrs.equals(other.attrs))
      return false;
    return true;
  }
  
  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("attrs", this.attrs);
    return b.toString();
  }
  
  @Pure
  public SftpATTRS getAttrs() {
    return this.attrs;
  }
}
