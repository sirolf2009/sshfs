package com.sirolf2009.sshfs

import com.jcraft.jsch.ChannelSftp
import java.io.IOException
import java.nio.ByteBuffer
import java.util.List
import org.jboss.shrinkwrap.api.nio.file.SeekableInMemoryByteChannel

class SSHSeekableByteChannel extends SeekableInMemoryByteChannel {
	
	val ChannelSftp channel
	val String path
	val List<Byte> originalContent
	
	new(ChannelSftp channel, String path, List<Byte> originalContent) {
		this.channel = channel
		this.path = path
		this.originalContent = originalContent
		if(originalContent !== null) {
			write(ByteBuffer.wrap(originalContent))
		}
		position(0)
	}
	
	override close() throws IOException {
		if(!originalContent.equals(getContents().toList())) {
			val out = channel.put(path)
			out.write(originalContent)
			out.flush()
			out.close()
			channel.disconnect()
		}
		super.close()
	}

}
