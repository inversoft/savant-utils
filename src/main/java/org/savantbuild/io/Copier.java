/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides file copying utilities using relative or absolute paths. All relative paths are resolved using the
 * baseDirectory that is passed into the constructor.
 *
 * @author Brian Pontarelli
 */
public class Copier {
  public final Path to;

  public List<FileSet> fileSets = new ArrayList<>();

  public Copier(Path to) {
    this.to = to;
  }

  public Copier(String to) {
    this.to = Paths.get(to);
  }

  public int copy() throws IOException {
    AtomicInteger count = new AtomicInteger(0);
    for (FileSet fileSet : fileSets) {
      if (!Files.isDirectory(fileSet.directory)) {
        continue;
      }

      Files.walkFileTree(fileSet.directory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Path relativeDestination = file.subpath(fileSet.directory.getNameCount(), file.getNameCount());
          Path resolvedDestination = to.resolve(relativeDestination);
          Files.createDirectories(resolvedDestination.getParent());
          Files.copy(file, resolvedDestination, StandardCopyOption.REPLACE_EXISTING);
          count.incrementAndGet();
          return FileVisitResult.CONTINUE;
        }
      });
    }

    return count.get();
  }

  public Copier fileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path passed to the Copier cannot be a file");
    }

    this.fileSets.add(fileSet);
    return this;
  }

  public Copier fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public Copier fileSet(String directory) throws IOException {
    return fileSet(Paths.get(directory));
  }
}
