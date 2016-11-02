package vn.bhxh.bhxhmail.mailstore;


import org.junit.Test;

import vn.bhxh.bhxhmail.mailstore.LocalFolder.MoreMessages;

import static org.junit.Assert.assertEquals;


public class MoreMessagesTest {
    private static final String ERROR_MESSAGE = "The return value of getDatabaseName() is used in the database and " +
            "must not be changed without data migration.";


    @Test
    public void UNKNOWN_getDatabaseName_shouldReturnUnknown() throws Exception {
        assertEquals(ERROR_MESSAGE, "unknown", MoreMessages.UNKNOWN.getDatabaseName());
    }

    @Test
    public void TRUE_getDatabaseName_shouldReturnTrue() throws Exception {
        assertEquals(ERROR_MESSAGE, "true", MoreMessages.TRUE.getDatabaseName());
    }

    @Test
    public void FALSE_getDatabaseName_shouldReturnFalse() throws Exception {
        assertEquals(ERROR_MESSAGE, "false", MoreMessages.FALSE.getDatabaseName());
    }
}
