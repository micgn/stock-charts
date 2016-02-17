/*
 * Copyright 2016 Michael Gnatz.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.mg.stock.server.util;

public final class NumberUtils {

    private NumberUtils() {
    }

    public static Long toLong(String s) {
        if (s == null) return null;

        if ("N/A".equals(s)) return null;

        int dotPos;
        if ((dotPos = s.indexOf('.')) != -1) {
            s = s.substring(0, dotPos + 3);
        }
        try {
            return Long.valueOf(s.replace(".", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
