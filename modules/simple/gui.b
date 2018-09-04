"""Dead simple API for making simple graphical user interfaces

This implementation uses gui.swing as a backend
"""
import gui.swing

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
            callback(KeyEvent(event))
          }
          def keyReleased(event) {}
          def keyTyped(event) {}
        })
      },
      () -> % {
        fail('Unrecognized event type ' + str(event_type))
      })
    return this
  }
}

class KeyEvent {
  def __init(keyEvent) {
    this._keyEvent = keyEvent
  }

  def __get_key() {
    return this._keyEvent.getKeyText()
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
