package fpt.edu.capstone.vms.persistence.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Option {
    private String label;
    private Object value;
}
