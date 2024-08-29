package it.pinta.demo.jpa.mysql.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class RepoAuditor {

    @Before("execution(* it.pinta.demo.jpa.mysql.model.repository.*.*(..))")
    public void logRepoAction(JoinPoint jp) {
        Object[] args = jp.getArgs();
        log.info("Bello de casa " + args);
    }
}
