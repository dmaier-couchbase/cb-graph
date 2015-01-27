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

import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.cfg.CouchbaseConfig;
import java.util.Arrays;

/**
 * Occours when the connection could not be established
 *  
 * @author David Maier <david.maier at couchbase.com>
 */
public class ConnectionException extends ABaseException {
    
    final private CouchbaseConfig cfg;
   
    public ConnectionException(Exception inner) {

        super(inner);
        
        this.cfg = ConfigManager.getCbConfig();

    }

    @Override
    public String toString() {
        
        return "Could not connect to the Couchbase cluster by using the following hosts " + Arrays.toString(cfg.getHosts()) + ":" + inner.getMessage();
    }
    
    
    
    
    
    
    
}
