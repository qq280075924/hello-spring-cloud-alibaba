package cn.mirrorming.spring.cloud.alibaba.consumer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class archiveManage implements Serializable {
    private Long id;

    private Long projectId;

    private Date publishTime;

    private String archiveType;

    private String archiveSummary;

    private Date createTime;

    private Date updateTime;

    private Long creatorId;

    private Long updaterId;

    private Byte isRemoved;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}