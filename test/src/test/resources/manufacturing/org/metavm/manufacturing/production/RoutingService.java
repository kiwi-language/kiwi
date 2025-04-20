package org.metavm.manufacturing.production;

import org.metavm.api.Component;
import org.metavm.manufacturing.production.dto.RoutingDTO;
import org.metavm.manufacturing.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class RoutingService {

    public Routing save(RoutingDTO routingDTO) {
        var routing = routingDTO.entity();
        if (routing == null) {
            routing = new Routing(
                    routingDTO.name(),
                    routingDTO.product(),
                    routingDTO.unit()
            );
        }
        else {
            routing.setName(routingDTO.name());
            routing.setProduct(routingDTO.product());
            routing.setUnit(routingDTO.unit());
        }

        var processes = new ArrayList<Routing.Process>();
        var seq2process = new HashMap<Integer, Routing.Process>();
        for (RoutingDTO.ProcessDTO processDTO : routingDTO.processes()) {
            var process = processDTO.entity();
            if (process != null) {
                process.setProcessCode(process.getProcessCode());
                process.setSequence(process.getSequence());
                process.setProcess(processDTO.process());
                process.setWorkCenter(processDTO.workCenter());
                process.setProcessDescription(process.getProcessDescription());
            }
            else {
                process = routing.new Process(
                        processDTO.processCode(),
                        processDTO.sequence(),
                        processDTO.process(),
                        processDTO.workCenter(),
                        processDTO.processDescription()
                );
            }
            processes.add(process);
            seq2process.put(process.getSequence(), process);
            var items = new ArrayList<Routing.Process.Item>();
            for (RoutingDTO.ProcessDTO.ItemDTO itemDTO : processDTO.items()) {
                var item = itemDTO.entity();
                if (item != null) {
                    item.setSequence(itemDTO.sequence());
                    item.setNumerator(itemDTO.numerator());
                }
                else
                    item = process.new Item(itemDTO.sequence(), itemDTO.numerator());
                items.add(item);
            }
            process.setItems(items);
        }
        routing.setProcesses(processes);

        var successions = new ArrayList<Routing.Succession>();
        for (RoutingDTO.SuccessionDTO successionDTO : routingDTO.successions()) {
            var succession = successionDTO.entity();
            var from = seq2process.get(successionDTO.fromSeq());
            var to = seq2process.get(successionDTO.toSeq());
            if (succession != null) {
                succession.setFrom(from);
                succession.setTo(to);
                succession.setUnit(successionDTO.unit());
                succession.setBaseQuantity(successionDTO.baseQuantity());
                succession.setReport(successionDTO.report());
                succession.setInbound(successionDTO.inbound());
                succession.setAutoInbound(successionDTO.autoInbound());
                succession.setQualityInspectionState(successionDTO.qualityInspectionState());
                succession.setFeedType(successionDTO.feedType());
            }
            else {
                succession = routing.new Succession(
                        from,
                        to,
                        successionDTO.product(),
                        successionDTO.unit(),
                        successionDTO.baseQuantity(),
                        successionDTO.report(),
                        successionDTO.inbound(),
                        successionDTO.autoInbound(),
                        successionDTO.qualityInspectionState(),
                        successionDTO.feedType()
                );
            }
            successions.add(succession);
        }
        routing.setSuccessions(successions);
        return routing;
    }

    public RoutingDTO get(Routing routing) {
        return new RoutingDTO(
                routing,
                routing.getName(),
                routing.getProduct(),
                routing.getUnit(),
                Utils.map(routing.getProcesses(), this::getProcess),
                Utils.map(routing.getSuccessions(), this::getSuccession)
        );
    }

    private RoutingDTO.ProcessDTO getProcess(Routing.Process process) {
        return new RoutingDTO.ProcessDTO(
                process,
                process.getProcessCode(),
                process.getSequence(),
                process.getProcess(),
                process.getWorkCenter(),
                process.getProcessDescription(),
                Utils.map(process.getItems(), this::getProcessItem)
        );
    }

    private RoutingDTO.ProcessDTO.ItemDTO getProcessItem(Routing.Process.Item item) {
        return new RoutingDTO.ProcessDTO.ItemDTO(item, item.getSequence(), item.getNumerator());
    }

    private RoutingDTO.SuccessionDTO getSuccession(Routing.Succession succession) {
        return new RoutingDTO.SuccessionDTO(
                succession,
                succession.getFrom().getSequence(),
                succession.getTo().getSequence(),
                succession.getProduct(),
                succession.getUnit(),
                succession.getBaseQuantity(),
                succession.isReport(),
                succession.isInbound(),
                succession.isAutoInbound(),
                succession.getQualityInspectionState(),
                succession.getFeedType()
        );
    }

}
