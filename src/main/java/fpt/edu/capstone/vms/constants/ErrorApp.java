package fpt.edu.capstone.vms.constants;


public enum ErrorApp {
    SUCCESS(200, I18n.getMessage("msg.success")),
    BAD_REQUEST(400, I18n.getMessage("msg.bad.request")),
    BAD_REQUEST_PATH(400, I18n.getMessage("msg.bad.request.path")),
    UNAUTHORIZED(401, I18n.getMessage("msg.unauthorized")),
    FORBIDDEN(403, I18n.getMessage("msg.access.denied")),
    INTERNAL_SERVER(500, I18n.getMessage("msg.internal.server")),
    ENTITY_NOT_FOUND(100, I18n.getMessage("msg.entity.notFound")),

    //User
    USER_CHANGE_SAME_PASSWORD(150, I18n.getMessage("msg.user.changeSamePassword")),
    ENTITY_USER_DUPLICATE(106, I18n.getMessage("msg.user.duplicate")),
    INCORRECT_PASSWORD(107, I18n.getMessage("msg.user.error.password")),
    USER_NOT_FOUND(1000, I18n.getMessage("msg.user.notFound")),
    EXPORT_ERROR(2000, I18n.getMessage("msg.export.errorOccurred")),
    IMPORT_HEADER_ERROR(3000, I18n.getMessage("msg.user.import.header.error")),

    ENTITY_CODE_DUPLICATE(101, I18n.getMessage("msg.entity.duplicate")),
    ENTITY_CODE_DUPLICATE_PRODUCTION_SITE(101, I18n.getMessage("msg.production.site.duplicate")),
    ENTITY_REPORT_CONFIG_CODE_DUPLICATE(101, I18n.getMessage("msg.entity.reportConfig.duplicateCode")),
    IS_PARENTS(104, I18n.getMessage("msg.entity.isParents")),
    IS_CHECK_PARENTS(105, I18n.getMessage("msg.entity.isCheckParents")),
    ENTITY_EMAIL_DUPLICATE(108, I18n.getMessage("msg.email.duplicate")),
    SERIAL_NOT_FOUND(102, I18n.getMessage("msg.entity.serialNotFound")),
    SERIAL_IS_ACTIVE(103, I18n.getMessage("msg.entity.serialIsActive")),
    IS_CHECK_REQUIRE_NAME(122, I18n.getMessage("msg.entity.require.name")),
    IS_CHECK_DUPLICATE_NAME(123, I18n.getMessage("msg.entity.name.duplicate")),
    IS_CHECK_RETIO(124, I18n.getMessage("msg.entity.is.check.ratio")),
    IS_CHECK_TOTALSCORE(125, I18n.getMessage("msg.entity.is.check.totalScore")),
    IS_CHECK_VALID_RANGE(126, I18n.getMessage("msg.entity.is_check.valid.range")),
    IS_CHECK_FROM_AND_TO(127, I18n.getMessage("msg.entity.is_check.from.and.to")),
    IS_CHECK_SHIP_FROM_AND_SHIP_TO(132, I18n.getMessage("msg.entity.is_check.ShipFrom.and.ShipTo")),

    FILE_EMPTY(104, I18n.getMessage("msg.file.empty")),

    FILE_NOT_FORMAT(105, I18n.getMessage("msg.file.format")),
    NAME_ENTITY_DUPLICATE(107, I18n.getMessage("msg.file.format")),

    FILE_HEADER_FORMAT(106, I18n.getMessage("msg.file.formatHeader")),

    MODEL_NOT_IN_WARRANTY(128, I18n.getMessage("msg.entity.modelNotInWarranty")),
    MODEL_IS_EXIST(129, I18n.getMessage("msg.entity.modelIsExist")),
    PRODUCT_NOT_IN_WARRANTY(130, I18n.getMessage("msg.entity.productNotInWarranty")),
    WARRANTY_CODE_IS_EXIST(130, I18n.getMessage("msg.entity.warrantyCodeIsExist")),
    WARRANTY_ADVANCED_CODE_IS_EXIST(131, I18n.getMessage("msg.entity.warrantyAdvancedCodeIsExist")),
    WARRANTY_STATION_IS_NOT_NULL(132, I18n.getMessage("msg.entity.warrantyStationIsNotNull")),
    WARRANTY_STATION_IS_NOT_UPDATE(133, I18n.getMessage("msg.entity.warrantyStationIsNotUpdate")),
    CAT_FEE_TRANSPORT_SHIP_ILLEGAL(134, I18n.getMessage("msg.entity.catFeeTransportShipIllegal")),
    CAT_FEE_TRANSPORT_SHIP_ILLEGAL1(134, I18n.getMessage("msg.entity.catFeeTransportShipIllegal1")),
    WAREHOUSE_IS_ACTIVE_IS_NOT_NULL(133, I18n.getMessage("msg.entity.warehouseIsActiveIsNotNull")),
    WAREHOUSE_NAME_IS_NOT_NULL(134, I18n.getMessage("msg.entity.warehouseNameIsNotNull")),
    WAREHOUSE_CODE_IS_NOT_NULL(135, I18n.getMessage("msg.entity.warehouseCodeIsNotNull")),
    WAREHOUSE_WARRANTY_STATION_IS_NOT_NULL(136, I18n.getMessage("msg.entity.warehouseWarrantyStationIsNotNull")),
    ENTITY_WAREHOUSE_CODE_DUPLICATE(137, I18n.getMessage("msg.warehouse.code.duplicate")),
    ENTITY_CAT_FEE_TRANSPORT_CODE_DUPLICATE(138, I18n.getMessage("msg.catFeeTransport.code.duplicate")),
    ENTITY_CAT_FEE_MOVE_CODE_DUPLICATE(139, I18n.getMessage("msg.catFeeMove.code.duplicate")),
    IS_CHECK_CAT_FEE_MOVE_SHIP_FROM_AND_SHIP_TO(140, I18n.getMessage("msg.entity.is.check.catFeeMove.ShipFrom.and.ShipTo")),
    EMAIL_SEND_FAILED(105, I18n.getMessage("msg.email.send.failed")),
    ROOM_IN_USE(106, I18n.getMessage("msg.room.in.use")),
    TEMPLATE_NOT_FOUND(107, I18n.getMessage("msg.template.not.found"));

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
