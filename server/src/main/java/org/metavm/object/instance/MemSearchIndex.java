package org.metavm.object.instance;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.search.SearchQuery;

import java.util.*;
import java.util.function.Consumer;

public class MemSearchIndex {

    private final String name;
    private final Set<String> aliases = new HashSet<>();

    private final Map<String, Source> sources = new HashMap<>();

    public MemSearchIndex(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public Map<String, Source> getSources() {
        return sources;
    }

    public List<Id> search(SearchQuery query) {
        var result = new ArrayList<Id>();
        for (var source : sources.values()) {
            if (match(source, query))
                result.add(source.id());
        }
        return result;
    }

    private boolean match(Source source, SearchQuery query) {
        if (!query.types().contains(source.typeKey().toTypeExpression()))
            return false;
        return query.condition() == null || query.condition().evaluate(source.fields());
    }

    public boolean contains(String id) {
        return sources.containsKey(id);
    }

    public void index(String id, Source source) {
        sources.put(id, source);
    }

    public void remove(String id) {
        sources.remove(id);
    }

    public boolean hasAlias(String alias) {
        return aliases.contains(alias);
    }

    public MemSearchIndex copy() {
        var copy = new MemSearchIndex(name);
        copy.sources.putAll(sources);
        copy.aliases.addAll(aliases);
        return copy;
    }

    public void removeAlias(String alias) {
        aliases.remove(alias);
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public void forEachName(Consumer<String> action) {
        action.accept(name);
        for (var alias : aliases) {
            action.accept(alias);
        }
    }

}
