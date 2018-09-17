package io.josephkim;

import static io.josephkim.BigTalkCore.*;
import static io.josephkim.BigTalkCore.Error;
import static io.josephkim.BigTalkCore.InternalError;
import static io.josephkim.BigTalkCore.Number;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

// Swing bindings for bigtalk.
public final class BigTalkSwing {
  static final Scope componentClass =
    makeNativeClass(
      Component.class,
      "Component",
      new Scope(null)
        .put(new Builtin("getSize", P(), (self, args) -> {
          Dimension dim = self.mustGetNative(Component.class).getSize();
          return Arr.of(Number.of(dim.width), Number.of(dim.height));
        }))
        .put(new Builtin("addKeyListener", P("listener"), (self, args) -> {
          Value listener = args[0];
          Value keyPressed = listener.mustGetAttribute(Symbol.of("keyPressed"));
          Value keyReleased = listener.mustGetAttribute(Symbol.of("keyReleased"));
          Value keyTyped = listener.mustGetAttribute(Symbol.of("keyTyped"));
          self.mustGetNative(Component.class).addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {
              keyPressed.call(listener, asNative(e));
            }
            @Override public void keyReleased(KeyEvent e) {
              keyReleased.call(listener, asNative(e));
            }
            @Override public void keyTyped(KeyEvent e) {
              keyTyped.call(listener, asNative(e));
            }
          });
          return nil;
        }))
        .put(new Builtin("addMouseListener", P("listener"), (self, args) -> {
          Value listener = args[0];
          Value mouseClicked = listener.mustGetAttribute(Symbol.of("mouseClicked"));
          Value mousePressed = listener.mustGetAttribute(Symbol.of("mousePressed"));
          Value mouseReleased = listener.mustGetAttribute(Symbol.of("mouseReleased"));
          Value mouseEntered = listener.mustGetAttribute(Symbol.of("mouseEntered"));
          Value mouseExited = listener.mustGetAttribute(Symbol.of("mouseExited"));
          self.mustGetNative(Component.class).addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
              mouseClicked.call(listener, asNative(e));
            }
            @Override public void mousePressed(MouseEvent e) {
              mousePressed.call(listener, asNative(e));
            }
            @Override public void mouseReleased(MouseEvent e) {
              mouseReleased.call(listener, asNative(e));
            }
            @Override public void mouseEntered(MouseEvent e) {
              mouseEntered.call(listener, asNative(e));
            }
            @Override public void mouseExited(MouseEvent e) {
              mouseExited.call(listener, asNative(e));
            }
          });
          return nil;
        }))
        .put(new Builtin("addMouseWheelListener", P("listener"), (self, args) -> {
          Value listener = args[0];
          self.mustGetNative(Component.class).addMouseWheelListener(event ->
            listener.call(null, asNative(event)));
          return nil;
        }))
        .put(new Builtin("repaint", P(), (self, args) -> {
          self.mustGetNative(Component.class).repaint();
          return nil;
        }))
        .put(new Builtin("setSize", P("width", "height"), (self, args) -> {
          self.mustGetNative(Component.class)
            .setSize(
              (int) args[0].mustCast(Number.class).get(),
              (int) args[1].mustCast(Number.class).get());
          return nil;
        }))
        .put(new Builtin("setBounds", P("x", "y", "width", "height"), (self, args) -> {
          self.mustGetNative(Component.class)
            .setBounds(
              (int) args[0].mustCast(Number.class).get(),
              (int) args[1].mustCast(Number.class).get(),
              (int) args[2].mustCast(Number.class).get(),
              (int) args[3].mustCast(Number.class).get());
          return nil;
        })));
  static final Scope containerClass =
    makeNativeClass(
      Container.class,
      "Container",
      listOf(componentClass),
      new Scope(null)
        .put(new Builtin("add", P("component"), (self, args) -> {
          self.mustGetNative(Container.class)
            .add(args[0].mustGetNative(Component.class));
          return nil;
        })));
  static final Scope jcomponentClass =
    makeNativeClass(
      JComponent.class,
      "JComponent",
      listOf(containerClass),
      new Scope(null)
        .put(new Builtin("setPreferredSize", P("width", "height"), (self, args) -> {
          self.mustGetNative(JComponent.class).setPreferredSize(new Dimension(
            (int) args[0].mustCast(Number.class).get(),
            (int) args[1].mustCast(Number.class).get()));
          return nil;
        }))
        .put(new Builtin("setBackground", P("color"), (self, args) -> {
          self.mustGetNative(JComponent.class).setBackground(args[0].mustGetNative(Color.class));
          return nil;
        })));
  static final Scope frameClass =
    makeNativeClass(
      JFrame.class,
      "Frame",
      listOf(jcomponentClass),
      new Scope(null)
        .put(new Builtin("pack", P(), (self, args) -> {
          self.mustGetNative(JFrame.class).pack();
          return nil;
        }))
        .put(new Builtin("isResizable", P(), (self, args) -> {
          return self.mustGetNative(JFrame.class).isResizable() ? tru : fal;
        }))
        .put(new Builtin("setResizable", P("resizable"), (self, args) -> {
          self.mustGetNative(JFrame.class).setResizable(args[0].truthy());
          return nil;
        }))
        .put(new Builtin("setVisible", P("visible"), (self, args) -> {
          self.mustGetNative(JFrame.class).setVisible(args[0].truthy());
          return nil;
        }))
        .put(new Builtin("setTitle", P("title"), (self, args) -> {
          self.mustGetNative(JFrame.class)
            .setTitle(args[0].mustCast(Str.class).get());
          return nil;
        }))
        .put(new Builtin("getContentPane", P(), (self, args) -> {
          return asNative(self.mustGetNative(JFrame.class).getContentPane());
        })))
    .put(new Builtin("__call", P(), (self, args) -> {
      JFrame frame = new JFrame();
      frame.setLocationRelativeTo(null);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      return asNative(frame);
    }));
  static final Scope panelClass =
    makeNativeClass(
      JPanel.class,
      "Panel",
      listOf(jcomponentClass),
      new Scope(null))
    .put(new Builtin("__call", P(), (self, args) -> asNative(new JPanel())));
  static final Scope buttonClass =
    makeNativeClass(
      JButton.class,
      "Button",
      listOf(jcomponentClass),
      new Scope(null)
        .put(new Builtin("addActionListener", P("listener"), (self, args) -> {
          Value listener = args[0];
          self.mustGetNative(JButton.class).addActionListener(e ->
            listener.call(null, asNative(e)));
          return nil;
        })))
    .put(new Builtin("__call", P("text"), (self, args) ->
      asNative(new JButton(args[0].toString()))));
  static final Scope actionEventClass =
    makeNativeClass(
      ActionEvent.class,
      "ActionEvent",
      new Scope(null));
  static final Scope inputEventClass =
    makeNativeClass(
      InputEvent.class,
      "InputEvent",
      new Scope(null)
        .put(new Builtin("isAltDown", P(), (self, args) ->
          self.mustGetNative(InputEvent.class).isAltDown() ? tru : fal))
        .put(new Builtin("isControlDown", P(), (self, args) ->
          self.mustGetNative(InputEvent.class).isControlDown() ? tru : fal))
        .put(new Builtin("isMetaDown", P(), (self, args) ->
          self.mustGetNative(InputEvent.class).isMetaDown() ? tru : fal))
        .put(new Builtin("isShiftDown", P(), (self, args) ->
          self.mustGetNative(InputEvent.class).isShiftDown() ? tru : fal))
        .put(new Builtin("getModifiersExText", P(), (self, args) ->
          Str.of(InputEvent.getModifiersExText(self.mustGetNative(InputEvent.class).getModifiersEx())))));
  static final Scope keyEventClass =
    makeNativeClass(
      KeyEvent.class,
      "KeyEvent",
      listOf(inputEventClass),
      new Scope(null)
        .put(new Builtin("getKeyChar", P(), (self, args) -> {
          KeyEvent event = self.mustGetNative(KeyEvent.class);
          Character c = event.getKeyChar();
          return c == KeyEvent.CHAR_UNDEFINED ? nil : Str.of(c.toString());
        }))
        .put(new Builtin("getKeyCode", P(), (self, args) ->
          Number.of(self.mustGetNative(KeyEvent.class).getKeyCode()))));
  static final Scope mouseEventClass =
    makeNativeClass(
      MouseEvent.class,
      "MouseEvent",
      listOf(inputEventClass),
      new Scope(null)
        .put(new Builtin("__repr", P(), (self, args) -> {
          MouseEvent event = self.mustGetNative(MouseEvent.class);
          Point point = event.getPoint();
          return Str.of(
            "MouseEvent(BUTTON" + event.getButton() +
            ", (" + point.x + "," + point.y + "), " +
            MouseEvent.getModifiersExText(event.getModifiersEx()) + ")");
        }))
        .put(new Builtin("getButton", P(), (self, args) -> {
          int button = self.mustGetNative(MouseEvent.class).getButton();
          return Number.of(button);
        }))
        .put(new Builtin("getPoint", P(), (self, args) -> {
          Point point = self.mustGetNative(MouseEvent.class).getPoint();
          return Arr.of(Number.of(point.x), Number.of(point.y));
        })));
  static final Scope mouseWheelEventClass =
    makeNativeClass(MouseWheelEvent.class, listOf(inputEventClass));
  static final Scope canvasClass =
    makeNativeClass(Canvas.class, listOf(panelClass))
    .put(new Builtin("__call", P("callback"), (self, args) ->
      asNative(new Canvas(args[0]))));
  static final Scope graphicsClass =
    makeNativeClass(
      Graphics.class,
      new Scope(null)
        .put(new Builtin("create", P("x", "y", "width", "height"), (self, args) -> {
          return asNative(self.mustGetNative(Graphics.class).create(
            (int) args[0].mustCast(Number.class).get(),
            (int) args[1].mustCast(Number.class).get(),
            (int) args[2].mustCast(Number.class).get(),
            (int) args[3].mustCast(Number.class).get()));
        }))
        .put(new Builtin("dispose", P(), (self, args) -> {
          self.mustGetNative(Graphics.class).dispose();
          return nil;
        }))
        .put(new Builtin("getFontMetrics", P("font"), (self, args) -> {
          Graphics g = self.mustGetNative(Graphics.class);
          Font font = args[0].mustGetNative(Font.class);
          return asNative(g.getFontMetrics(font));
        }))
        .put(new Builtin("setClip", P("x", "y", "width", "height"), (self, args) -> {
          self.mustGetNative(Graphics.class).setClip(
            (int) args[0].mustCast(Number.class).get(),
            (int) args[1].mustCast(Number.class).get(),
            (int) args[2].mustCast(Number.class).get(),
            (int) args[3].mustCast(Number.class).get());
          return nil;
        }))
        .put(new Builtin("getClipBounds", P(), (self, args) -> {
          Rectangle rect = self.mustGetNative(Graphics.class).getClipBounds();
          if (rect == null) {
            return nil;
          }
          return Arr.of(
            Number.of(rect.x),
            Number.of(rect.y),
            Number.of(rect.width),
            Number.of(rect.height));
        }))
        .put(new Builtin("setColor", P("color"), (self, args) -> {
          self.mustGetNative(Graphics.class).setColor(args[0].mustGetNative(Color.class));
          return nil;
        }))
        .put(new Builtin("fillRect", P("x", "y", "width", "height"), (self, args) -> {
          self.mustGetNative(Graphics.class).fillRect(
            (int) args[0].mustCast(Number.class).get(),
            (int) args[1].mustCast(Number.class).get(),
            (int) args[2].mustCast(Number.class).get(),
            (int) args[3].mustCast(Number.class).get());
          return nil;
        }))
        .put(new Builtin("drawRect", P("x", "y", "width", "height"), (self, args) -> {
          self.mustGetNative(Graphics.class).drawRect(
            (int) args[0].mustCast(Number.class).get(),
            (int) args[1].mustCast(Number.class).get(),
            (int) args[2].mustCast(Number.class).get(),
            (int) args[3].mustCast(Number.class).get());
          return nil;
        }))
        .put(new Builtin("setFont", P("font"), (self, args) -> {
          Graphics g = self.mustGetNative(Graphics.class);
          g.setFont(args[0].mustGetNative(Font.class));
          return nil;
        }))
        .put(new Builtin("drawString", P("str", "x", "y"), (self, args) -> {
          self.mustGetNative(Graphics.class).drawString(
            args[0].mustCast(Str.class).get(),
            (int) args[1].mustCast(Number.class).get(),
            (int) args[2].mustCast(Number.class).get());
          return nil;
        })));
  static final Scope graphics2DClass =
    makeNativeClass(Graphics2D.class, listOf(graphicsClass));
  static final Scope fontClass =
    makeNativeClass(Font.class)
    .put(new Builtin("__call", P("name", "style", "size"), (self, args) ->
      asNative(new Font(
        args[0].mustCast(Str.class).get(),
        (int) args[1].mustCast(Number.class).get(),
        (int) args[2].mustCast(Number.class).get()))));
  static final Scope fontMetricsClass =
    makeNativeClass(
      FontMetrics.class,
      new Scope(null)
        .put(new Builtin("stringWidth", P("str"), (self, args) -> {
          FontMetrics metrics = self.mustGetNative(FontMetrics.class);
          String s = args[0].mustCast(Str.class).get();
          return Number.of(metrics.stringWidth(s));
        }))
        .put(new Builtin("getHeight", P(), (self, args) -> {
          return Number.of(self.mustGetNative(FontMetrics.class).getHeight());
        })));


  public static void init() {
    addNativeModule("gui.swing", () -> new Scope(null)
      .put("Frame", frameClass)
      .put("Panel", panelClass)
      .put("Button", buttonClass)
      .put("Canvas", canvasClass)
      .put("Font", fontClass)
      .put("MONOSPACED", Str.of(Font.MONOSPACED))
      .put("SERIF", Str.of(Font.SERIF))
      .put("SANS_SERIF", Str.of(Font.SANS_SERIF))
      .put(new Builtin("Color", P("r", "g", "b", "/a"), (self, args) -> {
        double r, g, b, alpha = 1.0;
        r = args[0].mustCast(Number.class).get();
        g = args[1].mustCast(Number.class).get();
        b = args[2].mustCast(Number.class).get();
        if (args.length == 4) {
          alpha = args[3].mustCast(Number.class).get();
        }
        return asNative(new Color((float) r, (float) g, (float) b, (float) alpha));
      }))
      .put("black", asNative(Color.black))
      .put("blue", asNative(Color.blue))
      .put("cyan", asNative(Color.cyan))
      .put("darkGray", asNative(Color.darkGray))
      .put("gray", asNative(Color.gray))
      .put("green", asNative(Color.green))
      .put("lightGray", asNative(Color.lightGray))
      .put("magenta", asNative(Color.magenta))
      .put("orange", asNative(Color.orange))
      .put("pink", asNative(Color.pink))
      .put("red", asNative(Color.red))
      .put("white", asNative(Color.white))
      .put("yellow", asNative(Color.yellow))
      .put("VK_0", Number.of(KeyEvent.VK_0))
      .put("VK_1", Number.of(KeyEvent.VK_1))
      .put("VK_2", Number.of(KeyEvent.VK_2))
      .put("VK_3", Number.of(KeyEvent.VK_3))
      .put("VK_4", Number.of(KeyEvent.VK_4))
      .put("VK_5", Number.of(KeyEvent.VK_5))
      .put("VK_6", Number.of(KeyEvent.VK_6))
      .put("VK_7", Number.of(KeyEvent.VK_7))
      .put("VK_8", Number.of(KeyEvent.VK_8))
      .put("VK_9", Number.of(KeyEvent.VK_9))
      .put("VK_A", Number.of(KeyEvent.VK_A))
      .put("VK_ACCEPT", Number.of(KeyEvent.VK_ACCEPT))
      .put("VK_ADD", Number.of(KeyEvent.VK_ADD))
      .put("VK_AGAIN", Number.of(KeyEvent.VK_AGAIN))
      .put("VK_ALL_CANDIDATES", Number.of(KeyEvent.VK_ALL_CANDIDATES))
      .put("VK_ALPHANUMERIC", Number.of(KeyEvent.VK_ALPHANUMERIC))
      .put("VK_ALT", Number.of(KeyEvent.VK_ALT))
      .put("VK_ALT_GRAPH", Number.of(KeyEvent.VK_ALT_GRAPH))
      .put("VK_AMPERSAND", Number.of(KeyEvent.VK_AMPERSAND))
      .put("VK_ASTERISK", Number.of(KeyEvent.VK_ASTERISK))
      .put("VK_AT", Number.of(KeyEvent.VK_AT))
      .put("VK_B", Number.of(KeyEvent.VK_B))
      .put("VK_BACK_QUOTE", Number.of(KeyEvent.VK_BACK_QUOTE))
      .put("VK_BACK_SLASH", Number.of(KeyEvent.VK_BACK_SLASH))
      .put("VK_BACK_SPACE", Number.of(KeyEvent.VK_BACK_SPACE))
      .put("VK_BEGIN", Number.of(KeyEvent.VK_BEGIN))
      .put("VK_BRACELEFT", Number.of(KeyEvent.VK_BRACELEFT))
      .put("VK_BRACERIGHT", Number.of(KeyEvent.VK_BRACERIGHT))
      .put("VK_C", Number.of(KeyEvent.VK_C))
      .put("VK_CANCEL", Number.of(KeyEvent.VK_CANCEL))
      .put("VK_CAPS_LOCK", Number.of(KeyEvent.VK_CAPS_LOCK))
      .put("VK_CIRCUMFLEX", Number.of(KeyEvent.VK_CIRCUMFLEX))
      .put("VK_CLEAR", Number.of(KeyEvent.VK_CLEAR))
      .put("VK_CLOSE_BRACKET", Number.of(KeyEvent.VK_CLOSE_BRACKET))
      .put("VK_CODE_INPUT", Number.of(KeyEvent.VK_CODE_INPUT))
      .put("VK_COLON", Number.of(KeyEvent.VK_COLON))
      .put("VK_COMMA", Number.of(KeyEvent.VK_COMMA))
      .put("VK_COMPOSE", Number.of(KeyEvent.VK_COMPOSE))
      .put("VK_CONTEXT_MENU", Number.of(KeyEvent.VK_CONTEXT_MENU))
      .put("VK_CONTROL", Number.of(KeyEvent.VK_CONTROL))
      .put("VK_CONVERT", Number.of(KeyEvent.VK_CONVERT))
      .put("VK_COPY", Number.of(KeyEvent.VK_COPY))
      .put("VK_CUT", Number.of(KeyEvent.VK_CUT))
      .put("VK_D", Number.of(KeyEvent.VK_D))
      .put("VK_DEAD_ABOVEDOT", Number.of(KeyEvent.VK_DEAD_ABOVEDOT))
      .put("VK_DEAD_ABOVERING", Number.of(KeyEvent.VK_DEAD_ABOVERING))
      .put("VK_DEAD_ACUTE", Number.of(KeyEvent.VK_DEAD_ACUTE))
      .put("VK_DEAD_BREVE", Number.of(KeyEvent.VK_DEAD_BREVE))
      .put("VK_DEAD_CARON", Number.of(KeyEvent.VK_DEAD_CARON))
      .put("VK_DEAD_CEDILLA", Number.of(KeyEvent.VK_DEAD_CEDILLA))
      .put("VK_DEAD_CIRCUMFLEX", Number.of(KeyEvent.VK_DEAD_CIRCUMFLEX))
      .put("VK_DEAD_DIAERESIS", Number.of(KeyEvent.VK_DEAD_DIAERESIS))
      .put("VK_DEAD_DOUBLEACUTE", Number.of(KeyEvent.VK_DEAD_DOUBLEACUTE))
      .put("VK_DEAD_GRAVE", Number.of(KeyEvent.VK_DEAD_GRAVE))
      .put("VK_DEAD_IOTA", Number.of(KeyEvent.VK_DEAD_IOTA))
      .put("VK_DEAD_MACRON", Number.of(KeyEvent.VK_DEAD_MACRON))
      .put("VK_DEAD_OGONEK", Number.of(KeyEvent.VK_DEAD_OGONEK))
      .put("VK_DEAD_SEMIVOICED_SOUND", Number.of(KeyEvent.VK_DEAD_SEMIVOICED_SOUND))
      .put("VK_DEAD_TILDE", Number.of(KeyEvent.VK_DEAD_TILDE))
      .put("VK_DEAD_VOICED_SOUND", Number.of(KeyEvent.VK_DEAD_VOICED_SOUND))
      .put("VK_DECIMAL", Number.of(KeyEvent.VK_DECIMAL))
      .put("VK_DELETE", Number.of(KeyEvent.VK_DELETE))
      .put("VK_DIVIDE", Number.of(KeyEvent.VK_DIVIDE))
      .put("VK_DOLLAR", Number.of(KeyEvent.VK_DOLLAR))
      .put("VK_DOWN", Number.of(KeyEvent.VK_DOWN))
      .put("VK_E", Number.of(KeyEvent.VK_E))
      .put("VK_END", Number.of(KeyEvent.VK_END))
      .put("VK_ENTER", Number.of(KeyEvent.VK_ENTER))
      .put("VK_EQUALS", Number.of(KeyEvent.VK_EQUALS))
      .put("VK_ESCAPE", Number.of(KeyEvent.VK_ESCAPE))
      .put("VK_EURO_SIGN", Number.of(KeyEvent.VK_EURO_SIGN))
      .put("VK_EXCLAMATION_MARK", Number.of(KeyEvent.VK_EXCLAMATION_MARK))
      .put("VK_F", Number.of(KeyEvent.VK_F))
      .put("VK_F1", Number.of(KeyEvent.VK_F1))
      .put("VK_F10", Number.of(KeyEvent.VK_F10))
      .put("VK_F11", Number.of(KeyEvent.VK_F11))
      .put("VK_F12", Number.of(KeyEvent.VK_F12))
      .put("VK_F13", Number.of(KeyEvent.VK_F13))
      .put("VK_F14", Number.of(KeyEvent.VK_F14))
      .put("VK_F15", Number.of(KeyEvent.VK_F15))
      .put("VK_F16", Number.of(KeyEvent.VK_F16))
      .put("VK_F17", Number.of(KeyEvent.VK_F17))
      .put("VK_F18", Number.of(KeyEvent.VK_F18))
      .put("VK_F19", Number.of(KeyEvent.VK_F19))
      .put("VK_F2", Number.of(KeyEvent.VK_F2))
      .put("VK_F20", Number.of(KeyEvent.VK_F20))
      .put("VK_F21", Number.of(KeyEvent.VK_F21))
      .put("VK_F22", Number.of(KeyEvent.VK_F22))
      .put("VK_F23", Number.of(KeyEvent.VK_F23))
      .put("VK_F24", Number.of(KeyEvent.VK_F24))
      .put("VK_F3", Number.of(KeyEvent.VK_F3))
      .put("VK_F4", Number.of(KeyEvent.VK_F4))
      .put("VK_F5", Number.of(KeyEvent.VK_F5))
      .put("VK_F6", Number.of(KeyEvent.VK_F6))
      .put("VK_F7", Number.of(KeyEvent.VK_F7))
      .put("VK_F8", Number.of(KeyEvent.VK_F8))
      .put("VK_F9", Number.of(KeyEvent.VK_F9))
      .put("VK_FINAL", Number.of(KeyEvent.VK_FINAL))
      .put("VK_FIND", Number.of(KeyEvent.VK_FIND))
      .put("VK_FULL_WIDTH", Number.of(KeyEvent.VK_FULL_WIDTH))
      .put("VK_G", Number.of(KeyEvent.VK_G))
      .put("VK_GREATER", Number.of(KeyEvent.VK_GREATER))
      .put("VK_H", Number.of(KeyEvent.VK_H))
      .put("VK_HALF_WIDTH", Number.of(KeyEvent.VK_HALF_WIDTH))
      .put("VK_HELP", Number.of(KeyEvent.VK_HELP))
      .put("VK_HIRAGANA", Number.of(KeyEvent.VK_HIRAGANA))
      .put("VK_HOME", Number.of(KeyEvent.VK_HOME))
      .put("VK_I", Number.of(KeyEvent.VK_I))
      .put("VK_INPUT_METHOD_ON_OFF", Number.of(KeyEvent.VK_INPUT_METHOD_ON_OFF))
      .put("VK_INSERT", Number.of(KeyEvent.VK_INSERT))
      .put("VK_INVERTED_EXCLAMATION_MARK", Number.of(KeyEvent.VK_INVERTED_EXCLAMATION_MARK))
      .put("VK_J", Number.of(KeyEvent.VK_J))
      .put("VK_JAPANESE_HIRAGANA", Number.of(KeyEvent.VK_JAPANESE_HIRAGANA))
      .put("VK_JAPANESE_KATAKANA", Number.of(KeyEvent.VK_JAPANESE_KATAKANA))
      .put("VK_JAPANESE_ROMAN", Number.of(KeyEvent.VK_JAPANESE_ROMAN))
      .put("VK_K", Number.of(KeyEvent.VK_K))
      .put("VK_KANA", Number.of(KeyEvent.VK_KANA))
      .put("VK_KANA_LOCK", Number.of(KeyEvent.VK_KANA_LOCK))
      .put("VK_KANJI", Number.of(KeyEvent.VK_KANJI))
      .put("VK_KATAKANA", Number.of(KeyEvent.VK_KATAKANA))
      .put("VK_KP_DOWN", Number.of(KeyEvent.VK_KP_DOWN))
      .put("VK_KP_LEFT", Number.of(KeyEvent.VK_KP_LEFT))
      .put("VK_KP_RIGHT", Number.of(KeyEvent.VK_KP_RIGHT))
      .put("VK_KP_UP", Number.of(KeyEvent.VK_KP_UP))
      .put("VK_L", Number.of(KeyEvent.VK_L))
      .put("VK_LEFT", Number.of(KeyEvent.VK_LEFT))
      .put("VK_LEFT_PARENTHESIS", Number.of(KeyEvent.VK_LEFT_PARENTHESIS))
      .put("VK_LESS", Number.of(KeyEvent.VK_LESS))
      .put("VK_M", Number.of(KeyEvent.VK_M))
      .put("VK_META", Number.of(KeyEvent.VK_META))
      .put("VK_MINUS", Number.of(KeyEvent.VK_MINUS))
      .put("VK_MODECHANGE", Number.of(KeyEvent.VK_MODECHANGE))
      .put("VK_MULTIPLY", Number.of(KeyEvent.VK_MULTIPLY))
      .put("VK_N", Number.of(KeyEvent.VK_N))
      .put("VK_NONCONVERT", Number.of(KeyEvent.VK_NONCONVERT))
      .put("VK_NUM_LOCK", Number.of(KeyEvent.VK_NUM_LOCK))
      .put("VK_NUMBER_SIGN", Number.of(KeyEvent.VK_NUMBER_SIGN))
      .put("VK_NUMPAD0", Number.of(KeyEvent.VK_NUMPAD0))
      .put("VK_NUMPAD1", Number.of(KeyEvent.VK_NUMPAD1))
      .put("VK_NUMPAD2", Number.of(KeyEvent.VK_NUMPAD2))
      .put("VK_NUMPAD3", Number.of(KeyEvent.VK_NUMPAD3))
      .put("VK_NUMPAD4", Number.of(KeyEvent.VK_NUMPAD4))
      .put("VK_NUMPAD5", Number.of(KeyEvent.VK_NUMPAD5))
      .put("VK_NUMPAD6", Number.of(KeyEvent.VK_NUMPAD6))
      .put("VK_NUMPAD7", Number.of(KeyEvent.VK_NUMPAD7))
      .put("VK_NUMPAD8", Number.of(KeyEvent.VK_NUMPAD8))
      .put("VK_NUMPAD9", Number.of(KeyEvent.VK_NUMPAD9))
      .put("VK_O", Number.of(KeyEvent.VK_O))
      .put("VK_OPEN_BRACKET", Number.of(KeyEvent.VK_OPEN_BRACKET))
      .put("VK_P", Number.of(KeyEvent.VK_P))
      .put("VK_PAGE_DOWN", Number.of(KeyEvent.VK_PAGE_DOWN))
      .put("VK_PAGE_UP", Number.of(KeyEvent.VK_PAGE_UP))
      .put("VK_PASTE", Number.of(KeyEvent.VK_PASTE))
      .put("VK_PAUSE", Number.of(KeyEvent.VK_PAUSE))
      .put("VK_PERIOD", Number.of(KeyEvent.VK_PERIOD))
      .put("VK_PLUS", Number.of(KeyEvent.VK_PLUS))
      .put("VK_PREVIOUS_CANDIDATE", Number.of(KeyEvent.VK_PREVIOUS_CANDIDATE))
      .put("VK_PRINTSCREEN", Number.of(KeyEvent.VK_PRINTSCREEN))
      .put("VK_PROPS", Number.of(KeyEvent.VK_PROPS))
      .put("VK_Q", Number.of(KeyEvent.VK_Q))
      .put("VK_QUOTE", Number.of(KeyEvent.VK_QUOTE))
      .put("VK_QUOTEDBL", Number.of(KeyEvent.VK_QUOTEDBL))
      .put("VK_R", Number.of(KeyEvent.VK_R))
      .put("VK_RIGHT", Number.of(KeyEvent.VK_RIGHT))
      .put("VK_RIGHT_PARENTHESIS", Number.of(KeyEvent.VK_RIGHT_PARENTHESIS))
      .put("VK_ROMAN_CHARACTERS", Number.of(KeyEvent.VK_ROMAN_CHARACTERS))
      .put("VK_S", Number.of(KeyEvent.VK_S))
      .put("VK_SCROLL_LOCK", Number.of(KeyEvent.VK_SCROLL_LOCK))
      .put("VK_SEMICOLON", Number.of(KeyEvent.VK_SEMICOLON))
      .put("VK_SEPARATER", Number.of(KeyEvent.VK_SEPARATER))
      .put("VK_SEPARATOR", Number.of(KeyEvent.VK_SEPARATOR))
      .put("VK_SHIFT", Number.of(KeyEvent.VK_SHIFT))
      .put("VK_SLASH", Number.of(KeyEvent.VK_SLASH))
      .put("VK_SPACE", Number.of(KeyEvent.VK_SPACE))
      .put("VK_STOP", Number.of(KeyEvent.VK_STOP))
      .put("VK_SUBTRACT", Number.of(KeyEvent.VK_SUBTRACT))
      .put("VK_T", Number.of(KeyEvent.VK_T))
      .put("VK_TAB", Number.of(KeyEvent.VK_TAB))
      .put("VK_U", Number.of(KeyEvent.VK_U))
      .put("VK_UNDEFINED", Number.of(KeyEvent.VK_UNDEFINED))
      .put("VK_UNDERSCORE", Number.of(KeyEvent.VK_UNDERSCORE))
      .put("VK_UNDO", Number.of(KeyEvent.VK_UNDO))
      .put("VK_UP", Number.of(KeyEvent.VK_UP))
      .put("VK_V", Number.of(KeyEvent.VK_V))
      .put("VK_W", Number.of(KeyEvent.VK_W))
      .put("VK_WINDOWS", Number.of(KeyEvent.VK_WINDOWS))
      .put("VK_X", Number.of(KeyEvent.VK_X))
      .put("VK_Y", Number.of(KeyEvent.VK_Y))
      .put("VK_Z", Number.of(KeyEvent.VK_Z)));
  }

  public static final class Canvas extends JPanel {
    private static final long serialVersionUID = 0L;

    private final Value callback;
    public Canvas(Value callback) {
      this.callback = callback;
    }
    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      callback.call(null, asNative(g));
    }
  }
}
