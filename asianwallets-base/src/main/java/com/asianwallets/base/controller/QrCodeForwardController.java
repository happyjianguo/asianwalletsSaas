package com.asianwallets.base.controller;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Api(description = "静态码跳转功能")
@RequestMapping("/qr")
public class QrCodeForwardController {
    /**
     * 跳转到前端url
     */
    @Value("${file.http.frontPage}")
    private String frontPage;

    @ApiOperation(value = "静态码跳转功能")
    @GetMapping("/forward")
    @CrossOrigin
    public void forward(@RequestParam @ApiParam String id, HttpServletResponse response, HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        ModelAndView mv=new ModelAndView();
        mv.addObject("id",id);
        mv.addObject("userAgent",userAgent);
        if(userAgent!=null){
            try {
                response.sendRedirect(frontPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
