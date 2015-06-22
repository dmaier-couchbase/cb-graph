/*
 * Copyright 2015 Couchbase, Inc.
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

import com.couchbase.graph.cfg.BaseConfig;
import com.couchbase.graph.error.ResourceReadException;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class TestConfig extends BaseConfig {

    public TestConfig() throws ResourceReadException {
        super("test.properties");
    }
    
    public boolean isPerformanceEnabled()
    {
        String str = this.props.getProperty("test.performance");
        return Boolean.parseBoolean(str);   
    }
    
    
    public boolean isDebugEnabled()
    {
        String str = this.props.getProperty("test.debug");
        return Boolean.parseBoolean(str);   
    }
    
    public boolean isGraphEnabled()
    {
        String str = this.props.getProperty("test.graph");        
        return Boolean.parseBoolean(str);    
    }
}
