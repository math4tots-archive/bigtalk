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
    loc = Point(0, 0)
    dim = [width, height] = List(this._canvas.getSize())
    ctx = DrawingContext(loc, dim, g)
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

class Font {
  def __init(name, size = 12) {
    """
    name: name of this font
    size: point size of this font
    """
    this.name = name
    this.size = size
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
}

Monospace = Font('monospace')
SansSerif = Font('sans serif')
Serif = Font('serif')

_swing_font_name_table = {
  Monospace.name: swing.MONOSPACED,
  SansSerif.name: swing.SANS_SERIF,
  Serif.name: swing.SERIF,
}

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
  def __init(loc, dim, g) {
    """
    loc: Point indicating the upper left corner of drawing area
    dim: [width, height] pair indicating dimension of drawing area
    g: swing.Graphics instance to draw on
    """
    this._loc = loc
    this._dim = dim
    this._g = g
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

  def fill_rectangle(x, y, width, height, color) {
    this._g.setColor(color._to_swing_color())
    this._g.fillRect(x, y, width, height)
  }

  def draw_rectangle(x, y, width, height, color) {
    this._g.setColor(color._to_swing_color())
    this._g.drawRect(x, y, width, height)
  }

  def draw_string(x, y, s, font, color) {
    this._g.setColor(color._to_swing_color())
    this._g.setFont(font.name, 0, font.size)
    this._g.drawString(s, x, y)
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
