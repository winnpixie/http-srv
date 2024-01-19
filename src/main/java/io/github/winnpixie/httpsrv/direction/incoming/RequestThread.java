package io.github.winnpixie.httpsrv.direction.incoming;

import io.github.winnpixie.httpsrv.HttpServer;
import io.github.winnpixie.httpsrv.direction.outgoing.Response;
import io.github.winnpixie.httpsrv.direction.outgoing.ResponseStatus;
import io.github.winnpixie.httpsrv.direction.shared.SocketHandler;

import java.net.Socket;

public class RequestThread extends Thread {
    private final HttpServer server;
    private final SocketHandler socketHandler;

    public RequestThread(HttpServer server, Socket socket) {
        this.server = server;
        this.socketHandler = new SocketHandler(socket);

        super.setName("http-srv_%s_%d".formatted(socket.getInetAddress(), System.nanoTime()));
    }

    public HttpServer getServer() {
        return server;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    @Override
    public void run() {
        try (Socket sock = socketHandler.getSocket()) {
            Request request = new Request(this);
            request.read();

            Response response = new Response(request);
            if (request.getHeader("Host", false).isEmpty() || request.getPath().indexOf('/') > 0) {
                response.setStatus(ResponseStatus.BAD_REQUEST);
            } else {
                server.getEndpointManager().getEndpoint(request.getPath()).getHandler().accept(response);
            }
            response.write();

            server.getLogger().info("ip-addr=%s/x-fwd=%s path='%s' status-code=%d user-agent='%s'"
                    .formatted(sock.getInetAddress(), request.getHeader("X-Forwarded-For", false), request.getPath(),
                            response.getStatus().getCode(), request.getHeader("User-Agent", false)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}