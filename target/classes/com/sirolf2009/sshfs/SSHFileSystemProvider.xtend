package com.sirolf2009.sshfs

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.LsEntry
import java.io.IOException
import java.net.URI
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.DirectoryStream.Filter
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.ProviderMismatchException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.Map
import java.util.Set
import java.util.function.Consumer
import java.util.function.Function

class SSHFileSystemProvider extends FileSystemProvider {

	val filesystemsCache = new FileSystemCache()
	val poolMap = new HashMap<SSHFileSystem, ConnectionPool>

	override getScheme() {
		return "ssh"
	}

	override newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		synchronized(filesystemsCache) {
			if(filesystemsCache.containsKey(uri)) {
				throw new FileSystemAlreadyExistsException("A file system already exists for " + uri)
			}
			filesystemsCache.put(uri, new SSHFileSystem(this, uri, env))
			return filesystemsCache.get(uri)
		}
	}

	override checkAccess(Path path, AccessMode... modes) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override copy(Path source, Path target, CopyOption... options) throws IOException {
		copy(source.checkPath(), target.checkPath(), options)
	}

	def copy(SSHPath source, SSHPath target, CopyOption... options) throws IOException {
		val sourceChannel = source.sftp()
		val targetChannel = target.sftp()
		val sourceInput = sourceChannel.get(source.getPath())
		val targetOutput = targetChannel.put(target.getPath())
		try {
			var int n
			val buffer = newByteArrayOfSize(16384)
			while((n = sourceInput.read(buffer)) != -1) {
				targetOutput.write(buffer, 0, n)
			}
		} finally {
			sourceInput.close()
			targetOutput.close()
			sourceChannel.release(source)
			targetChannel.release(target)
		}
	}

	override createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		createDirectory(dir.checkPath(), attrs)
	}

	def createDirectory(SSHPath dir, FileAttribute<?>... attrs) throws IOException {
		dir.sftpc[mkdir(dir.getPath())]
	}

	override delete(Path path) throws IOException {
		delete(path.checkPath())
	}

	def delete(SSHPath path) throws IOException {
		path.sftpc [
			if(Files.isDirectory(path)) {
				rmdir(path.getPath())
			} else {
				rm(path.getPath())
			}
		]
		val sftp = path.sftp()
		if(Files.isDirectory(path)) {
			sftp.rmdir(path.getPath())
		} else {
			sftp.rm(path.getPath())
		}
	}

	override <V extends FileAttributeView> getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override getFileStore(Path path) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override getFileSystem(URI uri) {
		return getFileSystem(uri, false)
	}

	override getPath(URI uri) {
		return getFileSystem(uri, true).getPath(uri.getPath())
	}

	def getFileSystem(URI uri, boolean create) {
		synchronized(filesystemsCache) {
			if(filesystemsCache.containsKey(uri)) {
				return filesystemsCache.get(uri)
			}
			if(create) {
				return newFileSystem(uri, null)
			}
			return null
		}
	}

	override isHidden(Path path) throws IOException {
		return false
	}

	override isSameFile(Path path, Path path2) throws IOException {
		path.checkPath().toString().equals(path2.checkPath().toString())
	}

	override move(Path source, Path target, CopyOption... options) throws IOException {
		move(source.checkPath(), target.checkPath(), options)
	}

	def move(SSHPath source, SSHPath target, CopyOption... options) throws IOException {
		copy(source, target, options)
		delete(source)
	}

	override newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
		return newByteChannel(path.checkPath(), options, attrs)
	}

	def newByteChannel(SSHPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
		val sourceChannel = path.getSession().openChannel("sftp") as ChannelSftp
		sourceChannel.connect()
		val input = sourceChannel.get(path.getPath())
		val buffer = newByteArrayOfSize(16384)
		val content = new ArrayList()
		var int n
		while((n = input.read(buffer)) != -1) {
			content.addAll(buffer.subList(0, n))
		}
		input.close()
		return new SSHSeekableByteChannel(sourceChannel, path.getPath(), content)
	}

	override newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		return newDirectoryStream(dir.checkPath(), filter)
	}

	def DirectoryStream<Path> newDirectoryStream(SSHPath dir, Filter<? super Path> filter) throws IOException {
		try {
			dir.sftp [
				val children = ls(dir.getSSHPath()).map[it as LsEntry].map[new SSHPath(dir.getSSHFileSystem(), dir.getPath().ensureTrailingSlash() + getFilename()) as Path].toList()
				new DirectoryStream<Path>() {
					override iterator() {
						return children.iterator()
					}

					override close() throws IOException {
					}
				}
			]
		} catch(Exception e) {
			throw new RuntimeException('''Failed to open dir stream for «dir» with filters «filter». «dir.sftp().isConnected()» «dir.sftp().isClosed()»  «dir.sftp().isEOF()»''', e)
		}
	}

	override <A extends BasicFileAttributes> readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
		try {
			val spath = path.checkPath()
			val stats = spath.sftp[stat(spath.getSSHPath())]

			return new SSHFileAttributes(stats) as A
		} catch(Exception e) {
			throw new RuntimeException('''Failed to read attributes for «path» of type «type» with options «Arrays.asList(options)»''', e)
		}
	}

	override readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	def private <B> B sftp(SSHPath path, Function<ChannelSftp, B> mapper) {
		val channel = path.sftp()
		try {
			return mapper.apply(channel)
		} finally {
			channel.release(path)
		}
	}

	def private sftpc(SSHPath path, Consumer<ChannelSftp> mapper) {
		val channel = path.sftp()
		try {
			mapper.accept(channel)
		} finally {
			channel.release(path)
		}
	}

	def private sftp(SSHPath path) {
		val pool = getPool(path)
		return pool.getConnection()
	}

	def private release(ChannelSftp channel, SSHPath path) {
		val pool = getPool(path)
		return pool.releaseConnection(channel)
	}

	def private getPool(SSHPath path) {
		if(poolMap.containsKey(path.getFileSystem())) {
			return poolMap.get(path.getFileSystem())
		} else {
			val newPool = new ConnectionPool(path.getSession())
			poolMap.put(path.getFileSystem() as SSHFileSystem, newPool)
			newPool
		}
	}

	def static getSSHPath(SSHPath path) {
		return getSSHPath(path.getPath())
	}

	def static String getSSHPath(String path) {
		while(path.contains("//")) {
			return getSSHPath(path.replace("//", "/"))
		}
		return if(!path.equals("/")) path.ensureNoTrailingSlash() else "/"
	}

	def static String ensureNoTrailingSlash(String path) {
		if(path.endsWith("/")) {
			return path.substring(0, path.length() - 1).ensureNoTrailingSlash()
		}
		return path
	}

	def static String ensureTrailingSlash(String path) {
		if(!path.endsWith("/")) {
			return path + "/"
		}
		return path
	}

	def static checkPath(Path path) {
		if(path === null) {
			throw new NullPointerException()
		} else if(!(path instanceof SSHPath)) {
			throw new ProviderMismatchException()
		} else {
			return path as SSHPath
		}
	}

	static class FileSystemCache extends HashMap<String, SSHFileSystem> {

		def containsKey(URI uri) {
			return containsKey(uri.getHost() + ":" + uri.getPort())
		}

		def get(URI uri) {
			return get(uri.getHost() + ":" + uri.getPort())
		}

		def put(URI uri, SSHFileSystem fileSystem) {
			put(uri.getHost() + ":" + uri.getPort(), fileSystem)
		}

	}

}
