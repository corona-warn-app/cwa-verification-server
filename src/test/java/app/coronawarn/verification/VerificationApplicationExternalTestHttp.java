//package app.coronawarn.verification;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.InetAddress;
//import java.net.Socket;
//
//import app.coronawarn.verification.config.SecurityConfig;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = RANDOM_PORT)
//@ComponentScan(basePackages = {"app.coronawan.verification"},
//  excludeFilters = {@ComponentScan.Filter(
//    type = FilterType.ASSIGNABLE_TYPE,
//    value = {SecurityConfig.class})
//  })
//public class VerificationApplicationExternalTestHttp {
//
//  @LocalServerPort
//  private int port;
//
//  @Test
//  public void testChunkedModeIsDenied() throws IOException {
//    Socket socket = new Socket(InetAddress.getLocalHost(), port);
//    PrintWriter writer = new PrintWriter(socket.getOutputStream());
//
//    writer.print("POST /version/v1/registrationToken HTTP/1.1\r\n");
//    writer.print("Host: 127.0.0.1:" + port + "\r\n");
//    writer.print("Content-Type: application/json\r\n");
//    writer.print("Transfer-Encoding: Chunked\r\n\r\n");
//    writer.flush();
//
//    writer.print("{ \"randomBody\": 42 }");
//    writer.flush();
//
//    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//    Assert.assertEquals("HTTP/1.1 406", reader.readLine().trim());
//
//    reader.close();
//    socket.close();
//  }
//
//  private class SecurityAutoConfiguration {
//  }
//}
