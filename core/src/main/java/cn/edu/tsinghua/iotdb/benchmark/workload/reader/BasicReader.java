/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package cn.edu.tsinghua.iotdb.benchmark.workload.reader;

import cn.edu.tsinghua.iotdb.benchmark.conf.Config;
import cn.edu.tsinghua.iotdb.benchmark.conf.ConfigDescriptor;
import cn.edu.tsinghua.iotdb.benchmark.conf.Constants;
import cn.edu.tsinghua.iotdb.benchmark.workload.ingestion.Batch;
import cn.edu.tsinghua.iotdb.benchmark.workload.schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasicReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicReader.class);
  protected static final Config config = ConfigDescriptor.getInstance().getConfig();
  private final List<String> files;
  protected BufferedReader bufferedReader;

  private int currentFileIndex = 0;
  protected String currentFileName;

  public BasicReader(List<String> files) {
    this.files = files;
  }

  public boolean hasNextBatch() {
    if(currentFileIndex < files.size()){
      try{
        currentFileName = files.get(currentFileIndex);
        bufferedReader = new BufferedReader(new FileReader(currentFileName));
      }catch (IOException ioException){
        LOGGER.error("Failed to read " + files.get(currentFileIndex));
      }
      currentFileIndex++;
      return true;
    }
    return false;
  }

  /** convert the cachedLines to Record list */
  public abstract Batch nextBatch();

  /**
   * get device schema based on file name and data set type
   *
   * @return device schema list to register
   */
  public static Map<String, Map<String, Type>> getDeviceSchemaList() {
    Path path = Paths.get(config.getFILE_PATH(), Constants.SCHEMA_PATH);
    if(!Files.exists(path) || !Files.isRegularFile(path)){
      LOGGER.error("Failed to find schema file in " + path.getFileName().toString());
      System.exit(2);
    }
    Map<String, Map<String, Type>> result = new HashMap<>();
    try{
      List<String> schemaLines = Files.readAllLines(path);
      for(String schemaLine: schemaLines){
        if(schemaLine.trim().length() != 0){
          String line[] = schemaLine.split(" ");
          if(!result.containsKey(line[0])){
            result.put(line[0], new HashMap<>());
          }
          result.get(line[0]).put(line[1], Type.getType(Integer.valueOf(line[2])));
        }
      }
    }catch (IOException exception){
      LOGGER.error("Failed to init register");
    }

    return result;
  }
}
