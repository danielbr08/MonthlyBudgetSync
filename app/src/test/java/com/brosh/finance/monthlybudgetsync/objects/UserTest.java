package com.brosh.finance.monthlybudgetsync.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the User class.
 * Tests all getters, setters, constructors, and helper methods.
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("uid-123", "John Doe", "john@example.com", "+1234567890", "db-key-123");
    }

    // ============================================
    // CONSTRUCTOR TESTS
    // ============================================

    @Test
    public void testDefaultConstructor() {
        User defaultUser = new User();
        assertNull(defaultUser.getUid());
        assertNull(defaultUser.getName());
        assertNull(defaultUser.getEmail());
        assertNull(defaultUser.getPhone());
        assertNull(defaultUser.getDbKey());
        assertNull(defaultUser.getOwnerUid());
        assertNotNull(defaultUser.getUserSettings());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("uid-123", user.getUid());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("+1234567890", user.getPhone());
        assertEquals("db-key-123", user.getDbKey());
        assertEquals("uid-123", user.getOwnerUid());
        assertNotNull(user.getUserSettings());
    }

    @Test
    public void testConstructorWithNullDbKey_UsesUid() {
        User userNullDb = new User("uid-456", "Jane", "jane@example.com", "+9876543210", null);
        assertEquals("uid-456", userNullDb.getDbKey());
    }

    @Test
    public void testConstructorWithAllNullValues() {
        User nullUser = new User(null, null, null, null, null);
        assertNull(nullUser.getUid());
        assertNull(nullUser.getName());
        assertNull(nullUser.getEmail());
        assertNull(nullUser.getPhone());
        assertNull(nullUser.getDbKey()); // Falls back to uid which is null
    }

    // ============================================
    // GETTER AND SETTER TESTS
    // ============================================

    @Test
    public void testSetAndGetUid() {
        user.setUid("new-uid");
        assertEquals("new-uid", user.getUid());
    }

    @Test
    public void testSetAndGetName() {
        user.setName("Jane Smith");
        assertEquals("Jane Smith", user.getName());
    }

    @Test
    public void testSetAndGetEmail() {
        user.setEmail("jane@example.com");
        assertEquals("jane@example.com", user.getEmail());
    }

    @Test
    public void testSetAndGetPhone() {
        user.setPhone("+9999999999");
        assertEquals("+9999999999", user.getPhone());
    }

    @Test
    public void testSetAndGetDbKey() {
        user.setDbKey("new-db-key");
        assertEquals("new-db-key", user.getDbKey());
    }

    @Test
    public void testSetAndGetOwnerUid() {
        user.setOwnerUid("owner-uid");
        assertEquals("owner-uid", user.getOwnerUid());
    }

    @Test
    public void testSetAndGetUserSettings() {
        UserSettings settings = new UserSettings();
        user.setUserSettings(settings);
        assertEquals(settings, user.getUserSettings());
    }

    @Test
    public void testSetUserSettingsWithNull_ReplacesWithDefault() {
        user.setUserSettings(null);
        assertNotNull(user.getUserSettings());
    }

    // ============================================
    // EMAIL COMMA CONVERSION TESTS
    // ============================================

    @Test
    public void testGetEmailComma_ReplacesDots() {
        assertEquals("john@example,com", user.getEmailComma());
    }

    @Test
    public void testGetEmailComma_MultipleDots() {
        user.setEmail("john.doe@mail.example.com");
        assertEquals("john,doe@mail,example,com", user.getEmailComma());
    }

    @Test
    public void testGetEmailComma_NullEmail() {
        user.setEmail(null);
        assertEquals("", user.getEmailComma());
    }

    @Test
    public void testGetEmailComma_EmptyEmail() {
        user.setEmail("");
        assertEquals("", user.getEmailComma());
    }

    @Test
    public void testGetEmailComma_TrimsWhitespace() {
        user.setEmail("  john@example.com  ");
        assertEquals("john@example,com", user.getEmailComma());
    }

    // ============================================
    // OWNER STATUS TESTS
    // ============================================

    @Test
    public void testIsOwner_True_WhenUidEqualsDbKey() {
        user.setUid("same-key");
        user.setDbKey("same-key");
        assertTrue(user.isOwner());
    }

    @Test
    public void testIsOwner_False_WhenUidDifferentFromDbKey() {
        user.setUid("uid-123");
        user.setDbKey("different-key");
        assertFalse(user.isOwner());
    }

    @Test
    public void testIsOwner_False_WhenUidIsNull() {
        user.setUid(null);
        user.setDbKey("some-key");
        assertFalse(user.isOwner());
    }

    @Test
    public void testIsOwner_False_WhenBothNull() {
        user.setUid(null);
        user.setDbKey(null);
        assertFalse(user.isOwner());
    }

    // ============================================
    // EQUALS AND HASHCODE TESTS
    // ============================================

    @Test
    public void testEquals_SameObject() {
        assertEquals(user, user);
    }

    @Test
    public void testEquals_SameUid() {
        User user2 = new User("uid-123", "Different Name", "different@email.com", "+0000000000", "different-db");
        assertEquals(user, user2);
    }

    @Test
    public void testEquals_DifferentUid() {
        User user2 = new User("uid-456", "John Doe", "john@example.com", "+1234567890", "db-key-123");
        assertNotEquals(user, user2);
    }

    @Test
    public void testEquals_NullUid() {
        User user1 = new User(null, "Name", "email@test.com", "123", "key");
        user1.setUid(null);
        User user2 = new User(null, "Name2", "email2@test.com", "456", "key2");
        user2.setUid(null);
        assertEquals(user1, user2); // Both null UIDs are equal
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(user, null);
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(user, "not a user");
    }

    @Test
    public void testHashCode_SameUid() {
        User user2 = new User("uid-123", "Different", "diff@email.com", "+000", "diff-key");
        assertEquals(user.hashCode(), user2.hashCode());
    }

    @Test
    public void testHashCode_Consistent() {
        int hash1 = user.hashCode();
        int hash2 = user.hashCode();
        assertEquals(hash1, hash2);
    }

    // ============================================
    // TOSTRING TEST
    // ============================================

    @Test
    public void testToString_ContainsAllFields() {
        String str = user.toString();
        
        assertTrue(str.contains("uid-123"));
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("john@example.com"));
    }

    @Test
    public void testToString_ContainsOwnerStatus() {
        String str = user.toString();
        assertTrue(str.contains("isOwner"));
    }

    // ============================================
    // SERIALIZATION TEST
    // ============================================

    @Test
    public void testSerialVersionUID() {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            long serialVersionUID = field.getLong(null);
            assertEquals(1L, serialVersionUID);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("serialVersionUID field not found or not accessible");
        }
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testEmptyStringFields() {
        user.setName("");
        user.setEmail("");
        user.setPhone("");
        
        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPhone());
    }

    @Test
    public void testUnicodeFields() {
        user.setName("יוחנן דו בעברית");
        assertEquals("יוחנן דו בעברית", user.getName());
    }

    @Test
    public void testSpecialCharactersInName() {
        user.setName("O'Brien-Smith Jr.");
        assertEquals("O'Brien-Smith Jr.", user.getName());
    }

    @Test
    public void testPhoneNumberVariations() {
        String[] phoneNumbers = {
            "+1-234-567-8900",
            "(123) 456-7890",
            "123.456.7890",
            "+972-50-123-4567"
        };
        
        for (String phone : phoneNumbers) {
            user.setPhone(phone);
            assertEquals(phone, user.getPhone());
        }
    }

    @Test
    public void testEmailVariations() {
        String[] emails = {
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "user-name@example.co.uk"
        };
        
        for (String email : emails) {
            user.setEmail(email);
            assertEquals(email, user.getEmail());
        }
    }

    @Test
    public void testSharingScenario_GuestUser() {
        // Guest user has their own UID but uses owner's dbKey
        User guest = new User("guest-uid", "Guest User", "guest@example.com", "123", "owner-db-key");
        guest.setOwnerUid("owner-uid");
        
        assertEquals("guest-uid", guest.getUid());
        assertEquals("owner-db-key", guest.getDbKey());
        assertEquals("owner-uid", guest.getOwnerUid());
        assertFalse(guest.isOwner()); // Not owner because uid != dbKey
    }
}
