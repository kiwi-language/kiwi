package org.metavm.object.instance.persistence;

import org.metavm.util.BytesUtils;

import java.util.Arrays;

public class IndexKeyUtils {

    public static boolean equals(IndexKeyPO key1, IndexKeyPO key2) {
        for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
            if(!Arrays.equals(key1.getColumn(i), key2.getColumn(i)))
                return false;
        }
        return true;
    }

    public static int compare(IndexKeyPO key1, IndexKeyPO key2) {
        for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
            var d = BytesUtils.compareBytes(key1.getColumn(i), key2.getColumn(i));
            if(d != 0)
                return d;
        }
        return 0;
    }

}
