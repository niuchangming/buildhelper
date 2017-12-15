package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import actions.AuthAction;
import models.Account;
import models.Engineer;
import models.Project;
import models.ResponseData;
import models.Term;
import play.Application;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.*;

@SuppressWarnings("unchecked")
public class COSController extends Controller{
	@Inject private FormFactory formFactory;
	@Inject private JPAApi jpaApi;
	@Inject private Provider<Application> application;
	
	@With(AuthAction.class)
	@Transactional
	public Result createCOS(long projectId) {
		ResponseData responseData = new ResponseData();

		Account account = (Account) ctx().args.get("account");
		Engineer engineer = jpaApi.em().find(Engineer.class, account.id);
		if (engineer == null) {
			responseData.code = 4000;
			responseData.message = "You do not have permission.";
		}else{
			Project project = jpaApi.em().find(Project.class, projectId);
			if(project != null) {
				List<String> locationLineArray = jpaApi.em().createNativeQuery("SELECT df.location FROM drawingfile df WHERE df.project_id = :projectId")
						.setParameter("projectId", project.id)
						.getResultList();
				
				List<String> locations = new ArrayList<String>();
				if(locationLineArray != null && locationLineArray.size() > 0) {
					for(String locationLine : locationLineArray) {
						String[] loc = locationLine.split("\\|");
						locations.addAll(Arrays.asList(loc));
					}
				}
				
				List<Term> terms = jpaApi.em().createQuery("FROM Term", Term.class).getResultList();
				return ok(requestcos.render(project, locations, terms));
			}else {
				responseData.code = 4000;
				responseData.message = "Project doesn't exist.";
			}
		}

		return notFound(errorpage.render(responseData));
	}

}