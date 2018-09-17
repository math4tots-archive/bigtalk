"""Dead simple API for making simple graphical user interfaces

For a more fun gui api that's uses this module as a backend, see simple.gui

This implementation uses gui.swing as a backend
"""
import gui.swing

_keymap = {
  nil: nil,
  swing.VK_0: "0",
  swing.VK_1: "1",
  swing.VK_2: "2",
  swing.VK_3: "3",
  swing.VK_4: "4",
  swing.VK_5: "5",
  swing.VK_6: "6",
  swing.VK_7: "7",
  swing.VK_8: "8",
  swing.VK_9: "9",
  swing.VK_A: "A",
  swing.VK_ACCEPT: "ACCEPT",
  swing.VK_ADD: "ADD",
  swing.VK_AGAIN: "AGAIN",
  swing.VK_ALL_CANDIDATES: "ALL_CANDIDATES",
  swing.VK_ALPHANUMERIC: "ALPHANUMERIC",
  swing.VK_ALT: "ALT",
  swing.VK_ALT_GRAPH: "ALT_GRAPH",
  swing.VK_AMPERSAND: "AMPERSAND",
  swing.VK_ASTERISK: "ASTERISK",
  swing.VK_AT: "AT",
  swing.VK_B: "B",
  swing.VK_BACK_QUOTE: "BACK_QUOTE",
  swing.VK_BACK_SLASH: "BACK_SLASH",
  swing.VK_BACK_SPACE: "BACK_SPACE",
  swing.VK_BEGIN: "BEGIN",
  swing.VK_BRACELEFT: "BRACELEFT",
  swing.VK_BRACERIGHT: "BRACERIGHT",
  swing.VK_C: "C",
  swing.VK_CANCEL: "CANCEL",
  swing.VK_CAPS_LOCK: "CAPS_LOCK",
  swing.VK_CIRCUMFLEX: "CIRCUMFLEX",
  swing.VK_CLEAR: "CLEAR",
  swing.VK_CLOSE_BRACKET: "CLOSE_BRACKET",
  swing.VK_CODE_INPUT: "CODE_INPUT",
  swing.VK_COLON: "COLON",
  swing.VK_COMMA: "COMMA",
  swing.VK_COMPOSE: "COMPOSE",
  swing.VK_CONTEXT_MENU: "CONTEXT_MENU",
  swing.VK_CONTROL: "CONTROL",
  swing.VK_CONVERT: "CONVERT",
  swing.VK_COPY: "COPY",
  swing.VK_CUT: "CUT",
  swing.VK_D: "D",
  swing.VK_DEAD_ABOVEDOT: "DEAD_ABOVEDOT",
  swing.VK_DEAD_ABOVERING: "DEAD_ABOVERING",
  swing.VK_DEAD_ACUTE: "DEAD_ACUTE",
  swing.VK_DEAD_BREVE: "DEAD_BREVE",
  swing.VK_DEAD_CARON: "DEAD_CARON",
  swing.VK_DEAD_CEDILLA: "DEAD_CEDILLA",
  swing.VK_DEAD_CIRCUMFLEX: "DEAD_CIRCUMFLEX",
  swing.VK_DEAD_DIAERESIS: "DEAD_DIAERESIS",
  swing.VK_DEAD_DOUBLEACUTE: "DEAD_DOUBLEACUTE",
  swing.VK_DEAD_GRAVE: "DEAD_GRAVE",
  swing.VK_DEAD_IOTA: "DEAD_IOTA",
  swing.VK_DEAD_MACRON: "DEAD_MACRON",
  swing.VK_DEAD_OGONEK: "DEAD_OGONEK",
  swing.VK_DEAD_SEMIVOICED_SOUND: "DEAD_SEMIVOICED_SOUND",
  swing.VK_DEAD_TILDE: "DEAD_TILDE",
  swing.VK_DEAD_VOICED_SOUND: "DEAD_VOICED_SOUND",
  swing.VK_DECIMAL: "DECIMAL",
  swing.VK_DELETE: "DELETE",
  swing.VK_DIVIDE: "DIVIDE",
  swing.VK_DOLLAR: "DOLLAR",
  swing.VK_DOWN: "DOWN",
  swing.VK_E: "E",
  swing.VK_END: "END",
  swing.VK_ENTER: "ENTER",
  swing.VK_EQUALS: "EQUALS",
  swing.VK_ESCAPE: "ESCAPE",
  swing.VK_EURO_SIGN: "EURO_SIGN",
  swing.VK_EXCLAMATION_MARK: "EXCLAMATION_MARK",
  swing.VK_F: "F",
  swing.VK_F1: "F1",
  swing.VK_F10: "F10",
  swing.VK_F11: "F11",
  swing.VK_F12: "F12",
  swing.VK_F13: "F13",
  swing.VK_F14: "F14",
  swing.VK_F15: "F15",
  swing.VK_F16: "F16",
  swing.VK_F17: "F17",
  swing.VK_F18: "F18",
  swing.VK_F19: "F19",
  swing.VK_F2: "F2",
  swing.VK_F20: "F20",
  swing.VK_F21: "F21",
  swing.VK_F22: "F22",
  swing.VK_F23: "F23",
  swing.VK_F24: "F24",
  swing.VK_F3: "F3",
  swing.VK_F4: "F4",
  swing.VK_F5: "F5",
  swing.VK_F6: "F6",
  swing.VK_F7: "F7",
  swing.VK_F8: "F8",
  swing.VK_F9: "F9",
  swing.VK_FINAL: "FINAL",
  swing.VK_FIND: "FIND",
  swing.VK_FULL_WIDTH: "FULL_WIDTH",
  swing.VK_G: "G",
  swing.VK_GREATER: "GREATER",
  swing.VK_H: "H",
  swing.VK_HALF_WIDTH: "HALF_WIDTH",
  swing.VK_HELP: "HELP",
  swing.VK_HIRAGANA: "HIRAGANA",
  swing.VK_HOME: "HOME",
  swing.VK_I: "I",
  swing.VK_INPUT_METHOD_ON_OFF: "INPUT_METHOD_ON_OFF",
  swing.VK_INSERT: "INSERT",
  swing.VK_INVERTED_EXCLAMATION_MARK: "INVERTED_EXCLAMATION_MARK",
  swing.VK_J: "J",
  swing.VK_JAPANESE_HIRAGANA: "JAPANESE_HIRAGANA",
  swing.VK_JAPANESE_KATAKANA: "JAPANESE_KATAKANA",
  swing.VK_JAPANESE_ROMAN: "JAPANESE_ROMAN",
  swing.VK_K: "K",
  swing.VK_KANA: "KANA",
  swing.VK_KANA_LOCK: "KANA_LOCK",
  swing.VK_KANJI: "KANJI",
  swing.VK_KATAKANA: "KATAKANA",
  swing.VK_KP_DOWN: "KP_DOWN",
  swing.VK_KP_LEFT: "KP_LEFT",
  swing.VK_KP_RIGHT: "KP_RIGHT",
  swing.VK_KP_UP: "KP_UP",
  swing.VK_L: "L",
  swing.VK_LEFT: "LEFT",
  swing.VK_LEFT_PARENTHESIS: "LEFT_PARENTHESIS",
  swing.VK_LESS: "LESS",
  swing.VK_M: "M",
  swing.VK_META: "META",
  swing.VK_MINUS: "MINUS",
  swing.VK_MODECHANGE: "MODECHANGE",
  swing.VK_MULTIPLY: "MULTIPLY",
  swing.VK_N: "N",
  swing.VK_NONCONVERT: "NONCONVERT",
  swing.VK_NUM_LOCK: "NUM_LOCK",
  swing.VK_NUMBER_SIGN: "NUMBER_SIGN",
  swing.VK_NUMPAD0: "NUMPAD0",
  swing.VK_NUMPAD1: "NUMPAD1",
  swing.VK_NUMPAD2: "NUMPAD2",
  swing.VK_NUMPAD3: "NUMPAD3",
  swing.VK_NUMPAD4: "NUMPAD4",
  swing.VK_NUMPAD5: "NUMPAD5",
  swing.VK_NUMPAD6: "NUMPAD6",
  swing.VK_NUMPAD7: "NUMPAD7",
  swing.VK_NUMPAD8: "NUMPAD8",
  swing.VK_NUMPAD9: "NUMPAD9",
  swing.VK_O: "O",
  swing.VK_OPEN_BRACKET: "OPEN_BRACKET",
  swing.VK_P: "P",
  swing.VK_PAGE_DOWN: "PAGE_DOWN",
  swing.VK_PAGE_UP: "PAGE_UP",
  swing.VK_PASTE: "PASTE",
  swing.VK_PAUSE: "PAUSE",
  swing.VK_PERIOD: "PERIOD",
  swing.VK_PLUS: "PLUS",
  swing.VK_PREVIOUS_CANDIDATE: "PREVIOUS_CANDIDATE",
  swing.VK_PRINTSCREEN: "PRINTSCREEN",
  swing.VK_PROPS: "PROPS",
  swing.VK_Q: "Q",
  swing.VK_QUOTE: "QUOTE",
  swing.VK_QUOTEDBL: "QUOTEDBL",
  swing.VK_R: "R",
  swing.VK_RIGHT: "RIGHT",
  swing.VK_RIGHT_PARENTHESIS: "RIGHT_PARENTHESIS",
  swing.VK_ROMAN_CHARACTERS: "ROMAN_CHARACTERS",
  swing.VK_S: "S",
  swing.VK_SCROLL_LOCK: "SCROLL_LOCK",
  swing.VK_SEMICOLON: "SEMICOLON",
  swing.VK_SEPARATER: "SEPARATER",
  swing.VK_SEPARATOR: "SEPARATOR",
  swing.VK_SHIFT: "SHIFT",
  swing.VK_SLASH: "SLASH",
  swing.VK_SPACE: "SPACE",
  swing.VK_STOP: "STOP",
  swing.VK_SUBTRACT: "SUBTRACT",
  swing.VK_T: "T",
  swing.VK_TAB: "TAB",
  swing.VK_U: "U",
  swing.VK_UNDEFINED: "UNDEFINED",
  swing.VK_UNDERSCORE: "UNDERSCORE",
  swing.VK_UNDO: "UNDO",
  swing.VK_UP: "UP",
  swing.VK_V: "V",
  swing.VK_W: "W",
  swing.VK_WINDOWS: "WINDOWS",
  swing.VK_X: "X",
  swing.VK_Y: "Y",
  swing.VK_Z: "Z",
}

