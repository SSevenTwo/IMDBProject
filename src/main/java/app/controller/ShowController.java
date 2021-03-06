package app.controller;
import app.controller.paths.Template;
import app.controller.utils.ViewUtil;
import app.dao.CreditsRollDAO;
import app.dao.ImageDAO;
import app.dao.PersonDAO;
import app.dao.ProCoDAO;
import app.dao.ShowDAO;
import app.dao.UserReviewDAO;
import app.model.CreditsRoll;
import app.model.Person;
import app.model.ProductionCompany;
import app.model.Show;
import app.model.ShowImage;
import app.model.Enumerations.ShowStatus;
import app.model.Enumerations.ShowType;
import io.javalin.http.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam.Mode;


public class ShowController
{
    public static Handler serveShowPage = ctx ->
    {
        Map<String, Object> model = ViewUtil.baseModel(ctx);

        String showText = ctx.queryParam("show");
        Show show = ShowDAO.getShowByID(showText);
        List<CreditsRoll> creditsRoll = CreditsRollDAO.getCreditsRollByShowID(showText);
        String duration = Double.toString(show.getLength());
        String userID = ctx.sessionAttribute("currentUser");
        double averageRating = UserReviewDAO.getShowAverageRating(showText);

        model.put("show", show);
        model.put("hour",duration.substring(0,1));
        model.put("minutes", duration.substring(2));
        model.put("credits", creditsRoll);
        model.put("averageRating", averageRating);
        model.put("userID", userID);
        ctx.render(Template.SHOW, model);
    };

    public static Handler serveResultsPage = ctx -> {
        Map<String, Object> model = ViewUtil.baseModel(ctx);

        String searchText = ctx.queryParam("showTitleSearch");
        List<Show> shows = ShowDAO.getAllShowsByTitleFilter(searchText);
        model.put("shows", shows);
        model.put("query", searchText);
        ctx.render(Template.SEARCH_RESULTS, model);
    };

    public static Handler serveSearchByActorPage = ctx ->
    {
        Map<String, Object> model = ViewUtil.baseModel(ctx);
        String searchText = ctx.queryParam("showActorSearch");
        List<Show> shows = ShowDAO.getAllShowsByPersonFilter(searchText);
        model.put("shows", shows);
        model.put("query", searchText);
        ctx.render(Template.SEARCH_BY_ACTOR, model);
    };
    
    public static Handler serveAddNewShowForm = ctx ->{
    	Map<String, Object> model = ViewUtil.baseModel(ctx);
    	
    	List<ProductionCompany> productionCompanies = ProCoDAO.getAllProductionCompanies();
    	List<Person> people = PersonDAO.getAllPeople();
    	
    	model.put("PersonDAO", PersonDAO.class);
    	model.put("procos", productionCompanies);
    	model.put("people", people);
    	ctx.render(Template.NEW_SHOW_FORM, model);
    };
    	
    
    public static Handler handleAddNewShowForm = ctx ->{
    	Map<String, Object> model = ViewUtil.baseModel(ctx);
    	String title = ctx.formParam("show_title");
    	String genre = ctx.formParam("genre");
    	Double length = Double.parseDouble(ctx.formParam("length"));
    	int type = Integer.parseInt(ctx.formParam("type"));
    	ProductionCompany proco = ProCoDAO.getProductionCompanyByID(ctx.formParam("production_company"));
    	int year = Integer.parseInt(ctx.formParam("year"));
    	String synopsis = ctx.formParam("synopsis");
    	int status = Integer.parseInt(ctx.formParam("status"));	
    	
    	Show showToAdd = new Show(title, length, type, proco, genre, year, synopsis, status);
    	int showID = ShowDAO.addNewShow(showToAdd);
    	
    	boolean successAddingCast = false;
    	boolean successAddingImage = false;
    	
    	//only add credit rolls if show was successfully added
    	if(showID != -1){
    		List<CreditsRoll> creditRolls = new ArrayList<CreditsRoll>();
        	int i = 1;
        	while(ctx.formParam("actor" + i) != null) {
        		int actorID = Integer.parseInt(ctx.formParam("actor" + i));
        		String role = ctx.formParam("role" + i);
        		String character = (ctx.formParam("character" + i) == "") ? "N/A" : ctx.formParam("character" + i);
        		creditRolls.add(new CreditsRoll(actorID, role, character, year, 0, showID));
        		i++;		
        	}
        	
        	successAddingCast = CreditsRollDAO.addNewCreditRolls(creditRolls);
        	successAddingImage = ImageDAO.addNewShowImage(new ShowImage(ctx.formParam("image"), showID));
    		
    	}else {
    		model.put("status", "Your new show entry has failed. Please try again.");
    	}
    	
    	if(successAddingCast && successAddingImage) {
    		model.put("status", "Your new show entry has been submitted and will "
    				+ "be reviewed by a member of our team. Thank you.");
    	}
    	
    	ctx.render(Template.FORM_OUTCOME, model);
    };
    
