package tech.metavm.management.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Result;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.object.instance.cache.RedisCache;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.StreamVisitor;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/perf")
public class BytePerfController {

    private final InstanceContextFactory instanceContextFactory;

    private final RedisCache cache;

    public BytePerfController(InstanceContextFactory instanceContextFactory, RedisCache cache) {
        this.instanceContextFactory = instanceContextFactory;
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
                    input.readMessage();
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
                visitor.visitMessage();
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        return Result.success(elapsed / runs);
    }


    private IEntityContext newContext() {
        return instanceContextFactory.newEntityContext(false);
    }
}
