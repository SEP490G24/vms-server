package fpt.edu.capstone.vms.constants;


public enum ErrorApp {
    SUCCESS(200, I18n.getMessage("msg.success")),
    BAD_REQUEST(400, I18n.getMessage("msg.bad.request")),
    BAD_REQUEST_PATH(400, I18n.getMessage("msg.bad.request.path")),
    UNAUTHORIZED(401, I18n.getMessage("msg.unauthorized")),
    FORBIDDEN(403, I18n.getMessage("msg.access.denied")),
    INTERNAL_SERVER(500, I18n.getMessage("msg.internal.server")),
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
    FILE_UPLOAD_FAILED(109, I18n.getMessage("msg.file.upload.failed")),

    //template
    TEMPLATE_NOT_FOUND(107, I18n.getMessage("msg.template.not.found")),
    TEMPLATE_DUPLICATE(108, I18n.getMessage("msg.template.duplicate.code")),
    TEMPLATE_ERROR_IN_PROCESS_DELETE(179, I18n.getMessage("msg.template.error.in.process.delete")),
    TEMPLATE_IS_USING_CAN_NOT_DELETE(180, I18n.getMessage("msg.template.is.using.can.not.delete")),

    //department
    DEPARTMENT_DUPLICATE(108, I18n.getMessage("msg.department.duplicate.code")),
    DEPARTMENT_NOT_FOUND(109, I18n.getMessage("msg.department.not.found")),
    DEPARTMENT_CAN_NOT_DELETE(180, I18n.getMessage("msg.department.can.not.delete")),
    DEPARTMENT_CAN_NOT_DISABLE(181, I18n.getMessage("msg.department.can.not.disable")),
    DEPARTMENT_IS_USING_CAN_NOT_DELETE(182, I18n.getMessage("msg.department.is.using.can.not.delete")),

    //site
    SITE_NOT_NULL(110, I18n.getMessage("msg.entity.site.null")),
    SITE_NOT_FOUND(111, I18n.getMessage("msg.site.not.found")),
    SITE_CODE_EXIST(112, I18n.getMessage("msg.site.code.exist")),
    SITE_CODE_NULL(113, I18n.getMessage("msg.site.code.null")),
    SITE_ID_NULL(152, I18n.getMessage("msg.site.id.null")),
    SITE_NOT_USE_CARD(168, I18n.getMessage("msg.site.not.use.card")),
    SITE_PLEASE_CREATE_SITE(189, I18n.getMessage("msg.site.please.create.site")),

    //user
    USER_NOT_PERMISSION(403, I18n.getMessage("msg.user.not.permission")),
    USER_CAN_NOT_CREATE(170, I18n.getMessage("msg.user.can.not.create")),
    USER_NOT_FOUND(171, I18n.getMessage("msg.user.not.found")),
    USER_NEW_PASSWORD_SAME_OLD_PASSWORD(172, I18n.getMessage("msg.user.new.password.same.old.password")),
    USER_OLD_PASSWORD_NOT_CORRECT(173, I18n.getMessage("msg.user.old.password.not.correct")),
    USER_NEW_PASSWORD_NOT_NULL(174, I18n.getMessage("msg.user.new.password.not.null")),
    USER_CAN_NOT_FOUND_FILE_IMAGE(175, I18n.getMessage("msg.user.can.not.found.file.image")),
    //device
    DEVICE_NOT_FOUND(151, I18n.getMessage("msg.device.not.found")),
    DEVICE_TYPE_SCAN_CARD(153, I18n.getMessage("msg.device.type.scan.card")),
    DEVICE_DUPLICATE(108, I18n.getMessage("msg.device.duplicate.code")),
    DEVICE_ERROR_IN_PROCESS_DELETE(177, I18n.getMessage("msg.device.error.in.process.delete")),
    DEVICE_CAN_NOT_DISABLE(182, I18n.getMessage("msg.device.can.not.disable")),
    DEVICE_MAC_IP_IS_EXIST_IN_THIS_SITE(183, I18n.getMessage("msg.device.mac.ip.is.exist.in.this.site")),
    DEVICE_NOT_BELONG_SITE(184, I18n.getMessage("msg.device.not.belong.site")),
    DEVICE_IS_USING_CAN_NOT_DELETE(185, I18n.getMessage("msg.device.is.using.can.not.delete")),
    DEVICE_IS_EXIST_IN_ROOM(186, I18n.getMessage("msg.device.is.exist.in.room")),
    DEVICE_CAN_NOT_CHANGE_TYPE(187, I18n.getMessage("msg.device.can.not.change.type")),

