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

import java.io.InputStream;
import java.util.logging.Logger;
import com.couchbase.graph.error.ResourceReadException;
import com.couchbase.graph.helper.StringHelper;
import java.io.IOException;

/**
 * A basic file resource, which means more exactly a text file resource
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class FileResource {

    protected final static Logger LOG = Logger.getLogger(FileResource.class.getName());


    /**
     * The file to read the JS code from
     */
    protected String fileName;
    
    /**
     * The content of the JS file
     */
    protected String fileContent;
   


    /**
     * The constructor which takes the configuration file as argument
     * 
     * @param fileName
     * @throws com.couchbase.graph.error.ResourceReadException
     */
    public FileResource(String fileName) throws ResourceReadException
    {
        this.fileName = fileName;

        InputStream jsFileStream  = this.getClass().getResourceAsStream("/" + fileName);
                
        try {
            
            this.fileContent = StringHelper.inputStreamToString(jsFileStream);
        
        } catch (IOException ex) {
            
            throw new ResourceReadException(fileName, ex);
        }
               
    }

    @Override
    public String toString() {
    
        return this.fileContent;
        
    }
    
    

}