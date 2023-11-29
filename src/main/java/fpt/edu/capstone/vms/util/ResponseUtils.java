package fpt.edu.capstone.vms.util;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.persistence.dto.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseUtils.class);
    private static final Integer STATUS_FAIL = 0;
    private static final Integer STATUS_SUCCESS = 1;

    /**
     * Return data to client
     *
     * @param itemObject
     * @return
     */
    public static ResponseEntity<Object> getResponseEntity(Object itemObject) {
        BaseResponse baseResponse = new BaseResponse();
        if (itemObject != null) {
            baseResponse.setData(itemObject);
        }
        baseResponse.setCode(ErrorApp.SUCCESS.getCode());
        baseResponse.setMessage(ErrorApp.SUCCESS.getDescription());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    /**
     * Return data to client
     *
     * @param errorApp
     * @param itemObject
     * @return
     */
    public static ResponseEntity<Object> getResponseEntity(ErrorApp errorApp, Object itemObject) {
        BaseResponse baseResponse = new BaseResponse();
        if (itemObject != null) {
            baseResponse.setData(itemObject);
        }
        baseResponse.setCode(errorApp.getCode());
        baseResponse.setMessage(errorApp.getDescription());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    /**
     * Return mess to client
     *
     * @param code
     * @param description
     * @return
     */
    public static ResponseEntity<Object> getResponseEntity(int code, String description) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(code);
        baseResponse.setMessage(description);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    /**
     * Return mess to client
     *
     * @param code
     * @param description
     * @return
     */
    public static ResponseEntity<Object> getResponseEntity(int code, String description, HttpStatus httpStatus) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(code);
        baseResponse.setMessage(description);
        if (httpStatus == null) httpStatus = HttpStatus.OK;
        return new ResponseEntity<>(baseResponse, httpStatus);
    }

    /**
     * Return mess to client
     *
     * @param errorApp
     * @param httpStatus
     * @return
     */
    public static ResponseEntity<Object> getResponseEntity(ErrorApp errorApp, HttpStatus httpStatus) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(errorApp.getCode());
        baseResponse.setMessage(errorApp.getDescription());
        if (httpStatus == null) httpStatus = HttpStatus.OK;
        return new ResponseEntity<>(baseResponse, httpStatus);
    }


    /**
     * Return data to client with status
     *
     * @param itemObject
     * @return
     */
    public static ResponseEntity<Object> getResponseEntityStatus(Object itemObject) {
        return new ResponseEntity<>(itemObject, HttpStatus.OK);
    }

    /**
     * Return mess to client with status
     *
     * @param errorApp
     * @param httpStatus
     * @return
     */
    public static ResponseEntity<Object> getResponseEntityStatus(ErrorApp errorApp, HttpStatus httpStatus) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(errorApp.getCode());
        baseResponse.setMessage(errorApp.getDescription());
        baseResponse.setStatus(STATUS_FAIL);
        if (httpStatus == null) httpStatus = HttpStatus.OK;
        return new ResponseEntity<>(baseResponse, httpStatus);
    }

    /**
     * Return mess to client with status
     *
     * @param validate
     * @param httpStatus
     * @return
     */
    public static ResponseEntity<Object> getResponseEntityStatus(String validate, HttpStatus httpStatus) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage(validate);
        baseResponse.setStatus(STATUS_FAIL);
        if (httpStatus == null) httpStatus = HttpStatus.OK;
        return new ResponseEntity<>(baseResponse, httpStatus);
    }

   /* @NotNull
    public static ResponseEntity<Object> getByteArrayResourceObjectForResponseEntity(byte[] file, String fileName) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        header.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        return new ResponseEntity<>(new ByteArrayResource(file), header, HttpStatus.OK);
    }

    @NotNull
    public static ResponseEntity<Object> getImageForResponseEntity(byte[] file, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        if (SysLogFileExtension.PNG.toString().equalsIgnoreCase(contentType)) headers.setContentType(MediaType.IMAGE_PNG);
        if (SysLogFileExtension.JPEG.toString().equalsIgnoreCase(contentType) || SysLogFileExtension.JPG.toString().equalsIgnoreCase(contentType))
            headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(new ByteArrayResource(file), headers, HttpStatus.OK);
    }*/
}
