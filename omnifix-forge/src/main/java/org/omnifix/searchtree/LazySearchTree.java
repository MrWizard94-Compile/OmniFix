package org.omnifix.searchtree;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Function;

/**
 * Defers SearchRegistry tree construction until first non-empty search.
 */
public final class LazySearchTree<T> implements RefreshableSearchTree<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<T> contents;
    private final Function<List<T>, RefreshableSearchTree<T>> treeBuilder;
    private volatile RefreshableSearchTree<T> realTree;

    public LazySearchTree(List<T> contents, Function<List<T>, RefreshableSearchTree<T>> treeBuilder) {
        this.contents = contents;
        this.treeBuilder = treeBuilder;
    }

    private RefreshableSearchTree<T> getRealTree() {
        var t = realTree;
        if (t == null) {
            synchronized (this) {
                t = realTree;
                if (t == null) {
                    LOGGER.info("[OmniFix] Building search tree for {} items...", contents.size());
                    Stopwatch s = Stopwatch.createStarted();
                    t = this.treeBuilder.apply(contents);
                    t.refresh();
                    s.stop();
                    LOGGER.info("[OmniFix] Building search tree for {} items took {}", contents.size(), s);
                    realTree = t;
                }
            }
        }
        return t;
    }

    @Override
    public List<T> search(String query) {
        if (query.isEmpty()) {
            return this.contents;
        }
        return getRealTree().search(query);
    }

    @Override
    public void refresh() {
        var t = this.realTree;
        if (t != null) {
            t.refresh();
        }
    }

    public static <T> SearchRegistry.TreeBuilderSupplier<T> decorate(SearchRegistry.TreeBuilderSupplier<T> originalSupplier) {
        return list -> new LazySearchTree<>(list, originalSupplier);
    }
}