    //organization
    ORGANIZATION_NOT_FOUND(113, I18n.getMessage("msg.organization.not.found")),
    ORGANIZATION_CODE_EXIST(114, I18n.getMessage("msg.organization.code.exist")),
    ORGANIZATION_CODE_NULL(115, I18n.getMessage("msg.organization.code.null")),
    ORGANIZATION_ID_NULL(116, I18n.getMessage("msg.organization.id.null")),
    ORGANIZATION_NOT_PERMISSION(117, I18n.getMessage("msg.organization.not.permission")),

    //room
    ROOM_NOT_FOUND(118, I18n.getMessage("msg.room.not.found")),
    ROOM_CODE_EXIST(119, I18n.getMessage("msg.room.code.exist")),
    ROOM_CODE_NULL(120, I18n.getMessage("msg.room.code.null")),
    ROOM_USER_CAN_NOT_CREATE_TICKET(141, I18n.getMessage("msg.room.user.can.not.create.ticket")),
    ROOM_HAVE_TICKET_IN_THIS_TIME(142, I18n.getMessage("msg.room.have.ticket.in.this.time")),
    ROOM_DUPLICATE(108, I18n.getMessage("msg.room.duplicate.code")),
    ROOM_ERROR_IN_PROCESS_DELETE(178, I18n.getMessage("msg.room.error.in.process.delete")),
    ROOM_CAN_NOT_DISABLE(182, I18n.getMessage("msg.room.can.not.disable")),
    ROOM_NOT_BELONG_SITE(184, I18n.getMessage("msg.room.not.belong.site")),
    ROOM_CAN_NOT_DELETE(185, I18n.getMessage("msg.room.can.not.delete")),

    //setting group
    SETTING_GROUP_NOT_FOUND(121, I18n.getMessage("msg.setting.group.not.found")),
    SETTING_GROUP_CODE_EXIST(122, I18n.getMessage("msg.setting.group.code.exist")),
    SETTING_GROUP_CODE_NULL(123, I18n.getMessage("msg.setting.group.code.null")),

    //setting
    SETTING_NOT_FOUND(124, I18n.getMessage("msg.setting.not.found")),
    SETTING_CODE_EXIST(125, I18n.getMessage("msg.setting.code.exist")),
    SETTING_CODE_NULL(126, I18n.getMessage("msg.setting.code.null")),
    SETTING_ID_NULL(127, I18n.getMessage("msg.setting.id.null")),
    SETTING_VALUE_NULL(128, I18n.getMessage("msg.setting.value.null")),


    //province
    PROVINCE_NOT_FOUND(129, I18n.getMessage("msg.province.not.found")),
    PROVINCE_NULL(130, I18n.getMessage("msg.province.null")),

    //district
    DISTRICT_NOT_FOUND(131, I18n.getMessage("msg.district.not.found")),
    DISTRICT_NULL(132, I18n.getMessage("msg.district.null")),
    DISTRICT_NOT_FOUND_BY_PROVINCE(133, I18n.getMessage("msg.district.not.found.by.province")),

    //commune
    COMMUNE_NOT_FOUND(134, I18n.getMessage("msg.commune.not.found")),
    COMMUNE_NULL(135, I18n.getMessage("msg.commune.null")),
    COMMUNE_NOT_FOUND_BY_DISTRICT(136, I18n.getMessage("msg.commune.not.found.by.district")),

