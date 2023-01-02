package io.jackhenry.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class JSendResponse {
  private final static String SUCCESS = "success";
  private final static String FAIL = "fail";
  private final static String ERROR = "error";

  private record Message(String message) {
  }

  protected String status;
  protected String message;
  protected Object data;

  protected JSendResponse() {
  }

  public String getStatus() {
    return this.status;
  }

  public String getMessage() {
    return this.message;
  }

  public Object getData() {
    return this.data;
  }

  public static JSendResponseBuilder status(String status) {
    return JSendResponseBuilder.newInstance().status(status);
  }

  public static JSendResponseBuilder success(Object data) {
    return JSendResponseBuilder.newInstance().status(JSendResponse.SUCCESS).data(data);
  }

  public static JSendResponseBuilder fail(Object data) {
    if (data instanceof String) {
      var message = (String) data;
      return JSendResponseBuilder.newInstance().status(JSendResponse.FAIL).data(new Message(message));
    }
    return JSendResponseBuilder.newInstance().status(JSendResponse.FAIL).data(data);
  }

  public static JSendResponseBuilder error(String message) {
    return JSendResponseBuilder.newInstance().status(JSendResponse.ERROR).message(message);
  }

  public static class JSendResponseBuilder {
    private JSendResponse response;

    protected JSendResponseBuilder() {
      this.response = new JSendResponse();
    }

    public JSendResponseBuilder status(String status) {
      this.response.status = status;
      return this;
    }

    public JSendResponseBuilder data(Object data) {
      this.response.data = data;
      return this;
    }

    public JSendResponseBuilder message(String message) {
      this.response.message = message;
      return this;
    }

    public JSendResponse build() {
      return this.response;
    }

    public static JSendResponseBuilder newInstance() {
      return new JSendResponseBuilder();
    }
  }
}
