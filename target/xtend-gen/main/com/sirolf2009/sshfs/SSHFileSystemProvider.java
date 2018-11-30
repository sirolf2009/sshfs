package com.sirolf2009.sshfs;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.sirolf2009.sshfs.ConnectionPool;
import com.sirolf2009.sshfs.SSHFileAttributes;
import com.sirolf2009.sshfs.SSHFileSystem;
import com.sirolf2009.sshfs.SSHPath;
import com.sirolf2009.sshfs.SSHSeekableByteChannel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class SSHFileSystemProvider extends FileSystemProvider {
  public static class FileSystemCache extends HashMap<String, SSHFileSystem> {
    public boolean containsKey(final URI uri) {
      String _host = uri.getHost();
      String _plus = (_host + ":");
      int _port = uri.getPort();
      String _plus_1 = (_plus + Integer.valueOf(_port));
      return this.containsKey(_plus_1);
    }
    
    public SSHFileSystem get(final URI uri) {
      String _host = uri.getHost();
      String _plus = (_host + ":");
      int _port = uri.getPort();
      String _plus_1 = (_plus + Integer.valueOf(_port));
      return this.get(_plus_1);
    }
    
    public SSHFileSystem put(final URI uri, final SSHFileSystem fileSystem) {
      String _host = uri.getHost();
      String _plus = (_host + ":");
      int _port = uri.getPort();
      String _plus_1 = (_plus + Integer.valueOf(_port));
      return this.put(_plus_1, fileSystem);
    }
  }
  
  private final SSHFileSystemProvider.FileSystemCache filesystemsCache = new SSHFileSystemProvider.FileSystemCache();
  
  private final HashMap<SSHFileSystem, ConnectionPool> poolMap = new HashMap<SSHFileSystem, ConnectionPool>();
  
  @Override
  public String getScheme() {
    return "ssh";
  }
  
  @Override
  public FileSystem newFileSystem(final URI uri, final Map<String, ?> env) throws IOException {
    synchronized (this.filesystemsCache) {
      boolean _containsKey = this.filesystemsCache.containsKey(uri);
      if (_containsKey) {
        throw new FileSystemAlreadyExistsException(("A file system already exists for " + uri));
      }
      SSHFileSystem _sSHFileSystem = new SSHFileSystem(this, uri, env);
      this.filesystemsCache.put(uri, _sSHFileSystem);
      return this.filesystemsCache.get(uri);
    }
  }
  
  @Override
  public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
    this.copy(SSHFileSystemProvider.checkPath(source), SSHFileSystemProvider.checkPath(target), options);
  }
  
  public void copy(final SSHPath source, final SSHPath target, final CopyOption... options) throws IOException {
    try {
      final ChannelSftp sourceChannel = this.sftp(source);
      final ChannelSftp targetChannel = this.sftp(target);
      final InputStream sourceInput = sourceChannel.get(source.getPath());
      final OutputStream targetOutput = targetChannel.put(target.getPath());
      try {
        int n = 0;
        final byte[] buffer = new byte[16384];
        while (((n = sourceInput.read(buffer)) != (-1))) {
          targetOutput.write(buffer, 0, n);
        }
      } finally {
        sourceInput.close();
        targetOutput.close();
        this.release(sourceChannel, source);
        this.release(targetChannel, target);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public void createDirectory(final Path dir, final FileAttribute<?>... attrs) throws IOException {
    this.createDirectory(SSHFileSystemProvider.checkPath(dir), attrs);
  }
  
  public void createDirectory(final SSHPath dir, final FileAttribute<?>... attrs) throws IOException {
    final Consumer<ChannelSftp> _function = new Consumer<ChannelSftp>() {
      @Override
      public void accept(final ChannelSftp it) {
        try {
          it.mkdir(dir.getPath());
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    this.sftpc(dir, _function);
  }
  
  @Override
  public void delete(final Path path) throws IOException {
    this.delete(SSHFileSystemProvider.checkPath(path));
  }
  
  public void delete(final SSHPath path) throws IOException {
    try {
      final Consumer<ChannelSftp> _function = new Consumer<ChannelSftp>() {
        @Override
        public void accept(final ChannelSftp it) {
          try {
            boolean _isDirectory = Files.isDirectory(path);
            if (_isDirectory) {
              it.rmdir(path.getPath());
            } else {
              it.rm(path.getPath());
            }
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.sftpc(path, _function);
      final ChannelSftp sftp = this.sftp(path);
      boolean _isDirectory = Files.isDirectory(path);
      if (_isDirectory) {
        sftp.rmdir(path.getPath());
      } else {
        sftp.rm(path.getPath());
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public <V extends FileAttributeView> V getFileAttributeView(final Path path, final Class<V> type, final LinkOption... options) {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public FileStore getFileStore(final Path path) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public FileSystem getFileSystem(final URI uri) {
    return this.getFileSystem(uri, false);
  }
  
  @Override
  public Path getPath(final URI uri) {
    return this.getFileSystem(uri, true).getPath(uri.getPath());
  }
  
  public FileSystem getFileSystem(final URI uri, final boolean create) {
    try {
      synchronized (this.filesystemsCache) {
        boolean _containsKey = this.filesystemsCache.containsKey(uri);
        if (_containsKey) {
          return this.filesystemsCache.get(uri);
        }
        if (create) {
          return this.newFileSystem(uri, null);
        }
        return null;
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public boolean isHidden(final Path path) throws IOException {
    return false;
  }
  
  @Override
  public boolean isSameFile(final Path path, final Path path2) throws IOException {
    return SSHFileSystemProvider.checkPath(path).toString().equals(SSHFileSystemProvider.checkPath(path2).toString());
  }
  
  @Override
  public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
    this.move(SSHFileSystemProvider.checkPath(source), SSHFileSystemProvider.checkPath(target), options);
  }
  
  public void move(final SSHPath source, final SSHPath target, final CopyOption... options) throws IOException {
    this.copy(source, target, options);
    this.delete(source);
  }
  
  @Override
  public SeekableByteChannel newByteChannel(final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) throws IOException {
    return this.newByteChannel(SSHFileSystemProvider.checkPath(path), options, attrs);
  }
  
  public SSHSeekableByteChannel newByteChannel(final SSHPath path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) throws IOException {
    try {
      Channel _openChannel = path.getSession().openChannel("sftp");
      final ChannelSftp sourceChannel = ((ChannelSftp) _openChannel);
      sourceChannel.connect();
      final InputStream input = sourceChannel.get(path.getPath());
      final byte[] buffer = new byte[16384];
      final ArrayList<Byte> content = new ArrayList<Byte>();
      int n = 0;
      while (((n = input.read(buffer)) != (-1))) {
        content.addAll(((List<Byte>)Conversions.doWrapArray(buffer)).subList(0, n));
      }
      input.close();
      String _path = path.getPath();
      return new SSHSeekableByteChannel(sourceChannel, _path, content);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public DirectoryStream<Path> newDirectoryStream(final Path dir, final DirectoryStream.Filter<? super Path> filter) throws IOException {
    return this.newDirectoryStream(SSHFileSystemProvider.checkPath(dir), filter);
  }
  
  public DirectoryStream<Path> newDirectoryStream(final SSHPath dir, final DirectoryStream.Filter<? super Path> filter) throws IOException {
    DirectoryStream<Path> _xtrycatchfinallyexpression = null;
    try {
      final Function<ChannelSftp, DirectoryStream<Path>> _function = new Function<ChannelSftp, DirectoryStream<Path>>() {
        @Override
        public DirectoryStream<Path> apply(final ChannelSftp it) {
          try {
            DirectoryStream<Path> _xblockexpression = null;
            {
              final Function1<Object, ChannelSftp.LsEntry> _function = new Function1<Object, ChannelSftp.LsEntry>() {
                @Override
                public ChannelSftp.LsEntry apply(final Object it) {
                  return ((ChannelSftp.LsEntry) it);
                }
              };
              final Function1<ChannelSftp.LsEntry, Path> _function_1 = new Function1<ChannelSftp.LsEntry, Path>() {
                @Override
                public Path apply(final ChannelSftp.LsEntry it) {
                  SSHFileSystem _sSHFileSystem = dir.getSSHFileSystem();
                  String _ensureTrailingSlash = SSHFileSystemProvider.ensureTrailingSlash(dir.getPath());
                  String _filename = it.getFilename();
                  String _plus = (_ensureTrailingSlash + _filename);
                  SSHPath _sSHPath = new SSHPath(_sSHFileSystem, _plus);
                  return ((Path) _sSHPath);
                }
              };
              final List<Path> children = IterableExtensions.<Path>toList(ListExtensions.<ChannelSftp.LsEntry, Path>map(ListExtensions.<Object, ChannelSftp.LsEntry>map(it.ls(SSHFileSystemProvider.getSSHPath(dir)), _function), _function_1));
              _xblockexpression = new DirectoryStream<Path>() {
                @Override
                public Iterator<Path> iterator() {
                  return children.iterator();
                }
                
                @Override
                public void close() throws IOException {
                }
              };
            }
            return _xblockexpression;
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      _xtrycatchfinallyexpression = this.<DirectoryStream<Path>>sftp(dir, _function);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Failed to open dir stream for ");
        _builder.append(dir);
        _builder.append(" with filters ");
        _builder.append(filter);
        _builder.append(". ");
        boolean _isConnected = this.sftp(dir).isConnected();
        _builder.append(_isConnected);
        _builder.append(" ");
        boolean _isClosed = this.sftp(dir).isClosed();
        _builder.append(_isClosed);
        _builder.append("  ");
        boolean _isEOF = this.sftp(dir).isEOF();
        _builder.append(_isEOF);
        throw new RuntimeException(_builder.toString(), e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }
  
  @Override
  public <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options) throws IOException {
    try {
      final SSHPath spath = SSHFileSystemProvider.checkPath(path);
      final Function<ChannelSftp, SftpATTRS> _function = new Function<ChannelSftp, SftpATTRS>() {
        @Override
        public SftpATTRS apply(final ChannelSftp it) {
          try {
            return it.stat(SSHFileSystemProvider.getSSHPath(spath));
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      final SftpATTRS stats = this.<SftpATTRS>sftp(spath, _function);
      SSHFileAttributes _sSHFileAttributes = new SSHFileAttributes(stats);
      return ((A) _sSHFileAttributes);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Failed to read attributes for ");
        _builder.append(path);
        _builder.append(" of type ");
        _builder.append(type);
        _builder.append(" with options ");
        List<LinkOption> _asList = Arrays.<LinkOption>asList(options);
        _builder.append(_asList);
        throw new RuntimeException(_builder.toString(), e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Override
  public Map<String, Object> readAttributes(final Path path, final String attributes, final LinkOption... options) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options) throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  private <B extends Object> B sftp(final SSHPath path, final Function<ChannelSftp, B> mapper) {
    final ChannelSftp channel = this.sftp(path);
    try {
      return mapper.apply(channel);
    } finally {
      this.release(channel, path);
    }
  }
  
  private void sftpc(final SSHPath path, final Consumer<ChannelSftp> mapper) {
    final ChannelSftp channel = this.sftp(path);
    try {
      mapper.accept(channel);
    } finally {
      this.release(channel, path);
    }
  }
  
  private ChannelSftp sftp(final SSHPath path) {
    final ConnectionPool pool = this.getPool(path);
    return pool.getConnection();
  }
  
  private boolean release(final ChannelSftp channel, final SSHPath path) {
    final ConnectionPool pool = this.getPool(path);
    return pool.releaseConnection(channel);
  }
  
  private ConnectionPool getPool(final SSHPath path) {
    ConnectionPool _xifexpression = null;
    boolean _containsKey = this.poolMap.containsKey(path.getFileSystem());
    if (_containsKey) {
      return this.poolMap.get(path.getFileSystem());
    } else {
      ConnectionPool _xblockexpression = null;
      {
        Session _session = path.getSession();
        final ConnectionPool newPool = new ConnectionPool(_session);
        FileSystem _fileSystem = path.getFileSystem();
        this.poolMap.put(((SSHFileSystem) _fileSystem), newPool);
        _xblockexpression = newPool;
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  public static String getSSHPath(final SSHPath path) {
    return SSHFileSystemProvider.getSSHPath(path.getPath());
  }
  
  public static String getSSHPath(final String path) {
    while (path.contains("//")) {
      return SSHFileSystemProvider.getSSHPath(path.replace("//", "/"));
    }
    String _xifexpression = null;
    boolean _equals = path.equals("/");
    boolean _not = (!_equals);
    if (_not) {
      _xifexpression = SSHFileSystemProvider.ensureNoTrailingSlash(path);
    } else {
      _xifexpression = "/";
    }
    return _xifexpression;
  }
  
  public static String ensureNoTrailingSlash(final String path) {
    boolean _endsWith = path.endsWith("/");
    if (_endsWith) {
      int _length = path.length();
      int _minus = (_length - 1);
      return SSHFileSystemProvider.ensureNoTrailingSlash(path.substring(0, _minus));
    }
    return path;
  }
  
  public static String ensureTrailingSlash(final String path) {
    boolean _endsWith = path.endsWith("/");
    boolean _not = (!_endsWith);
    if (_not) {
      return (path + "/");
    }
    return path;
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
}
