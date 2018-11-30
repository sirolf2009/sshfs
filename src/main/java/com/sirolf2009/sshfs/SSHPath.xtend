package com.sirolf2009.sshfs

import java.io.IOException
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.ProviderMismatchException
import java.nio.file.WatchEvent.Kind
import java.nio.file.WatchEvent.Modifier
import java.nio.file.WatchService
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

@Accessors class SSHPath implements Path {

	val SSHFileSystem fileSystem
	val String path
	val List<String> explodedPath

	new(SSHFileSystem fileSystem, String path) {
		this.fileSystem = fileSystem
		this.path = path
		this.explodedPath = path.split("/")
	}
	
	override compareTo(Path other) {
		return path.compareTo(other.checkPath().path)
	}

	override endsWith(Path other) {
		return path.endsWith(other.checkPath().path)
	}

	override endsWith(String other) {
		return path.endsWith(other)
	}

	override getFileName() {
		return new SSHPath(fileSystem, explodedPath.last())
	}

	override getFileSystem() {
		return fileSystem
	}

	override getName(int index) {
		return new SSHPath(fileSystem, (0 ..< index).map[explodedPath.get(it)].join("/"))
	}

	override getNameCount() {
		return explodedPath.size()
	}

	override getParent() {
		if(explodedPath.size() > 1) {
			return new SSHPath(fileSystem, (0 ..< getNameCount() - 1).map[explodedPath.get(it)].join("/"))
		} else {
			return null
		}
	}

	override getRoot() {
		return new SSHPath(fileSystem, "/")
	}

	override isAbsolute() {
		return path.startsWith("/")
	}

	override iterator() {
		(0 ..< getNameCount()).map[getName(it)].iterator()
	}

	override normalize() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override register(WatchService watcher, Kind<?>... events) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override relativize(Path other) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override resolve(Path other) {
		return resolve(other.checkPath())
	}

	def resolve(SSHPath other) {
		if(other.isAbsolute()) {
			return other
		}
		return new SSHPath(fileSystem, if(path.endsWith("/")) path + other.path else path + "/" + other.path)
	}

	override resolve(String other) {
		return resolve(fileSystem.getPath(other))
	}

	override resolveSibling(Path other) {
		val parent = getParent()
		if(parent === null) {
			return other
		} else {
			return parent.resolve(other)
		}
	}

	override resolveSibling(String other) {
		resolveSibling(fileSystem.getPath(other))
	}

	override startsWith(Path other) {
		return path.startsWith(other.checkPath().path)
	}

	override startsWith(String other) {
		return path.startsWith(other)
	}

	override subpath(int beginIndex, int endIndex) {
		return new SSHPath(fileSystem, explodedPath.subList(beginIndex, endIndex).join("/"))
	}

	override toAbsolutePath() {
		if(isAbsolute()) {
			return this
		}
		return new SSHPath(fileSystem, "/" + explodedPath.join("/"))
	}

	override toFile() {
		throw new UnsupportedOperationException()
	}

	override toRealPath(LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override toUri() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override toString() {
		return path
	}
	
	def getSSHFileSystem() {
		return fileSystem
	}
	
	def getSession() {
		return getSSHFileSystem().getSession()
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

}
