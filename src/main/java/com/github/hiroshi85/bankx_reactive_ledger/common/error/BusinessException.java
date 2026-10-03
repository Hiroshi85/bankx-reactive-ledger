package com.github.hiroshi85.bankx_reactive_ledger.common.error;

public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code) { 
        super(code); this.code = code; 
    }

    public String code() { 
        return code; 
    }
}
