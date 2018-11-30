package com.sirolf2009.sshfs

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import java.util.List

class ConnectionPool {

	val Session session
	val List<ChannelSftp> connectionPool
	val List<ChannelSftp> usedConnections

	new(Session session) {
		this.session = session

		connectionPool = newArrayList()
		usedConnections = newArrayList()
	}

	def getConnection() {
		val connection = if(connectionPool.isEmpty()) {
				createConnection()
			} else {
				connectionPool.remove(connectionPool.size() - 1)
			}
		usedConnections.add(connection)
		return connection
	}
	
	def private createConnection() {
		val channel = session.openChannel("sftp") as ChannelSftp
		channel.connect()
		return channel
	}
	
	def releaseConnection(ChannelSftp channel) {
		try {
		if(channel.isConnected() && !channel.isClosed() && !channel.isEOF()) {
			connectionPool.add(channel)
		}
		return usedConnections.remove(channel)
		} catch(Exception e) {
			throw new RuntimeException('''Failed to release channel «channel»''', e)
		}
	}

}
