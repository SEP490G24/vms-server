package fpt.edu.capstone.vms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public final class PageableUtils {

    public static List<Sort.Order> converterSort2List(Sort sort) {
        var results = new ArrayList<Sort.Order>();
        for (Sort.Order order : sort) {
            results.add(order);
        }
        return results;
    }
}
