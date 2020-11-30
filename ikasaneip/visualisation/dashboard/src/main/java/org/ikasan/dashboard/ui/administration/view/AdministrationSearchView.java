package org.ikasan.dashboard.ui.administration.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.ikasan.dashboard.ui.layout.IkasanAppLayout;
import org.ikasan.dashboard.ui.search.component.SolrSearchFilteringGrid;
import org.ikasan.dashboard.ui.search.component.filter.SearchFilter;
import org.ikasan.dashboard.ui.util.DateFormatter;
import org.ikasan.solr.model.IkasanSolrDocument;
import org.ikasan.solr.service.SolrGeneralServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Route(value = "adminSearchView", layout = IkasanAppLayout.class)
@UIScope
@Component
@PageTitle("Ikasan - Administration Search")
public class AdministrationSearchView extends VerticalLayout implements BeforeEnterObserver
{
    private Logger logger = LoggerFactory.getLogger(AdministrationSearchView.class);

//    @Resource
//    private UserService userService;
//
//    @Resource
//    private SecurityService securityService;
//
//    @Resource
//    private SystemEventService systemEventService;
//
//    @Resource
//    private SystemEventLogger systemEventLogger;
//
//    private FilteringGrid<User> userGrid;
//
//    private DataProvider<User, UserFilter> dataProvider;
//    private ConfigurableFilterDataProvider<User,Void,UserFilter> filteredDataProvider;
//
//    private List<User> users;
//
//    private UserFilter userFilter = new UserFilter();
//
//    private Tooltip newUserTooltip;
//    private Button addNewUserButton;

    @Resource
    private SolrGeneralServiceImpl solrSearchService;

    private SolrSearchFilteringGrid searchResultsGrid;
    private SearchFilter searchFilter = new SearchFilter();

    /**
     * Constructor
     */
    public AdministrationSearchView()
    {
        super();
    }

    protected void init()
    {
        this.setSizeFull();
        this.setSpacing(true);

        H2 userDirectoriesLabel = new H2(getTranslation("label.user-management", UI.getCurrent().getLocale(), null));

        HorizontalLayout labelLayout = new HorizontalLayout();
        labelLayout.setJustifyContentMode(JustifyContentMode.START);
        labelLayout.setVerticalComponentAlignment(Alignment.CENTER, userDirectoriesLabel);
        labelLayout.setWidth("100%");
        labelLayout.add(userDirectoriesLabel);

        this.searchResultsGrid = new SolrSearchFilteringGrid(this.solrSearchService, this.searchFilter, new Label(""));
        // Add the icon column to the grid
        this.searchResultsGrid.addColumn(new ComponentRenderer<>(ikasanSolrDocument ->
        {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setWidth("100%");
            horizontalLayout.setJustifyContentMode(JustifyContentMode.START);

            horizontalLayout.add(ikasanSolrDocument.getEvent());


            return horizontalLayout;
        })).setFlexGrow(12).setHeader("System Event").setResizable(true);
        this.searchResultsGrid.addColumn(TemplateRenderer.<IkasanSolrDocument>of(
            "<div>[[item.date]]</div>")
            .withProperty("date",
                ikasanSolrDocument -> DateFormatter.getFormattedDate(ikasanSolrDocument.getTimeStamp()))).setHeader(getTranslation("table-header.timestamp", UI.getCurrent().getLocale()))
            .setSortable(true)
            .setKey("timestamp")
            .setFlexGrow(2)
            .setResizable(true);
        this.searchResultsGrid.setSizeFull();

        add(this.searchResultsGrid);
    }

    private void updateUsers()
    {
        this.searchResultsGrid.init(0, System.currentTimeMillis(), ""
            , List.of("systemEvent"), false, this.searchFilter);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent)
    {
        if(this.searchResultsGrid == null) {
            init();
        }

        updateUsers();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent)
    {

    }
}
