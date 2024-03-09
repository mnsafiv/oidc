package ru.safonoviv.oidclmsboi.lms.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.safonoviv.oidclmsboi.boa.exceptions.NotFoundException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SortUtil {
    public void checkSortParameters(Sort sort, Field[] declaredFields) throws NotFoundException{
        Set<String> fields = Arrays.stream(declaredFields).map(Field::getName).collect(Collectors.toSet());
        sort.stream().forEach(t -> {
            if (!fields.contains(t.getProperty())) {
                System.out.println();
                throw new NotFoundException("��� ���������� �� ������ ���������: " + t, HttpStatus.BAD_REQUEST);
            }
        });
    }
    public List<Order> getSort(Sort sort, Root<?> root, CriteriaBuilder cb) {
        return sort.stream().map(t -> {
            if (t.getDirection().isAscending()) {
                return cb.asc(root.get(t.getProperty()));
            } else {
                return cb.desc(root.get(t.getProperty()));
            }
        }).toList();
    }
}