class Window {
  def __init() {
    this._frame = swing.Frame()
    this._canvas = swing.Canvas(g -> this._draw(g))
    this._draw_callback = nil
    this._frame.getContentPane().add(this._canvas)
  }

  def __set_title(title) {
    """Not all implementations of simple gui will support __set_title
    """
    this._frame.setTitle(title)
  }

  def __set_size(dimension) {
    """Not all implementations of simple gui will support __set_size

    dimension: [width, height] pair indicating dimensions of this window
    """
    [width, height] = dimension
    this._canvas.setPreferredSize(width, height)
    this._frame.pack()
  }

  def __get_resizable() {
    return this._frame.isResizable()
  }

  def __set_resizable(resizable) {
    this._frame.setResizable(resizable)
  }

  def _draw(g) {
    """
    g: swing.Graphics instance
    """
    dim = [width, height] = List(this._canvas.getSize())
    ctx = DrawingContext(g, dim)
    ctx.font = _default_font
    ctx.color = _default_color
    if (this._draw_callback is not nil) {
      this._draw_callback(ctx)
    }
  }

  def show() {
    this._frame.setVisible(true)
  }

  def repaint() {
    """Method to call to let the system know that the window
    probably needs to be redrawn
    """
    this._frame.repaint()
    this._canvas.repaint()
  }