    //purpose
    PURPOSE_NOT_FOUND(137, I18n.getMessage("msg.purpose.not.found")),
    PURPOSE_OTHER_NOT_NULL_OTHER(138, I18n.getMessage("msg.purpose.other.not.null.other")),
    PURPOSE_OTHER_MUST_NULL_WHEN_TYPE_NOT_OTHER(157, I18n.getMessage("msg.purpose.other.must.null.when.type.not.other")),

    //customer
    CUSTOMER_NOT_FOUND(139, I18n.getMessage("msg.customer.not.found")),
    CUSTOMER_IS_NULL(140, I18n.getMessage("msg.customer.is.null")),
    CUSTOMER_IDENTITY_NOT_CORRECT(145, I18n.getMessage("msg.customer.identity.not.correct")),
    CUSTOMER_NOT_IN_ORGANIZATION(157, I18n.getMessage("msg.customer.not.in.organization")),
    CUSTOMER_IS_CHECK_IN(158, I18n.getMessage("msg.customer.is.check.in")),
    CUSTOMER_IS_CHECK_OUT(159, I18n.getMessage("msg.customer.is.check.out")),
    CUSTOMER_NOT_CHECK_IN_TO_CHECK_OUT(162, I18n.getMessage("msg.customer.not.check.in.to.check.out")),
    CUSTOMER_NOT_CHECK_IN_TO_ADD_CARD(169, I18n.getMessage("msg.customer.not.check.in.to.add.card")),
    CUSTOMER_ERROR_IN_PROCESS_DELETE(176, I18n.getMessage("msg.customer.error.in.process.delete")),
    CUSTOMER_EMAIL_NOT_FOUND(181, I18n.getMessage("msg.customer.email.not.found")),
    CUSTOMER_EMAIL_EXIST(182, I18n.getMessage("msg.customer.email.exist")),
    CUSTOMER_PHONE_NUMBER_NOT_FOUND(183, I18n.getMessage("msg.customer.phone.number.not.found")),
    CUSTOMER_PHONE_NUMBER_EXIST(184, I18n.getMessage("msg.customer.phone.number.exist")),
    CUSTOMER_IDENTIFICATION_NUMBER_NOT_FOUND(185, I18n.getMessage("msg.customer.identification.number.not.found")),
    CUSTOMER_IDENTITY_EXIST(186, I18n.getMessage("msg.customer.identity.exist")),
    CUSTOMER_NOT_EMPTY_WHEN_CREATE_TICKET(187, I18n.getMessage("msg.customer.not.empty.when.create.ticket")),
    CUSTOMER_NOT_EMPTY_WHEN_UPDATE_TICKET(188, I18n.getMessage("msg.customer.not.empty.when.update.ticket")),

