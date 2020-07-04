package org.camunda.test.bpm.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.test.bpm.model.CustomerData;

public class StoreDataDelegate implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		CustomerData customerData = (CustomerData) execution.getVariable("customerData");
		execution.setVariable("customerData_firstname", customerData.getFirstname());
	}

}