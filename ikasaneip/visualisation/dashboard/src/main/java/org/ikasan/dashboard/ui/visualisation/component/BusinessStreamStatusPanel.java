package org.ikasan.dashboard.ui.visualisation.component;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import org.ikasan.business.stream.metadata.model.BusinessStream;
import org.ikasan.business.stream.metadata.model.Flow;
import org.ikasan.dashboard.broadcast.FlowState;
import org.ikasan.dashboard.broadcast.State;
import org.ikasan.dashboard.cache.CacheStateBroadcaster;
import org.ikasan.dashboard.cache.FlowStateCache;
import org.ikasan.dashboard.ui.general.component.TooltipHelper;
import org.ikasan.dashboard.ui.visualisation.event.GraphViewChangeEvent;
import org.ikasan.dashboard.ui.visualisation.event.GraphViewChangeListener;
import org.ikasan.spec.metadata.ModuleMetaData;
import org.ikasan.spec.metadata.ModuleMetaDataService;
import org.ikasan.spec.module.client.ModuleControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class BusinessStreamStatusPanel extends HorizontalLayout implements GraphViewChangeListener
{
    private Logger logger = LoggerFactory.getLogger(BusinessStreamStatusPanel.class);

    private Button runningButton;
    private Button stoppedButton;
    private Button stoppedInErrorButton;
    private Button recoveringButton;
    private Button pauseButton;

    private BusinessStream currentBusinessStream;
    private BusinessStreamVisualisation businessStreamVisualisation;
    private ModuleMetaDataService moduleMetaDataService;

    private Registration broadcasterRegistration;
    private ModuleControlService moduleControlRestService;

    private Tooltip runningButtonTooltip;
    private Tooltip stoppedButtonTooltip;
    private Tooltip stoppedInErrorButtonTooltip;
    private Tooltip recoveringButtonTooltip;
    private Tooltip pauseButtonTooltip;

    private Map<String, ModuleMetaData> moduleMetaDataMap;

    public BusinessStreamStatusPanel(ModuleControlService moduleControlRestService,
                                     ModuleMetaDataService moduleMetaDataService)
    {
        this.moduleControlRestService = moduleControlRestService;
        this.moduleMetaDataService = moduleMetaDataService;
        init();
    }

    protected void init()
    {
        Label runningLabel = new Label(getTranslation("status-label.running", UI.getCurrent().getLocale()));
        runningLabel.getStyle().set("font-size", "8pt");
        Label stoppedLabel = new Label(getTranslation("status-label.stopped", UI.getCurrent().getLocale()));
        stoppedLabel.getStyle().set("font-size", "8pt");
        Label stoppedInErrorLabel = new Label(getTranslation("status-label.stopped-in-error", UI.getCurrent().getLocale()));
        stoppedInErrorLabel.getStyle().set("font-size", "8pt");
        Label recoveringLabel = new Label(getTranslation("status-label.recovering", UI.getCurrent().getLocale()));
        recoveringLabel.getStyle().set("font-size", "8pt");
        Label pausedLabel = new Label(getTranslation("status-label.paused", UI.getCurrent().getLocale()));
        pausedLabel.getStyle().set("font-size", "8pt");

        runningButton = this.createStatusButton();
        runningButton.setText("0");
        runningButtonTooltip = TooltipHelper.getTooltipForComponentBottom(runningButton, getTranslation("status.click-for-module-status", UI.getCurrent().getLocale()));

        VerticalLayout runningButtonLayout = this.createStatusButtonLayout(runningButton, runningLabel);
        runningButtonLayout.add(runningButtonTooltip);

        stoppedButton = this.createStatusButton();
        stoppedButton.setText("0");
        stoppedButtonTooltip = TooltipHelper.getTooltipForComponentBottom(stoppedButton, getTranslation("status.click-for-module-status", UI.getCurrent().getLocale()));

        VerticalLayout stoppedButtonLayout = this.createStatusButtonLayout(stoppedButton, stoppedLabel);
        stoppedButtonLayout.add(stoppedButtonTooltip);

        stoppedInErrorButton = this.createStatusButton();
        stoppedInErrorButton.setText("0");
        stoppedInErrorButtonTooltip = TooltipHelper.getTooltipForComponentBottom(stoppedInErrorButton, getTranslation("status.click-for-module-status", UI.getCurrent().getLocale()));

        VerticalLayout stoppedInErrorButtonLayout = this.createStatusButtonLayout(stoppedInErrorButton, stoppedInErrorLabel);
        stoppedInErrorButtonLayout.add(stoppedInErrorButtonTooltip);

        recoveringButton = this.createStatusButton();
        recoveringButton.setText("0");
        recoveringButtonTooltip = TooltipHelper.getTooltipForComponentBottom(recoveringButton, getTranslation("status.click-for-module-status", UI.getCurrent().getLocale()));

        VerticalLayout recoveringButtonLayout = this.createStatusButtonLayout(recoveringButton, recoveringLabel);
        recoveringButtonLayout.add(recoveringButtonTooltip);

        pauseButton = this.createStatusButton();
        pauseButton.setText("0");
        pauseButtonTooltip = TooltipHelper.getTooltipForComponentBottom(pauseButton, getTranslation("status.click-for-module-status", UI.getCurrent().getLocale()));

        VerticalLayout pauseButtonLayout = this.createStatusButtonLayout(pauseButton, pausedLabel);
        pauseButtonLayout.add(pauseButtonTooltip);

        this.moduleMetaDataMap = new HashMap<>();
        this.moduleMetaDataService.findAll().forEach(moduleMetaData -> this.moduleMetaDataMap.put(moduleMetaData.getName(),
            moduleMetaData));

        this.setSpacing(false);
        this.setMargin(false);
        this.expand(runningButtonLayout, stoppedButtonLayout, stoppedInErrorButtonLayout, recoveringButtonLayout, pauseButtonLayout);
        this.add(runningButtonLayout, stoppedButtonLayout, stoppedInErrorButtonLayout, recoveringButtonLayout, pauseButtonLayout);
        this.setVerticalComponentAlignment(Alignment.BASELINE, runningButtonLayout, stoppedButtonLayout, stoppedInErrorButtonLayout, recoveringButtonLayout, pauseButtonLayout);
    }

    private Button createStatusButton()
    {
        Button statusButton = new Button();
        statusButton.getStyle().set("color", "rgb(0,0,0)");
        statusButton.getStyle().set("font-weight", "bold");
        statusButton.getStyle().set("font-size", "14pt");
        statusButton.getStyle().set("border", "solid 2px");
        statusButton.getStyle().set("border-color", "rgb(241,90,35)");
        statusButton.setHeight("35px");
        statusButton.setWidth("35px");
        statusButton.setEnabled(true);

        statusButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent ->
        {
//            ModuleStatusDialog moduleStatusDialog = new ModuleStatusDialog(currentModule
//                , this.moduleControlRestService, this.moduleVisualisation);
//            moduleStatusDialog.open();
        });

        return statusButton;
    }

    /**
     * Create the button layout
     *
     * @param button
     * @param label
     * @return
     */
    private VerticalLayout createStatusButtonLayout(Button button, Label label)
    {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setMargin(false);
        buttonLayout.setSpacing(false);

        label.setHeight("10px");
        buttonLayout.add(button, label);
        buttonLayout.setHorizontalComponentAlignment(Alignment.CENTER, button);
        buttonLayout.setHorizontalComponentAlignment(Alignment.CENTER, label);

        buttonLayout.setFlexGrow(4.0, button);
        buttonLayout.setFlexGrow(1.0, label);

        return buttonLayout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent)
    {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = CacheStateBroadcaster.register(flowState ->
        {
            ui.access(() ->
            {
                // do something interesting here.
                logger.info("Received flow state: " + flowState);

                calculateStatus();
            });
        });

        this.stoppedButtonTooltip.attachToComponent(stoppedButton);
        this.recoveringButtonTooltip.attachToComponent(recoveringButton);
        this.runningButtonTooltip.attachToComponent(runningButton);
        this.stoppedInErrorButtonTooltip.attachToComponent(stoppedInErrorButton);
        this.pauseButtonTooltip.attachToComponent(pauseButton);
    }

    protected void calculateStatus()
    {
        if(currentBusinessStream == null){
            return;
        }

        int running = 0;
        int stopped = 0;
        int inError = 0;
        int recovering = 0;
        int paused = 0;

        for(Flow flow: currentBusinessStream.getFlows())
        {
            FlowState flowState = FlowStateCache.instance().get(moduleMetaDataMap.get(flow.getModuleName())
                , flow.getFlowName());

            if(flowState == null)
            {
                continue;
            }
            else if(flowState.getState().equals(State.RUNNING_STATE))
            {
                running++;
            }
            else if(flowState.getState().equals(State.STOPPED_STATE))
            {
                stopped++;
            }
            else if(flowState.getState().equals(State.RECOVERING_STATE))
            {
                recovering++;
            }
            else if(flowState.getState().equals(State.PAUSED_STATE))
            {
                paused++;
            }
            else if(flowState.getState().equals(State.STOPPED_IN_ERROR_STATE))
            {
                inError++;
            }
        }

        this.recoveringButton.setText(Integer.toString(recovering));
        this.stoppedButton.setText(Integer.toString(stopped));
        this.runningButton.setText(Integer.toString(running));
        this.pauseButton.setText(Integer.toString(paused));
        this.stoppedInErrorButton.setText(Integer.toString(inError));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent)
    {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    @Override
    public void onChange(GraphViewChangeEvent event)
    {
        calculateStatus();
    }

    public void setBusinessStreamVisualisation(BusinessStreamVisualisation businessStreamVisualisation) {
        this.businessStreamVisualisation = businessStreamVisualisation;
    }

    public void setBusinessStream(BusinessStream businessStream) {
        this.currentBusinessStream = businessStream;
    }
}