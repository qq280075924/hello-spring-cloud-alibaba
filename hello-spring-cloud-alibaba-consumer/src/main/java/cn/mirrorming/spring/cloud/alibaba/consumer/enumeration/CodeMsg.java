package cn.mirrorming.spring.cloud.alibaba.consumer.enumeration;

public enum CodeMsg {
    IMPORT_FIELD_IS_EAMPTY("import_file_is_empty"),
    IMPORT_FIELD_FORMAT_ERROR("import_file_format_error"),
    OVER_MAX_USER_IMPORT_LIMIT("over_max_user_import_limit");

    private String code;

    CodeMsg(String code) {
        this.code = code;
    }

    public String fillArgs(int rowNo, String msg) {
        return code+"rowNo:"+rowNo+msg;
    }

    public String fillArgs(int rowNo) {
        return code+"rowNo:"+rowNo;
    }
}
