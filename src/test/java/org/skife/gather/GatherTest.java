package org.skife.gather;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.skife.clocked.ClockedExecutorService;

import javax.inject.Named;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class GatherTest
{
    private static final Executor direct = MoreExecutors.directExecutor();
    private ClockedExecutorService clock;

    @Before
    public void setUp() throws Exception
    {
        clock = new ClockedExecutorService();
    }

    @Test
    public void testApi() throws Exception
    {
        Gather<String> g = new Gather(String.class, clock, Duration.ofSeconds(1), direct, new Object()
        {
            @Priority(3)
            public String both(Cat _c, Dog _d)
            {
                return "woof meow";
            }

            @Priority(2)
            public String dog(@Named("Brian's dog") Dog _d)
            {
                return "woof";
            }

            @Priority(1)
            public String cat(Cat c, GatherContext ctx)
            {
                if (c.getLivesRemaining() < 1) {
                    ctx.reject();
                }
                return "meow";
            }

            @Priority(0)
            public String fallback()
            {
                return "whimper";
            }
        });

        Future<String> f = g.start();

        g.provide("Brian's dog", new Dog("Bean", 17));
        g.provide(new Cat(7));

        assertThat(f.get()).isEqualTo("woof meow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongReturnType() throws Exception
    {
        Gather<String> g = new Gather(String.class, clock, Duration.parse("PT1s"), direct, new Object()
        {
            @Priority(1)
            public int foo(Dog _d)
            {
                return 1;
            }
        });
    }
}
