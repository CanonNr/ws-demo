package com.example.wsdemo.service;

import com.example.wsdemo.exception.SocketServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/target")
@Component
@Slf4j
public class WebSocketServer {

    private static final ConcurrentHashMap<String,WebSocketServer> socketMap = new ConcurrentHashMap<String,WebSocketServer>();

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    // 接收sid
    private String sid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        sid = UUID.randomUUID().toString().replace("-","");
        this.session = session;
        socketMap.put(sid,this);     //加入set中
        try {
            sendMessage(sid);
        } catch (IOException e) {
            log.error("websocket IO异常");
        }
    }

    @OnClose
    public void onClose() {
        socketMap.remove(sid);  //从set中删除
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("收到来自窗口"+sid+"的信息:"+message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static void sendInfo(String message,String sid) throws SocketServerException {
        log.info("推送消息到窗口"+sid+"，推送内容:"+message);
        try {
            WebSocketServer server = socketMap.get(sid);
            server.sendMessage(message);
        } catch (IOException e) {
            log.error("Server Error : {}",e.getMessage());
            throw new SocketServerException("服务端错误");
        }catch (NullPointerException e){
            log.error("Session Id : {} Not Found",sid);
            throw new SocketServerException("没有找到对应的会话");
        }

    }

    public static ConcurrentHashMap<String, WebSocketServer> getWebSocketSet() {
        return socketMap;
    }
}

