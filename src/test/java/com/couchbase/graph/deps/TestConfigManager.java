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

package com.couchbase.graph.deps;

import com.couchbase.graph.cfg.*;
import com.couchbase.graph.error.ResourceReadException;
import java.util.logging.Logger;

/**
 * This class helps to access the existing configurations
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class TestConfigManager {
   
    private static final Logger LOG = Logger.getLogger(TestConfigManager.class.getName());
    private static TestConfig testConfig;
    
    
    public static TestConfig getTestConfig() {
        
        if (testConfig == null) try {
            
            testConfig = new TestConfig();
        
        } catch (ResourceReadException ex) {
        
            LOG.severe(ex.toString());
        }
        
        return testConfig;
    }

}