  def on(event_type, callback) {
    """Register callbacks for various events related to this window.

    event_type: String indicating the event type to register for.
      Possible values:
        'draw': register a callback to draw the
    """
    switch(event_type,
      'draw', () -> % {
        this._draw_callback = callback
      },
      'key', () -> % {
        this._frame.addKeyListener(new KeyListener {
          def keyPressed(event) {
            callback(KeyEvent(event, false))
          }
          def keyReleased(event) {}
          def keyTyped(event) {}
        })
      },
      'type', () -> % {
        this._frame.addKeyListener(new KeyListener {
          def keyPressed(event) {}
          def keyReleased(event) {}
          def keyTyped(event) {
            callback(KeyEvent(event, true))
          }
        })
      },
      () -> % {
        fail('Unrecognized event type ' + str(event_type))
      })
    return this
  }
}

class KeyEvent {
  def __init(keyEvent, is_type_event) {
    this._keyEvent = keyEvent
    this._modifiers = nil
    this._char = nil
    this._is_type_event = is_type_event
  }

  def __get_key() {
    return _keymap[this._keyEvent.getKeyCode()]
  }

  def __get_char() {
    if (this._is_type_event and this._char is nil) {
      this._char = this._keyEvent.getKeyChar()
    }
    return this._char
  }

