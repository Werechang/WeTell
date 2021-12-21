package com.gebb.wetell.connection;

import java.io.ObjectInputFilter;

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
        if (obj.getName().equals("com.gebb.wetell.connection.Datapacket") || obj.getName().equals("java.util.ArrayList") || obj.getName().equals("[Ljava.lang.Object;") || obj.getName().equals("[B") || obj.getName().equals("com.gebb.wetell.dataclasses.MessageData") || obj.getName().equals("com.gebb.wetell.dataclasses.ChatData") || obj.getName().equals("com.gebb.wetell.dataclasses.ContactData")) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    }
}
