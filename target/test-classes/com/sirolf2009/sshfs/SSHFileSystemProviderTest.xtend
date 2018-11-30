package com.sirolf2009.sshfs

import java.net.URI
import java.nio.file.Paths
import org.junit.Test
import java.nio.file.Files
import org.junit.Assert

class SSHFileSystemProviderTest {

	@Test
	def void testCreatePath() {
		Assert.assertEquals("/playground/", ((Paths.get(URI.create("ssh://root@localhost/playground")) as SSHPath).getPath()))
		Assert.assertEquals("/", ((Paths.get(URI.create("ssh://root@localhost")) as SSHPath).getPath()))
	}

	@Test
	def void testDirectoryStream() {
		val path = Paths.get(URI.create("ssh://root@localhost"))
		val stream = Files.newDirectoryStream(path)
		stream.map[it as SSHPath].forEach [
			if(Files.isRegularFile(it)) {
				println("File: " + toString())
			} else {
				println("Folder: " + toString() + "/")
			}
		]
		stream.close()
	}

	@Test
	def void getZooKeeperPath() {
		Assert.assertEquals("/", SSHFileSystemProvider.getSSHPath("/"))
		Assert.assertEquals("/", SSHFileSystemProvider.getSSHPath("//"))
		Assert.assertEquals("/cluster", SSHFileSystemProvider.getSSHPath("//cluster"))
	}

	@Test
	def void ensureNoTrailingSlash() {
		Assert.assertEquals("/playground", SSHFileSystemProvider.ensureNoTrailingSlash("/playground"))
		Assert.assertEquals("/playground", SSHFileSystemProvider.ensureNoTrailingSlash("/playground/"))
		Assert.assertEquals("/playground", SSHFileSystemProvider.ensureNoTrailingSlash("/playground//"))
	}

}
