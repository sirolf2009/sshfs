package com.sirolf2009.sshfs

import java.nio.file.attribute.BasicFileAttributes
import org.eclipse.xtend.lib.annotations.Data
import java.nio.file.attribute.FileTime
import com.jcraft.jsch.SftpATTRS
import java.util.concurrent.TimeUnit

@Data class SSHFileAttributes implements BasicFileAttributes {
	
	val SftpATTRS attrs
	
	override creationTime() {
		return lastModifiedTime()
	}
	
	override fileKey() {
		return null
	}
	
	override isDirectory() {
		return attrs.isDir()
	}
	
	override isOther() {
		return false
	}
	
	override isRegularFile() {
		return !isDirectory()
	}
	
	override isSymbolicLink() {
		return false
	}
	
	override lastAccessTime() {
		return FileTime.from(attrs.ATime, TimeUnit.SECONDS)
	}
	
	override lastModifiedTime() {
		return FileTime.from(attrs.MTime, TimeUnit.SECONDS)
	}
	
	override size() {
		return attrs.getSize()
	}
	
}