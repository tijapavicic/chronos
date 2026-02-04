# ascii_tree — ASCII directory tree utility

This document summarizes the ascii_tree project (inspired by yzhong52/ascii_tree). It provides an overview, build/run instructions, examples for CLI usage and embedding from Java, and sample output. Use this as a quick reference when working with or integrating the ascii_tree codebase.

## Overview
ascii_tree is a small utility that renders directory structures (or generic tree data) as an ASCII-art tree. It can be used as:
- A command-line tool to print a directory tree.
- A Java library to generate tree strings programmatically.

Goals:
- Simple, dependency-light implementation.
- Clear, readable ASCII output suitable for consoles and logs.
- Easy to embed in Java applications.

## Features
- Print filesystem trees.
- Optionally include file sizes, last-modified, or filter by file extensions (where implemented).
- Lightweight API to build and render trees from arbitrary tree/data structures.

## Build (from source)
Typical Java build steps (adjust for your build tool and project layout):

- Using Maven:
  1. mvn clean package
  2. The jar will be in `target/` (e.g., `target/ascii-tree.jar`)

- Using Gradle:
  1. ./gradlew build
  2. Find the jar in `build/libs/`

If the repo uses a simple single-module build, run the appropriate wrapper or installed tool to produce the runnable JAR.

## Command-line usage (example)
After building a runnable jar, you can run:

java -jar target/ascii-tree.jar /path/to/directory

Common CLI options (may vary by implementation):
- -d, --depth N       Limit depth to N levels
- -a, --all           Include hidden files
- -s, --sizes         Show file sizes
- -h, --help          Show help

Example:
java -jar target/ascii-tree.jar -d 3 /home/user/projects

## Embedding as a library (Java example)
Below is a minimal conceptual example to generate a tree string from a root path. The exact class/method names may differ in the implementation — adapt to the repository API.

```java
// Conceptual example — adapt to actual API from the repo
import java.nio.file.Path;
import java.nio.file.Paths;

public class TreeExample {
    public static void main(String[] args) {
        Path root = Paths.get("/path/to/root");
        // Example API: AsciiTree.build(root).render();
        String tree = AsciiTree.builder()
                               .root(root)
                               .includeHidden(false)
                               .maxDepth(4)
                               .build()
                               .render();
        System.out.println(tree);
    }
}
```

If the real API exposes a static helper, it might look like:
String tree = AsciiTree.printTree(rootPath);

Refer to the repository code for exact method names and options.

## Sample output
A typical ASCII tree output:

project
├── src
│   ├── main
│   │   ├── java
│   │   └── resources
│   └── test
└── pom.xml

Files and directories are shown with connectors; depth and detail depend on options.

## License
Refer to the original repository for license details. If you copy or adapt code from the original project, respect the original license and attribution requirements.

## Notes and tips
- For long directories use depth limiting to keep output readable.
- Redirect output to a file for sharing or archival: java -jar ... > tree.txt
- If you plan to embed the library, add unit tests that validate output for various directory shapes.

--- 
This document is a concise guide derived from the ascii_tree project. Inspect the repository source for exact API signatures, advanced options, and tests.
