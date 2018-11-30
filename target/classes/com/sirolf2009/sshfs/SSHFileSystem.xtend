package com.sirolf2009.sshfs

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystem
import java.util.Map
import java.util.Optional

import static extension com.sirolf2009.util.MapExtensions.*

class SSHFileSystem extends FileSystem {

	public static val CONFIG_KNOWN_HOSTS = "knownHosts"
	public static val CONFIG_IDENTITY = "identity"

	val SSHFileSystemProvider fileSystemProvider
	val Session session

	new(SSHFileSystemProvider fileSystemProvider, URI uri, Map<String, ?> env) {
		this.fileSystemProvider = fileSystemProvider
		val jsch = new JSch()
		if(env !== null) {
			jsch.setKnownHosts(env.getOptional(CONFIG_KNOWN_HOSTS).map[it as String].orElse(System.getProperty("user.home") + "/.ssh/known_hosts"))
			jsch.addIdentity(env.getOptional(CONFIG_IDENTITY).map[it as String].orElse(System.getProperty("user.home") + "/.ssh/id_rsa"))
		}
		val user = Optional.ofNullable(uri.getUserInfo()).orElse(System.getProperty("user.name"))
		val port = if(uri.getPort() == -1) 22 else uri.getPort()
		session = jsch.getSession(user, uri.getHost(), port)
		session.setConfig("StrictHostKeyChecking", "no")
		session.connect(6000)
	}

	override provider() {
		return fileSystemProvider
	}

	override close() throws IOException {
		session.disconnect()
	}

	override getRootDirectories() {
		return #[new SSHPath(this, "/")]
	}

	override getFileStores() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override getPath(String first, String... more) {
		if(more.size() == 0) {
			return new SSHPath(this, first + "/" + more.join("/"))
		}
	}

	override getPathMatcher(String syntaxAndPattern) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override getSeparator() {
		return "/"
	}

	override getUserPrincipalLookupService() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override isOpen() {
		return session.isConnected()
	}

	override isReadOnly() {
		return false
	}

	override newWatchService() throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override supportedFileAttributeViews() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	def getSession() {
		return session
	}

}
