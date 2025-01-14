package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpResponseStatus;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

public class HttpMethod {
    private final String verb;
    private final Consumer<HttpRequest> sendFunction;

    public static final HttpMethod GET = new HttpMethod("GET", request -> execute(request, conn -> {
    }));

    public static final HttpMethod POST = new HttpMethod("POST", request -> execute(request, conn -> {
        conn.setRequestProperty("Content-Length", Integer.toString(request.getBody().length));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(request.getBody());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }));

    public HttpMethod(String verb, Consumer<HttpRequest> sendFunction) {
        this.verb = verb;
        this.sendFunction = sendFunction;
    }

    public String getVerb() {
        return verb;
    }

    public Consumer<HttpRequest> getSendFunction() {
        return sendFunction;
    }

    public static HttpMethod custom(String verb, Consumer<HttpRequest> onSend) {
        return new HttpMethod(verb, onSend);
    }

    public static HttpMethod getByVerb(String verb) {
        return switch (verb) {
            case "GET" -> GET;
            case "POST" -> POST;
            default -> null;
        };
    }

    private static void execute(HttpRequest request, Consumer<HttpURLConnection> additionalSetupFunction) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) request.getUrl().openConnection(request.getProxy());
            conn.setRequestMethod(request.getMethod().getVerb());
            conn.setInstanceFollowRedirects(request.isFollowRedirects());
            request.getHeaders().forEach(conn::setRequestProperty);

            additionalSetupFunction.accept(conn);

            try (InputStream is = conn.getInputStream()) {
                request.getOnSuccess().accept(new HttpResponse(request, HttpResponseStatus.fromCode(conn.getResponseCode()),
                        IOHelper.toByteArray(is), conn.getHeaderFields()));
            }
        } catch (Exception e) {
            request.getOnFailure().accept(e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}