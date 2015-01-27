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

package com.couchbase.graph.views;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class ViewDefTest {
    
    
    private static final String ALL_EDGES = 

            "function map(doc, meta)\n" +
            "{\n" +
            "    if ( doc.type === \"edge\" )\n" +
            "    {\n" +
            "        emit(meta.id, null);\n" +
            "    }\n" +
            "}";
    
    
    @Test
    public void testReadAllEdgesViewDef() {
    
        AllEdgesViewDef allEdgesViewDef = new AllEdgesViewDef();
        
        String mapFunc = allEdgesViewDef.getMapFunc();
        
        System.out.println(mapFunc);
        assertEquals(true, mapFunc.contains(ALL_EDGES));

    }
}
