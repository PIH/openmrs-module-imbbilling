/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 *  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.mohbilling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class is meant to be used to initialize a test environment with the test data defined in BillingTestData
 * This test should not normally run in the course of normal building, but is meant to be run ad-hoc by a developer
 * If you wish to use this class, you will need to fill in valid database credentials for the system you wish to affect
 * WARNING: Use this class with caution!  This includes utility methods to delete all billing-related data, and repopulate
 */
public class TestEnvironmentSetup extends BaseModuleContextSensitiveTest {

    protected static final Log log = LogFactory.getLog(TestEnvironmentSetup.class);

    @Override
    public Boolean useInMemoryDatabase() {
        return true;
    }

    /**
     * To run this on a non-in-memory database, enter the appropriate connection information below that you have access to
     */
    @Override
    public Properties getRuntimeProperties() {
        Properties p = super.getRuntimeProperties();
        if (!useInMemoryDatabase()) {
            p.setProperty("connection.url", "jdbc:mysql://localhost:3306/change_me_with_caution?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
            p.setProperty("connection.username", "openmrs");
            p.setProperty("connection.password", "openmrs");
            p.setProperty("junit.username", "junit");
            p.setProperty("junit.password", "Test123");
        }
        return p;
    }

    @Test
    //@Rollback(false)
    public void executeSetup() throws Exception {
        if (!useInMemoryDatabase()) {
            if (!Context.isSessionOpen()) {
                Context.openSession();
            }
            Context.clearSession();
            authenticate();
        }
        setupAllBillingData();
    }

    public void setupAllBillingData() {
        BillingTestData.createFacilityServicePrices();
    }

    /**
     * Removes all billing-related data from the billing tables
     */
    protected void deleteAllBillingData() {
        for (String tablename : getBillingTables()) {
            executeSqlUpdate("truncate table " + tablename);
        }
    }

    /**
     * This method can be used to reset your database to pre-module-install state
     */
    protected void dropBillingSchema() {
        for (String tablename : getBillingTables()) {
            executeSqlUpdate("drop table " + tablename);
        }
        executeSqlUpdate("update global_property set property_value = '' where property = 'mohbilling.database_version'");
    }

    /**
     * @return a List of all billing tables in order of dependency descending
     */
    protected List<String> getBillingTables() {
        List<String> l = new ArrayList<String>();
        l.add("moh_bill_patient_service_bill");
        l.add("moh_bill_billable_service");
        l.add("moh_bill_payment");
        l.add("moh_bill_facility_service_price");
        l.add("moh_bill_service_category");
        l.add("moh_bill_insurance_rate");
        l.add("moh_bill_recovery");
        l.add("moh_bill_patient_bill");
        l.add("moh_bill_beneficiary");
        l.add("moh_bill_insurance_policy");
        l.add("moh_bill_third_party");
        l.add("moh_bill_insurance");
        return l;
    }

    /**
     * Execute the passed in SQL update query
     */
    private void executeSqlUpdate(String sql) {
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.executeUpdate();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to execute sql <" + sql + ">", e);
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception e) {
                }
            }
        }
    }
}
