package com.dodo.error;

import org.apache.camel.Exchange;

public class ErrorService {
    
    public void locationError(Exchange exchange) {
        exchange.getIn().setBody("{ 'error': '`latitude` or `longitudue` is not appropriate }");
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
    }
    
}
