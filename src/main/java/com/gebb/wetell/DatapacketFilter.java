package com.gebb.wetell;

import javax.crypto.Cipher;
import java.io.ObjectInputFilter;
import java.util.ArrayList;

public class DatapacketFilter implements ObjectInputFilter {
    @Override
    public Status checkInput(FilterInfo filterInfo) {
        // Check if already filtered
        ObjectInputFilter filter = ObjectInputFilter.Config.getSerialFilter();
        if (filter != null) {
            ObjectInputFilter.Status status = filter.checkInput(filterInfo);
            if (status != ObjectInputFilter.Status.UNDECIDED) {
                return status;
            }
        }
        Class<?> obj = filterInfo.serialClass();
        if (obj == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        if (obj.getName().equals("com.gebb.wetell.Datapacket") || obj.getName().equals("java.util.ArrayList") || obj.getName().equals("[Ljava.lang.Object;") || obj.getName().equals("[B")) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    }
}