    //ticket
    TICKET_NOT_FOUND(147, I18n.getMessage("msg.ticket.not.found")),
    TICKET_START_TIME_IS_AFTER_THAN_END_TIME(148, I18n.getMessage("msg.ticket.start.time.is.after.than.end.time")),
    TICKET_TIME_MEETING_MUST_GREATER_THAN_15_MINUTES(149, I18n.getMessage("msg.ticket.time.meeting.must.greater.than.15.minutes")),
    TICKET_TIME_CANCEL_MEETING_MUST_BEFORE_2_HOURS(150, I18n.getMessage("msg.ticket.time.cancel.meeting.must.before.2.hours")),
    TICKET_ID_NULL(154, I18n.getMessage("msg.ticket.id.null")),
    TICKET_TIME_UPDATE_MEETING_MUST_BEFORE_2_HOURS(155, I18n.getMessage("msg.ticket.time.update.meeting.must.before.2.hours")),
    TICKET_YOU_CAN_UPDATE_THIS_TICKET(156, I18n.getMessage("msg.ticket.you.can.update.this.ticket")),
    TICKET_IS_EXPIRED_CAN_NOT_CHECK_IN(160, I18n.getMessage("msg.ticket.is.expired.can.not.check.in")),
    TICKET_IS_EXPIRED_CAN_NOT_CHECK_OUT(164, I18n.getMessage("msg.ticket.is.expired.can.not.check.out")),
    TICKET_NOT_START_CAN_NOT_CHECK_OUT(161, I18n.getMessage("msg.ticket.not.start.can.not.check.out")),
    TICKET_NOT_START_CAN_NOT_CHECK_IN(163, I18n.getMessage("msg.ticket.not.start.can.not.check.in")),
    TICKET_NOT_START_CAN_NOT_REJECT(165, I18n.getMessage("msg.ticket.not.start.can.not.reject")),
    TICKET_IS_EXPIRED_CAN_NOT_REJECT(166, I18n.getMessage("msg.ticket.is.expired.can.not.reject")),
    TICKET_CAN_NOT_VIEW(167, I18n.getMessage("msg.ticket.can.not.view")),
    TICKET_IS_CANCEL_CAN_NOT_DO_CHECK(168, I18n.getMessage("msg.ticket.is.cancel.can.not.do.check")),
    TICKET_IS_COMPLETE_CAN_NOT_DO_CHECK(169, I18n.getMessage("msg.ticket.is.complete.can.not.do.check")),
    TICKET_IS_DRAFT_CAN_NOT_DO_CHECK(170, I18n.getMessage("msg.ticket.is.draft.can.not.do.check")),
    MUST_TO_CHOOSE_SITE(168, I18n.getMessage("msg.must.to.choose.site")),
    TICKET_IS_CANCEL_CAN_NOT_DO_UPDATE(185, I18n.getMessage("msg.ticket.is.cancel.can.not.do.update")),
    TICKET_IS_COMPLETE_CAN_NOT_DO_UPDATE(186, I18n.getMessage("msg.ticket.is.complete.can.not.do.update")),
    TICKET_IS_EXPIRED_CAN_NOT_UPDATE(187, I18n.getMessage("msg.ticket.is.expired.can.not.update")),
    TICKET_IS_EXPIRED(188, I18n.getMessage("msg.ticket.is.expired")),
    TICKET_IS_PENDING_CAN_NOT_SAVE_IS_DRAFT(189, I18n.getMessage("msg.ticket.is.pending.can.not.save.is.draft")),
    TICKET_START_TIME_MUST_GREATER_THEM_CURRENT_TIME(190, I18n.getMessage("msg.ticket.start.time.must.greater.them.current.time")),
    TICKET_IS_COMPLETE_CAN_NOT_DO_CANCEL(203, I18n.getMessage("msg.ticket.is.complete.can.not.do.cancel")),
    TICKET_IS_CANCEL_CAN_NOT_DO_CANCEL(204, I18n.getMessage("msg.ticket.is.cancel.can.not.do.cancel")),
    TICKET_IS_EXPIRED_CAN_NOT_DO_CANCEL(205, I18n.getMessage("msg.ticket.is.expired.can.not.do.cancel")),
    TICKET_IS_DRAFT_CAN_NOT_DO_CANCEL(206, I18n.getMessage("msg.ticket.is.draft.can.not.do.cancel")),
    YOU_CAN_NOT_SET_BOOKMARK_FOR_THIS_TICKET(207, I18n.getMessage("msg.you.can.not.set.bookmark.for.this.ticket")),
    //QRCode
    QRCODE_NOT_FOUND(190, I18n.getMessage("msg.qrcode.not.found")),

    //Role
    ROLE_USED(200, I18n.getMessage("msg.role.used")),

    //card
    CARD_NOT_FOUND(170, I18n.getMessage("msg.card.not.found")),
    CARD_ID_NULL(171, I18n.getMessage("msg.card.id.null")),
    CARD_IS_EXIST_WITH_CUSTOMER_IN_SITE(172, I18n.getMessage("msg.card.is.exist.with.customer.in.site")),
    CUSTOMER_IS_REJECT(201, I18n.getMessage("msg.customer.is.reject"));
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
