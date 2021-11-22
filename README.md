# WeTell
Encrypted Messenger

# Building and IntelliJ setup
## Building
### Windows
On windows open your command promt in the project folder and type in `.\gradlew.bat jpackage`
You need to have "WiX" and Java 15 installed.
### Linux/macOS
For Linux and macOS you do the same only with `.\gradlew jpackage`. Java 15 is required.

## IntelliJ setup
Download and install Gradle 7.1.1. In IntelliJ open a new project with `Get from VCS`. [Specify this repository](https://github.com/Werechang/WeTell) in the URL field.
It may import the project with some errors. Go to `File > Project Structure > Project` and choose JDK 15.

If it still does not build, follow these instructions:
Go to `File > Settings > Build, Execution, Deployment > Build Tools > Gradle` and choose `Specified Location` in the `Use Gradle from:` field. Click `Apply`. 
The project should reload and you should be able to execute `WeTellClient.main()` or `WeTellServer.main()`

For school PCs with minimum storage per account: Choose the desktop folder in the `Gradle user home:` field in `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`. You would have to redownload these files on every sign up.