    public static Handler serveEditShowForm = ctx ->{
    	Map<String, Object> model = ViewUtil.baseModel(ctx);
    	
    	Show show = ShowDAO.getShowByID(ctx.queryParam("show"));
    	model.put("show", show);
    	
    	List<ProductionCompany> procos = ProCoDAO.getAllProductionCompanies();
    	model.put("procos", procos);
    	
    	ShowImage image = show.getImages().get(0);
    	model.put("image", image.getUrl());
    	
    	List<CreditsRoll> creditsRoll = CreditsRollDAO.getCreditsRollByShowID(Integer.toString(show.getShowID()));
    	
    	//need to change the person to get ALL roles as rn role is whatever is from credits roll
    	for(CreditsRoll creditRoll : creditsRoll) {
    		creditRoll.setPerson(PersonDAO.getPersonById(Integer.toString(creditRoll.getPerson().getPersonId())));
    	}
    	model.put("credits" , creditsRoll);

    	model.put("submitted", ShowStatus.SUBMITTED.getNumDisplay());
    	model.put("pending", ShowStatus.PENDING.getNumDisplay());
    	model.put("rejected", ShowStatus.REJECTED.getNumDisplay());
    	model.put("under_inv", ShowStatus.UNDER_INVESTIGATION.getNumDisplay());
    	model.put("visible", ShowStatus.VISIBLE.getNumDisplay());
    	model.put("suspended", ShowStatus.SUSPENDED.getNumDisplay());
    	
    	model.put("movie", ShowType.MOVIE.getNumDisplay());
    	model.put("tv_series", ShowType.TV_SERIES.getNumDisplay());
    	
    	ctx.render(Template.ADMIN_EDIT_SHOW, model);
    };
    	
    
    public static Handler handleEditShowForm = ctx ->{
    	Map<String, Object> model = ViewUtil.baseModel(ctx);

    	int show_id = Integer.parseInt(ctx.formParam("show_id"));
    	String title = ctx.formParam("show_title");
    	String genre = ctx.formParam("genre");
    	Double length = Double.parseDouble(ctx.formParam("length"));
    	int type = Integer.parseInt(ctx.formParam("type"));
    	ProductionCompany proco = ProCoDAO.getProductionCompanyByID(ctx.formParam("production_company"));
    	int year = Integer.parseInt(ctx.formParam("year"));
    	String synopsis = ctx.formParam("synopsis");
    	int status = Integer.parseInt(ctx.formParam("status"));

    	Show showToEdit = new Show(show_id, title, length, type, proco, genre, year, synopsis, status);

    	ShowImage imageToEdit = new ShowImage(ctx.formParam("image"),show_id);

    	List<CreditsRoll> creditRollsToEdit = new ArrayList<CreditsRoll>();
    	int i = 1;
    	while(ctx.formParam("actor" + i) != null) {
    		int actorID = Integer.parseInt(ctx.formParam("actor" + i));
    		String role = ctx.formParam("role" + i);
    		String character = (ctx.formParam("character" + i) == "") ? "N/A" : ctx.formParam("character" + i) ;
    		creditRollsToEdit.add(new CreditsRoll(actorID, role, character, year, 0, show_id));
    		i++;
    	}

    	if(ShowDAO.editShow(showToEdit) && ImageDAO.editShowImage(imageToEdit) && CreditsRollDAO.editCreditRolls(creditRollsToEdit)){
    		model.put("status", "Show successfully updated.");
    	}else {
    		model.put("status", "Show failed to update. Please try again.");
    	}
    	ctx.render(Template.FORM_OUTCOME, model);
    };

    // HANDLE SHOW DELETE HERE

	public static Handler handleDeleteShowButton = ctx ->
	{
		Map<String, Object> model = ViewUtil.baseModel(ctx);

		int show_id = Integer.parseInt(ctx.formParam("show_id"));

		Show show = ShowDAO.getShowByID(String.valueOf(show_id));

		String title = show.getShowTitle();
		String genre = show.getGenre();
		Double length = show.getLength();
		int type = show.getType();
		ProductionCompany proco = show.getProco();
		int year = show.getYear();
		String synopsis = show.getSynopsis();
		int status = show.getStatus();

		Show showToDelete = new Show(show_id, title, length, type, proco, genre, year, synopsis, status);

		ShowImage imageToDelete = new ShowImage(ctx.formParam("image"),show_id);

		List<CreditsRoll> creditRollsToDelete = new ArrayList<CreditsRoll>();
		int i = 1;
		while(ctx.formParam("actor" + i) != null) {
			int actorID = Integer.parseInt(ctx.formParam("actor" + i));
			String role = ctx.formParam("role" + i);
			String character = (ctx.formParam("character" + i) == "") ? "N/A" : ctx.formParam("character" + i) ;
			creditRollsToDelete.add(new CreditsRoll(actorID, role, character, year, 0, show_id));
			i++;
		}

		if(CreditsRollDAO.deleteCreditRolls(creditRollsToDelete) && ImageDAO.deleteShowImage(imageToDelete) && ShowDAO.deleteShow(showToDelete)){
			model.put("status", "Show successfully deleted.");
		}else {
			model.put("status", "Show failed to delete. Please try again.");
		}
		ctx.render(Template.FORM_OUTCOME, model);
	};

	// END OF METHOD

	public static Handler serveShowsByStatusPage = ctx -> {
		Map<String, Object> model = ViewUtil.baseModel(ctx);
		List<Show> shows = ShowDAO.getAllShows();
		model.put("shows", shows);
		ctx.render(Template.ADMIN_SHOW_BY_STATUS, model);
	};
	
	public static Handler serveShowsForProco = ctx -> {
		Map<String, Object> model = ViewUtil.baseModel(ctx);
		String procoID = ctx.sessionAttribute("currentProcoID");
		List<Show> shows = ShowDAO.getAllByProcoID(procoID);
		model.put("shows", shows);
		ctx.render(Template.ADMIN_SHOW_BY_STATUS, model);
	};
	
    
    
}