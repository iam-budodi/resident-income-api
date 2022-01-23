package com.income.calculator.filter;

import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.income.calculator.model.User;
import com.income.calculator.repository.UserRepository;

@RequestScoped
public class AuthenticatedUserProducer {

    @Produces
    @RequestScoped
    @AuthenticatedUser
    private User authenticatedUser;

    @Inject
    UserRepository userRepository;
    
    @Inject
	private Logger logger;

    public void handleAuthenticationEvent(@Observes @AuthenticatedUser String login) { 
        this.authenticatedUser = userRepository.findUserByLogin(login).get(0);
		logger.info("\n\n#### valid authenticated username in handler : " + authenticatedUser);
        
    }
}
