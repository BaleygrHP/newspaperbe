package com.hungpham.dtos;

import com.hungpham.common.enums.AuditActionEnum;
import com.hungpham.common.enums.AuditEntityTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogDto extends AbstractDto<AuditLogDto>{
    private String actorId;
    private AuditActionEnum action;
    private AuditEntityTypeEnum entityType;
    private String entityId;
    private String meta;
    private String ipAddress;
    private String userAgent;

}
