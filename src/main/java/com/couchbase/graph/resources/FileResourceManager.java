/*
 * Copyright 2014 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.graph.resources;

import com.couchbase.graph.error.ResourceReadException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class helps to access the existing JS files.
 * 
 * The JS files are by default located under 'resources'
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class FileResourceManager {
   
    /**
     * The relative path where file resources are searched
     */
    public static final String RESOURCE_PATH = "resources";
    
    /**
     * The logger of this class
     */
    private static final Logger LOG = Logger.getLogger(FileResourceManager.class.getName());
 
    /**
     * To store the references to the file resources by their name
     */
    private static final Map<String, FileResource> files = new HashMap<>();
    
    
    
    /**
     * To access a file resource
     * @param name
     * @return 
     */
    public static FileResource getFileResource(String name) {
        
        if (! files.containsKey(name)) try {

            files.put(name, new FileResource(RESOURCE_PATH + "/" + name));
       
        } catch (ResourceReadException ex) {
            
            LOG.severe(ex.toString());
        }
        
        return files.get(name);
    }
}
