package com.sirolf2009.sshfs;

import com.jcraft.jsch.ChannelSftp;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.jboss.shrinkwrap.api.nio.file.SeekableInMemoryByteChannel;

@SuppressWarnings("all")
public class SSHSeekableByteChannel extends SeekableInMemoryByteChannel {
  private final ChannelSftp channel;
  
  private final String path;
  
  private final List<Byte> originalContent;
  
  public SSHSeekableByteChannel(final ChannelSftp channel, final String path, final List<Byte> originalContent) {
    try {
      this.channel = channel;
      this.path = path;
      this.originalContent = originalContent;
      if ((originalContent != null)) {
        this.write(ByteBuffer.wrap(((byte[])Conversions.unwrapArray(originalContent, byte.class))));
      }
      this.position(0);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public void close() throws IOException {
    try {
      boolean _equals = this.originalContent.equals(IterableExtensions.<Byte>toList(((Iterable<Byte>)Conversions.doWrapArray(this.getContents()))));
      boolean _not = (!_equals);
      if (_not) {
        final OutputStream out = this.channel.put(this.path);
        out.write(((byte[])Conversions.unwrapArray(this.originalContent, byte.class)));
        out.flush();
        out.close();
        this.channel.disconnect();
      }
      super.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
