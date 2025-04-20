package org.metavm.manufacturing.production;

import org.metavm.api.Component;
import org.metavm.manufacturing.production.dto.BomDTO;
import org.metavm.manufacturing.production.dto.ComponentMaterialDTO;
import org.metavm.manufacturing.production.dto.ComponentMaterialItemDTO;
import org.metavm.manufacturing.production.dto.SecondaryOutputDTO;

@Component
public class BomService {

    public BOM create(BomDTO bomDto) {
        var bom = new BOM(
                bomDto.product(),
                bomDto.unit(),
                bomDto.routing(),
                bomDto.reportingProcess(),
                bomDto.state(),
                bomDto.inbound(),
                bomDto.autoInbound()
        );
        for (ComponentMaterialDTO componentDto : bomDto.components()) {
            var component = bom.new Component(
                    componentDto.sequence(),
                    componentDto.material(),
                    componentDto.unit(),
                    componentDto.numerator(),
                    componentDto.denominator(),
                    componentDto.attritionRate(),
                    componentDto.version(),
                    componentDto.pickMethod(),
                    componentDto.routingSpecified(),
                    componentDto.process(),
                    componentDto.qualityInspectionState(),
                    componentDto.feedType()
            );
            for (ComponentMaterialItemDTO itemDto : componentDto.items()) {
                component.new Item(
                       itemDto.sequence(),
                       itemDto.numerator(),
                       itemDto.denominator(),
                       itemDto.process(),
                       itemDto.qualityInspectionState(),
                       itemDto.feedType()
                );
            }
        }
        for (SecondaryOutputDTO secondaryOutputDto : bomDto.secondaryOutputs()) {
            bom.new SecondaryOutput(
              secondaryOutputDto.seq(),
              secondaryOutputDto.product(),
              secondaryOutputDto.baseFigure(),
              secondaryOutputDto.unit(),
              secondaryOutputDto.inbound(),
              secondaryOutputDto.autoInbound(),
              secondaryOutputDto.process()
            );
        }
        return bom;
    }

}
