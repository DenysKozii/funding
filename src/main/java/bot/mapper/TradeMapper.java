package bot.mapper;

import bot.dto.TradeDto;
import bot.entity.Trade;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TradeMapper {
    TradeMapper INSTANCE = Mappers.getMapper(TradeMapper.class);

    TradeDto mapToDto(Trade trade);
}
