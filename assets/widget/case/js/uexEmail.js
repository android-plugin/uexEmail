if(UNIT_TEST){
    var uexEmailCase = {
        "open":function(){
            var email = "fei.ye@zymobi.com";
            var subject = "测试邮件";
            var content = "我是邮件";
            var attachmentPath = "res://image.jpg";
            var mimeType = "image/jpeg";
            uexEmail.open(email, subject, content,attachmentPath,mimeType);
            UNIT_TEST.assert(true);
        }
    }
    UNIT_TEST.addCase("email",uexEmailCase);
}