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

package org.zywx.wbpalmstar.widgetone;

import java.lang.reflect.Field;

public class Smith<T> {
	private Object obj;
	private String fieldName;

	private boolean inited;
	private Field field;

	public Smith(Object obj, String fieldName) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		this.obj = obj;
		this.fieldName = fieldName;
	}

	private void prepare() {
		if (inited)
			return;
		inited = true;

		Class<?> c = obj.getClass();
		while (c != null) {
			try {
				Field f = c.getDeclaredField(fieldName);
				f.setAccessible(true);
				field = f;
				return;
			} catch (Exception e) {
			} finally {
				c = c.getSuperclass();
			}
		}
	}

	public T get() throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		prepare();

		if (field == null)
			throw new NoSuchFieldException();

		try {
			@SuppressWarnings("unchecked")
			T r = (T) field.get(obj);
			return r;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("unable to cast object");
		}
	}

	public void set(T val) throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		prepare();

		if (field == null)
			throw new NoSuchFieldException();

		field.set(obj, val);
	}
}
