package com.runssnail.ddd.spring;

import com.runssnail.ddd.command.CommandBus;
import com.runssnail.ddd.command.DefaultCommandBus;
import com.runssnail.ddd.command.handler.CommandExceptionHandler;
import com.runssnail.ddd.command.handler.CommandHandler;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.runssnail.ddd.command.handler.CannotFindCommandHandlerException;
import com.runssnail.ddd.command.interceptor.CommandInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * CommandBus FactoryBean
 *
 * @author zhengwei
 * @date 2019-11-07 17:14
 **/
@Slf4j
public class CommandBusFactoryBean implements FactoryBean<CommandBus>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    private CommandBus commandBus;

    @Autowired
    private CommandExceptionHandler commandExceptionHandler;

    private boolean detectAllCommandHandlers = true;
    private boolean detectAllCommandInterceptors = true;

    @Override
    public CommandBus getObject() throws Exception {
        return this.commandBus;
    }

    @Override
    public Class<?> getObjectType() {
        return CommandBus.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultCommandBus commandBus = new DefaultCommandBus();
        commandBus.setCommandExceptionHandler(this.commandExceptionHandler);
        commandBus.init();

        if (this.detectAllCommandHandlers) {
            detectAllCommandHandlers(commandBus);
        }
        if (this.detectAllCommandInterceptors) {
            detectAllCommandInterceptors(commandBus);
        }

        this.commandBus = commandBus;
    }

    private void detectAllCommandInterceptors(DefaultCommandBus commandBus) {
        Map<String, CommandInterceptor> beansOfInterceptors = this.applicationContext.getBeansOfType(CommandInterceptor.class);

        if (MapUtils.isEmpty(beansOfInterceptors)) {
            log.info("can not find any CommandInterceptor in the spring context");
            return;
        }

        log.info("find CommandInterceptor in the spring context {}", beansOfInterceptors.size());

        List<CommandInterceptor> interceptors = new ArrayList<>(beansOfInterceptors.values());

        // 排序
        AnnotationAwareOrderComparator.sort(interceptors);

        for (CommandInterceptor interceptor : interceptors) {
            commandBus.registerCommandInterceptor(interceptor);
        }

        log.info("find CommandInterceptor final commandInterceptors={}", interceptors);

    }

    private void detectAllCommandHandlers(DefaultCommandBus commandBus) {
        Map<String, CommandHandler> beansOfCommandHandlers = this.applicationContext.getBeansOfType(CommandHandler.class);

        if (MapUtils.isEmpty(beansOfCommandHandlers)) {
            throw new CannotFindCommandHandlerException("can not find any CommandHandler in spring context");
        }

        log.info("find CommandHandler in spring context {}", beansOfCommandHandlers.size());

        Collection<CommandHandler> commandHandlers = beansOfCommandHandlers.values();
        for (CommandHandler commandHandler : commandHandlers) {
            commandBus.registerCommandHandler(commandHandler);
        }

        log.info("find CommandHandler final {}", commandHandlers);
    }

    public boolean isDetectAllCommandHandlers() {
        return detectAllCommandHandlers;
    }

    public void setDetectAllCommandHandlers(boolean detectAllCommandHandlers) {
        this.detectAllCommandHandlers = detectAllCommandHandlers;
    }

    public boolean isDetectAllCommandInterceptors() {
        return detectAllCommandInterceptors;
    }

    public void setDetectAllCommandInterceptors(boolean detectAllCommandInterceptors) {
        this.detectAllCommandInterceptors = detectAllCommandInterceptors;
    }
}
