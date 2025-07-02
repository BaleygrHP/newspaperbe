package com.hungpham.common.enums;

public enum CategoryEnum {
    Main,
    Sub1,
    Sub2,
    Sub3;

    @Override
    public String toString() {
        return this.name();
    }
}
