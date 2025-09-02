package co.edu.puj.secchub_backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

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
        
        return mapper;
    }
}