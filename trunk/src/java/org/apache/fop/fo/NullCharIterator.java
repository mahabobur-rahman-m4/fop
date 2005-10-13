/*
 * Copyright 2005 The Apache Software Foundation.
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

/* $Id$ */

package org.apache.fop.fo;

import java.util.NoSuchElementException;

/**
 * Class providing an iterator for zero characters. Used by the Block FO.
 */
public class NullCharIterator extends CharIterator {

    private static CharIterator instance;
    
    public static CharIterator getInstance() {
        if (instance == null) {
            instance = new NullCharIterator();
        }
        return instance;
    }

    /**
     * Constructor
     */
    public NullCharIterator() {
        //nop
    }

    /** @see java.util.Iterator#hasNext() */
    public boolean hasNext() {
        return false;
    }

    /** @see org.apache.fop.fo.CharIterator#nextChar() */
    public char nextChar() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

}

