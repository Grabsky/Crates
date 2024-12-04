# Crates
[![Latest Release](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.github.com%2Frepos%2FGrabsky%2FCrates%2Freleases%2Flatest&query=tag_name&logo=gradle&style=for-the-badge&label=%20&labelColor=%231C2128&color=%23454F5A)](https://github.com/Grabsky/Crates/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Grabsky/Crates/gradle.yml?style=for-the-badge&logo=github&logoColor=white&label=%20)](https://github.com/Grabsky/Crates/actions)
[![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/Grabsky/Crates/main?style=for-the-badge&logo=codefactor&logoColor=white&label=%20)](https://www.codefactor.io/repository/github/grabsky/crates)

Yet another crates plugin, but this one is relatively simple. Maintained for use on servers I develop for and no public release is planned as of now.

<br />

> [!IMPORTANT]
> Default configuration and translations are in Polish language, making the plugin not suitable for external use. This is something I may work on in the future, but no promises. Contributions are always welcome.

<br />

## Requirements
Requires **Java 21** (or higher) and **Paper 1.21.3** (or higher).

<br />

## Building
Some dependencies use **[GitHub Gradle Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)** and thus may require extra configuration steps for the project to build properly.

```shell
# Cloning the repository.
$ git clone https://github.com/Grabsky/Crates.git
# Entering the cloned repository.
$ cd ./Crates
# Compiling and building artifacts.
$ gradlew shadowJar
```

<br />

## Contributing
This project is open for contributions. Any kind of help is greatly appreciated.