# SSHFS
A Java filesystem backed by SSH

## Download
```xml
<dependency>
	<groupId>com.sirolf2009</groupId>
	<artifactId>sshfs</artifactId>
	<version>0.0.1</version>
</dependency>
```

## Usage
```java
//First, create an uri to connect with
URI uri = URI.create("ssh://root@localhost");
//Then, you can use it as any URI
Files.newDirectoryStream(uri).forEach(file -> System.out.println(file));

//By default, it searches for an id_rsa and a known_hosts in ~/.ssh. If you'd like to change these parameters, you can pass them via a map
Map<String, ?> parameters = new HashMap<>();
parameters.put(SSHFileSystem.CONFIG_KNOWN_HOSTS, "/some/other/known_hosts");
parameters.put(SSHFileSystem.CONFIG_IDENTITY, "/some/other/id_rsa");
URI uri = URI.create("ssh://root@localhost");
FileSystems.newFileSystem(uri, parameters);
```
