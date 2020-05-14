package com.asianwallets.common.dto.th.ISO8583;

import jdk.nashorn.internal.objects.NativeUint8Array;

import java.util.Map;

/**
 * @description:
 * @author: YangXu
 * @create: 2020-05-08 10:58
 **/
public class demo {


    public static void main(String[] args) {

        String IP = "58.248.241.169";
        String port = "10089";
        String reqCharset = "UTF-8";
        ISO8583DTO iso8583DTO = new ISO8583DTO();
        iso8583DTO.setMessageType("0800");
        iso8583DTO.setSystemTraceAuditNumber_11("198124");
        iso8583DTO.setAcquiringInstitutionIdentificationCode_32("00008600005");
        iso8583DTO.setCardAcceptorTerminalIdentification_41("00018644");
        iso8583DTO.setCardAcceptorIdentificationCode_42("852999958120501");
        iso8583DTO.setReservedPrivate_60("50000001003");
        iso8583DTO.setReservedPrivate_63("001");


        String sendMsg;
        try {
             //组包
            sendMsg = "6006090000"+"800100000000"+NumberStringUtil.str2HexStr("8529999581205010001864400000000860000500000000")
                    +NumberStringUtil.str2HexStr("852999958120501")+ISO8583Util.packISO8583DTO(iso8583DTO);
            String strHex2 = String.format("%04x",sendMsg.length()/2);
            sendMsg = strHex2 + sendMsg;
            System.out.println(" ===  sendMsg  ====   "+sendMsg);
            //Map<String, String> respMap = ISO8583Util.sendTCPRequest(IP, port, NumberStringUtil.str2Bcd(sendMsg), reqCharset);
            //String result = respMap.get("respData");
            //System.out.println(" ====  result  ===   "+result);
             //解包
            ISO8583DTO iso8583DTO1281 = ISO8583Util.unpackISO8583DTO("00E660000000028001000000003835323939393935383132303035303030303032373533333030303030303030303030303033303030303030303138353239393939353831323030353030303030303131350210703800810EC48011163437363133343030303030303030313900900000000000010819808214594105130008085200013130313130303034323735303036323734333030303030303237353338353239393939353831323030353000355F5109BDBBD2D7B3C9B9A6025F551430303030303202313938303832023035313302023334340014220000020006003044364532343830");
            System.out.println(iso8583DTO1281.toString());
        } catch (Exception e) {
            System.out.println(e);
        }

    }


    //public static void main(String[] args) {
    //
    //    byte[] n = NumberStringUtil.str2Bcd("9");
    //    for (byte c :n ) {
    //        System.out.println(c);
    //    }
    //    //System.out.println(  NumberStringUtil.hexToBinaryString("0020000100C00012"));
    //
    //String s = "600002000080010000000038353239393939353831323030353030303030323735333330303030303030303030303030333030303030303031383532393939393538313230303530303030303030353408000020000100C000121981240852000001303030303237353338353239393939353831323030353000115000000100300003303031";
    //    System.out.println(s.length()/2);
    //String strHex2 = String.format("%04x",s.length()/2);
    //    System.out.println(strHex2);
    //}
}
