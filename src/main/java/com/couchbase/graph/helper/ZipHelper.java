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

import com.couchbase.client.deps.io.netty.handler.codec.bytes.ByteArrayEncoder;
import com.couchbase.graph.error.ABaseException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Helper which wraps java.util.zip functionality for de-/compression purposes.
 * 
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class ZipHelper {
    
    
    public static class DecompressionException extends ABaseException {

        public DecompressionException(Exception inner) {
            super(inner);
        }

        @Override
        public String toString() {
            
          return "Could not decompress the  data: " + inner.getMessage();
        }   
    }
    
    public static class CompressionException extends ABaseException {

        
        public CompressionException(Exception inner) {
            super(inner);
        }

        @Override
        public String toString() {
            
          return "Could not compress the  string: " + inner.getMessage();
        }   
    }
    
    
    public static String comprBytesToString(byte[] bytes) {
     
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public static byte[] comprStringToBytes(String str)  {
     
        return Base64.getDecoder().decode(str);
    }
    
    
    
    /**
     * Compress some text
     *
     * @param text
     * @return
     * @throws com.couchbase.graph.helper.ZipHelper.CompressionException
     */
    public static byte[] compress(String text) throws CompressionException {

        ByteArrayOutputStream bas = new ByteArrayOutputStream();

        try {

            GZIPOutputStream gzip = new GZIPOutputStream(bas);
            gzip.write(text.getBytes("UTF-8"));
            gzip.close();
            
            return bas.toByteArray();

        } catch (IOException ex) {

            throw new CompressionException(ex);
        }

    }

    /**
     * Decompress some previously compressed data
     * 
     * @param data
     * @return 
     * @throws com.couchbase.graph.helper.ZipHelper.DecompressionException 
     */
    public static String decompress(byte[] data) throws DecompressionException {
               
        try {
            
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
            
            StringBuilder result = new StringBuilder();
            
            String line; 
            while ((line=reader.readLine())!=null) {
                
                result.append(line);
            }
            
            gzip.close();
            
            return result.toString();
           
        } catch (IOException ex) {
           
            throw new DecompressionException(ex);
        }
    }
    
}
