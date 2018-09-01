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
        .put(new Builtin("getKeyCode", P(), (self, args) ->
          Number.of(self.mustGetNative(KeyEvent.class).getKeyCode())))
        .put(new Builtin("getKeyText", P(), (self, args) ->
          Str.of(KeyEvent.getKeyText(self.mustGetNative(KeyEvent.class).getKeyCode())))));
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
        .put(new Builtin("getClipBounds", P(), (self, args) -> {
          Rectangle rect = self.mustGetNative(Graphics.class).getClipBounds();
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
        .put(new Builtin("drawString", P("str", "x", "y"), (self, args) -> {
          self.mustGetNative(Graphics.class).drawString(
            args[0].mustCast(Str.class).get(),
            (int) args[1].mustCast(Number.class).get(),
            (int) args[2].mustCast(Number.class).get());
          return nil;
        })));
  static final Scope graphics2DClass =
    makeNativeClass(Graphics2D.class, listOf(graphicsClass));

  public static void init() {
    addNativeModule("gui.swing", () -> new Scope(null)
      .put("Frame", frameClass)
      .put("Panel", panelClass)
      .put("Button", buttonClass)
      .put("Canvas", canvasClass));
    addNativeModule("gui.swing.color", () -> new Scope(null)
      .put(new Builtin("of", P("r", "g", "b", "/a"), (self, args) -> {
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
      .put("yellow", asNative(Color.yellow)));
  }

  public static final class Canvas extends JPanel {
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
