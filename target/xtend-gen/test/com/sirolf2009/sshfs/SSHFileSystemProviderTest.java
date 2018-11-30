package com.sirolf2009.sshfs;

import com.sirolf2009.sshfs.SSHFileSystemProvider;
import com.sirolf2009.sshfs.SSHPath;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class SSHFileSystemProviderTest {
  @Test
  public void testCreatePath() {
    Path _get = Paths.get(URI.create("ssh://root@localhost/playground"));
    Assert.assertEquals("/playground/", ((SSHPath) _get).getPath());
    Path _get_1 = Paths.get(URI.create("ssh://root@localhost"));
    Assert.assertEquals("/", ((SSHPath) _get_1).getPath());
  }
  
  @Test
  public void testDirectoryStream() {
    try {
      final Path path = Paths.get(URI.create("ssh://root@localhost"));
      final DirectoryStream<Path> stream = Files.newDirectoryStream(path);
      final Function1<Path, SSHPath> _function = new Function1<Path, SSHPath>() {
        @Override
        public SSHPath apply(final Path it) {
          return ((SSHPath) it);
        }
      };
      final Consumer<SSHPath> _function_1 = new Consumer<SSHPath>() {
        @Override
        public void accept(final SSHPath it) {
          boolean _isRegularFile = Files.isRegularFile(it);
          if (_isRegularFile) {
            String _string = it.toString();
            String _plus = ("File: " + _string);
            InputOutput.<String>println(_plus);
          } else {
            String _string_1 = it.toString();
            String _plus_1 = ("Folder: " + _string_1);
            String _plus_2 = (_plus_1 + "/");
            InputOutput.<String>println(_plus_2);
          }
        }
      };
      IterableExtensions.<Path, SSHPath>map(stream, _function).forEach(_function_1);
      stream.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void getZooKeeperPath() {
    Assert.assertEquals("/", SSHFileSystemProvider.getSSHPath("/"));
    Assert.assertEquals("/", SSHFileSystemProvider.getSSHPath("//"));
    Assert.assertEquals("/cluster", SSHFileSystemProvider.getSSHPath("//cluster"));
  }
  
  @Test
  public void ensureNoTrailingSlash() {
    Assert.assertEquals("/playground", SSHFileSystemProvider.ensureNoTrailingSlash("/playground"));
    Assert.assertEquals("/playground", SSHFileSystemProvider.ensureNoTrailingSlash("/playground/"));
    Assert.assertEquals("/playground", SSHFileSystemProvider.ensureNoTrailingSlash("/playground//"));
  }
}
