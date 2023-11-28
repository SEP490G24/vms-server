package fpt.edu.capstone.vms.constants;


public enum ErrorApp {
    SUCCESS(200, I18n.getMessage("msg.success")),
    BAD_REQUEST(400, I18n.getMessage("msg.bad.request")),
    BAD_REQUEST_PATH(400, I18n.getMessage("msg.bad.request.path")),
    UNAUTHORIZED(401, I18n.getMessage("msg.unauthorized")),
    FORBIDDEN(403, I18n.getMessage("msg.access.denied")),
    INTERNAL_SERVER(500, I18n.getMessage("msg.internal.server")),
    ENTITY_NOT_FOUND(100, I18n.getMessage("msg.entity.notFound")),
    OBJECT_NOT_EMPTY(101, I18n.getMessage("msg.object.not.empty")),
    OBJECT_CODE_NULL(102, I18n.getMessage("msg.object.code.null")),


    //export
    EXPORT_ERROR(2000, I18n.getMessage("msg.export.errorOccurred")),
    IMPORT_HEADER_ERROR(3000, I18n.getMessage("msg.user.import.header.error")),

    //file
    FILE_EMPTY(104, I18n.getMessage("msg.file.empty")),
    FILE_NOT_FORMAT(105, I18n.getMessage("msg.file.format")),
    FILE_INVALID_IMAGE_EXTENSION(106, I18n.getMessage("msg.file.invalid.image.extension")),
    FILE_OVER_SIZE(107, I18n.getMessage("msg.file.over.size")),
    FILE_NOT_FOUND(108, I18n.getMessage("msg.file.not.found")),

    //template
    TEMPLATE_NOT_FOUND(107, I18n.getMessage("msg.template.not.found")),

    //department
    DEPARTMENT_DUPLICATE(108, I18n.getMessage("msg.department.duplicate.code")),
    DEPARTMENT_NOT_FOUND(109, I18n.getMessage("msg.department.not.found")),

    //site
    SITE_NOT_NULL(110, I18n.getMessage("msg.entity.site.null")),
    SITE_NOT_FOUND(111, I18n.getMessage("msg.site.not.found")),

    //user
    USER_NOT_PERMISSION(403, I18n.getMessage("msg.user.not.permission")),

    //device
    DEVICE_NOT_FOUND(112, I18n.getMessage("msg.device.not.found")),

    //organization
    ORGANIZATION_NOT_FOUND(113, I18n.getMessage("msg.organization.not.found")),
    ORGANIZATION_CODE_EXIST(114, I18n.getMessage("msg.organization.code.exist")),
    ORGANIZATION_CODE_NULL(115, I18n.getMessage("msg.organization.code.null")),
    ORGANIZATION_ID_NULL(116, I18n.getMessage("msg.organization.id.null")),
    ORGANIZATION_NOT_PERMISSION(117, I18n.getMessage("msg.organization.not.permission"));
    private final int code;
    private final String description;

    ErrorApp(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }
}
