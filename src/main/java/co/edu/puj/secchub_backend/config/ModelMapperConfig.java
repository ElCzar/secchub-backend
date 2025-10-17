package co.edu.puj.secchub_backend.config;

import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for ModelMapper bean.
 * Sets up ModelMapper with strict matching strategy and field access.´
 */
@Configuration
public class ModelMapperConfig {
    
    /**
     * Creates and configures a ModelMapper bean.
     * @return Configured ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        // Configure matching strategy
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setSourceNamingConvention(org.modelmapper.convention.NamingConventions.JAVABEANS_ACCESSOR)
                .setDestinationNamingConvention(org.modelmapper.convention.NamingConventions.JAVABEANS_MUTATOR);
        
        // Fix field mapping for classRoomTypeId -> classroomTypeId
        mapper.createTypeMap(RequestScheduleRequestDTO.class, RequestSchedule.class)
                .addMapping(RequestScheduleRequestDTO::getClassRoomTypeId, RequestSchedule::setClassroomTypeId);
        
        return mapper;
    }
}