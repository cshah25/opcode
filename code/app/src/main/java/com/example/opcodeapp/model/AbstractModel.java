package com.example.opcodeapp.model;

import android.os.Parcelable;

import java.util.Map;

/**
 * Abstraction layer for all models. Subclasses should implement methods from the
 * {@link Parcelable} interface. This layer keeps track if the objects in memory are synced
 * with their Firestore counterparts using a {@link #dirty} flag
 */
public abstract class AbstractModel implements Parcelable {
    protected boolean dirty;

    /**
     * Constructor
     */
    public AbstractModel() {
        this.dirty = false;
    }

    /**
     * @return {@code true} if the object is dirty, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Setter method for the dirty flag. Subclasses should be set to dirty when calling other
     * setters and set to not dirty when updated in its respective {@link com.example.opcodeapp.repository.Repository}
     * class.
     *
     * @param dirty Whether the object is dirty or not
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    protected static boolean hasRequiredFields(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (!map.containsKey(k) || map.get(k) == null)
                return false;
        }
        return true;
    }
}
