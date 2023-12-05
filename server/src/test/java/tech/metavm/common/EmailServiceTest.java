package tech.metavm.common;

import junit.framework.TestCase;

public class EmailServiceTest extends TestCase {

    private EmailService emailService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        emailService = new EmailServiceImpl("smtp.163.com",
                25,
                "15968879210@163.com",
                "85263670");
    }

    public void test() {
        emailService.send("15968879210@163.com", "Test", "Message sent by Java application");
    }

}