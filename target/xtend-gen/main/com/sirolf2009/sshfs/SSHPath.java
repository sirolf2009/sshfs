package com.sirolf2009.sshfs;

import com.jcraft.jsch.Session;
import com.sirolf2009.sshfs.SSHFileSystem;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.List;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

@Accessors
@SuppressWarnings("all")
public class SSHPath implements Path {
  private final SSHFileSystem fileSystem;
  
  private final String path;
  
  private final List<String> explodedPath;
  
  public SSHPath(final SSHFileSystem fileSystem, final String path) {
    this.fileSystem = fileSystem;
    this.path = path;
    this.explodedPath = ((List<String>)Conversions.doWrapArray(path.split("/")));
  }
  
  @Override
  public int compareTo(final Path other) {
    return this.path.compareTo(SSHPath.checkPath(other).path);
  }
  
  @Override
  public boolean endsWith(final Path other) {
    return this.path.endsWith(SSHPath.checkPath(other).path);
  }
  
  @Override
  public boolean endsWith(final String other) {
    return this.path.endsWith(other);
  }
  
  @Override
  public Path getFileName() {
    String _last = IterableExtensions.<String>last(this.explodedPath);
    return new SSHPath(this.fileSystem, _last);
  }
  
  @Override
  public FileSystem getFileSystem() {
    return this.fileSystem;
  }
  
  @Override
  public Path getName(final int index) {
    final Function1<Integer, String> _function = new Function1<Integer, String>() {
      @Override
      public String apply(final Integer it) {
        return SSHPath.this.explodedPath.get((it).intValue());
      }
    };
    String _join = IterableExtensions.join(IterableExtensions.<Integer, String>map(new ExclusiveRange(0, index, true), _function), "/");
    return new SSHPath(this.fileSystem, _join);
  }
  
  @Override
  public int getNameCount() {
    return this.explodedPath.size();
  }
  
  @Override
  public Path getParent() {
    int _size = this.explodedPath.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      int _nameCount = this.getNameCount();
      int _minus = (_nameCount - 1);
      final Function1<Integer, String> _function = new Function1<Integer, String>() {
        @Override
        public String apply(final Integer it) {
          return SSHPath.this.explodedPath.get((it).intValue());
        }
      };
      String _join = IterableExtensions.join(IterableExtensions.<Integer, String>map(new ExclusiveRange(0, _minus, true), _function), "/");
      return new SSHPath(this.fileSystem, _join);
    } else {
      return null;
    }
  }
  
  @Override
  public Path getRoot() {
    return new SSHPath(this.fileSystem, "/");
  }
  
  @Override
  public boolean isAbsolute() {
    return this.path.startsWith("/");
  }
  
  @Override
  public Iterator<Path> iterator() {
    int _nameCount = this.getNameCount();
    final Function1<Integer, Path> _function = new Function1<Integer, Path>() {
      @Override
      public Path apply(final Integer it) {
        return SSHPath.this.getName((it).intValue());
      }
    };
    return IterableExtensions.<Integer, Path>map(new ExclusiveRange(0, _nameCount, true), _function).iterator();
  }
  
  @Override
  public Path normalize() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events, final WatchEvent.Modifier... modifiers) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public Path relativize(final Path other) {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public Path resolve(final Path other) {
    return this.resolve(SSHPath.checkPath(other));
  }
  
  public SSHPath resolve(final SSHPath other) {
    boolean _isAbsolute = other.isAbsolute();
    if (_isAbsolute) {
      return other;
    }
    String _xifexpression = null;
    boolean _endsWith = this.path.endsWith("/");
    if (_endsWith) {
      _xifexpression = (this.path + other.path);
    } else {
      _xifexpression = ((this.path + "/") + other.path);
    }
    return new SSHPath(this.fileSystem, _xifexpression);
  }
  
  @Override
  public Path resolve(final String other) {
    return this.resolve(this.fileSystem.getPath(other));
  }
  
  @Override
  public Path resolveSibling(final Path other) {
    final Path parent = this.getParent();
    if ((parent == null)) {
      return other;
    } else {
      return parent.resolve(other);
    }
  }
  
  @Override
  public Path resolveSibling(final String other) {
    return this.resolveSibling(this.fileSystem.getPath(other));
  }
  
  @Override
  public boolean startsWith(final Path other) {
    return this.path.startsWith(SSHPath.checkPath(other).path);
  }
  
  @Override
  public boolean startsWith(final String other) {
    return this.path.startsWith(other);
  }
  
  @Override
  public Path subpath(final int beginIndex, final int endIndex) {
    String _join = IterableExtensions.join(this.explodedPath.subList(beginIndex, endIndex), "/");
    return new SSHPath(this.fileSystem, _join);
  }
  
  @Override
  public Path toAbsolutePath() {
    boolean _isAbsolute = this.isAbsolute();
    if (_isAbsolute) {
      return this;
    }
    String _join = IterableExtensions.join(this.explodedPath, "/");
    String _plus = ("/" + _join);
    return new SSHPath(this.fileSystem, _plus);
  }
  
  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Path toRealPath(final LinkOption... options) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public URI toUri() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public String toString() {
    return this.path;
  }
  
  public SSHFileSystem getSSHFileSystem() {
    return this.fileSystem;
  }
  
  public Session getSession() {
    return this.getSSHFileSystem().getSession();
  }
  
  public static SSHPath checkPath(final Path path) {
    if ((path == null)) {
      throw new NullPointerException();
    } else {
      if ((!(path instanceof SSHPath))) {
        throw new ProviderMismatchException();
      } else {
        return ((SSHPath) path);
      }
    }
  }
  
  @Pure
  public String getPath() {
    return this.path;
  }
  
  @Pure
  public List<String> getExplodedPath() {
    return this.explodedPath;
  }
}
