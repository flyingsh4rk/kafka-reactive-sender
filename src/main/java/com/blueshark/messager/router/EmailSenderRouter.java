//package com.blueshark.messager.router;
//
//import com.blueshark.messager.handler.EmailHandler;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.RouterFunctions;
//import org.springframework.web.reactive.function.server.ServerResponse;
//
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//
//@Configuration(proxyBeanMethods = false)
//public class EmailSenderRouter {
//    @Bean
//    public RouterFunction<ServerResponse> emailRouter(EmailHandler emailHandler) {
//        return RouterFunctions
//                .nest(path("/email"), route(POST("send").and(accept(MediaType.APPLICATION_JSON)), emailHandler::send));
//    }
//}
