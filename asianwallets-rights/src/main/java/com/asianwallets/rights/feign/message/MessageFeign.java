package com.asianwallets.rights.feign.message;
import com.asianwallets.common.enums.Status;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.rights.feign.message.impl.MessageFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 短信和邮件的服务
 */
@FeignClient(value = "asianwallets-message", fallback = MessageFeignImpl.class)
public interface MessageFeign {

    @ApiOperation(value = "国内普通发送")
    @PostMapping("/sms/sendSimple")
    BaseResponse sendSimple(@RequestParam(value = "mobile") @ApiParam String mobile, @RequestParam(value = "content") @ApiParam String content);

    @ApiOperation(value = "国际短信发送")
    @PostMapping("/sms/sendInternation")
    BaseResponse sendInternation(@RequestParam(value = "mobile") @ApiParam String mobile,@RequestParam(value = "content") @ApiParam String content);

    @ApiOperation(value = "国内普通短信模板")
    @PostMapping("/sms/sendSimpleTemplate")
    BaseResponse sendSimpleTemplate(@RequestParam(value = "language") @ApiParam String language,@RequestParam(value = "num") @ApiParam
            Status num,@RequestParam(value = "mobile") @ApiParam String mobile, @RequestBody Map<String, Object> content);

    @ApiOperation(value = "国际短信模板")
    @PostMapping("/sms/sendIntTemplate")
    BaseResponse sendIntTemplate(@RequestParam(value = "language") @ApiParam String language,@RequestParam(value = "num") @ApiParam
            Status num,@RequestParam(value = "mobile") @ApiParam String mobile, @RequestBody Map<String, Object> content);

    @ApiOperation(value = "发送简单邮件")
    @PostMapping("/email/sendSimpleMail")
    BaseResponse sendSimpleMail(@RequestParam(value = "sendTo") @ApiParam String sendTo,
                                @RequestParam(value = "title") @ApiParam String title,
                                @RequestParam(value = "content") @ApiParam String content);

    @ApiOperation(value = "发送模板邮件")
    @PostMapping("/email/sendTemplateMail")
    BaseResponse sendTemplateMail(@RequestParam(value = "sendTo") @ApiParam String sendTo, @RequestParam(value = "languageNum") @ApiParam
            String languageNum, @RequestParam(value = "templateNum") @ApiParam Status templateNum, @RequestBody Map<String, Object> param);
}
