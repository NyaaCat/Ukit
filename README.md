# Ukit
Player's Utilities


## Build locally

1. Clone the repository

2. [Acquire a Github personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) with at least read:packages scope.

3. Modify the `~/.m2/settings.xml` file with GITHUB_USERNAME and GITHUB_TOKEN acquired in step 2.

   ```xml
   <settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
        ...
        <servers>
            ...
            <server>
                <id>ghpkg-SimpleLanguageLoader</id>
                <username>GITHUB_USERNAME</username>
                <password>GITHUB_TOKEN</password>
            </server>
            <server>
                <id>ghpkg-SimpleLanguageLoader</id>
                <username>GITHUB_USERNAME</username>
                <password>GITHUB_TOKEN</password>
            </server>
            <server>
                <id>ghpkg-SimpleLanguageLoader</id>
                <username>GITHUB_USERNAME</username>
                <password>GITHUB_TOKEN</password>
            </server>
        </servers>
    </settings>
   ```
4. Run the build and publish (locally) with maven.