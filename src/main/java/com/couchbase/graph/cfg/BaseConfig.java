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

package com.couchbase.graph.cfg;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import com.couchbase.graph.error.ResourceReadException;
import java.io.IOException;

/**
 * The basic configuration
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class BaseConfig {

    protected final static Logger LOG = Logger.getLogger(BaseConfig.class.getName());


    /**
     * The file to read the properties from
     */
    protected String propsFile;

    /**
     * The related properties object
     */
    protected Properties props;

    /**
     * The constructor which takes the configuration file as argument
     * 
     * @param propsFileName
     * @throws com.couchbase.graph.error.ResourceReadException
     */
    public BaseConfig(String propsFileName) throws ResourceReadException
    {
        props  = new Properties();
        InputStream propsStream = this.getClass().getResourceAsStream("/" + propsFileName);

        try {

            props.load(propsStream);

        } catch (IOException ex) {
            
            throw new ResourceReadException(propsFileName, ex);
                
        }
    }

}