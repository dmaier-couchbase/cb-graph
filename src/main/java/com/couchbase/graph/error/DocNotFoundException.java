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

package com.couchbase.graph.error;

/**
 *
 * Occours when a key does not exist
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class DocNotFoundException extends ABaseException {

    /**
     * The affected key
     */
    private final String key;
    
    
    /**
     * The constructor
     * 
     * @param key
     * @param inner 
     */
    public DocNotFoundException(String key, Exception inner) {
        
        super(inner);
        
        this.key = key;
    }

    /**
     * The error message
     * @return 
     */
    @Override
    public String toString() {
        
        return "The key " + key + "does not exist : " + inner.getMessage();
    }
    
    
    
}
