package com.lee.agentgazjku.app;



import com.lee.agentgazjku.advisor.MyLoggerAdvisor;
import com.lee.agentgazjku.chatmemory.MysqlBasedChatMemory;
import com.lee.agentgazjku.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class CampusApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是张家口学院明湖校区的学生，一位充满亲和力的可爱校园学姐，" +
            "你的定位是校园吉祥物，专门帮助学生和老师解决校园问题，你可以像精灵一样穿梭校园，你并没有班级、专业之分" +
            "你的全名是 绿豆・糕・番茄炒蛋・麻辣小龙虾・章鱼・丸 ，你可以叫自己丸丸，可以适当用颜文字和一些emoji表情" +
            "熟悉校园生活的方方面面，致力于用耐心、细致的态度帮助学弟学妹解决问题。你的目标是：" +
            "像朋友一样交流：用口语化、接地气的表达（避免生硬官方），让用户感受到温暖和支持；" +
            "主动引导深入：当用户问题模糊时，通过提问逐步明确需求（例如：你是遇到选课冲突了吗？还是不确定选哪门课呀？）；" +
            "提供具体建议：结合校园场景给出可操作的步骤，避免笼统回答（例如：如果想申请奖学金，可以先根据学生手册查看资格，然后找相应人员咨询哦）；" +
            "关注情感需求：当用户表现出焦虑、迷茫时，先共情再解答（例如：我明白期末复习压力很大，咱们可以一起梳理下复习计划～）。";

    /**
     * 构造函数，初始化校园学姐应用
     *
     * @param dashscopeChatModel 用于处理对话的模型 dashscopeChatModel
     */
    public CampusApp(ChatModel dashscopeChatModel, JdbcTemplate jdbcTemplate) {

        // 初始化基于mysql的对话记忆实例，用于维护对话历史  （JdbcTemplate jdbcTemplate）
        ChatMemory mysqlBasedChatMemory = new MysqlBasedChatMemory(jdbcTemplate);


        // 构建 ChatClient，设置默认系统提示和记忆顾问
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(mysqlBasedChatMemory).build()
                        // 日志记录顾问拦截器，用于记录对话过程中的详细信息  可以按需开启
                           ,new MyLoggerAdvisor()
                        // 会在每次用户输入前，重新读取一次用户输入，确保理解用户意图  可以按需开启
                        //   ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 执行对话，与校园学姐进行互动，返回校园学姐的回复内容
     *
     * @param userMessage 用户消息
     * @param chatId 对话ID，用于区分不同的对话会话
     * @return 校园学姐的回复内容
     */
    public String doChat (String userMessage, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }




    /**
     * 执行对话，与校园学姐进行互动，返回校园学姐的回复内容
     * 与doChat方法不同的是，该方法结束后会生成建议报告，标题为{用户名}的建议报告 内容为建议列表
     * @return 校园学姐的回复内容
     */

    record CampusReport(String title, String content) {}


    public CampusReport doChatWithReport (String userMessage, String chatId) {
        CampusReport campusReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "在每次用户输入后，都要生成一个建议报告，标题为{用户名}的建议指南 内容为建议列表")
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(CampusReport.class);
        log.info("campusReport: {}", campusReport);
        return campusReport;
    }

    @Resource
    private VectorStore campusAppVectorStore;

    @Resource
    private Advisor campusAppRagCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 使用向量数据库进行知识问答
     * 该方法会根据用户输入的问题，从向量数据库中查询相关的知识，并返回一个包含知识问答结果的字符串
     * @param userMessage
     * @param chatId
     * @return
     */

    public String doChatWithVectorStore (String userMessage, String chatId) {
        // 查询重写
       // userMessage = queryRewriter.rewrite(userMessage);

        ChatResponse chatResponse = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                // 日志记录顾问拦截器，用于记录对话过程中的详细信息
                .advisors(new MyLoggerAdvisor())
                // RAG 知识问答顾问
               // .advisors(new QuestionAnswerAdvisor(campusAppVectorStore))
                // 使用RAG进行知识问答 基于云端
                .advisors(campusAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();

        log.info("content: {}", content);
        return content;
    }

    /**
     * 使用向量数据库进行知识问答，返回一个包含知识问答结果的字符串流
     * 该方法会根据用户输入的问题，从向量数据库中查询相关的知识，并返回一个包含知识问答结果的字符串流
     * @param userMessage
     * @param chatId
     * @return 知识问答结果的字符串流
     */
    public Flux<String> doChatWithVectorStoreStream(String userMessage, String chatId) {
        // 先执行查询重写
        //String rewrittenQuery = queryRewriter.rewrite(userMessage);

        return chatClient.prompt()
                .user(userMessage) // 使用重写后的查询
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId)) // 会话记忆
                .advisors(new MyLoggerAdvisor()) // 日志顾问
                .advisors(campusAppRagCloudAdvisor) // 云端RAG顾问
                .stream() // 启用流式响应
                .content(); // 提取文本内容流
    }



    //调用工具
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTool (String userMessage, String chatId) {
        ChatResponse chatResponse  = chatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 使用工具进行对话，返回一个包含工具调用结果的字符串流
     * 该方法会根据用户输入的问题，调用注册的工具，并返回一个包含工具调用结果的字符串流
     * @param userMessage
     * @param chatId
     * @return 工具调用结果的字符串流
     */

    public Flux<String> doChatWithToolStream(String userMessage, String chatId) {
        // 先执行查询重写
        //  String rewrittenQuery = queryRewriter.rewrite(userMessage);

        return chatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .advisors(campusAppRagCloudAdvisor) // 云端RAG顾问
                .toolCallbacks(allTools)
                .stream()
                .content();
    }



}