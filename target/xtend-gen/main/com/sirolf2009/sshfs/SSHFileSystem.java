package com.sirolf2009.sshfs;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.sirolf2009.sshfs.SSHFileSystemProvider;
import com.sirolf2009.sshfs.SSHPath;
import com.sirolf2009.util.MapExtensions;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class SSHFileSystem extends FileSystem {
  public final static String CONFIG_KNOWN_HOSTS = "knownHosts";
  
  public final static String CONFIG_IDENTITY = "identity";
  
  private final SSHFileSystemProvider fileSystemProvider;
  
  private final Session session;
  
  public SSHFileSystem(final SSHFileSystemProvider fileSystemProvider, final URI uri, final Map<String, ?> env) {
    try {
      this.fileSystemProvider = fileSystemProvider;
      final JSch jsch = new JSch();
      if ((env != null)) {
        final Function<Object, String> _function = (Object it) -> {
          return ((String) it);
        };
        Optional<String> _map = MapExtensions.getOptional(env, SSHFileSystem.CONFIG_KNOWN_HOSTS).<String>map(_function);
        String _property = System.getProperty("user.home");
        String _plus = (_property + "/.ssh/known_hosts");
        jsch.setKnownHosts(_map.orElse(_plus));
        final Function<Object, String> _function_1 = (Object it) -> {
          return ((String) it);
        };
        Optional<String> _map_1 = MapExtensions.getOptional(env, SSHFileSystem.CONFIG_IDENTITY).<String>map(_function_1);
        String _property_1 = System.getProperty("user.home");
        String _plus_1 = (_property_1 + "/.ssh/id_rsa");
        jsch.addIdentity(_map_1.orElse(_plus_1));
      }
      final String user = Optional.<String>ofNullable(uri.getUserInfo()).orElse(System.getProperty("user.name"));
      int _xifexpression = (int) 0;
      int _port = uri.getPort();
      boolean _equals = (_port == (-1));
      if (_equals) {
        _xifexpression = 22;
      } else {
        _xifexpression = uri.getPort();
      }
      final int port = _xifexpression;
      this.session = jsch.getSession(user, uri.getHost(), port);
      this.session.setConfig("StrictHostKeyChecking", "no");
      this.session.connect(6000);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public FileSystemProvider provider() {
    return this.fileSystemProvider;
  }
  
  @Override
  public void close() throws IOException {
    this.session.disconnect();
  }
  
  @Override
  public Iterable<Path> getRootDirectories() {
    SSHPath _sSHPath = new SSHPath(this, "/");
    return Collections.<Path>unmodifiableList(CollectionLiterals.<Path>newArrayList(_sSHPath));
  }
  
  @Override
  public Iterable<FileStore> getFileStores() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public Path getPath(final String first, final String... more) {
    int _size = ((List<String>)Conversions.doWrapArray(more)).size();
    boolean _equals = (_size == 0);
    if (_equals) {
      String _join = IterableExtensions.join(((Iterable<?>)Conversions.doWrapArray(more)), "/");
      String _plus = ((first + "/") + _join);
      return new SSHPath(this, _plus);
    }
    return null;
  }
  
  @Override
  public PathMatcher getPathMatcher(final String syntaxAndPattern) {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public String getSeparator() {
    return "/";
  }
  
  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public boolean isOpen() {
    return this.session.isConnected();
  }
  
  @Override
  public boolean isReadOnly() {
    return false;
  }
  
  @Override
  public WatchService newWatchService() throws IOException {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  @Override
  public Set<String> supportedFileAttributeViews() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  public Session getSession() {
    return this.session;
  }
}
