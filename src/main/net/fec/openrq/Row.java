/*
 * Copyright 2014 OpenRQ Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fec.openrq;


import java.util.Set;


/**
 */
final class Row {

    int position;
    int nonZeros;
    final int originalDegree;
    Set<Integer> nodes = null;
    final boolean isHDPC;


    Row(int i, int r, int d, boolean hdpc) {

        position = i;
        nonZeros = r;
        originalDegree = d;
        isHDPC = hdpc;
    }

    Row(int i, int r, int d, boolean hdpc, Set<Integer> e) {

        position = i;
        nonZeros = r;
        originalDegree = d;
        nodes = e;
        isHDPC = hdpc;
    }
}
