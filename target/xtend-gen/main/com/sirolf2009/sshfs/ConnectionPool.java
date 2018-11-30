package com.sirolf2009.sshfs;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class ConnectionPool {
  private final Session session;
  
  private final List<ChannelSftp> connectionPool;
  
  private final List<ChannelSftp> usedConnections;
  
  public ConnectionPool(final Session session) {
    this.session = session;
    this.connectionPool = CollectionLiterals.<ChannelSftp>newArrayList();
    this.usedConnections = CollectionLiterals.<ChannelSftp>newArrayList();
  }
  
  public ChannelSftp getConnection() {
    ChannelSftp _xifexpression = null;
    boolean _isEmpty = this.connectionPool.isEmpty();
    if (_isEmpty) {
      _xifexpression = this.createConnection();
    } else {
      int _size = this.connectionPool.size();
      int _minus = (_size - 1);
      _xifexpression = this.connectionPool.remove(_minus);
    }
    final ChannelSftp connection = _xifexpression;
    this.usedConnections.add(connection);
    return connection;
  }
  
  private ChannelSftp createConnection() {
    try {
      Channel _openChannel = this.session.openChannel("sftp");
      final ChannelSftp channel = ((ChannelSftp) _openChannel);
      channel.connect();
      return channel;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public boolean releaseConnection(final ChannelSftp channel) {
    try {
      if (((channel.isConnected() && (!channel.isClosed())) && (!channel.isEOF()))) {
        this.connectionPool.add(channel);
      }
      return this.usedConnections.remove(channel);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Failed to release channel ");
        _builder.append(channel);
        throw new RuntimeException(_builder.toString(), e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
