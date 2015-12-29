/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine;

import java.util.ArrayList;

public class EPreloadQueue {

    private ArrayList<String> eventQueue;

    public EPreloadQueue() {
        eventQueue = new ArrayList<String>();
    }

    public void add(String name) {
        synchronized (eventQueue) {
            eventQueue.add(name);
        }
    }

    public void remove(String name) {
        synchronized (eventQueue) {
            eventQueue.remove(name);
        }
    }

    public void notifyFinish(String name) {
        synchronized (eventQueue) {
            eventQueue.remove(name);
        }
    }

    public boolean isEmpty() {
        synchronized (eventQueue) {
            return eventQueue.isEmpty();
        }
    }

    public void clear() {
        synchronized (eventQueue) {
            eventQueue.clear();
        }
    }
}
