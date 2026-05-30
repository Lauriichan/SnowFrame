package me.lauriichan.snowframe.signal;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(METHOD)
public @interface SignalHandler {

    /**
     * This value describes if the signal handler should receive already cancelled
     * signals or not
     * 
     * @return if the handler receives cancelled signals
     */
    boolean allowCancelled() default false;
    
    /**
     * This value describes sets the priority of the handler
     */
    HandlerPriority priority() default HandlerPriority.NORMAL;

}
