# Using Cds Data Views in Java

**Version:** 1.4.4

[![Build Status](https://dev.azure.com/AVEVA-VSTS/Cloud%20Platform/_apis/build/status%2Fproduct-readiness%2FADH%2FAVEVA.sample-adh-data_views-java?repoName=AVEVA%2Fsample-adh-data_views-java&branchName=main)](https://dev.azure.com/AVEVA-VSTS/Cloud%20Platform/_build/latest?definitionId=16139&repoName=AVEVA%2Fsample-adh-data_views-java&branchName=main)

The sample code in this demonstrates how to invoke Data View REST APIs via the sample Java client [library](https://github.com/AVEVA/sample-adh-sample_libraries-java). The sample demonstrates how to establish a connection to SDS, obtain an authorization token, create an SdsType and SdsStream with data (if needed), create a data view, update it, retrieve it, and retrieve data from it in different ways. At the end of the sample, everything that was created is deleted.

## Summary of steps to run the Java demo

1. Clone a local copy of this GitHub repository.
1. Install the [Java Client Library](https://github.com/AVEVA/sample-adh-sample_libraries-java) (see its [readme](https://github.com/AVEVA/sample-adh-sample_libraries-java/blob/main/README.md) for instructions)
1. The sample is configured using the file [appsettings.placeholder.json](appsettings.placeholder.json). Before editing, rename this file to `appsettings.json`.
   - This repository's `.gitignore` rules should prevent the file from ever being checked in to any fork or branch, to ensure credentials are not compromised.
1. Replace the configuration strings in `appsettings.json`
1. Build and run the project.
   1. `cd` to your project location.
   1. run `mvn package exec:java` on cmd.

\*Currently this project is not hosted on the central Maven repo and must be compiled and installed locally.

---

Tested against Maven 3.6.3 and Java Runtime Environment 11.

For the main Cds data view samples page [ReadMe](https://github.com/AVEVA/AVEVA-Samples-CloudOperations/blob/main/docs/DATA_VIEWS.md)  
For the main Cds samples page [ReadMe](https://github.com/AVEVA/AVEVA-Samples-CloudOperations)  
For the main AVEVA samples page [ReadMe](https://github.com/AVEVA/AVEVA-Samples)
