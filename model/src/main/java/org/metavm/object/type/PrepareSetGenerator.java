package org.metavm.object.type;

import org.metavm.entity.IEntityContext;
import org.metavm.flow.rest.CallNodeParam;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.ScopeNodeParam;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrepareSetGenerator {

    public static Set<String> generate(List<KlassDTO> klassDTOs, IEntityContext context) {
        var generator = new PrepareSetGenerator(klassDTOs, context);
        generator.process();
        return generator.result;
    }

    private final Set<String> result = new HashSet<>();
    private final IEntityContext context;
    private final List<KlassDTO> klassDTOs;

    public PrepareSetGenerator(List<KlassDTO> klassDTOs, IEntityContext context) {
        this.klassDTOs = klassDTOs;
        this.context = context;
    }

    private void process() {
        klassDTOs.forEach(this::processKlas);
    }

    private void processKlas(KlassDTO klassDTO) {
        var klass = context.getKlass(klassDTO.id());
        if(klass == null || context.isNewEntity(klass))
            return;
        if(klassDTO.fields() != null) {
            for (FieldDTO fieldDTO : klassDTO.fields()) {
                processField(fieldDTO);
            }
        }
        if(klassDTO.staticFields() != null){
            for (FieldDTO fieldDTO : klassDTO.staticFields()) {
                processField(fieldDTO);
            }
        }
        if(klassDTO.flows() != null) {
            for (FlowDTO methodDTO : klassDTO.flows()) {
                processMethod(methodDTO);
            }
        }
    }

    private void processField(FieldDTO fieldDTO) {
        if(fieldDTO.isStatic())
            return;
        var field = context.getField(fieldDTO.id());
        if(field == null) {
            result.add(fieldDTO.id());
        }
    }

    private void processMethod(FlowDTO methodDTO) {
        var method = context.getMethod(methodDTO.id());
        if(method == null && isInitializerName(methodDTO.name())) {
            result.add(methodDTO.id());
            var rootScope = methodDTO.rootScope();
            if(rootScope != null)
                rootScope.nodes().forEach(this::processNode);
        }
    }

    private void processNode(NodeDTO nodeDTO) {
        switch (nodeDTO.param()) {
            case CallNodeParam callNodeParam -> {
                var flowRef = callNodeParam.getFlowRef();
                if (flowRef != null) {
                    var flow = context.getFlow(flowRef.rawFlowId());
                    if (flow == null)
                        result.add(flowRef.rawFlowId());
                }
            }
            case ScopeNodeParam scopeNodeParam-> scopeNodeParam.getBodyScope().nodes().forEach(this::processNode);
            case null, default -> {}
        }
    }

    private boolean isInitializerName(String name) {
        return name.length() > 4 && name.startsWith("__") && name.endsWith("__");
    }

}
