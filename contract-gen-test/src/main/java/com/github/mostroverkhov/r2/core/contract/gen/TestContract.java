package com.github.mostroverkhov.r2.core.contract.gen;

import com.github.mostroverkhov.r2.core.contract.*;
import com.github.mostroverkhov.r2.core.contract.gen.model.Bar;
import com.github.mostroverkhov.r2.core.contract.gen.model.Foo;
import org.reactivestreams.Publisher;

@Service("test")
public interface TestContract {

  @RequestChannel("channel")
  Publisher<Foo> channel(Publisher<Bar> bar);

  @RequestStream("stream")
  Publisher<Bar> stream(Foo foo);

  @RequestResponse("response")
  Publisher<Bar> response(Foo foo);

  @FireAndForget("fnf")
  Publisher<Bar> fnf(Foo foo);

  @Close()
  Publisher<Void> close();

  @OnClose()
  Publisher<Void> onClose();
}
