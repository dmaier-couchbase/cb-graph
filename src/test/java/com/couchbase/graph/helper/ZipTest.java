/*
 * Copyright 2016 Couchbase, Inc.
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
package com.couchbase.graph.helper;

import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class ZipTest {
    
    @Test
    public void testCompress() throws ZipHelper.CompressionException, UnsupportedEncodingException {
    
        System.out.println("-- testCompress");
        
        String hello =  "Hello Hello Hello Hello Hello Hello Hello Hello" +
                        "Hello Hello Hello Hello Hello Hello Hello Hello" +
                        "Hello Hello Hello Hello Hello Hello Hello Hello" +
                        "Hello Hello Hello Hello Hello Hello Hello Hello";
        
        System.out.println("Size of input: " + hello.getBytes("UTF-8").length);
        
        byte[] zipped = ZipHelper.compress(hello);
               
        System.out.println("Size of output: " + zipped.length);
        
        System.out.println(zipped);
    
    }
    
    
     @Test
    public void testDeCompress() throws ZipHelper.CompressionException, ZipHelper.DecompressionException, UnsupportedEncodingException {
    
        System.out.println("-- testDeCompress");
        
        String hello =  "Hello Hello Hello Hello Hello Hello Hello Hello";
       
        byte[] zipped = ZipHelper.compress(hello);  
        String decompressed = ZipHelper.decompress(zipped);        
        assertEquals(hello, decompressed);
    }
}
