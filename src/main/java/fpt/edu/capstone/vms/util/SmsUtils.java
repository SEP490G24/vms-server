package fpt.edu.capstone.vms.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;


public class SmsUtils {

    public static final String ACCOUNT_SID = "AC2da6d79c68c1f74446bf1046da487719";
    public static final String AUTH_TOKEN = "577ac015572c84b45d67bf100cab3c48";
    public static final String PHONE_NUMBER = "+18324626979";


    public static String sendSms(String messageBody, String toPhoneNumber) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber),
                new com.twilio.type.PhoneNumber(PHONE_NUMBER),
                messageBody)
            .create();
        return message.getSid();
    }

}
