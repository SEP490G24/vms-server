package fpt.edu.capstone.vms.util;

import org.hibernate.MappingException;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.util.Properties;
import java.util.UUID;
public class StringSequenceIdentifier implements IdentifierGenerator, Configurable {
    public static final String SEQUENCE_PREFIX = "sequence_prefix";

    private String sequencePrefix;
    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        final ConfigurationService configurationService = serviceRegistry.getService(ConfigurationService.class);
        String globalEntityIdentifierPrefix = configurationService.getSetting("entity.identifier.prefix", String.class, "SEQ_");
        sequencePrefix = ConfigurationHelper.getString(SEQUENCE_PREFIX, params, globalEntityIdentifierPrefix);
    }

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        return sequencePrefix + UUID.randomUUID();
    }
}
