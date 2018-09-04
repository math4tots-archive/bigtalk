package io.josephkim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Stack;


// BigTalkCore!!
// This is the core of the language.
// It should only use external classes from java.util.*,
// and even then, sparingly.
// By limiting the dependencies this way maximizes
// chances of doing an easy port to javascript (e.g.
// with GWT or other transpiler) or iOS (e.g. with
// lightweight JVM or C target transpiler).
// For extensions specific to the desktop, see BigTalkDesktop
@SuppressWarnings("serial")
public final class BigTalkCore {
  private static final int STACK_SIZE = 3000;
  private static final ThreadLocal<TokenStack> tokenStack =
    new ThreadLocal<TokenStack>() {
      @Override protected TokenStack initialValue() {
        return new TokenStack(STACK_SIZE);
      }
    };
  private static final ThreadLocal<ValueStack> valueStack =
    new ThreadLocal<ValueStack>() {
      @Override protected ValueStack initialValue() {
        return new ValueStack(STACK_SIZE);
      }
    };
  private static final int NEXT = -1;
  private static final int RETURN = -2;
  private static final int YIELD = -3;
  private static final int JUMP_NOT_SET = -3000;
  private static final Symbol thisSymbol = Symbol.of("this");
  private static final Symbol __protoSymbol = Symbol.of("__proto");
  private static final Symbol __initSymbol = Symbol.of("__init");
  private static final Symbol __classSymbol = Symbol.of("__class");
  private static final Symbol __basesSymbol = Symbol.of("__bases");
  private static final Symbol __nameSymbol = Symbol.of("__name");
  private static final Symbol __callSymbol = Symbol.of("__call");
  private static final Symbol __getitemSymbol = Symbol.of("__getitem");
  private static final Symbol __setitemSymbol = Symbol.of("__setitem");
  private static final Symbol __reprSymbol = Symbol.of("__repr");
  private static final Symbol __strSymbol = Symbol.of("__str");
  private static final Symbol __powSymbol = Symbol.of("__pow");
  private static final Symbol __mulSymbol = Symbol.of("__mul");
  private static final Symbol __divSymbol = Symbol.of("__div");
  private static final Symbol __floordivSymbol = Symbol.of("__floordiv");
  private static final Symbol __modSymbol = Symbol.of("__mod");
  private static final Symbol __addSymbol = Symbol.of("__add");
  private static final Symbol __subSymbol = Symbol.of("__sub");
  private static final Symbol __ltSymbol = Symbol.of("__lt");
  private static final Symbol __iterSymbol = Symbol.of("__iter");
  private static final Symbol __nextSymbol = Symbol.of("__next");
  private static final Symbol __lenSymbol = Symbol.of("__len");
  private static final Symbol __ancestorsSymbol = Symbol.of("__ancestors");
  private static final Symbol lambdaName = Symbol.of("$lambda");
  private static final Map<Class<?>, Scope> nativeProtoTable = new HashMap<>();
  static final Nil nil = new Nil();
  static final Bool tru = new Bool(true);
  static final Bool fal = new Bool(false);
  static final Builtin makeListFn =
    new Builtin("makeList", P("*args"), (self, args) ->
      new Arr(listOf(args)));
  static final Builtin makeSetFn =
    new Builtin("makeSet", P("*args"), (self, args) ->
      new XSet(new HashSet<>(listOf(args))));
  static final Builtin makeMapFn =
    new Builtin("makeMap", P("*args"), (self, args) -> {
      if (args.length % 2 != 0) {
        throw new TypeError(
          "makeMap requires an even number of arguments, but got " +
          args.length);
      }
      HashMap<Value, Value> map = new HashMap<>();
      for (int i = 0; i + 1 < args.length; i += 2) {
        map.put(args[i], args[i + 1]);
      }
      return new XMap(map);
    });
  static final Builtin lessThanFn =
    new Builtin("lessThan", P("a", "b"), (self, args) -> {
      return args[0].lessThan(args[1]) ? tru : fal;
    });
  static final Builtin lessThanOrEqualFn =
    new Builtin("lessThanOrEqual", P("a", "b"), (self, args) -> {
      return !args[1].lessThan(args[0]) ? tru : fal;
    });
  static final Builtin greaterThanFn =
    new Builtin("greaterThan", P("a", "b"), (self, args) -> {
      return args[1].lessThan(args[0]) ? tru : fal;
    });
  static final Builtin greaterThanOrEqualFn =
    new Builtin("greaterThanOrEqual", P("a", "b"), (self, args) -> {
      return !args[0].lessThan(args[1]) ? tru : fal;
    });
  static final Builtin equalsFn =
    new Builtin("equals", P("a", "b"), (self, args) -> {
      return args[0].equals(args[1]) ? tru : fal;
    });
  static final Builtin notEqualsFn =
    new Builtin("notEquals", P("a", "b"), (self, args) -> {
      return !args[0].equals(args[1]) ? tru : fal;
    });
  static final Builtin isFn =
    new Builtin("_is", P("a", "b"), (self, args) -> {
      return args[0] == args[1] ? tru : fal;
    });
  static final Builtin isNotFn =
    new Builtin("_isNot", P("a", "b"), (self, args) -> {
      return args[0] != args[1] ? tru : fal;
    });
  static final Builtin identityFn =
    new Builtin("_identity", P("x"), (self, args) -> args[0]);
  static final Scope objectProto = new Scope(null);
  static final Scope objectClass = makeClass("Object", makeEmptyList(), objectProto);
  static final Scope classProto = new Scope(null)
    .put(new Builtin("__lt", P("other"), (self, args) -> {
      return isBaseClass(
        args[0].mustCast(Scope.class), self.mustCast(Scope.class)) ? tru : fal;
    }))
    .put(new Builtin("__repr", P(), (self, args) -> {
      return Str.of("<class " + self.mustGetAttribute(__nameSymbol) + ">");
    }))
    .put(new Builtin("__call", P("*args"), (self, args) -> {
      Scope proto = self.mustGetAttribute(__protoSymbol).mustCast(Scope.class);
      Value init = proto.getAttribute(__initSymbol);
      Scope object = new Scope(proto);
      if (init != null) {
        init.call(object, args);
      }
      return object;
    }));
  static final Scope classClass = makeClass("Class", classProto);
  static final Scope nativeProto = new Scope(null)
    .put(new Builtin("__str", P(), (self, args) ->
      Str.of(self.mustGetNative(Object.class).toString())))
    .put(new Builtin("__repr", P(), (self, args) ->
      Str.of(
        "<native " +
        self.mustGetNative(Object.class).getClass().getName() +
        ">")));
  static final Scope nativeClass = makeClass("Native", nativeProto);
  static final Scope nilProto = new Scope(null);
  static final Scope nilClass = makeClass("Nil", nilProto);
  static final Scope boolProto = new Scope(null);
  static final Scope boolClass = makeClass("Bool", boolProto);
  static final Scope numberProto = new Scope(null)
    .put(new Builtin("__add", P("x"), (self, args) -> {
      return Number.of(
        self.mustCast(Number.class).value +
        args[0].mustCast(Number.class).value);
    }))
    .put(new Builtin("__sub", P("x"), (self, args) -> {
      return Number.of(
        self.mustCast(Number.class).value -
        args[0].mustCast(Number.class).value);
    }))
    .put(new Builtin("__mul", P("x"), (self, args) -> {
      return Number.of(
        self.mustCast(Number.class).value *
        args[0].mustCast(Number.class).value);
    }))
    .put(new Builtin("__div", P("x"), (self, args) -> {
      return Number.of(
        self.mustCast(Number.class).value /
        args[0].mustCast(Number.class).value);
    }))
    .put(new Builtin("__pow", P("n"), (self, args) -> {
      return Number.of(Math.pow(
        self.mustCast(Number.class).value,
        args[0].mustCast(Number.class).value));
    }))
    .put(new Builtin("__floordiv", P("x"), (self, args) -> {
      return Number.of(Math.floor(
        self.mustCast(Number.class).value /
        args[0].mustCast(Number.class).value));
    }))
    .put(new Builtin("__mod", P("x"), (self, args) -> {
      return Number.of(
        self.mustCast(Number.class).value %
        args[0].mustCast(Number.class).value);
    }));
  static final Scope numberClass = makeClass("Number", numberProto);
  static final Scope stringProto = new Scope(null)
    .put(new Builtin("__len", P(), (self, args) ->
      Number.of(self.mustCast(Str.class).size())))
    .put(new Builtin("__getitem", P("index"), (self, args) ->
      self.mustCast(Str.class).getItem(
        (int) args[0].mustCast(Number.class).get())))
    .put(new Builtin("__add", P("x"), (self, args) -> {
      return Str.of(
        self.mustCast(Str.class).value +
        args[0].mustCast(Str.class).value);
    }))
    .put(new Builtin("strip", P(), (self, args) -> {
      return Str.of(self.mustCast(Str.class).value.trim());
    }))
    .put(new Builtin("join", P("arg"), (self, args) -> {
      String sep = self.str();
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      Value iterator = args[0].iterator();
      for (Value arg = iterator.next(); arg != null; arg = iterator.next()) {
        if (!first) {
          sb.append(sep);
        }
        sb.append(arg.str());
      }
      return Str.of(sb.toString());
    }));
  static final Scope stringClass = makeClass("String", stringProto);
  static final Scope iterableProto = new Scope(null)
    .put(new Builtin("all", P("/test"), (self, args) -> {
      Value testfunc = args.length == 0 ? identityFn : args[0];
      Value iterator = self.iterator();
      Value next = iterator.next();
      while (next != null) {
        if (!testfunc.call(null, next).truthy()) {
          return fal;
        }
        next = iterator.next();
      }
      return tru;
    }))
    .put(new Builtin("any", P("/test"), (self, args) -> {
      Value testfunc = args.length == 0 ? identityFn : args[0];
      Value iterator = self.iterator();
      Value next = iterator.next();
      while (next != null) {
        if (testfunc.call(null, next).truthy()) {
          return tru;
        }
        next = iterator.next();
      }
      return fal;
    }))
    .put(new Builtin("each", P("f"), (self, args) -> {
      Value iterator = self.iterator();
      Value next = iterator.next();
      while (next != null) {
        args[0].call(null, next);
        next = iterator.next();
      }
      return nil;
    }));
  static final Scope iterableClass = makeClass("Iterable", iterableProto);
  static final Scope randomAccessContainerClass =
    makeClass(
      "RandomAccessContainer",
      listOf(iterableClass),
      new Scope(null));
  static final Scope listProto = new Scope(null)
    .put(new Builtin("__getitem", P("index"), (self, args) -> {
      return self.mustCast(Arr.class).value
        .get((int) args[0].mustCast(Number.class).get());
    }))
    .put(new Builtin("__setitem", P("index", "value"), (self, args) -> {
      return self.mustCast(Arr.class).value
        .set((int) args[0].mustCast(Number.class).get(), args[1]);
    }))
    .put(new Builtin("__mul", P("n"), (self, args) -> {
      ArrayList<Value> original = self.mustCast(Arr.class).value;
      int n = (int) args[0].mustCast(Number.class).value;
      ArrayList<Value> arr = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        arr.addAll(original);
      }
      return new Arr(arr);
    }))
    .put(new Builtin("map", P("f"), (self, args) -> {
      ArrayList<Value> ret = new ArrayList<>();
      for (Value v: self.mustCast(Arr.class).value) {
        ret.add(args[0].call(null, v));
      }
      return new Arr(ret);
    }))
    .put(new Builtin("push", P("x"), (self, args) -> {
      self.mustCast(Arr.class).value.add(args[0]);
      return nil;
    }))
    .put(new Builtin("pop", P(), (self, args) -> {
      List<Value> arr = self.mustCast(Arr.class).value;
      return arr.remove(arr.size() - 1);
    }))
    .put(new Builtin("__len", P(), (self, args) -> {
      return Number.of(self.mustCast(Arr.class).value.size());
    }));
  static final Scope listClass =
    makeClass("List", listOf(randomAccessContainerClass), listProto)
    .put(new Builtin("__call", P("xs"), (self, args) -> {
      ArrayList<Value> arr = new ArrayList<>();
      Value iterator = args[0].iterator();
      Value next = iterator.next();
      while (next != null) {
        arr.add(next);
        next = iterator.next();
      }
      return new Arr(arr);
    }));
  static final Scope setProto = new Scope(null)
    .put(new Builtin("__len", P(), (self, args) ->
      Number.of(self.mustCast(XSet.class).value.size())));
  static final Scope setClass = makeClass("Set", setProto);
  static final Scope mapProto = new Scope(null)
    .put(new Builtin("__len", P(), (self, args) ->
      Number.of(self.mustCast(XSet.class).value.size())));
  static final Scope mapClass = makeClass("Map", mapProto);
  static final Scope singletonProto = new Scope(null)
    .put(new Builtin("__repr", P(), (self, args) -> {
      return Str.of("<" + self.mustGetAttribute(__nameSymbol) + ">");
    }));
  static final Scope singletonClass = makeClass("Singleton", singletonProto);
  static final Scope functionProto = new Scope(null)
    .put(new Builtin("_ops", P(), (self, args) -> {
      BaseUserFunction f = self.mustCast(BaseUserFunction.class);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < f.opcodes.length; i++) {
        Opcode op = f.opcodes[i];
        sb.append(i + ": " + op + "\n");
      }
      return Str.of(sb.toString());
    }));
  static final Scope functionClass = makeClass("Function", functionProto);
  static final Scope generatorObjectProto = new Scope(null);
  static final Scope generatorObjectClass = makeClass("GeneratorObject", listOf(iterableClass), generatorObjectProto);
  static final Scope randomClass =
    makeNativeClass(
      Random.class,
      new Scope(null)
        .put(new Builtin("int", P("a", "b"), (self, args) -> {
          Random random = self.mustGetNative(Random.class);
          int a = (int) args[0].mustCast(Number.class).get();
          int b = (int) args[1].mustCast(Number.class).get();
          return Number.of(a + random.nextInt(b - a));
        }))
        .put(new Builtin("pick", P("container"), (self, args) -> {
          Random random = self.mustGetNative(Random.class);
          Value iterator = args[0].iterator();
          Value next = iterator.next();
          ArrayList<Value> arr = new ArrayList<>();
          while (next != null) {
            arr.add(next);
            next = iterator.next();
          }
          return arr.get(random.nextInt(arr.size()));
        })))
    .put(new Builtin("__call", P("/seed"), (self, args) -> {
      Random random =
        args.length == 0 ?
        new Random() :
        new Random((long) args[0].mustCast(Number.class).get());
      return asNative(random);
    }));
  static final Scope bufferedIteratorClass =
    makeNativeClass(
      BufferedIterator.class,
      new Scope(null)
        .put(new Builtin("has_more", P(), (self, args) ->
          self.mustGetNative(BufferedIterator.class).hasNext() ? tru : fal))
        .put(new Builtin("next", P(), (self, args) ->
          self.mustGetNative(BufferedIterator.class).next())));
  static final Scope globals = new Scope(null)
    .put("Object", objectClass)
    .put("Class", classClass)
    .put("Nil", nilClass)
    .put("Bool", boolClass)
    .put("Number", numberClass)
    .put("String", stringClass)
    .put("Function", functionClass)
    .put("List", listClass)
    .put("RandomAccessContainer", randomAccessContainerClass)
    .put("assert", makeSingleton("assert")
      .put(new Builtin("type", P("obj", "type"), (self, args) -> {
        expectType(args[0], args[1].mustCast(Scope.class));
        return nil;
      }))
      .put(new Builtin("__call", P("x"), (self, args) -> {
        if (!args[0].truthy()) {
          throw new AssertError("Assertion failed");
        }
        return nil;
      }))
      .put(new Builtin("equal", P("a", "b"), (self, args) -> {
        if (!args[0].equals(args[1])) {
          throw new AssertError("Expected " + args[0] + " to equal " + args[1]);
        }
        return nil;
      })))
    .put(new Builtin("_keys", P("obj"), (self, args) -> {
      ArrayList<String> names =
        map(args[0].mustCast(Scope.class).table.keySet(), Symbol::toString);
      ArrayList<Value> values = map(sorted(names), Str::of);
      return new Arr(values);
    }))
    .put(new Builtin("fail", P("message"), (self, args) -> {
      throw new AssertError(args[0].str());
    }))
    .put(new Builtin("iter", P("obj"), (self, args) ->
      asNative(new BufferedIterator(args[0].iterator()))))
    .put(new Builtin("type", P("obj"), (self, args) -> {
      Value klass = args[0].getAttribute(__classSymbol);
      if (klass != null) {
        return klass;
      }
      return objectClass;
    }))
    .put(new Builtin("len", P("obj"), (self, args) -> {
      return args[0].callMethod(__lenSymbol, new Value[0]);
    }))
    .put(new Builtin("str", P("x"), (self, args) ->
      Str.of(args[0].str())))
    .put(new Builtin("repr", P("x"), (self, args) -> {
      return Str.of(args[0].repr());
    }));
  static Parameters P(String... names) {
    return Parameters.of(names);
  }
  private static final Importer importer = new Importer()
    .put("_globals", () -> BigTalkCore.importer.getGlobals());
  static {
    addNativeModule("random", () -> new Scope(null)
      .put("Random", randomClass));
  }
  public static void addNativeModule(String name, NativeModule nm) {
    importer.put(name, nm);
  }
  public static void addSourceLoader(SourceLoader loader) {
    importer.addSourceLoader(loader);
  }
  public static void setGlobals(Scope globals) {
    importer.setGlobals(globals);
  }
  public static Scope importModule(String name) {
    return importer.importModule(name);
  }

  public static final class Source {
    public final String module;
    public final String filename;
    public final String text;
    public Source(String module, String filename, String text) {
      this.module = module;
      this.filename = filename;
      this.text = text;
    }
  }
  public static final class Token {
    public final Source source;
    public final int start;
    public final int end;
    public final String type;
    public final Object value;
    public Token(Source source, int start, int end, String type, Object value) {
      this.source = source;
      this.start = start;
      this.end = end;
      this.type = type;
      this.value = value;
    }
    public Token(Source source, int start, String type, Object value) {
      this(source, start, start, type, value);
    }
    public Token(String type, Object value) {
      this(null, 0, 0, type, value);
    }
    public Token(String type) {
      this(type, type);
    }
    @Override public String toString() {
      return "Token(" + type + ", " + value + ")";
    }
    @Override public boolean equals(Object other) {
      return other instanceof Token && equals((Token) other);
    }
    private boolean equals(Token other) {
      return type.equals(other.type) && value.equals(other.value);
    }
    @Override public int hashCode() {
      return Objects.hash(type, value);
    }
    public int lineno() {
      if (source == null) {
        return 0;
      }
      return strcount(source.text, "\n", 0, start) + 1;
    }
    public String line() {
      if (source == null) {
        return "";
      }
      String s = source.text;
      return s.substring(
        strrfind(s, "\n", start) + 1, strfind(s, "\n", start));
    }
    public int colno() {
      if (source == null) {
        return 0;
      }
      String s = source.text;
      return start - strrfind(s, "\n", start);
    }
    public String locationMessage(int indentCount) {
      // TODO: strrep(" ", colno() - 1) doesn't work when
      // the line in question involves Chinese characters or emojis
      // that don't translate to normal width even in monospace
      // font. It might be nice to handle this at some point.
      String indent = "";
      for (int i = 0; i < indentCount; i++) {
        indent += " ";
      }
      return
        indent + "On line " + lineno() + " column " + colno() +
        " " + source.module + "\n" +
        indent + line() + "\n" +
        indent + strrep(" ", colno() - 1) + "*\n";
    }
    public String locationMessage() {
      return locationMessage(0);
    }
  }
  public static Source makeStringSource(String body) {
    return new Source("<string>", "<string>", body);
  }
  public static class Error extends RuntimeException {
    protected final String message;
    public Error(String message) {
      this(message, null);
    }
    public Error(String message, Throwable e) {
      this(toList(tokenStack.get()), message, e);
    }
    private Error(List<Token> tokens, String message, Throwable e) {
      this(0, strjoin("", tokensToLocationMessages(tokens)) + message, e);
    }
    private Error(int i, String formattedMessage, Throwable e) {
      super(formattedMessage, e);
      this.message = formattedMessage;
    }
    private static List<String> tokensToLocationMessages(Iterable<Token> tokens) {
      List<String> ret = new ArrayList<String>();
      for (Token token: tokens) {
        ret.add(token.locationMessage(4));
      }
      return ret;
    }
  }
  public static final class InternalError extends Error {
    public InternalError(String message) {
      super(message);
    }
  }
  public static abstract class UserError extends Error {
    public UserError(Throwable throwable) {
      super("", throwable);
    }
    public UserError(String message) {
      super(message);
    }
    public UserError(List<Token> tokens, String message, Throwable e) {
      super(tokens, message, e);
    }
    public final String getMessage() {
      return getClass().getSimpleName() + "\n" + message;
    }
  }
  public static final class SyntaxError extends UserError {
    public SyntaxError(String message) {
      super(message);
    }
    public SyntaxError(Token token, String message) {
      super(toList(tokenStack.get(), Arrays.asList(token)), message, null);
    }
  }
  public static final class MiscError extends UserError {
    public MiscError(String message) {
      super(message);
    }
  }
  public static final class KeyError extends UserError {
    public KeyError(String message) {
      super(message);
    }
  }
  public static final class TypeError extends UserError {
    public TypeError(String message) {
      super(message);
    }
  }
  public static final class AssertError extends UserError {
    public AssertError(String message) {
      super(message);
    }
  }

  public static abstract class Opcode {
    public final Token token;
    Opcode(Token token) {
      this.token = token;
    }
    public abstract int step(Scope scope, ValueStack stack);
    @Override public String toString() {
      return getClass().getSimpleName() + "()";
    }
  }
  public static final class Noop extends Opcode {
    Noop(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      return NEXT;
    }
  }
  public static final class PopReturn extends Opcode {
    PopReturn(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      return RETURN;
    }
  }
  public static final class DoYield extends Opcode {
    DoYield(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      return YIELD;
    }
  }
  public static final class Pop extends Opcode {
    Pop(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.pop();
      return NEXT;
    }
    @Override public String toString() {
      return "Pop()";
    }
  }
  public static final class PushLogicalNot extends Opcode {
    PushLogicalNot(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(stack.pop().truthy() ? fal : tru);
      return NEXT;
    }
  }
  public static final class PushLiteral extends Opcode {
    private final Value value;
    PushLiteral(Token token, Value value) {
      super(token);
      this.value = value;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(value);
      return NEXT;
    }
    @Override public String toString() {
      return "PushLiteral(" + value.getTypename() + ")";
    }
  }
  public static final class DuplicateTop extends Opcode {
    DuplicateTop(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(stack.peek());
      return NEXT;
    }
  }
  public static final class SaveVariable extends Opcode {
    private final Symbol name;
    SaveVariable(Token token, Symbol name) {
      super(token);
      this.name = name;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      scope.put(name, stack.peek());
      return NEXT;
    }
    @Override public String toString() {
      return "SaveVariable(" + name + ")";
    }
  }
  public static final class SavePattern extends Opcode {
    private final Pattern pattern;
    SavePattern(Token token, Pattern pattern) {
      super(token);
      this.pattern = pattern;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      pattern.bind(scope, stack.peek());
      return NEXT;
    }
  }
  public static final class PushVariable extends Opcode {
    private final Symbol name;
    PushVariable(Token token, Symbol name) {
      super(token);
      this.name = name;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      Value value = scope.get(name);
      if (value == null) {
        withToken(token, () -> {
          throw new KeyError("No variable named " + name);
        });
      }
      stack.push(value);
      return NEXT;
    }
    @Override public String toString() {
      return "PushVariable(" + name + ")";
    }
  }
  public static final class PushAttribute extends Opcode {
    private final Symbol name;
    private final Symbol methodName;
    PushAttribute(Token token, Symbol name) {
      super(token);
      this.name = name;
      this.methodName = Symbol.of("__get_" + name.toString());
    }
    @Override public int step(Scope scope, ValueStack stack) {
      Value owner = stack.pop();
      Value value = owner.getattr(name);
      if (value == null) {
        Value backupMethod = owner.getAttribute(methodName);
        if (backupMethod == null) {
          withToken(token, () -> {
            throw new KeyError(
              "No attribute " + name + " for " + owner.getTypename());
          });
        }
        Value[] pointer = new Value[1];
        withToken(token, () -> {
          pointer[0] = backupMethod.call(owner);
        });
        value = pointer[0];
      }
      stack.push(value);
      return NEXT;
    }
  }
  public static final class PopAttribute extends Opcode {
    private final Symbol name;
    private final Symbol methodName;
    PopAttribute(Token token, Symbol name) {
      super(token);
      this.name = name;
      this.methodName = Symbol.of("__set_" + name.toString());
    }
    @Override public int step(Scope scope, ValueStack stack) {
      Value value = stack.pop();
      Value owner = stack.pop();
      Value setterMethod = owner.getAttribute(methodName);
      if (setterMethod != null) {
        setterMethod.call(owner, value);
      } else {
        owner.setAttribute(name, value);
      }
      stack.push(value);
      return NEXT;
    }
  }
  public static final class PushFunctionCall extends Opcode {
    private final int argc;
    PushFunctionCall(Token token, int argc) {
      super(token);
      this.argc = argc;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      Value[] args = stack.pop(argc);
      Value function = stack.pop();
      withToken(token, () -> stack.push(function.call(null, args)));
      return NEXT;
    }
    @Override public String toString() {
      return "PushFunctionCall(" + argc + ")";
    }
  }
  public static final class PushImport extends Opcode {
    private final String name;
    PushImport(Token token, String name) {
      super(token);
      this.name = name;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(importer.importModule(name));
      return NEXT;
    }
    @Override public String toString() {
      return "PushImport(" + name + ")";
    }
  }
  public static final class MakeFunction extends Opcode {
    private final Parameters parameters;
    private final Opcode[] opcodes;
    MakeFunction(Token token, Parameters parameters, Opcode[] opcodes) {
      super(token);
      this.parameters = parameters;
      this.opcodes = opcodes;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(new UserFunction(parameters, opcodes, scope));
      return NEXT;
    }
  }
  public static final class MakeGenerator extends Opcode {
    private final Parameters parameters;
    private final Opcode[] opcodes;
    MakeGenerator(Token token, Parameters parameters, Opcode[] opcodes) {
      super(token);
      this.parameters = parameters;
      this.opcodes = opcodes;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(new GeneratorFunction(parameters, opcodes, scope));
      return NEXT;
    }
  }
  public static final class MakeClass extends Opcode {
    private final Symbol name;
    private final int baseCount;
    private final Symbol[] methodNames;
    MakeClass(Token token, Symbol name, int baseCount, Symbol[] methodNames) {
      super(token);
      this.name = name;
      this.baseCount = baseCount;
      this.methodNames = methodNames;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      Value[] methods = stack.pop(methodNames.length);
      List<Scope> bases = map(listOf(stack.pop(baseCount)), v -> v.mustCast(Scope.class));
      Scope proto = new Scope(null);
      for (int i = 0; i < methodNames.length; i++) {
        proto.setAttribute(methodNames[i], methods[i]);
      }
      stack.push(makeClass(name.toString(), bases, proto));
      return NEXT;
    }
    @Override public String toString() {
      return "MakeClass(" + name + ", " + baseCount + ", " + listOf(methodNames) + ")";
    }
  }
  public static abstract class Branch extends Opcode {
    int loc = JUMP_NOT_SET;
    Branch(Token token) {
      super(token);
    }
    public Branch set(int loc) {
      this.loc = loc;
      return this;
    }
  }
  public static final class Jump extends Branch {
    Jump(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      return loc;
    }
  }
  public static final class JumpIf extends Branch {
    JumpIf(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      if (stack.pop().truthy()) {
        return loc;
      }
      return NEXT;
    }
  }
  public static final class JumpIfNot extends Branch {
    JumpIfNot(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      if (!stack.pop().truthy()) {
        return loc;
      }
      return NEXT;
    }
  }
  public static final class ForLoopSetup extends Opcode {
    ForLoopSetup(Token token) {
      super(token);
    }
    @Override public int step(Scope scope, ValueStack stack) {
      stack.push(stack.pop().iterator());
      return NEXT;
    }
  }
  public static final class ForLoopNext extends Branch {
    private final Pattern pattern;
    ForLoopNext(Token token, Pattern pattern) {
      super(token);
      this.pattern = pattern;
    }
    @Override public int step(Scope scope, ValueStack stack) {
      Value next = stack.peek().next();
      if (next == null) {
        stack.pop();
        return loc;
      }
      pattern.bind(scope, next);
      return NEXT;
    }
  }

  public static abstract class Node {
    public final Token token;
    public Node(Token token) {
      this.token = token;
    }
    public abstract void compile(List<Opcode> out);
    public final Opcode[] compile() {
      List<Opcode> out = new ArrayList<>();
      compile(out);
      return out.toArray(new Opcode[0]);
    }
  }
  public static abstract class Statement extends Node {
    public Statement(Token token) {
      super(token);
    }
  }
  public static abstract class Expression extends Node {
    public Expression(Token token) {
      super(token);
    }
  }
  public static final class Block extends Statement {
    private final List<Statement> statements;
    public Block(Token token, List<Statement> statements) {
      super(token);
      this.statements = statements;
    }
    @Override public void compile(List<Opcode> out) {
      for (Statement statement: statements) {
        statement.compile(out);
      }
    }
  }
  public static final class If extends Statement {
    private final Expression condition;
    private final Block body;
    private final Statement other;
    public If(Token token, Expression condition, Block body, Statement other) {
      super(token);
      this.condition = condition;
      this.body = body;
      this.other = other;
    }
    @Override public void compile(List<Opcode> out) {
      condition.compile(out);
      Branch jumpIf = new JumpIfNot(token);
      out.add(jumpIf);
      body.compile(out);
      if (other != null) {
        Branch jump = new Jump(token);
        out.add(jump);
        jumpIf.set(out.size());
        other.compile(out);
        jump.set(out.size());
      } else {
        jumpIf.set(out.size());
      }
    }
  }
  public static final class While extends Statement {
    private final Expression condition;
    private final Block body;
    public While(Token token, Expression condition, Block body) {
      super(token);
      this.condition = condition;
      this.body = body;
    }
    @Override public void compile(List<Opcode> out) {
      int start = out.size();
      condition.compile(out);
      Branch jumpIf = new JumpIfNot(token);
      out.add(jumpIf);
      body.compile(out);
      out.add(new Jump(token).set(start));
      jumpIf.set(out.size());
    }
  }
  public static final class Return extends Statement {
    private final Expression expression;
    public Return(Token token, Expression expression) {
      super(token);
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new PopReturn(token));
    }
  }
  public static final class For extends Statement {
    private final Pattern pattern;
    private final Expression expression;
    private final Block body;
    public For(Token token, Pattern pattern, Expression expression, Block body) {
      super(token);
      this.pattern = pattern;
      this.expression = expression;
      this.body = body;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new ForLoopSetup(token));
      int step = out.size();
      Branch forLoopNext = new ForLoopNext(token, pattern);
      out.add(forLoopNext);
      body.compile(out);
      out.add(new Jump(token).set(step));
      forLoopNext.set(out.size());
    }
  }
  public static final class ImportStatement extends Statement {
    public final String name, alias;
    public ImportStatement(Token token, String name, String alias) {
      super(token);
      this.name = name;
      this.alias = alias;
    }
    @Override public void compile(List<Opcode> out) {
      out.add(new PushImport(token, name));
      out.add(new SaveVariable(token, Symbol.of(alias)));
      out.add(new Pop(token));
    }
  }
  public static final class ExpressionStatement extends Statement {
    public final Expression expression;
    public ExpressionStatement(Token token, Expression expression) {
      super(token);
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new Pop(token));
    }
  }
  public static final class Literal extends Expression {
    public final Value value;
    public Literal(Token token, Value value) {
      super(token);
      this.value = value;
    }
    @Override public void compile(List<Opcode> out) {
      out.add(new PushLiteral(token, value));
    }
  }
  public static final class GetVariable extends Expression {
    public final Symbol name;
    public GetVariable(Token token, Symbol name) {
      super(token);
      this.name = name;
    }
    @Override public void compile(List<Opcode> out) {
      out.add(new PushVariable(token, name));
    }
  }
  public static final class SetVariable extends Expression {
    public final Symbol name;
    public final Expression expression;
    public SetVariable(Token token, Symbol name, Expression expression) {
      super(token);
      this.name = name;
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new SaveVariable(token, name));
    }
  }
  public static final class SetPattern extends Expression {
    public final Pattern pattern;
    public final Expression expression;
    public SetPattern(Token token, Pattern pattern, Expression expression) {
      super(token);
      this.pattern = pattern;
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new SavePattern(token, pattern));
    }
  }
  public static final class GetAttribute extends Expression {
    public final Expression owner;
    public final Symbol name;
    public GetAttribute(Token token, Expression owner, Symbol name) {
      super(token);
      this.name = name;
      this.owner = owner;
    }
    @Override public void compile(List<Opcode> out) {
      owner.compile(out);
      out.add(new PushAttribute(token, name));
    }
  }
  public static final class SetAttribute extends Expression {
    public final Expression owner;
    public final Symbol name;
    public final Expression expression;
    public SetAttribute(Token token, Expression owner, Symbol name, Expression expression) {
      super(token);
      this.name = name;
      this.owner = owner;
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      owner.compile(out);
      expression.compile(out);
      out.add(new PopAttribute(token, name));
    }
  }
  public static final class CallFunction extends Expression {
    public final Expression functionExpression;
    public final List<Expression> argumentExpressions;
    public CallFunction(Token token, Expression functionExpression, List<Expression> argumentExpressions) {
      super(token);
      this.functionExpression = functionExpression;
      this.argumentExpressions = argumentExpressions;
    }
    @Override public void compile(List<Opcode> out) {
      functionExpression.compile(out);
      for (Expression expression: argumentExpressions) {
        expression.compile(out);
      }
      out.add(new PushFunctionCall(token, argumentExpressions.size()));
    }
  }
  public static final class Yield extends Expression {
    private final Expression expression;
    public Yield(Token token, Expression expression) {
      super(token);
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new DoYield(token));
    }
  }
  public static final class BlockExpression extends Expression {
    private final List<Statement> statements;
    private final Expression expression;
    public BlockExpression(Token token, List<Statement> statements) {
      super(token);
      this.statements = new ArrayList<Statement>(statements);
      if (this.statements.isEmpty()) {
        this.expression = new Literal(token, nil);
      } else {
        Statement lastStatement =
          this.statements.remove(this.statements.size() - 1);
        if (lastStatement instanceof ExpressionStatement) {
          this.expression = ((ExpressionStatement) lastStatement).expression;
        } else {
          this.expression = new Literal(token, nil);
          this.statements.add(lastStatement);
        }
      }
    }
    @Override public void compile(List<Opcode> out) {
      for (Statement statement: statements) {
        statement.compile(out);
      }
      expression.compile(out);
    }
  }
  public static abstract class BaseDef extends Expression {
    final Symbol name;
    final Parameters parameters;
    final Statement body;
    public BaseDef(Token token, Symbol name, Parameters parameters, Statement body) {
      super(token);
      this.name = name;
      this.parameters = parameters;
      this.body = body;
    }
  }
  public static final class Def extends BaseDef {
    public Def(Token token, Symbol name, Parameters parameters, Statement body) {
      super(token, name, parameters, body);
    }
    public Def(Token token, Symbol name, Parameters parameters, Expression e) {
      this(token, name, parameters, new Return(token, e));
    }
    @Override public void compile(List<Opcode> out) {
      out.add(new MakeFunction(token, parameters, body.compile()));
    }
  }
  public static final class DefStar extends BaseDef {
    public DefStar(Token token, Symbol name, Parameters parameters, Statement body) {
      super(token, name, parameters, body);
    }
    @Override public void compile(List<Opcode> out) {
      out.add(new MakeGenerator(token, parameters, body.compile()));
    }
  }
  public static final class ClassDef extends Expression {
    private final Symbol name;
    private final List<Expression> bases;
    private final List<Pair<Symbol, Expression>> pairs;
    public ClassDef(Token token, Symbol name, List<Expression> bases, List<Pair<Symbol, Expression>> pairs) {
      super(token);
      this.name = name;
      this.bases = bases;
      this.pairs = pairs;
    }
    @Override public void compile(List<Opcode> out) {
      Symbol[] methodNames = map(pairs, pair -> pair.a).toArray(new Symbol[0]);
      for (Expression base: bases) {
        base.compile(out);
      }
      for (Pair<Symbol, Expression> pair: pairs) {
        pair.b.compile(out);
      }
      out.add(new MakeClass(token, name, bases.size(), methodNames));
    }
  }
  public static final class Not extends Expression {
    private final Expression expression;
    public Not(Token token, Expression expression) {
      super(token);
      this.expression = expression;
    }
    @Override public void compile(List<Opcode> out) {
      expression.compile(out);
      out.add(new PushLogicalNot(token));
    }
  }
  public static final class Or extends Expression {
    private final Expression left, right;
    public Or(Token token, Expression left, Expression right) {
      super(token);
      this.left = left;
      this.right = right;
    }
    @Override public void compile(List<Opcode> out) {
      left.compile(out);
      out.add(new DuplicateTop(token));
      Branch jumpIf = new JumpIf(token);
      out.add(jumpIf);
      out.add(new Pop(token));
      right.compile(out);
      jumpIf.set(out.size());
    }
  }
  public static final class And extends Expression {
    private final Expression left, right;
    public And(Token token, Expression left, Expression right) {
      super(token);
      this.left = left;
      this.right = right;
    }
    @Override public void compile(List<Opcode> out) {
      left.compile(out);
      out.add(new DuplicateTop(token));
      Branch jumpIfNot = new JumpIfNot(token);
      out.add(jumpIfNot);
      out.add(new Pop(token));
      right.compile(out);
      jumpIfNot.set(out.size());
    }
  }
  public static final class Ternary extends Expression {
    private final Expression condition, left, right;
    public Ternary(Token token, Expression condition, Expression left, Expression right) {
      super(token);
      this.condition = condition;
      this.left = left;
      this.right = right;
    }
    @Override public void compile(List<Opcode> out) {
      condition.compile(out);
      Branch jumpIfNot = new JumpIfNot(token);
      out.add(jumpIfNot);

      left.compile(out);
      Branch jumpToEnd = new Jump(token);
      out.add(jumpToEnd);

      jumpIfNot.set(out.size());
      right.compile(out);

      jumpToEnd.set(out.size());
    }
  }
  public static abstract class Pattern {
    public final Token token;
    Pattern(Token token) {
      this.token = token;
    }
    public abstract void bind(Scope scope, Value value);
  }
  public static final class NamePattern extends Pattern {
    public final Symbol name;
    public NamePattern(Token token, Symbol name) {
      super(token);
      this.name = name;
    }
    @Override public void bind(Scope scope, Value value) {
      scope.put(name, value);
    }
  }
  public static final class ListPattern extends Pattern {
    public final List<Pattern> patterns;
    public final Symbol varname;
    public ListPattern(Token token, List<Pattern> patterns, Symbol varname) {
      super(token);
      this.patterns = patterns;
      this.varname = varname;
    }
    @Override public void bind(Scope scope, Value value) {
      withToken(token, () -> {
        Value iterator = value.iterator();
        Value next = iterator.next();
        for (Pattern subpattern: patterns) {
          if (next == null) {
            throw new TypeError("Not enough values to unpack");
          }
          subpattern.bind(scope, next);
          next = iterator.next();
        }
        if (varname == null) {
          if (next != null) {
            throw new TypeError("Too many values to unpack");
          }
        } else {
          ArrayList<Value> arr = new ArrayList<>();
          while (next != null) {
            arr.add(next);
            next = iterator.next();
          }
          scope.put(varname, new Arr(arr));
        }
      });
    }
  }

  public static abstract class Value {
    private static final Map<Class<?>, String> typenames =
      new HashMap<>();
    static {
      typenames.put(Nil.class, "nil");
      typenames.put(Bool.class, "bool");
      typenames.put(Number.class, "double");
      typenames.put(Str.class, "string");
      typenames.put(Arr.class, "list");
      typenames.put(Scope.class, "object");
    }
    private Value() {}  // Forbid subclassing outside BigTalkCore
    public final String getTypename() {
      return Optional.ofNullable(getAttribute(__classSymbol))
        .map(cls -> cls.mustGetAttribute(__nameSymbol).str())
        .orElseGet(() ->
          typenames.computeIfAbsent(getClass(), Class::getSimpleName));
    }
    public final <T extends Value> T cast(Class<T> cls) {
      if (cls.isInstance(this)) {
        return cls.cast(this);
      }
      return null;
    }
    public final <T extends Value> T mustCast(Class<T> cls) {
      T t = cast(cls);
      if (t == null) {
        throw new TypeError(
          "Expected " + typenames.computeIfAbsent(cls, Class::getSimpleName) +
          " but got " + getTypename());
      }
      return t;
    }
    public final <T> T getNative(Class<T> cls) {
      Native n = cast(Native.class);
      if (n != null && cls.isInstance(n.value)) {
        return cls.cast(n.value);
      }
      return null;
    }
    public final <T> T mustGetNative(Class<T> cls) {
      T t = getNative(cls);
      if (t == null) {
        Native n = cast(Native.class);
        if (n == null) {
          throw new TypeError("Expected Native, but got " + getTypename());
        }
        throw new TypeError(
          "Expected " + cls + " but got " + n.value.getClass());
      }
      return t;
    }
    public boolean truthy() {
      return true;
    }
    public Value bind(Value owner) {
      return this;
    }
    public Value call(Value owner, Value... args) {
      throw new TypeError(getTypename() + " is not a function");
    }
    public Value callMethod(Symbol name, Value... args) {
      return mustGetAttribute(name).call(this, args);
    }
    public final Value getattr(Symbol key) {
      Value value = getAttribute(key);
      if (value != null) {
        return value.bind(this);
      }
      return null;
    }
    public abstract Value getAttribute(Symbol key);
    public abstract void setAttribute(Symbol key, Value value);
    public Value mustGetAttribute(Symbol key) {
      Value value = getAttribute(key);
      if (value == null) {
        throw new KeyError("No such attribute " + key + " for " + getTypename());
      }
      return value;
    }
    public Value mustGetattr(Symbol key) {
      return mustGetAttribute(key).bind(this);
    }
    public Value next() {
      throw new TypeError(getTypename() + " is not an iterator");
    }
    public Value iterator() {
      throw new TypeError(getTypename() + " is not an iterable");
    }
    public String repr() {
      return "<" + getTypename() + ">";
    }
    public String str() {
      return repr();
    }
    public boolean lessThan(Value other) {
      return callMethod(__ltSymbol, other).truthy();
    }
    @Override public String toString() {
      return str();
    }
  }
  public static abstract class SimpleValue extends Value {
    private SimpleValue() {}
    public abstract Scope getProto();
    @Override public final Value getAttribute(Symbol key) {
      return getProto().getAttribute(key);
    }
    @Override public final void setAttribute(Symbol key, Value value) {
      throw new TypeError(getTypename() + " does not support setting attributes");
    }
  }
  public static final class Native extends SimpleValue {
    private final Object value;
    private final Scope proto;
    private Native(Object value, Scope proto) {
      this.value  = value;
      this.proto  = proto;
    }
    private Native(Object value) {
      this(value, nativeProto);
    }
    public Scope getProto() {
      return proto;
    }
    @Override public String repr() {
      Value method = getAttribute(__reprSymbol);
      if (method == null) {
        return getDefaultRepr();
      }
      return method.call(this).mustCast(Str.class).value;
    }
    @Override public String str() {
      Value method = getAttribute(__strSymbol);
      if (method == null) {
        return getDefaultRepr();
      }
      return method.call(this).mustCast(Str.class).value;
    }
    private String getDefaultRepr() {
      return value.toString();
    }
  }
  public static final class Nil extends SimpleValue {
    private Nil() {
    }
    @Override public Scope getProto() {
      return nilProto;
    }
    @Override public boolean truthy() {
      return false;
    }
    @Override public String repr() {
      return "nil";
    }
  }
  public static final class Bool extends SimpleValue {
    private final boolean value;
    private Bool(boolean value) {
      this.value = value;
    }
    @Override public Scope getProto() {
      return boolProto;
    }
    @Override public boolean truthy() {
      return value;
    }
    @Override public String repr() {
      return value ? "true" : "false";
    }
  }
  public static final class Number extends SimpleValue {
    public static Number of(double value) {
      return new Number(value);
    }
    private final double value;
    private Number(double value) {
      this.value = value;
    }
    public double get() {
      return value;
    }
    @Override public boolean equals(Object other) {
      return other instanceof Number && value == ((Number) other).value;
    }
    @Override public int hashCode() {
      return Double.valueOf(value).hashCode();
    }
    @Override public boolean truthy() {
      return value != 0;
    }
    @Override public String repr() {
      return !Double.isInfinite(value) && value == Math.floor(value) ?
        Long.valueOf((long) value).toString() :
        Double.valueOf(value).toString();
    }
    @Override public boolean lessThan(Value other) {
      return value < other.mustCast(Number.class).value;
    }
    @Override public Scope getProto() {
      return numberProto;
    }
  }
  public static final class Str extends SimpleValue {
    public static Str of(String value) {
      return new Str(value);
    }
    public static Str fromCodePoint(int codePoint) {
      int[] codePoints = new int[]{codePoint};
      return new Str(new String(codePoints, 0, 1), codePoints);
    }
    private final String value;
    private int[] codePoints;
    private Str(String value) {
      this(value, null);
    }
    private Str(String value, int[] codePoints) {
      this.value = value;
      this.codePoints = codePoints;
    }
    private int[] getCodePoints() {
      if (codePoints == null) {
        codePoints = value.codePoints().toArray();
      }
      return codePoints;
    }
    public int size() {
      return getCodePoints().length;
    }
    public Str getItem(int i) {
      return fromCodePoint(getCodePoints()[i]);
    }
    @Override public boolean equals(Object other) {
      return other instanceof Str && value.equals(((Str) other).value);
    }
    @Override public int hashCode() {
      return value.hashCode();
    }
    @Override public boolean truthy() {
      return value.length() > 0;
    }
    @Override public String repr() {
      return "\"" + strescape(value) + "\"";
    }
    @Override public String str() {
      return value;
    }
    @Override public Scope getProto() {
      return stringProto;
    }
    @Override public boolean lessThan(Value other) {
      return value.compareTo(other.mustCast(Str.class).value) < 0;
    }
    public String get() {
      return value;
    }
  }
  public static final class Arr extends SimpleValue {
    public static Arr of(Value... args) {
      return new Arr(new ArrayList<>(Arrays.asList(args)));
    }
    private final ArrayList<Value> value;
    public Arr(ArrayList<Value> value) {
      this.value = value;
    }
    @Override public boolean equals(Object other) {
      return other instanceof Arr && value.equals(((Arr) other).value);
    }
    @Override public int hashCode() {
      return value.hashCode();
    }
    @Override public boolean truthy() {
      return value.size() > 0;
    }
    @Override public String repr() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      boolean first = true;
      for (Value v: value) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(v.repr());
        first = false;
      }
      sb.append("]");
      return sb.toString();
    }
    @Override public Scope getProto() {
      return listProto;
    }
    @Override public Value iterator() {
      return new ArrIterator(value);
    }
  }
  public static final class ArrIterator extends SimpleValue {
    private final ArrayList<Value> arr;
    private int i = 0;
    ArrIterator(ArrayList<Value> arr) {
      this.arr = arr;
    }
    @Override public Scope getProto() {
      return objectProto;
    }
    @Override public Value next() {
      return i < arr.size() ? arr.get(i++) : null;
    }
  }
  public static final class XSet extends SimpleValue {
    private final HashSet<Value> value;
    public XSet(HashSet<Value> value) {
      this.value = value;
    }
    @Override public boolean equals(Object other) {
      return other instanceof XSet && value.equals(((XSet) other).value);
    }
    @Override public int hashCode() {
      return value.hashCode();
    }
    @Override public boolean truthy() {
      return value.size() > 0;
    }
    @Override public String repr() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      boolean first = true;
      for (Value v: value) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(v.repr());
        first = false;
      }
      sb.append("}");
      return sb.toString();
    }
    @Override public Scope getProto() {
      return setProto;
    }
  }
  public static final class XMap extends SimpleValue {
    private final HashMap<Value, Value> value;
    public XMap(HashMap<Value, Value> value) {
      this.value = value;
    }
    @Override public boolean equals(Object other) {
      return other instanceof XMap && value.equals(((XMap) other).value);
    }
    @Override public int hashCode() {
      return value.hashCode();
    }
    @Override public boolean truthy() {
      return value.size() > 0;
    }
    @Override public String repr() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      boolean first = true;
      for (Map.Entry<Value, Value> entry: value.entrySet()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().repr() + ": " + entry.getValue().repr());
        first = false;
      }
      sb.append("}");
      return sb.toString();
    }
    @Override public Scope getProto() {
      return mapProto;
    }
  }
  public static final class Scope extends Value {
    private final Scope parent;
    private final Map<Symbol, Value> table = new HashMap<>();
    public Scope(Scope parent) {
      this.parent = parent;
    }
    public Value get(Symbol key) {
      Value value = table.get(key);
      if (value != null) {
        return value;
      }
      if (parent != null) {
        return parent.get(key);
      }
      return null;
    }
    public Scope put(Symbol key, Value value) {
      table.put(key, value);
      return this;
    }
    public Scope put(Builtin builtin) {
      return put(builtin.name, builtin);
    }
    public Scope put(String key, Value value) {
      return put(Symbol.of(key), value);
    }
    public Scope updateFrom(Map<Symbol, Value> t) {
      for (Map.Entry<Symbol, Value> entry: t.entrySet()) {
        table.put(entry.getKey(), entry.getValue());
      }
      return this;
    }
    public Scope updateFrom(Scope other) {
      return updateFrom(other.table);
    }
    public void updateIfMissing(Map<Symbol, Value> t) {
      for (Map.Entry<Symbol, Value> entry: t.entrySet()) {
        if (!table.containsKey(entry.getKey())) {
          table.put(entry.getKey(), entry.getValue());
        }
      }
    }
    public void updateIfMissing(Scope other) {
      updateIfMissing(other.table);
    }
    @Override public Value iterator() {
      Value getIterator = getAttribute(__iterSymbol);
      if (getIterator == null) {
        return super.iterator();
      }
      return getIterator.call(this);
    }
    @Override public Value next() {
      Value getNext = getAttribute(__nextSymbol);
      if (getNext == null) {
        return super.next();
      }
      return getNext.call(this);
    }
    @Override public Value getAttribute(Symbol key) {
      return get(key);
    }
    @Override public void setAttribute(Symbol key, Value value) {
      put(key, value);
    }
    @Override public Value call(Value owner, Value... args) {
      Value method = getAttribute(__callSymbol);
      if (method == null) {
        return super.call(owner, args);
      }
      return method.call(this, args);
    }
    @Override public String repr() {
      Value method = getAttribute(__reprSymbol);
      if (method == null) {
        return super.repr();
      }
      return method.call(this).mustCast(Str.class).value;
    }
    @Override public String str() {
      Value method = getAttribute(__strSymbol);
      if (method == null) {
        return super.str();
      }
      return method.call(this).mustCast(Str.class).value;
    }
  }
  public static abstract class BaseFunction extends SimpleValue {
    final Parameters parameters;
    private BaseFunction(Parameters parameters) {
      this.parameters = parameters;
    }
    @Override public Scope getProto() {
      return functionProto;
    }
  }
  public static abstract class BaseUserFunction extends BaseFunction {
    final Opcode[] opcodes;
    final Scope parentScope;
    private BaseUserFunction(Parameters parameters, Opcode[] opcodes, Scope parentScope) {
      super(parameters);
      this.opcodes = opcodes;
      this.parentScope = parentScope;
    }
    @Override public BoundFunction bind(Value owner) {
      return new BoundFunction(owner, this);
    }
  }
  public static final class BoundFunction extends BaseFunction {
    private final Value owner;
    private final BaseUserFunction function;
    public BoundFunction(Value owner, BaseUserFunction function) {
      super(function.parameters);
      this.owner = owner;
      this.function = function;
    }
    @Override public Value call(Value owner, Value... args) {
      return function.call(this.owner, args);
    }
    @Override public String repr() {
      return "<bound " + function.repr() + ">";
    }
  }
  public static final class UserFunction extends BaseUserFunction {
    public UserFunction(Parameters parameters, Opcode[] opcodes, Scope parentScope) {
      super(parameters, opcodes, parentScope);
    }
    @Override public Value call(Value owner, Value... args) {
      Scope scope = new Scope(parentScope);
      parameters.bind(scope, owner, args);
      return run(scope, valueStack.get(), opcodes);
    }
    public static Value run(Scope scope, ValueStack stack, Opcode[] opcodes) {
      int ip = 0;
      while (ip < opcodes.length) {
        int result = opcodes[ip].step(scope, stack);
        switch (result) {
          case NEXT:
            ip++;
            break;
          case RETURN:
            return stack.pop();
          case YIELD:
            throw new TypeError("YIELD from inside a function");
          default:
            ip = result;
        }
      }
      return nil;
    }
  }
  public static final class GeneratorFunction extends BaseUserFunction {
    public GeneratorFunction(Parameters parameters, Opcode[] opcodes, Scope parentScope) {
      super(parameters, opcodes, parentScope);
    }
    @Override public Value call(Value owner, Value... args) {
      Scope scope = new Scope(parentScope);
      parameters.bind(scope, owner, args);
      return new GeneratorObject(opcodes, scope);
    }
  }
  public static final class GeneratorObject extends SimpleValue {
    private final Opcode[] opcodes;
    private final Scope scope;
    private final ValueStack stack = new ValueStack(5);
    private int ip = 0;
    public GeneratorObject(Opcode[] opcodes, Scope scope) {
      this.opcodes = opcodes;
      this.scope = scope;
    }
    public Value resume(Value input) {
      stack.push(input);
      while (ip < opcodes.length) {
        int result = opcodes[ip].step(scope, stack);
        switch (result) {
          case NEXT:
            ip++;
            break;
          case RETURN:
            throw new TypeError("RETURN from inside a generator");
          case YIELD:
            ip++;
            return stack.pop();
          default:
            ip = result;
        }
      }
      return null;
    }
    @Override public Value next() {
      return resume(nil);
    }
    @Override public Value iterator() {
      return this;
    }
    @Override public Scope getProto() {
      return generatorObjectProto;
    }
  }
  public interface Implementation {
    Value call(Value owner, Value... args);
  }
  public static final class Builtin extends BaseUserFunction {
    private final Symbol name;
    private final Implementation implementation;
    public Builtin(String name, Parameters params, Implementation i) {
      super(params, null, null);
      this.name = Symbol.of(name);
      this.implementation = i;
    }
    @Override public Value call(Value owner, Value... args) {
      parameters.checkOnly(owner, args);
      return implementation.call(owner, args);
    }
    @Override public String repr() {
      return "<builtin " + name +">";
    }
  }
  public static final class Parameters {
    static Parameters of(String... parts) {
      List<Symbol> names = new ArrayList<>();
      List<Symbol> opts = new ArrayList<>();
      Symbol var = null;
      for (String part: parts) {
        if (part.startsWith("/")) {
          opts.add(Symbol.of(part.substring(1)));
        } else if (part.startsWith("*")) {
          var = Symbol.of(part.substring(1));
        } else {
          names.add(Symbol.of(part));
        }
      }
      return new Parameters(
        names.toArray(new Symbol[0]), opts.toArray(new Symbol[0]), null, var);
    }
    private final Symbol[] names;
    private final Symbol[] opts;
    private final Value[] defs;  // assert(defs.length == opts.length)
    private final Symbol var;
    public Parameters(Symbol[] names, Symbol[] opts, Value[] defs, Symbol var) {
      this.names = names == null ? new Symbol[0] : names;
      this.opts = opts == null ? new Symbol[0] : opts;
      this.defs = defs == null ? new Value[opts.length] : defs;
      this.var = var;
      if (this.opts.length != this.defs.length) {
        throw new InternalError(this.opts.length + ", " + this.defs.length);
      }
      for (int i = 0; i < this.defs.length; i++) {
        if (this.defs[i] == null) {
          this.defs[i] = nil;
        }
      }
    }
    public void checkOnly(Value owner, Value... args) {
      if (args.length < names.length) {
        throw new TypeError(
          "Expected at least " + names.length + " args but got " +
          args.length);
      }
      if (var == null) {
        if (names.length + opts.length < args.length) {
          throw new TypeError(
            "Expected at most " + (names.length + opts.length) +
            " args but got " + args.length);
        }
      }
    }
    public void bind(Scope scope, Value owner, Value... args) {
      scope.put(thisSymbol, owner);
      if (args.length < names.length) {
        throw new TypeError(
          "Expected at least " + names.length + " args but got " +
          args.length);
      }
      int ap = 0;
      for (int i = 0; i < names.length; i++, ap++) {
        scope.put(names[i], args[ap]);
      }
      int op = 0;
      for (; op < opts.length && ap < args.length; op++, ap++) {
        scope.put(opts[op], args[ap]);
      }
      for (; op < opts.length; op++) {
        scope.put(opts[op], defs[op]);
      }
      if (var == null) {
        if (names.length + opts.length < args.length) {
          throw new TypeError(
            "Expected at most " + names.length + opts.length +
            " args but got " + args.length);
        }
      } else {
        ArrayList<Value> rest = new ArrayList<>();
        for (int i = names.length + opts.length; i < args.length; i++) {
          rest.add(args[i]);
        }
        scope.put(var, new Arr(rest));
      }
    }
  }
  public static Scope makeClass(String name, Iterable<Scope> bases, Scope proto) {
    HashSet<Value> ancestors = new HashSet<>();
    for (Scope base: bases) {
      Scope baseProto = base.mustGetAttribute(__protoSymbol).mustCast(Scope.class);
      proto.updateIfMissing(baseProto);

      ancestors.addAll(
        base.mustGetAttribute(__ancestorsSymbol).mustCast(XSet.class).value);
    }

    Scope klass = new Scope(classProto);
    ancestors.add(klass);
    klass.setAttribute(__nameSymbol, Str.of(name));
    klass.setAttribute(__protoSymbol, proto);
    klass.setAttribute(__basesSymbol, new Arr(toList(bases)));
    klass.setAttribute(__ancestorsSymbol, new XSet(ancestors));
    proto.setAttribute(__classSymbol, klass);
    return klass;
  }
  public static Scope makeClass(String name, Scope proto) {
    return makeClass(name, listOf(objectClass), proto);
  }
  public static Scope makeNativeClass(Class<?> c, String name, Iterable<Scope> bases, Scope proto) {
    Scope userClass = makeClass(name, bases, proto);
    registerNativeClass(c, proto);
    return userClass;
  }
  public static Scope makeNativeClass(Class<?> cls, String name, Scope proto) {
    return makeNativeClass(cls, name, listOf(nativeClass), proto);
  }
  public static Scope makeNativeClass(Class<?> cls, Scope proto) {
    return makeNativeClass(cls, cls.getSimpleName(), proto);
  }
  public static Scope makeNativeClass(Class<?> cls, String name) {
    return makeNativeClass(cls, name, new Scope(null));
  }
  public static Scope makeNativeClass(Class<?> cls, Iterable<Scope> bases) {
    return makeNativeClass(cls, cls.getSimpleName(), bases, new Scope(null));
  }
  public static Scope makeNativeClass(Class<?> cls) {
    return makeNativeClass(cls, cls.getSimpleName());
  }
  public static Scope makeSingleton(String name) {
    Scope singleton = new Scope(singletonProto);
    singleton.setAttribute(__nameSymbol, Str.of(name));
    return singleton;
  }
  public static void expectType(Value value, Scope expected) {
    Scope actual = value.getAttribute(__classSymbol).mustCast(Scope.class);
    if (actual == null) {
      actual = objectClass;
    }
    if (!isBaseClass(expected, actual)) {
      throw new TypeError("Expected " + expected + " but got " + actual);
    }
  }
  public static boolean isBaseClass(Scope base, Scope derived) {
    HashSet<Value> ancestors =
      derived.mustGetAttribute(__ancestorsSymbol).mustCast(XSet.class).value;
    return ancestors.contains(base);
  }

  public static final class BufferedIterator {
    private final Value iterator;
    private Value next;

    public BufferedIterator(Value iterator) {
      this.iterator = iterator;
      this.next = iterator.next();
    }

    public boolean hasNext() {
      return next != null;
    }

    public Value next() {
      if (next == null) {
        throw new MiscError("next called on finished iterator");
      }
      Value ret = next;
      next = iterator.next();
      return ret;
    }

    @Override public String toString() {
      return "<BufferedIterator>";
    }
  }

  //// Lexer
  public static final Set<String> keywords = new HashSet<>(Arrays.asList(
    "def",
    "nil", "true", "false",
    "as",
    "and", "or", "not", "is",
    // "this", "switch",

    // Keywords from Javascript
    "break", "case", "catch", "class", "const", "continue", "debugger",
    "default", "delete", "do", "else", "export", "extends", "finally",
    "for", "function", "if", "import", "in", "instanceof", "new",
    "return", "super", "throw", "try", "typeof", "var",
    "void", "while", "with", "yield",
    "enum",
    "implements", "interface", "let", "package", "private", "protected",
    "public", "static",
    "await"));
  public static final List<String> symbols = reversed(sorted(Arrays.asList(
    "\n",
    "=>", "->", "/", "//", "%%", "**",

    // ECMA 5 punctuators
    "{", "}", "(", ")", "[", "]",
    ".", ";", ",", "<", ">", "<=",
    ">=", "==", "!=", "===", "!==",
    "+", "-", "*", "%", "++", "--",
    "<<", ">>", ">>>", "&", "|", "^",
    "!", "~", "&&", "||", "?", ":",
    "=", "+=", "-=", "*=", "%=", "<<=",
    ">>=", ">>>=", "&=", "|=", "^=")));
  private static final class Lexer {
    private int i = 0;
    private final Source source;
    private final String s;
    public Lexer(Source source) {
      this.source = source;
      this.s = source.text;
    }
    private boolean eof() {
      return i >= s.length();
    }
    private char here() {
      return here(0);
    }
    private char here(int offset) {
      int j = i + offset;
      return j < s.length() ? s.charAt(j) : '\0';
    }
    private boolean startsWith(String prefix, int offset) {
      return s.startsWith(prefix, i + offset);
    }
    private boolean startsWith(String prefix) {
      return startsWith(prefix, 0);
    }
    private boolean consume(String prefix) {
      if (startsWith(prefix)) {
        i += prefix.length();
        return true;
      }
      return false;
    }
    private void skipWhitespace() {
      while (!eof() && here() != '\n' && Character.isWhitespace(here())) {
        i++;
      }
    }
    private Token makeToken(int start, String type, Object value) {
      return new Token(source, start, i, type, value);
    }
    private Token makeToken(int start, String type) {
      return makeToken(start, type, type);
    }
    private Token makeToken(String type, Object value) {
      return makeToken(i, type, value);
    }
    private Token makeToken(String type) {
      return makeToken(type, type);
    }
    private Token next() {
      skipWhitespace();
      if (eof()) {
        return null;
      }
      Token token = lexStringLiteral();
      if (token != null) {
        return token;
      }
      token = lexNumber();
      if (token != null) {
        return token;
      }
      token = lexNameOrKeyword();
      if (token != null) {
        return token;
      }
      token = lexSymbol();
      if (token != null) {
        return token;
      }
      withToken(makeToken("ERR"), () -> {
        throw new SyntaxError("Unrecognized token");
      });
      return null;  // unreachable
    }
    private Token lexStringLiteral() {
      int start = i;
      boolean raw = consume("r");
      String quote =
        consume("'''") ? "'''" :
        consume("\"\"\"") ? "\"\"\"" :
        consume("'") ? "'" :
        consume("\"") ? "\"" :
        null;
      if (quote == null) {
        i = start;
        return null;
      }
      while (!eof() && !startsWith(quote)) {
        if (!raw && here() == '\\') {
          i += 2;
        } else {
          i++;
        }
      }
      if (!consume(quote)) {
        withToken(makeToken(start, "ERR"), () -> {
          throw new SyntaxError("Unfinished string literal");
        });
      }
      String body = s.substring(
        start + (raw ? 1 : 0) + quote.length(),
        i - quote.length());
      String value = raw ? body : strunescape(body);
      return makeToken(start, "STR", value);
    }
    private Token lexNumber() {
      int start = i;
      if (!Character.isDigit(here()) &&
          !(here() == '-' && Character.isDigit(here(1))) &&
          !(here() == '-' && here(1) == '.' && Character.isDigit(here(2))) &&
          !(here() == '.' && Character.isDigit(here(1)))) {
        return null;
      }
      consume("-");
      while (Character.isDigit(here())) {
        i++;
      }
      if (consume(".")) {
        while (Character.isDigit(here())) {
          i++;
        }
      }
      String valueStr = s.substring(start, i);
      return makeToken(start, "NUM", Double.parseDouble(valueStr));
    }
    private static boolean isWordChar(int c) {
      return c == '_' || c == '$' ||
             Character.isDigit(c) || Character.isAlphabetic(c);
    }
    private Token lexNameOrKeyword() {
      int start = i;
      if (!isWordChar(here())) {
        return null;
      }
      while (isWordChar(here())) {
        i++;
      }
      String name = s.substring(start, i);
      return keywords.contains(name) ?
          makeToken(start, name) :
          makeToken(start, "ID", name);
    }
    private Token lexSymbol() {
      int start = i;
      if (consume("\n")) {
        return makeToken("NEWLINE");
      }
      for (String symbol: symbols) {
        if (consume(symbol)) {
          return makeToken(start, symbol);
        }
      }
      return null;
    }
    List<Token> lexAll() {
      List<Token> ret = new ArrayList<Token>();
      Token t = next();
      while (t != null) {
        ret.add(t);
        t = next();
      }
      ret.add(makeToken("EOF"));
      return ret;
    }
  }
  public static List<Token> lex(Source source) {
    return new Lexer(source).lexAll();
  }
  public static List<Token> lex(String text) {
    return lex(makeStringSource(text));
  }
  private static Map<Integer, Integer> getMatchingTokenMap(
      List<Token> tokens, boolean throwOnMismatch) {
    Map<Integer, Integer> ret = new HashMap<>();
    Stack<Integer> stack = new Stack<>();
    for (int i = 0; i < tokens.size(); i++) {
      String type = tokens.get(i).type;
      switch (type) {
        case "{":
        case "(":
        case "[":
          stack.push(i);
          break;
        case "]":
        case ")":
        case "}":
          if (stack.isEmpty()) {
            throw new SyntaxError(tokens.get(i), "Mismatched parentheses");
          }
          ret.put(stack.pop(), i);
          break;
      }
    }
    if (!stack.isEmpty()) {
      if (throwOnMismatch) {
        throw new SyntaxError(tokens.get(stack.pop()), "Mismatched grouping");
      } else {
        return null;
      }
    }
    return ret;
  }
  public static boolean isReadyForParse(String text) {
    return getMatchingTokenMap(lex(text), false) != null;
  }

  //// Parser
  private static abstract class BaseParser {
    int i = 0;
    final List<Token> tokens;
    private final Map<Integer, Integer> matchingTokenMap;
    private final Stack<Boolean> skippingNewlineStack =
      new Stack<>();
    BaseParser(List<Token> tokens) {
      this.tokens = tokens;
      this.matchingTokenMap = getMatchingTokenMap(tokens, true);
      skippingNewlineStack.push(false);
    }
    int getMatchingIndex(int i) {
      return matchingTokenMap.get(i);
    }
    <R> R skippingNewlineReturn(boolean skip, F0<R> f) {
      skippingNewlineStack.push(skip);
      try {
        return f.call();
      } finally {
        skippingNewlineStack.pop();
      }
    }
    void skippingNewline(boolean skip, Runnable f) {
      skippingNewlineReturn(skip, () -> {
        f.run();
        return null;
      });
    }
    Token peek() {
      if (skippingNewlineStack.peek()) {
        while (tokens.get(i).type.equals("NEWLINE")) {
          i++;
        }
      }
      return tokens.get(i);
    }
    Token next() {
      Token token = peek();
      i++;
      return token;
    }
    boolean at(String type) {
      return peek().type.equals(type);
    }
    boolean consume(String type) {
      if (at(type)) {
        next();
        return true;
      }
      return false;
    }
    Token expect(String type) {
      if (!at(type)) {
        throw new SyntaxError(peek(), "Expected " + type + " but got " + peek());
      }
      return next();
    }
    void consumeStatementDelimiters() {
      while (consume(";") || consume("NEWLINE"));
    }
    boolean atStatementDelimiter() {
      return at("EOF") || at(";") || at("NEWLINE");
    }
    void expectStatementDelimiter() {
      if (!at("EOF") && !consume(";")) {
        expect("NEWLINE");
      }
    }
  }
  private static final class Parser extends BaseParser {
    Parser(List<Token> tokens) {
      super(tokens);
    }
    Block parseAll() {
      Token token = peek();
      List<Statement> statements = new ArrayList<Statement>();
      consumeStatementDelimiters();
      while (!at("EOF")) {
        statements.add(parseStatement());
        consumeStatementDelimiters();
      }
      return new Block(token, statements);
    }
    If parseIf() {
      Token token = expect("if");
      expect("(");
      Expression condition = parseExpression();
      expect(")");
      Block body = parseBlock();
      Statement other =
        consume("else") ? at("if") ? parseIf() : parseBlock() : null;
      return new If(token, condition, body, other);
    }
    Block parseBlock() {
      Token token = expect("{");
      return skippingNewlineReturn(false, () -> {
        List<Statement> statements = new ArrayList<Statement>();
        consumeStatementDelimiters();
        while (!consume("}")) {
          statements.add(parseStatement());
          consumeStatementDelimiters();
        }
        return new Block(token, statements);
      });
    }
    Value parseConstant() {
      // TODO: Allow constant expressions that aren't just
      // pure literal expressions.
      Token token = peek();
      Expression expression = parseExpression();
      if (!(expression instanceof Literal)) {
        throw new SyntaxError(token, "Expected constant expression");
      }
      return ((Literal) expression).value;
    }
    Parameters parseParameters() {
      expect("(");
      List<Symbol> names = new ArrayList<>();
      List<Symbol> opts = new ArrayList<>();
      List<Value> defs = new ArrayList<>();
      Symbol var = null;
      boolean seenOptional = false;
      while (!consume(")")) {
        if (consume("*")) {
          var = Symbol.of((String) expect("ID").value);
          expect(")");
          break;
        }
        Symbol name = Symbol.of((String) expect("ID").value);
        if (consume("=")) {
          seenOptional = true;
          opts.add(name);
          defs.add(parseConstant());
        } else {
          if (seenOptional) {
            throw new SyntaxError(
              peek(),
              "Non-optional arguments cannot come after optional ones");
          }
          names.add(name);
        }
        if (!consume(",")) {
          expect(")");
          break;
        }
      }
      return new Parameters(
        names.toArray(new Symbol[0]),
        opts.toArray(new Symbol[0]),
        defs.toArray(new Value[0]),
        var);
    }
    BaseDef parseDef() {
      Token token = expect("def");
      boolean isGenerator = consume("*");
      Symbol name = Symbol.of((String) expect("ID").value);
      Parameters parameters = parseParameters();
      Block body = parseBlock();
      return isGenerator ?
        new DefStar(token, name, parameters, body) :
        new Def(token, name, parameters, body);
    }
    BlockExpression parseBlockExpression() {
      Token token = peek();
      return new BlockExpression(token, parseBlock().statements);
    }
    ClassDef parseClass() {
      Token token = expect("class");
      return parseClassContent(token);
    }
    ClassDef parseClassContent(Token token) {
      Symbol name = Symbol.of((String) expect("ID").value);
      List<Expression> bases = makeEmptyList();
      if (consume("(")) {
        while (!consume(")")) {
          bases.add(parseExpression());
          if (!consume(",")) {
            expect(")");
            break;
          }
        }
      } else {
        bases.add(new Literal(token, objectClass));
      }
      List<Pair<Symbol, Expression>> methods = makeEmptyList();
      expect("{");
      skippingNewline(false, () -> {
        consumeStatementDelimiters();
        while (!consume("}")) {
          BaseDef def = parseDef();
          methods.add(Pair.of(def.name, def));
          expectStatementDelimiter();
          consumeStatementDelimiters();
        }
      });
      return new ClassDef(token, name, bases, methods);
    }
    Statement parseStatement() {
      Token token = peek();
      if (consume("import")) {
        StringBuilder nameBuilder = new StringBuilder();
        String last = (String) expect("ID").value;
        nameBuilder.append(last);
        while (consume(".")) {
          nameBuilder.append(".");
          last = (String) expect("ID").value;
          nameBuilder.append(last);
        }
        String name = nameBuilder.toString();
        String alias = consume("as") ? (String) expect("ID").value : last;
        return new ImportStatement(token, name, alias);
      }
      if (at("if")) {
        return parseIf();
      }
      if (at("def")) {
        BaseDef def = parseDef();
        Symbol name = def.name;
        return new ExpressionStatement(
          token, new SetVariable(token, name, def));
      }
      if (at("class")) {
        ClassDef def = parseClass();
        Symbol name = def.name;
        return new ExpressionStatement(
          token, new SetVariable(token, name, def));
      }
      if (consume("new")) {
        ClassDef def = parseClassContent(token);
        Symbol name = def.name;
        return new ExpressionStatement(
          token,
          new SetVariable(
            token,
            name,
            new CallFunction(token, def, new ArrayList<>())));
      }
      if (consume("return")) {
        Expression expression = atStatementDelimiter() ?
          new Literal(token, nil) :
          parseExpression();
        expectStatementDelimiter();
        return new Return(token, expression);
      }
      if (consume("while")) {
        expect("(");
        Expression condition = parseExpression();
        expect(")");
        Block block = parseBlock();
        return new While(token, condition, block);
      }
      if (consume("for")) {
        Pattern pattern = parsePattern();
        expect("in");
        Expression container = parseExpression();
        Block body = parseBlock();
        return new For(token, pattern, container, body);
      }
      Expression expression = parseExpression();
      expectStatementDelimiter();
      return new ExpressionStatement(token, expression);
    }
    Expression parseExpression() {
      return parseTernary();
    }
    Pattern parsePattern() {
      Token token = peek();
      if (consume("[")) {
        ArrayList<Pattern> subpatterns = new ArrayList<>();
        Symbol varname = null;
        while (!consume("]")) {
          if (consume("*")) {
            varname = Symbol.of((String) expect("ID").value);
            expect("]");
            break;
          }
          subpatterns.add(parsePattern());
          if (!consume(",")) {
            expect("]");
            break;
          }
        }
        return new ListPattern(token, subpatterns, varname);
      }
      return new NamePattern(token, Symbol.of((String) expect("ID").value));
    }
    Expression parsePrimary() {
      Token token = peek();
      if (at("[")) {
        int save = i;
        i = getMatchingIndex(i);
        expect("]");
        if (at("=")) {
          i = save;
          Pattern pattern = parsePattern();
          expect("=");
          Expression expression = parseExpression();
          return new SetPattern(token, pattern, expression);
        } else {
          i = save;
          expect("[");
          List<Expression> expressions = new ArrayList<Expression>();
          skippingNewline(true, () -> {
            while (!consume("]")) {
              expressions.add(parseExpression());
              if (!consume(",")) {
                expect("]");
                break;
              }
            }
          });
          return functionCall(token, makeListFn, expressions);
        }
      }
      if (consume("%")) {
        return parseBlockExpression();
      }
      if (consume("%%")) {
        return new CallFunction(
          token,
          new Def(token, lambdaName, P(), parseBlockExpression()),
          new ArrayList<>());
      }
      if (at("(")) {
        int save = i;
        i = getMatchingIndex(i);
        expect(")");
        if (at("->")) {
          i = save;
          Parameters parameters = parseParameters();
          expect("->");
          Expression expression = parseExpression();
          return new Def(token, lambdaName, parameters, expression);
        } else {
          i = save;
          expect("(");
          return skippingNewlineReturn(true, () -> {
            Expression ret = parseExpression();
            expect(")");
            return ret;
          });
        }
      }
      if (at("def")) {
        return parseDef();
      }
      if (at("class")) {
        return parseClass();
      }
      if (consume("new")) {
        ClassDef def = parseClassContent(token);
        Symbol name = def.name;
        return new CallFunction(token, def, new ArrayList<>());
      }
      if (consume("{")) {
        return skippingNewlineReturn(true, () -> {
          if (consume(":")) {
            expect("}");
            return functionCall(token, makeMapFn);
          } else if (consume("}")) {
            return functionCall(token, makeSetFn);
          }
          List<Expression> expressions = new ArrayList<Expression>();
          expressions.add(parseExpression());
          if (consume("}")) {
            return functionCall(token, makeSetFn, expressions);
          }
          if (consume(":")) {
            expressions.add(parseExpression());
            while (true) {
              if (!consume(",")) {
                expect("}");
                break;
              }
              if (consume("}")) {
                break;
              }
              expressions.add(parseExpression());
              expect(":");
              expressions.add(parseExpression());
            }
            return functionCall(token, makeMapFn, expressions);
          }
          while (true) {
            if (!consume(",")) {
              expect("}");
              break;
            }
            if (consume("}")) {
              break;
            }
            expressions.add(parseExpression());
          }
          return functionCall(token, makeSetFn, expressions);
        });
      }
      if (consume("yield")) {
        Expression expression = parseExpression();
        return new Yield(token, expression);
      }
      if (consume("nil")) {
        return new Literal(token, nil);
      }
      if (consume("true")) {
        return new Literal(token, tru);
      }
      if (consume("false")) {
        return new Literal(token, fal);
      }
      if (consume("NUM")) {
        return new Literal(token, Number.of((Double) token.value));
      }
      if (consume("STR")) {
        return new Literal(token, Str.of((String) token.value));
      }
      if (consume("ID")) {
        String name = (String) token.value;
        if (consume("->")) {
          Expression expression = parseExpression();
          return new Def(token, lambdaName, P(name), expression);
        }
        if (consume("=")) {
          Expression expression = parseExpression();
          return new SetVariable(token, Symbol.of(name), expression);
        }
        return new GetVariable(token, Symbol.of(name));
      }
      throw new SyntaxError(token, "Expected expression but got " + token);
    }
    Expression parsePostfix() {
      Expression expression = parsePrimary();
      while (true) {
        Token token = peek();
        if (consume("(")) {
          Expression finalLastExpression = expression;
          expression = skippingNewlineReturn(true, () -> {
            List<Expression> args = new ArrayList<Expression>();
            while (!consume(")")) {
              args.add(parseExpression());
              if (!consume(",")) {
                expect(")");
                break;
              }
            }
            return new CallFunction(token, finalLastExpression, args);
          });
          continue;
        }
        if (consume(".")) {
          String name = (String) expect("ID").value;
          if (consume("=")) {
            Expression value = parseExpression();
            expression =
              new SetAttribute(token, expression, Symbol.of(name), value);
          } else {
            expression =
              new GetAttribute(token, expression, Symbol.of(name));
          }
          continue;
        }
        if (consume("[")) {
          List<Expression> args = skippingNewlineReturn(true, () -> {
            List<Expression> expressions = new ArrayList<>();
            expressions.add(parseExpression());
            while (consume(",")) {
              expressions.add(parseExpression());
            }
            expect("]");
            return expressions;
          });
          if (consume("=")) {
            args.add(parseExpression());
            expression = methodCall(token, expression, __setitemSymbol, args);
          } else {
            expression = methodCall(token, expression, __getitemSymbol, args);
          }
          continue;
        }
        break;
      }
      return expression;
    }
    Expression parsePrefix() {
      Token token = peek();
      if (consume("not")) {
        return new Not(token, parsePrefix());
      }
      return parsePostfix();
    }
    Expression parseExponentiation() {
      Expression e = parsePrefix();
      Token token = peek();
      if (consume("**")) {
        Expression arg = parseExponentiation();
        return methodCall(token, e, __powSymbol, arg);
      }
      return e;
    }
    static Expression methodCall(Token token, Expression owner, Symbol name, Expression... args) {
      return methodCall(token, owner, name, listOf(args));
    }
    static Expression methodCall(Token token, Expression owner, Symbol name, List<Expression> args) {
      return new CallFunction(token, new GetAttribute(token, owner, name), args);
    }
    static Expression functionCall(Token token, Value f, Expression... args) {
      return functionCall(token, f, listOf(args));
    }
    static Expression functionCall(Token token, Value f, List<Expression> args) {
      return new CallFunction(token, new Literal(token, f), args);
    }
    Expression parseProduct() {
      Expression ret = parseExponentiation();
      while (true) {
        Token token = peek();
        if (consume("*")) {
          Expression arg = parseExponentiation();
          ret = methodCall(token, ret, __mulSymbol, arg);
          continue;
        }
        if (consume("/")) {
          Expression arg = parseExponentiation();
          ret = methodCall(token, ret, __divSymbol, arg);
          continue;
        }
        if (consume("//")) {
          Expression arg = parseExponentiation();
          ret = methodCall(token, ret, __floordivSymbol, arg);
          continue;
        }
        if (consume("%")) {
          Expression arg = parseExponentiation();
          ret = methodCall(token, ret, __modSymbol, arg);
          continue;
        }
        break;
      }
      return ret;
    }
    Expression parseSum() {
      Expression ret = parseProduct();
      while (true) {
        Token token = peek();
        if (consume("+")) {
          Expression arg = parseProduct();
          ret = methodCall(token, ret, __addSymbol, arg);
          continue;
        }
        if (consume("-")) {
          Expression arg = parseProduct();
          ret = methodCall(token, ret, __subSymbol, arg);
          continue;
        }
        break;
      }
      return ret;
    }
    Expression parseInequality() {
      Expression ret = parseSum();
      Token token = peek();
      if (consume("<")) {
        Expression arg = parseSum();
        return functionCall(token, lessThanFn, ret, arg);
      }
      if (consume("<=")) {
        Expression arg = parseSum();
        return functionCall(token, lessThanOrEqualFn, ret, arg);
      }
      if (consume(">")) {
        Expression arg = parseSum();
        return functionCall(token, greaterThanFn, ret, arg);
      }
      if (consume(">=")) {
        Expression arg = parseSum();
        return functionCall(token, greaterThanOrEqualFn, ret, arg);
      }
      return ret;
    }
    Expression parseComparison() {
      Expression ret = parseInequality();
      Token token = peek();
      if (consume("==")) {
        Expression arg = parseInequality();
        return functionCall(token, equalsFn, ret, arg);
      }
      if (consume("!=")) {
        Expression arg = parseInequality();
        return functionCall(token, notEqualsFn, ret, arg);
      }
      if (consume("is")) {
        if (consume("not")) {
          Expression arg = parseInequality();
          return functionCall(token, isNotFn, ret, arg);
        }
        Expression arg = parseInequality();
        return functionCall(token, isFn, ret, arg);
      }
      return ret;
    }
    Expression parseAnd() {
      Expression ret = parseComparison();
      while (true) {
        Token token = peek();
        if (consume("and")) {
          Expression arg = parseComparison();
          ret = new And(token, ret, arg);
          continue;
        }
        break;
      }
      return ret;
    }
    Expression parseOr() {
      Expression ret = parseAnd();
      while (true) {
        Token token = peek();
        if (consume("or")) {
          Expression arg = parseAnd();
          ret = new Or(token, ret, arg);
          continue;
        }
        break;
      }
      return ret;
    }
    Expression parseTernary() {
      Expression condition = parseOr();
      Token token = peek();
      if (consume("?")) {
        Expression left = parseExpression();
        expect(":");
        Expression right = parseTernary();
        return new Ternary(token, condition, left, right);
      }
      return condition;
    }
  }
  public static Block parse(Source source) {
    return new Parser(lex(source)).parseAll();
  }
  public static Block parse(String text) {
    return parse(makeStringSource(text));
  }

  //// Importer
  public interface SourceLoader {
    Optional<Source> load(String name);
  }
  public final class EmptySourceLoader implements SourceLoader {
    @Override public Optional<Source> load(String name) {
      return Optional.empty();
    }
  }
  public interface NativeModule {
    Scope init();
  }
  private static final class Importer {
    private Scope globals = BigTalkCore.globals;
    private final List<SourceLoader> sourceLoaders = new ArrayList<>();
    private final Map<String, NativeModule> nativeModules =
      new HashMap<>();
    private final Map<String, Scope> loadedModules =
      new HashMap<>();
    public Scope getGlobals() {
      return globals;
    }
    public Importer setGlobals(Scope newGlobals) {
      if (globals != BigTalkCore.globals) {
        throw new TypeError("globals for the importer can only be set once");
      }
      globals = newGlobals;
      return this;
    }
    public Importer put(String name, NativeModule loader) {
      nativeModules.put(name, loader);
      return this;
    }
    public Importer addSourceLoader(SourceLoader loader) {
      sourceLoaders.add(loader);
      return this;
    }
    public Scope importModule(String name) {
      return loadedModules.computeIfAbsent(name, n ->
        Optional.ofNullable(nativeModules.get(n))
          .map(NativeModule::init)
          .orElseGet(() ->
            sourceLoaders.stream()
              .map(loader -> loader.load(n))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .findFirst()
              .map(source -> {
                Scope module = new Scope(globals);
                run(module, source);
                return module;
              })
              .orElseThrow(() ->
                new KeyError("Module " + n + " not found"))));
    }
  }

  //// Native types
  // For converting a native Java objects to 'Native' objects.
  private static Scope protoFor(Class<?> cls) {
    while (cls != null) {
      Scope proto = nativeProtoTable.get(cls);
      if (proto != null) {
        return proto;
      }
      cls = cls.getSuperclass();
    }
    return nativeProto;
  }
  public static void registerNativeClass(Class<?> cls, Scope proto) {
    if (nativeProtoTable.containsKey(cls)) {
      throw new KeyError("Native class " + cls.getName() + " already registered");
    }
    nativeProtoTable.put(cls, proto);
  }
  public static Native asNative(Object obj) {
    return new Native(obj, protoFor(obj.getClass()));
  }

  private static final class TokenStack implements Iterable<Token> {
    private final Token[] buffer;
    private int i = 0;
    public TokenStack(int limit) {
      buffer = new Token[limit];
    }
    public int size() {
      return i;
    }
    public void push(Token token) {
      buffer[i++] = token;
    }
    public Token pop() {
      Token ret = buffer[--i];
      buffer[i] = null;
      return ret;
    }
    public Token peek() {
      return buffer[i - 1];
    }
    public void with(Token token, Runnable runnable) {
      push(token);
      try {
        runnable.run();
      } finally {
        pop();
      }
    }
    @Override public Iterator<Token> iterator() {
      return new Iterator<Token>() {
        int i = 0;
        @Override public boolean hasNext() {
          return i < size();
        }
        @Override public Token next() {
          return buffer[i++];
        }
      };
    }
  }
  private static final class ValueStack implements Iterable<Value> {
    private Value[] buffer;
    private int i = 0;
    public ValueStack(int limit) {
      buffer = new Value[limit];
    }
    public int size() {
      return i;
    }
    public void push(Value value) {
      if (i >= buffer.length) {
        Value[] newBuffer = new Value[buffer.length * 2 + 5];
        for (int j = 0; j < buffer.length; j++) {
          newBuffer[j] = buffer[j];
        }
        buffer = newBuffer;
      }
      buffer[i++] = value;
    }
    public Value pop() {
      Value ret = buffer[--i];
      buffer[i] = null;
      return ret;
    }
    public Value peek() {
      return buffer[i - 1];
    }
    public Value[] pop(int n) {
      Value[] ret = new Value[n];
      for (int j = n - 1; j >= 0; j--) {
        i--;
        ret[j] = buffer[i];
        buffer[i] = null;
      }

      return ret;
    }
    @Override public Iterator<Value> iterator() {
      return new Iterator<Value>() {
        int i = 0;
        @Override public boolean hasNext() {
          return i < buffer.length;
        }
        @Override public Value next() {
          return buffer[i++];
        }
      };
    }
  }
  public static void withToken(Token token, Runnable runnable) {
    tokenStack.get().with(token, runnable);
  }

  public static void run(Scope scope, String text) {
    run(scope, makeStringSource(text));
  }
  public static void run(Scope scope, Source source) {
    Opcode[] opcodes = parse(source).compile();
    UserFunction.run(scope, valueStack.get(), opcodes);
  }
  public static Value runForRepl(Scope scope, String text) {
    return runForRepl(scope, makeStringSource(text));
  }
  public static Value runForRepl(Scope scope, Source source) {
    // Like 'run' except, if the source is a single expression statement,
    // return the result of the expression.
    // Otherwise return null.
    Block block = parse(source);
    boolean returnResult =
      block.statements.size() == 1 &&
      block.statements.get(0) instanceof ExpressionStatement;
    Opcode[] opcodes = block.compile();
    if (returnResult) {
      // Trim the last pop so that we get an extra element at the end.
      Opcode op = opcodes[opcodes.length - 1];
      if (!(op instanceof Pop)) {
        throw new InternalError("Invalid opcode assumption");
      }
      opcodes[opcodes.length - 1] = new Noop(op.token);
    }
    ValueStack vs = valueStack.get();
    UserFunction.run(scope, vs, opcodes);
    return returnResult ? vs.pop() : null;
  }

  //// Collection util
  @SuppressWarnings("unchecked")
  public static <T> ArrayList<T> listOf(T... args) {
    ArrayList<T> ret = new ArrayList<>();
    for (T t: args) {
      ret.add(t);
    }
    return ret;
  }
  public static <T> ArrayList<T> makeEmptyList() {
    return new ArrayList<T>();
  }
  public static <T> List<T> toList(Iterable<T> a, Iterable<T> b) {
    List<T> ret = new ArrayList<T>();
    for (T t: a) {
      ret.add(t);
    }
    for (T t: b) {
      ret.add(t);
    }
    return ret;
  }
  public static <R, T extends R> ArrayList<R> toList(Iterable<T> it) {
    ArrayList<R> ret = new ArrayList<R>();
    for (T t: it) {
      ret.add(t);
    }
    return ret;
  }
  public static <T> List<T> reversed(Iterable<T> list) {
    List<T> ret = new ArrayList<T>();
    for (T t: list) {
      ret.add(t);
    }
    Collections.reverse(ret);
    return ret;
  }
  public static <T extends Comparable<T>> ArrayList<T> sorted(Iterable<T> list) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T t: list) {
      ret.add(t);
    }
    Collections.sort(ret);
    return ret;
  }
  public static <T, R> ArrayList<R> map(Iterable<T> xs, F1<R, T> f) {
    ArrayList<R> ret = new ArrayList<R>();
    for (T t: xs) {
      ret.add(f.call(t));
    }
    return ret;
  }
  public static final class Pair<A, B> {
    public static <A, B> Pair<A, B> of(A a, B b) {
      return new Pair<A, B>(a, b);
    }
    public final A a;
    public final B b;
    public Pair(A a, B b) {
      this.a = a;
      this.b = b;
    }
    @Override public int hashCode() {
      return Objects.hash(a, b);
    }
    @Override public boolean equals(Object other) {
      return other instanceof Pair<?, ?> && equals((Pair<?, ?>) other);
    }
    public boolean equals(Pair<?, ?> other) {
      return Objects.equals(a, other.a) && Objects.equals(b, other.b);
    }
    @Override public String toString() {
      return "Pair(" + a + ", " + b + ")";
    }
  }

  public static final class Symbol {
    private static final Map<String, Symbol> symbolTable =
      new HashMap<String, Symbol>();
    public static Symbol of(String string) {
      return symbolTable.computeIfAbsent(string, Symbol::new);
    }

    private final String string;
    private Symbol(String string) {
      this.string = string;
    }
    @Override public String toString() {
      return string;
    }
  }

  //// Function util
  public interface F0<R> {
    R call();
  }
  public interface F1<R, A1> {
    R call(A1 a1);
  }

  //// String util
  public static int strfind(String s, String substr, int start) {
    while (start < s.length()) {
      if (s.startsWith(substr, start)) {
        return start;
      }
      start++;
    }
    return start;
  }
  public static int strfind(String s, String substr) {
    return strfind(s, substr, 0);
  }
  public static String strslice(String s, int start) {
    return strslice(s, start, s.length());
  }
  public static String strslice(String s, int start, int end) {
    return s.substring(start, Math.min(s.length(), end));
  }
  public static int strrfind(String s, String substr, int end) {
    int start = end - substr.length();
    while (start >= 0) {
      if (s.startsWith(substr, start)) {
        return start;
      }
      start--;
    }
    return start;
  }
  public static String strrep(String x, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(x);
    }
    return sb.toString();
  }
  public static int strcount(String s, String substr, int start, int end) {
    end = s.length() < end ? s.length() : end;
    int count = 0;
    for (int i = start; i < end; i++) {
      if (s.startsWith(substr, i)) {
        count++;
      }
    }
    return count;
  }
  public static String strjoin(String sep, Iterable<?> parts) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Object obj: parts) {
      if (!first) {
        sb.append(sep);
      }
      sb.append(obj.toString());
      first = false;
    }
    return sb.toString();
  }
  public static String strunescape(String escapedString) {
    return escapedString
      .replace("\\n", "\n")
      .replace("\\t", "\t")
      .replace("\\\"", "\"")
      .replace("\\\'", "\'")
      .replace("\\\\", "\\");
  }
  public static String strescape(String str) {
    return str
      .replace("\\", "\\\\")
      .replace("\n", "\\n")
      .replace("\t", "\\t")
      .replace("\"", "\\\"")
      .replace("\'", "\\\'");
  }
}
