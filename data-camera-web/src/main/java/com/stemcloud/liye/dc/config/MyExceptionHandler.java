package com.stemcloud.liye.dc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Belongs to data-camera-web
 * Description:
 *  self exception, HttpStatus.INTERNAL_SERVER_ERROR
 *  filter http status 500
 * @author liye on 2017/11/7
 */
@ControllerAdvice
public class MyExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 运行时异常
     * @param exception
     * @return
     */
    @ExceptionHandler({ RuntimeException.class })
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView processException(RuntimeException exception) {
        logger.info("Self-RuntimeException");
        ModelAndView m = new ModelAndView();
        m.addObject("exception", exception.getMessage());
        exception.printStackTrace();
        m.setViewName("exception");
        return m;
    }

    /**
     * Excepiton异常
     * @param exception
     * @return
     */
    @ExceptionHandler({ Exception.class })
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView processException(Exception exception) {
        logger.info("Self-Exception");
        ModelAndView m = new ModelAndView();
        m.addObject("exception", exception.getMessage());
        m.setViewName("exception");
        exception.printStackTrace();
        return m;

    }
}
