package io.josephkim;

import static io.josephkim.BigTalkCore.*;
import static io.josephkim.BigTalkCore.Error;
import static io.josephkim.BigTalkCore.InternalError;
import static io.josephkim.BigTalkCore.Number;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class BigTalkDesktop {
  private static final String CHARSET_NAME = "UTF-8";
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final String BIGTALK_PATH = "BIGTALK_PATH";

  private static ScheduledThreadPoolExecutor scheduler;

  private static ScheduledThreadPoolExecutor getScheduler() {
    if (scheduler == null) {
      scheduler = new ScheduledThreadPoolExecutor(1);
      scheduler.setCorePoolSize(0);
      scheduler.setKeepAliveTime(0, TimeUnit.MILLISECONDS);
    }
    return scheduler;
  }

  public static final Scope globals = new Scope(null)
    .updateFrom(BigTalkCore.globals)
    .put(new Builtin("wait_for", P("time", "callback"), (self, args) -> {
      long delayMillis = (long) (args[0].mustCast(Number.class).get() * 1000);
      Value callback = args[1];
      getScheduler().schedule(() -> {
        callback.call(null);
        return null;
      }, delayMillis, TimeUnit.MILLISECONDS);
      return nil;
    }))
    .put(new Builtin("print", P("x"), (self, args) -> {
      System.out.println(args[0]);
      return nil;
    }));

  // Java doesn't let you change the current directory.
  // Emulate a current directory.
  public static final ThreadLocal<Path> currentWorkingDirectory =
    new ThreadLocal<Path>() {
      @Override protected Path initialValue() {
        return Paths.get(System.getProperty("user.dir"));
      }
    };
  private static Path getPath(String path) {
    return currentWorkingDirectory.get().resolve(path);
  }

  static {
    addSourceLoader(name -> {
      Path relpath = Paths.get(name.replace(".", File.separator) + ".b");
      return Optional.ofNullable(System.getenv(BIGTALK_PATH))
        .flatMap(pathstr ->
          Arrays.asList(pathstr.split(File.pathSeparator))
            .stream()
            .map(Paths::get)
            .map(prefix -> prefix.resolve(relpath))
            .filter(Files::isReadable)
            .map(p -> new Source(name, p.toString(),  read(p)))
            .findFirst());
    });
    addNativeModule("os", () -> new Scope(null)
      .put(new Builtin("pwd", P(), (self, args) -> {
        return Str.of(currentWorkingDirectory.get().toString());
      }))
      .put(new Builtin("ls", P("/dir"), (self, args) -> {
        Path cwd = currentWorkingDirectory.get();
        Path path =
          args.length > 0 ? cwd.resolve(args[0].toString()) : cwd;
        try {
          return new Arr(new ArrayList<>(Files.list(path)
            .map(path::relativize)
            .map(Object::toString)
            .map(Str::of)
            .collect(Collectors.toList())));
        } catch (IOException ex) {
          throw new UserError(ex.toString());
        }
      })));
    BigTalkSwing.init();
  }

  public static void main(String[] args) {
    setGlobals(globals);
    importModule("_bt.prelude");
    Scope scope = new Scope(globals);
    switch (args.length) {
      case 0:
        repl(scope, System.in, System.out);
        break;
      case 1:
        Source source = new Source("_main", args[0], read(args[0]));
        run(scope, source);
        break;
      default:
        System.out.println("usage: bigtalk [script.b]");
        System.exit(1);
    }
  }

  public static void repl(Scope scope, InputStream fin, PrintStream fout) {
    String buffer = "";
    BufferedReader br =
      new BufferedReader(new InputStreamReader(fin, CHARSET));
    try {
      while (true) {
        if (buffer.isEmpty()) {
          fout.print(">> ");
        } else {
          fout.print(".. ");
        }
        String line = br.readLine();
        if (line == null) {
          break;
        }
        buffer += line;
        try {
          if (isReadyForParse(buffer)) {
            Value value = runForRepl(scope, buffer);
            buffer = "";
            if (value != null && value != nil) {
              System.out.println(value);
            }
          }
        } catch (UserError error) {
          System.err.println(error.getMessage());
          buffer = "";
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  //// File util
  public static String read(InputStream inputStream) {
    Scanner s =
        new Scanner(inputStream, CHARSET_NAME).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
  public static String read(String path) {
    return read(Paths.get(path));
  }
  public static String read(Path path) {
    try {
      return read(Files.newInputStream(path));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
