# drill [![Build Status](https://travis-ci.org/netindev/drill.svg?branch=master)](https://travis-ci.org/netindev/drill)
CryptonightV8/Monero miner written in java using JNI bindings.

## Download
* Binary releases: https://github.com/netindev/drill/releases
* Git tree: https://github.com/netindev/drill.git

## Usage
Usage: ```java -jar drill-x.x.x.jar -thread 2 -host localhost -port 3333 -user 4AignrnSVPiXUwk3nKBsTWVi4PCvAKPsrJKSpqinK55bQPFXHTsbYbe5FtUmxjJTbcATQ233gkntYA51fd6Hmur5F3v2o1G -pass x```

### Options
| Arg | Description | Required |
| --- | --- | --- |
| -host | Pool host to connect | Yes |
| -user | Username to login, also can be used your address | Yes |
| -port | Pool host port | Yes |
| -pass | Password to login | Optional |
| -thread | Thread count | Optional |
| -variant | Algorithm variant | Optional |
| -help | Prints the help | Optional |

## Build
CMake:
* Install [CMake](https://cmake.org/download/)
* Go to `..\drill\src\main\jni` and execute `cmake .`

Java:
* Install [Maven](https://maven.apache.org/download.html)
* Go to: `..\drill` and execute `mvn clean install`

## Contacts
* [email](mailto:contact@netindev.tk)
* [twitter](https://twitter.com/netindev)