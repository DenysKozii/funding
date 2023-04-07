package bot.mapper;

import bot.dto.FundingDto;
import bot.entity.Funding;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FundingMapper {
    FundingMapper INSTANCE = Mappers.getMapper(FundingMapper.class);

    FundingDto mapToDto(Funding funding);
}
