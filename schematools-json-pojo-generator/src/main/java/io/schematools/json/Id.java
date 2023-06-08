package io.schematools.json;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Id {

    private URI uri;

    public Id(String id) {
        URI uri = URI.create(id);
        if (Objects.isNull(uri.getHost()) || uri.getHost().isEmpty()) {
            //TODO Better validation and error response
            //TODO Validate Version
            throw new RuntimeException();
        }
        this.uri = uri;
    }

    public String convertToFullyQualifiedName() {
        List<String> hostSegments = Arrays.stream(uri.getHost().split("\\.")).collect(Collectors.toList());
        Collections.reverse(hostSegments);
        List<String> pathSegments = Arrays.stream(uri.getPath().split("/")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        String version = pathSegments.remove(pathSegments.size() - 1);
        String className = CaseHelper.convertToCamelCase(pathSegments.remove(pathSegments.size() - 1), true);
        hostSegments.addAll(pathSegments);
        String packageName = hostSegments.stream().collect(Collectors.joining("."));
        return packageName + "." + className + version.toUpperCase();
    }

}
