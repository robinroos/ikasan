package org.ikasan.dashboard.ui.visualisation.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import org.ikasan.dashboard.broadcast.FlowState;
import org.ikasan.dashboard.broadcast.FlowStateBroadcaster;
import org.ikasan.dashboard.cache.CacheStateBroadcaster;
import org.ikasan.dashboard.cache.FlowStateCache;
import org.ikasan.dashboard.ui.visualisation.adapter.service.BusinessStreamVisjsAdapter;
import org.ikasan.dashboard.ui.visualisation.model.business.stream.BusinessStream;
import org.ikasan.dashboard.ui.visualisation.model.business.stream.Flow;
import org.ikasan.rest.client.ConfigurationRestServiceImpl;
import org.ikasan.rest.client.ModuleControlRestServiceImpl;
import org.ikasan.rest.client.TriggerRestServiceImpl;
import org.ikasan.solr.model.IkasanSolrDocument;
import org.ikasan.solr.model.IkasanSolrDocumentSearchResults;
import org.ikasan.spec.metadata.ConfigurationMetaDataService;
import org.ikasan.spec.metadata.ModuleMetaData;
import org.ikasan.spec.metadata.ModuleMetaDataService;
import org.ikasan.spec.solr.SolrGeneralService;
import org.ikasan.vaadin.visjs.network.Edge;
import org.ikasan.vaadin.visjs.network.NetworkDiagram;
import org.ikasan.vaadin.visjs.network.Node;
import org.ikasan.vaadin.visjs.network.NodeFoundStatus;
import org.ikasan.vaadin.visjs.network.listener.DoubleClickListener;
import org.ikasan.vaadin.visjs.network.options.Interaction;
import org.ikasan.vaadin.visjs.network.options.Options;
import org.ikasan.vaadin.visjs.network.options.edges.ArrowHead;
import org.ikasan.vaadin.visjs.network.options.edges.Arrows;
import org.ikasan.vaadin.visjs.network.options.edges.EdgeColor;
import org.ikasan.vaadin.visjs.network.options.edges.Edges;
import org.ikasan.vaadin.visjs.network.options.physics.Physics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BusinessStreamVisualisation extends VerticalLayout implements BeforeEnterObserver
{
    private Logger logger = LoggerFactory.getLogger(BusinessStreamVisualisation.class);
    private NetworkDiagram networkDiagram;

    private SolrGeneralService<IkasanSolrDocument, IkasanSolrDocumentSearchResults> solrSearchService;

    private Registration flowStateBroadcasterRegistration;
    private Registration cacheStateBroadcasterRegistration;

    private ModuleControlRestServiceImpl moduleControlRestService;
    private ConfigurationRestServiceImpl configurationRestService;
    private TriggerRestServiceImpl triggerRestService;
    private ModuleMetaDataService moduleMetaDataService;
    private ConfigurationMetaDataService configurationMetadataService;

    private BusinessStream businessStream;
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Node> flows = new ArrayList<>();

    private Map<String, Flow> flowMap;

    private UI current;

    public BusinessStreamVisualisation(ModuleControlRestServiceImpl moduleControlRestService
        , ConfigurationRestServiceImpl configurationRestService, TriggerRestServiceImpl triggerRestService
        , ModuleMetaDataService moduleMetaDataService
        , ConfigurationMetaDataService configurationMetadataService
        , SolrGeneralService<IkasanSolrDocument, IkasanSolrDocumentSearchResults> solrSearchService)
    {
        this.moduleControlRestService = moduleControlRestService;
        if(this.moduleControlRestService == null){
            throw new IllegalArgumentException("moduleControlRestService cannot be null!");
        }
        this.configurationRestService = configurationRestService;
        if(this.configurationRestService == null){
            throw new IllegalArgumentException("configurationRestService cannot be null!");
        }
        this.triggerRestService = triggerRestService;
        if(this.triggerRestService == null){
            throw new IllegalArgumentException("triggerRestService cannot be null!");
        }
        this.moduleMetaDataService = moduleMetaDataService;
        if(this.moduleMetaDataService == null){
            throw new IllegalArgumentException("moduleMetaDataService cannot be null!");
        }
        this.configurationMetadataService = configurationMetadataService;
        if(this.configurationMetadataService == null){
            throw new IllegalArgumentException("configurationMetadataService cannot be null!");
        }
        this.solrSearchService = solrSearchService;
        if(this.solrSearchService == null){
            throw new IllegalArgumentException("solrSearchService cannot be null!");
        }

        current = UI.getCurrent();

        this.setSizeFull();
    }

    /**
     *
     * @param json
     */
    public void createBusinessStreamGraphGraph(String json) throws IOException {
        BusinessStreamVisjsAdapter adapter = new BusinessStreamVisjsAdapter();

        this.businessStream = adapter.toBusinessStreamGraph(json);

        nodes = new ArrayList<>();
        nodes.addAll(businessStream.getFlows());
        nodes.addAll(businessStream.getDestinations());
        nodes.addAll(businessStream.getIntegratedSystems());

        flows = new ArrayList<>();
        flows.addAll(businessStream.getFlows());

        if (this.networkDiagram != null) {
            this.remove(networkDiagram);
        }

        this.populateFlowMap(businessStream.getFlows());

        updateNetworkDiagram(nodes, businessStream.getEdges());
        this.add(networkDiagram);
    }

    /**
     * Method to update the network diagram with the node and edge lists.
     *
     * @param nodes a list containing all network nodes.
     * @param edges a list containing all the network edges.
     */
    protected void updateNetworkDiagram(List<Node> nodes, List<Edge> edges)
    {
        Physics physics = new Physics();
        physics.setEnabled(false);

        networkDiagram = new NetworkDiagram
            (Options.builder()
                .withAutoResize(true)
                .withPhysics(physics)
                .withInteraction(Interaction.builder().withDragNodes(false).build())
                .withEdges(
                    Edges.builder()
                        .withArrows(new Arrows(new ArrowHead()))
                        .withColor(EdgeColor.builder()
                            .withColor("#000000")
                            .build())
                        .withDashes(false)
                        .build())
                .build());

        networkDiagram.setSizeFull();

        networkDiagram.setNodes(nodes);
        networkDiagram.setEdges(edges);

        networkDiagram.addDoubleClickListener((DoubleClickListener) doubleClickEvent ->
        {
            logger.info(doubleClickEvent.getParams().toString());

            JsonArray nodesArray = doubleClickEvent.getParams().getArray("nodes");
            String nodeId = nodesArray.get(0).asString();

            logger.info(nodeId);
            logger.info("Flow + " + this.flowMap.get(nodeId));

            if(this.flowMap.get(nodeId) != null){
                ModuleMetaData moduleMetaData = this.moduleMetaDataService
                    .findById(nodeId.substring(0, nodeId.indexOf(".")));

                logger.info("ModuleMetaData + " + moduleMetaData);

                FlowVisualisationDialog flowVisualisationDialog
                    = new FlowVisualisationDialog(this.moduleControlRestService, this.configurationRestService,
                    this.triggerRestService, this.configurationMetadataService, moduleMetaData
                    , nodeId.substring(nodeId.indexOf(".") + 1), this.solrSearchService);

                flowVisualisationDialog.open();
            }
        });

    }

    private void drawFlowStatus(FlowState state)
    {
        if(this.flowMap != null && flowMap.containsKey(state.getModuleName() + "." + state.getFlowName())) {
            Flow flow = flowMap.get(state.getModuleName() + "." + state.getFlowName());
            this.networkDiagram.drawStatusBorder(flow.getX() - 40
                , flow.getY() - 30, 80
                , 60, state.getState().getStateColour());
        }

        this.networkDiagram.diagamRedraw();
    }

    public void search(List<String> entityTypes, String searchTerm, long startTime, long endTime) {
        Set<String> moduleNames = this.businessStream
            .getFlows()
            .stream()
            .map(flow -> flow.getModuleName())
            .collect(Collectors.toSet());

        Set<String> flowNames = this.businessStream
            .getFlows()
            .stream()
            .map(flow -> flow.getFlowName())
            .collect(Collectors.toSet());

        IkasanSolrDocumentSearchResults results = this.solrSearchService.search(moduleNames, flowNames, searchTerm, startTime, endTime, 100, entityTypes);

        logger.info("Results: " + results.getTotalNumberOfResults());
    }

    public void drawFoundStatus()
    {
        this.flows = (ArrayList<Node>) this.flows.stream().map(node -> {
            node.setWiretapFoundStatus(NodeFoundStatus.FOUND);
            node.setErrorFoundStatus(NodeFoundStatus.FOUND);
            node.setExclusionFoundStatus(NodeFoundStatus.FOUND);
            node.setReplayFoundStatus(NodeFoundStatus.FOUND);
            return node;
        }).collect(Collectors.toList());

        current.access(() ->
            networkDiagram.updateNodesStates(this.flows));
        current.access(() ->
            this.networkDiagram.drawNodeFoundStatus());

        this.networkDiagram.diagamRedraw();
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent)
    {
        this.redraw();
    }

    public void redraw()
    {
        if(this.businessStream != null) {
            nodes = new ArrayList<>();
            nodes.addAll(businessStream.getFlows());
            nodes.addAll(businessStream.getDestinations());
            nodes.addAll(businessStream.getIntegratedSystems());

            if (this.networkDiagram != null) {
                this.remove(networkDiagram);
            }

            this.populateFlowMap(businessStream.getFlows());

            updateNetworkDiagram(nodes, businessStream.getEdges());

            for(String key: this.flowMap.keySet()) {
                if(key.contains(".")) {
                    ModuleMetaData module = this.moduleMetaDataService
                        .findById(key.substring(0, key.indexOf(".")));

                    if(module != null) {
                        FlowState flowState = FlowStateCache.instance().get(module, key.substring(key.indexOf(".") + 1));

                        if (flowState != null) {
                            this.drawFlowStatus(flowState);
                        }
                    }
                }
            }

            this.add(networkDiagram);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent)
    {
        this.redraw();
        UI ui = attachEvent.getUI();
        flowStateBroadcasterRegistration = FlowStateBroadcaster.register(flowState ->
        {
            logger.info("Received flow state: " + flowState);
            this.drawFlowStatus(ui, flowState);
        });

        cacheStateBroadcasterRegistration = CacheStateBroadcaster.register(flowState ->
        {
            logger.info("Received flow state: " + flowState);
            this.drawFlowStatus(ui, flowState);
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent)
    {
        this.flowStateBroadcasterRegistration.remove();
        this.flowStateBroadcasterRegistration = null;
        this.cacheStateBroadcasterRegistration.remove();
        this.cacheStateBroadcasterRegistration = null;
    }

    protected void drawFlowStatus(UI ui, FlowState flowState)
    {
        ui.access(() ->
        {
            if(this.flowMap != null && this.flowMap.containsKey(flowState.getModuleName() + "." + flowState.getFlowName()))
            {
                this.drawFlowStatus(flowState);
            }
        });
    }

    private void populateFlowMap(List<Flow> flows){
        this.flowMap = flows
            .stream()
            .collect(Collectors.toMap(Flow::getId, Function.identity()));
    }
}
