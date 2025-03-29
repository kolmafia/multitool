# multitool
[![release](https://img.shields.io/github/v/release/kolmafia/multitool?color=blueviolet&label=%F0%9F%8D%B8%20release)](https://github.com/kolmafia/multitool/releases/latest)
[![minimum java](https://img.shields.io/static/v1?label=min%20java&message=v11&color=%23007396&logo=java)](https://adoptium.net/)
[![codecov](https://img.shields.io/codecov/c/github/kolmafia/multitool?logo=codecov&token=9Z41LO29KF)](https://codecov.io/github/kolmafia/multitool)

A utility for inspecting portions of the KoLmafia local environment, downloading new versions and launching [KoLmafia](https://github.com/kolmafia/kolmafia).


Note - this is a work in progress.  Assistance in verifying that permissions and protections and authorizations are the same as KoLmafia is solicited and appreciated.  Same for test coverage metrics, workflows and tasks associated with commits and PRs.

The tool makes several assumptions about naming conventions so there are environments where it won't work well.  For the moment, download the jar file into the directory containing the KoLmafia jar file (or that will contain it).  Open a command line.  Enter "java -jar multitool-rXXX.jar" to download updates to multitool and KoLmafia as needed and then run KoLmafia.
