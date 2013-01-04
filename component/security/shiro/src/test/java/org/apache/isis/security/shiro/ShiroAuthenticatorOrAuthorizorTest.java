/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.security.shiro;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.unittestsupport.jmock.auto.Mock;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ShiroAuthenticatorOrAuthorizorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;
    
    private ShiroAuthenticatorOrAuthorizor authOrAuth;

    @Before
    public void setUp() throws Exception {
        authOrAuth = new ShiroAuthenticatorOrAuthorizor(mockConfiguration);
    }
    
    @Test
    public void happyCase() throws Exception {

        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString(with("isis.security.shiro.iniLocation"));
                will(returnValue("classpath:shiro.ini"));
            }
        });


        authOrAuth.init();
        assertThat(authOrAuth.canAuthenticate(AuthenticationRequestPassword.class), is(true));
        
        AuthenticationRequest ar = new AuthenticationRequestPassword("lonestarr", "vespa");
        AuthenticationSession isisAuthSession = authOrAuth.authenticate(ar, null);
        
        assertThat(isisAuthSession, is(not(nullValue())));
        assertThat(isisAuthSession.getUserName(), is("lonestarr"));
        assertThat(isisAuthSession.getValidationCode(), is(nullValue()));

        Identifier changeAddressIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeAddress", String.class, String.class);
        assertThat(authOrAuth.isVisibleInAnyRole(changeAddressIdentifier), is(true));
        
        Identifier changeEmailIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeEmail", String.class);
        assertThat(authOrAuth.isVisibleInAnyRole(changeEmailIdentifier), is(true));

        Identifier submitOrderIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Order", "submit");
        assertThat(authOrAuth.isVisibleInAnyRole(submitOrderIdentifier), is(true));

        Identifier cancelOrderIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Order", "cancel");
        assertThat(authOrAuth.isVisibleInAnyRole(cancelOrderIdentifier), is(false));

        
//        // Use the shiro.ini file at the root of the classpath
//        // (file: and url: prefixes load from files and urls respectively):
//        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
//        SecurityManager securityManager = factory.getInstance();
//
//        // for this simple example quickstart, make the SecurityManager
//        // accessible as a JVM singleton.  Most applications wouldn't do this
//        // and instead rely on their container configuration or web.xml for
//        // webapps.  That is outside the scope of this simple quickstart, so
//        // we'll just do the bare minimum so you can continue to get a feel
//        // for things.
//        SecurityUtils.setSecurityManager(securityManager);
//
//        // Now that a simple Shiro environment is set up, let's see what you can do:
//
//        // get the currently executing user:
//        Subject currentUser = SecurityUtils.getSubject();
//
//        // Do some stuff with a Session (no need for a web or EJB container!!!)
//        Session session = currentUser.getSession();
//        session.setAttribute("someKey", "aValue");
//        String value = (String) session.getAttribute("someKey");
//        if (value.equals("aValue")) {
//            System.out.println("Retrieved the correct value! [" + value + "]");
//        }
//
//        // let's login the current user so we can check against roles and permissions:
//        if (!currentUser.isAuthenticated()) {
//            UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
//            token.setRememberMe(true);
//            try {
//                currentUser.login(token);
//            } catch (UnknownAccountException uae) {
//                System.out.println("There is no user with username of " + token.getPrincipal());
//            } catch (IncorrectCredentialsException ice) {
//                System.out.println("Password for account " + token.getPrincipal() + " was incorrect!");
//            } catch (LockedAccountException lae) {
//                System.out.println("The account for username " + token.getPrincipal() + " is locked.  " +
//                        "Please contact your administrator to unlock it.");
//            }
//            // ... catch more exceptions here (maybe custom ones specific to your application?
//            catch (AuthenticationException ae) {
//                //unexpected condition?  error?
//            }
//        }
//
//        //say who they are:
//        //print their identifying principal (in this case, a username):
//        System.out.println("User [" + currentUser.getPrincipal() + "] logged in successfully.");
//
//        //test a role:
//        if (currentUser.hasRole("schwartz")) {
//            System.out.println("May the Schwartz be with you!");
//        } else {
//            System.out.println("Hello, mere mortal.");
//        }
//
//        //test a typed permission (not instance-level)
//        if (currentUser.isPermitted("com.mycompany.myapp:Customer:changeAddress:w")) {
//            System.out.println("You may invoke the customer's changeAddress action.");
//        } else {
//            System.out.println("Sorry, changing address is only allowed for schwartz masters only.");
//        }
//
//        //all done - log out!
//        currentUser.logout();

    }

}