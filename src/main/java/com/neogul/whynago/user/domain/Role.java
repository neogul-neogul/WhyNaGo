package com.neogul.whynago.user.domain;

// 승격(USER -> ADMIN)은 운영 DB에서 직접 수행한다. 코드에는 권한을 바꾸는 경로를 두지 않는다.
public enum Role {

    USER,
    ADMIN
}