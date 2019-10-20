package org.ikasan.dashboard.ui.administration.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.ikasan.dashboard.ui.administration.filter.RoleFilter;
import org.ikasan.dashboard.ui.general.component.FilteringGrid;
import org.ikasan.dashboard.ui.util.SystemEventConstants;
import org.ikasan.dashboard.ui.util.SystemEventLogger;
import org.ikasan.security.model.IkasanPrincipal;
import org.ikasan.security.model.Role;
import org.ikasan.security.service.SecurityService;

import java.util.List;
import java.util.Set;

public class SelectRoleDialog extends Dialog
{
    private IkasanPrincipal principal;
    private SecurityService securityService;
    private SystemEventLogger systemEventLogger;

    public SelectRoleDialog(IkasanPrincipal principal, SecurityService securityService, SystemEventLogger systemEventLogger)
    {
        this.principal = principal;
        if(this.principal == null)
        {
            throw new IllegalArgumentException("principal cannot be null!");
        }
        this.securityService = securityService;
        if(this.securityService == null)
        {
            throw new IllegalArgumentException("securityService cannot be null!");
        }
        this.systemEventLogger = systemEventLogger;
        if(this.systemEventLogger == null)
        {
            throw new IllegalArgumentException("systemEventLogger cannot be null!");
        }

        init();
    }

    private void init()
    {
        H3 selectRoleLabel = new H3(getTranslation("label.select-role", UI.getCurrent().getLocale(), null));

        List<Role> roles = this.securityService.getAllRoles();

        Set<Role> principalRoles = principal.getRoles();

        roles.removeAll(principalRoles);

        RoleFilter roleFilter = new RoleFilter();

        FilteringGrid<Role> roleGrid = new FilteringGrid<>(roleFilter);
        roleGrid.setSizeFull();

        roleGrid.setClassName("my-grid");
        roleGrid.addColumn(Role::getName).setKey("role").setFlexGrow(5);

        roleGrid.addItemDoubleClickListener((ComponentEventListener<ItemDoubleClickEvent<Role>>) roleItemDoubleClickEvent ->
        {
            principal.getRoles().add(roleItemDoubleClickEvent.getItem());

            this.securityService.savePrincipal(principal);

            String action = String.format("Role [%s] added to group [%s].", roleItemDoubleClickEvent.getItem().getName(), principal.getName());

            this.systemEventLogger.logEvent(SystemEventConstants.DASHBOARD_PRINCIPAL_ROLE_CHANGED_CONSTANTS, action, principal.getName());

            this.close();
        });

        HeaderRow hr = roleGrid.appendHeaderRow();
        roleGrid.addGridFiltering(hr, roleFilter::setNameFilter, "role");

        roleGrid.setItems(roles);

        roleGrid.setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.add(selectRoleLabel, roleGrid);

        layout.setWidth("500px");
        layout.setHeight("300px");

        this.add(layout);
    }
}