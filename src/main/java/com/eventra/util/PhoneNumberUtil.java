// src/main/java/com/eventra/util/PhoneNumberUtil.java
package com.eventra.util;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberUtil {

    public String normalize(String phoneNumber) {
        // Simple normalization - remove spaces and special characters
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    public boolean isValid(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[0-9]{10,15}$");
    }
}