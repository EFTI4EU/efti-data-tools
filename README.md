> [!IMPORTANT]
> Development of this library has been moved to
> [https://github.com/fintraffic-efti/efti-data-tools](https://github.com/fintraffic-efti/efti-data-tools).
> 
> Please follow [fintraffic-efti/efti-data-tools/releases](https://github.com/fintraffic-efti/efti-data-tools/releases)
> for new releases and update you Maven repository configuration following the instructions at
> [fintraffic-efti/efti-data-tools#usage](https://github.com/fintraffic-efti/efti-data-tools#usage).

# efti-data-tools

Java libraries and command line tool for filtering subsets and generating random xml documents of eFTI consignment schemas
as defined at [reference-implementation](https://github.com/EFTI4EU/reference-implementation/tree/main/schema/xsd).

These tools are not part of the eFTI4EU reference implementation but may be used in implementing, development and testing
of eFTI applications.

Requires Java 17 or later.

## Usage

This project releases libraries and a command line application.

### Libraries

There are two libraries:

 1. schema - Tools for subset filtering and other xml utilities
 2. populate - Tools for populating pseudo-random consignment documents

Libraries are published to the Maven repository under this GitHub project at
[mvn-repo branch](https://raw.githubusercontent.com/EFTI4EU/efti-data-tools/mvn-repo/README.md). To use them in your 
Maven/Gradle project:

 1. In your project configuration, add a Maven repository at url `https://github.com/EFTI4EU/efti-data-tools/raw/mvn-repo`:
    * Gradle example:
      ```
      repositories {
        maven("https://github.com/EFTI4EU/efti-data-tools/raw/mvn-repo")
        mavenCentral()
      }
      ```
    * Maven example:
      ```
      <repository>
        <id>efti-data-tools</id>
        <name>efti-data-tools repository</name>
        <url>https://github.com/EFTI4EU/efti-data-tools/raw/mvn-repo</url>
       </repository>
      ```
 2. Add dependency `eu.efti.datatools:schema:<version>`, and if you need it, `eu.efti.datatools:populate:<version>`:
    * Gradle example:
      ```
      implementation("eu.efti.datatools:schema:0.3.0")
      ```
    * Maven example:
      ```
      <dependency>
        <groupId>eu.efti.datatools</groupId>
        <artifactId>schema</artifactId>
        <version>0.3.0</version>
      </dependency>
      ```

See [Java example](./example/java) for a complete example on library usage.

### Command line application

Get efti-data-tools-cli-<version>.zip from [releases](https://github.com/EFTI4EU/efti-data-tools/releases), unzip it and run with:
```
# On *nix:
./efti-datatools-cli-<version>/bin/app --help

# On Windows:
efti-datatools-cli-<version>\bin\app.bat --help
```

The following examples use gradle to simplify testing. Note how the xpath expressions use local xml names and ignore namespaces.

#### Get help

```shell
./gradlew app:run --args="-h"
```

#### Subset filtering

```shell
./gradlew app:run --args="filter -w -x identifier -i ../xsd/examples/consignment.xml -s FI01,FI02"
```

#### Populate documents

##### Set single value

```shell
./gradlew app:run --args="populate -x identifier -w -s 42 -t 'consignment/deliveryEvent/actualOccurrenceDateTime:=202412312359+0000'"
```

##### Delete node

```shell
./gradlew app:run --args="populate -x identifier -w -s 42 -d 'consignment/deliveryEvent/actualOccurrenceDateTime'"
```

##### Set multiple identifiers to same value

```shell
./gradlew app:run --args="populate -x identifier -w -s 42 -t 'consignment/usedTransportEquipment/id:=ABC-123'"
```

##### Set multiple identifiers to different values

```shell
./gradlew app:run --args="populate -x identifier -w -s 42 -t 'consignment/usedTransportEquipment[1]/id:=ABC-123' -t 'consignment/usedTransportEquipment[2]/id:=XYZ-789'"
```

##### Output both common and identifier documents with default filenames

```shell
./gradlew app:run --args="populate -x both -w -s 42
```

##### Output both common and identifier documents with custom filenames

```shell
./gradlew app:run --args="populate -x both -w -s 42 -oc my-common.xml -oi my-identifiers.xml
```

## Development

Build and run tests with:
```
./gradlew build distZip
```

### Creating releases

For example, to release version 0.3.0:
1. Set the version number in [gradle.properties](gradle.properties)
2. Commit
3. Add and push tag `v0.3.0`
4. Publish library artifacts manually to the Maven repository:
   1. Checkout branch `mvn-repo`
   2. Download library zip from https://github.com/EFTI4EU/efti-data-tools/releases/tag/v0.3.0
   3. Unzip the file (directory `eu` should be at root dir of the repo)
   4. Commit and push

Let us follow [semantic versioning](https://semver.org/).
