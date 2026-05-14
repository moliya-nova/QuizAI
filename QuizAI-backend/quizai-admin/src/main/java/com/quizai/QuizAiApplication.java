package com.quizai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.quizai.**.mapper")
@SpringBootApplication
public class QuizAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizAiApplication.class, args);
    }

}