  def __get_modifiers() {
    if (this._modifiers is nil) {
      this._modifiers = Set(this._keyEvent.getModifiersExText().split('+'))
    }
    return this._modifiers
  }

  def __repr() {
    return 'KeyEvent(' + this._keyEvent.getKeyText() + ')'
  }
}

class Color {
  def __init(r, g, b, a = 1.0) {
    this._r = r
    this._g = g
    this._b = b
    this._a = a
  }

  def _to_swing_color() {
    return swing.Color(this._r, this._g, this._b, this._a)
  }

  def __repr() {
    return 'Color(' + ' ,'.join([
      this._r, this._g, this._b, this._a,
    ].map(repr)) + ')'
  }
}

_default_color = Color(0, 0, 0)

class Font {
  def __init(name, size = 12) {
    """
    name: name of this font
    size: point size of this font
    """
    this.name = name
    this.size = size
    this._cached_swing_font = nil
  }

  def __getitem(new_size) {
    """
    Creates a new Font that's identical to this one, except
    with a different font size.
    """
    return Font(this.name, new_size)
  }

  def __repr() {
    return 'Font(' + this.name + ', ' + str(this.size) + ')'
  }

  def __get__swing_font_name() {
    return _swing_font_name_table[this.name]
  }

  def __get__swing_font() {
    if (this._cached_swing_font is nil) {
      this._cached_swing_font = swing.Font(this._swing_font_name, 0, this.size)
    }
    return this._cached_swing_font
  }
}

class FontMetrics {
  def __init(metrics) {
    this._metrics = metrics
  }

  def __get_height() {
    return this._metrics.getHeight()
  }

  def width(s) {
    return this._metrics.stringWidth(s)
  }
}

Monospace = Font('monospace')
SansSerif = Font('sans serif')
Serif = Font('serif')

_swing_font_name_table = {
  Monospace.name: swing.MONOSPACED,
  SansSerif.name: swing.SANS_SERIF,
  Serif.name: swing.SERIF,
}

_default_font = Monospace

class Point {
  def __init(x, y) {
    this.x = x
    this.y = y
  }

  def* __iter() {
    yield this.x
    yield this.y
  }
}

class DrawingContext {
  def __init(g, dim) {
    """
    DrawingContext carries information on how to draw things.
    Its constructor should be considered private to simple.gui.

    g: swing.Graphics instance to draw on
    dim: the boundaries of this drawing context
    """
    this._g = g
    this._dim = dim
  }

  def __set_color(color) {
    """Sets a new color for this drawing context.
    Unfortunately, retrieving the color is not currently supported.
    """
    this._g.setColor(color._to_swing_color())
  }

  def __set_font(font) {
    """Sets a new font for this drawing context.
    Unfortunately, retrieving the font is not currently supported.
    """
    this._g.setFont(font._swing_font)
  }

  def __get_dimension() {
    return this._dim
  }

  def __get_width() {
    return this._dim[0]
  }

  def __get_height() {
    return this._dim[1]
  }

  def font_metrics(font) {
    metrics = this._g.getFontMetrics(font._swing_font)
    return FontMetrics(metrics)
  }

  def fill_rectangle(x, y, width, height, color = nil) {
    if (color is not nil) {
      this.color = color
    }
    this._g.fillRect(x, y, width, height)
  }

  def draw_rectangle(x, y, width, height, color = nil) {
    if (color is not nil) {
      this.color = color
    }
    this._g.drawRect(x, y, width, height)
  }

  def draw_string(s, x, y, font = nil, color = nil) {
    if (font is not nil) {
      this.font = font
    }
    if (color is not nil) {
      this.color = color
    }
    this._g.drawString(s, x, y)
  }

  def clip(x, y, width, height, callback) {
    """Get a DrawingContext for a clipped rectangular area
    inside the drawing area for this drawing context.
    """
    "TODO: Use a try/finally idiom or equivalent"
    g = this._g.create(x, y, width, height)
    callback(DrawingContext(g, [width, height]))
    g.dispose()
  }
}
