/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.dv8.eks.testutil;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.persistence.EntityManager;

public class IntegrationTestModule extends AbstractModule {

    EntityManager em;

    @Override
    protected void configure() {
        em = IntegrationTestUtil.em();
    }

    @Provides
    public EntityManager em() {
        return em;
    }

}
