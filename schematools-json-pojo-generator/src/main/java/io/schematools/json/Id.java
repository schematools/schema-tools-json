package io.schematools.json;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Id(String id, URI uri, String packageName, String className, String version) {

    public static Id create(String id) {
        URI uri = URI.create(id);
        if (Objects.isNull(uri.getHost()) || uri.getHost().isEmpty()) {
            //TODO Better validation and error response
            throw new RuntimeException();
        }
        List<String> hostSegments = Arrays.stream(uri.getHost().split("\\.")).collect(Collectors.toList());
        Collections.reverse(hostSegments);
        List<String> pathSegments = Arrays.stream(uri.getPath().split("/")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        String version = pathSegments.remove(pathSegments.size() - 1);
        String className = CaseHelper.toCamelCase(pathSegments.remove(pathSegments.size() - 1), true) + version.toUpperCase();
        hostSegments.addAll(pathSegments);
        String packageName = String.join(".", hostSegments);
        return new Id(id, uri, packageName, className, version);
    }

    public String outputFileName(String targetPath) {
        return targetPath + "/" + packageName.replace('.', '/') + "/" + className + ".java";
    }

    public String baseUri() {
        return uri.getScheme() + "://" + uri.getHost();
    }

}
