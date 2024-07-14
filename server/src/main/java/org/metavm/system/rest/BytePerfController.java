package org.metavm.system.rest;

import org.metavm.common.Result;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.object.instance.cache.RedisCache;
import org.metavm.util.ContextUtil;
import org.metavm.util.InstanceInput;
import org.metavm.util.StreamVisitor;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/perf")
public class BytePerfController extends EntityContextFactoryAware {

    private final RedisCache cache;

    public BytePerfController(EntityContextFactory entityContextFactory, RedisCache cache) {
        super(entityContextFactory);
        this.cache = cache;
    }

    @GetMapping("/read-bytes/{id:[0-9]+}")
    public Result<Long> readBytes(@PathVariable("id") long id,
                                  @RequestParam(value = "runs", defaultValue = "100") int runs) {
        var bytes = cache.get(id);
        try (var context = newContext()) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < runs; i++) {
                var input = InstanceInput.create(bytes, context.getInstanceContext());
                try (var ignored = ContextUtil.getProfiler().enter("readCacheBytes")) {
                    input.readSingleMessageGrove();
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            return Result.success(elapsed / runs);
        }
    }

    @GetMapping("/skip-bytes/{id:[0-9]+}")
    public Result<Long> skipBytes(@PathVariable("id") long id,
                                  @RequestParam(value = "runs", defaultValue = "100") int runs) {
        var bytes = cache.get(id);
        long start = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            var visitor = new StreamVisitor(new ByteArrayInputStream(bytes));
            try (var ignored = ContextUtil.getProfiler().enter("readCacheBytes")) {
                visitor.visitGrove();
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        return Result.success(elapsed / runs);
    }


}
