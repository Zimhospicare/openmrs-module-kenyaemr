/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.reporting.data.converter.definition.evaluator.opd;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.opd.OPDClinicalDiagnosisDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.opd.OPDLabResultsDataDefinition;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.evaluator.EncounterDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates Lab results
 * MOH 240 Lab Register 
 */
@Handler(supports= OPDLabResultsDataDefinition.class, order=50)
public class OPDLabResultsDataEvaluator implements EncounterDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedEncounterData evaluate(EncounterDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedEncounterData c = new EvaluatedEncounterData(definition, context);

		String qry = "SELECT le.encounter_id,\n" +
			"if(le.lab_test in (SELECT concept_set FROM openmrs.concept_set), GROUP_CONCAT(\n" +
			"             CONCAT(COALESCE(le.result_test_name, '-'), '|', le.result_name)\n" +
			"     SEPARATOR ', '),le.result_name) as test_result\n" +
			"FROM kenyaemr_etl.etl_laboratory_extract le\n" +
			"INNER JOIN kenyaemr_etl.etl_patient_demographics p ON p.patient_id = le.patient_id and p.voided = 0\n" +
			"where date(le.visit_date) BETWEEN date(:startDate) AND date(:endDate)\n" +
			"group by le.encounter_id";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Date startDate = (Date)context.getParameterValue("startDate");
        Date endDate = (Date)context.getParameterValue("endDate");
        queryBuilder.addParameter("endDate", endDate);
        queryBuilder.addParameter("startDate", startDate);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
