package com.lee.agentgazjku.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest

class campusAppTest {

    @Resource
    private CampusApp campusApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();

        //第一轮对话
        String question = "你好，我叫lee";
        String answer = campusApp.doChat(question, chatId);
        Assertions.assertNotNull(answer);
        //第二轮对话
        question = "我最近学习上有点焦虑";
        answer = campusApp.doChat(question, chatId);
        Assertions.assertNotNull(answer);
        //第三轮对话
        question = "我刚才问你什么了";
        answer = campusApp.doChat(question, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        //一轮对话
        String question = "你好，我叫lee，最近要进行期末考试了，我不知道该怎么准备";
        CampusApp.CampusReport campusReport = campusApp.doChatWithReport(question, chatId);
        Assertions.assertNotNull(campusReport);
    }

    @Test
    void doChatWithVectorStore() {
        String chatId = UUID.randomUUID().toString();
        String question = "我想知道图书馆在哪里";
        String answer = campusApp.doChatWithVectorStore(question, chatId);
        Assertions.assertNotNull(answer);

        question = "学妹会爱上我吗";
        answer = campusApp.doChatWithVectorStore(question, chatId);
        Assertions.assertNotNull(answer);

        question = "去哪里找到学妹的联系方式";
        answer = campusApp.doChatWithVectorStore(question, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTool() {

        //PDF文件操作
       testMessage("生成一份宿舍时间pdf");

      //测试联网工具调用
        testMessage("明湖公园在哪里");

        //测试文件操作工具
        testMessage("保存我的复习资料为文件");


        //网页抓取
        testMessage("我想知道张家口学院官网有什么内容");

        //网络资源下载
        testMessage("下载一张张家口学院图片");

    }

    private void testMessage(String question) {
        String chatId = UUID.randomUUID().toString();
        String answer = campusApp.doChatWithTool(question, chatId);
        Assertions.assertNotNull(answer);
    }

/*    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String question = "我想知道姚家房有什么好吃的";
        String answer = campusApp.doChatWithMcp(question, chatId);
        Assertions.assertNotNull(answer);
    }*/
}