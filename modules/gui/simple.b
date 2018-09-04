"""Dead simple API for making a GUI

You get:

  * a single canvas to use as your window and do all your drawing,
  * chance to add some event listeners,

and that's it.

"""
import gui.swing

"Font names"
MONOSPACED = swing.MONOSPACED
SANS_SERIF = swing.SANS_SERIF
SERIF = swing.SERIF


class Gui {
  def __init(draw_callback) {
    this._frame = swing.Frame()
    this._canvas = swing.Canvas(g -> draw_callback(Graphics(g, this)))
    this._frame.getContentPane().add(this._canvas)
  }

  def __set_title(title) {
    """Not all implementations of simple gui will support __set_title
    """
    this._frame.setTitle(title)
  }

  def __set_size(dimensions) {
    """Not all implementations of simple gui will support __set_size
    """
    [width, height] = dimensions
    this._canvas.setPreferredSize(width, height)
    this._frame.pack()
  }

  def __get_resizable() {
    return this._frame.isResizable()
  }

  def __set_resizable(resizable) {
    this._frame.setResizable(resizable)
  }

  def start() {
    this._frame.setVisible(true)
  }

  def repaint() {
    this._frame.repaint()
    this._canvas.repaint()
  }

  def on(event_type, callback) {
    if (event_type == 'key') {
      this._frame.addKeyListener(new KeyListener {
        def keyPressed(event) {
          callback(KeyEvent(event))
        }
        def keyReleased(event) {
        }
        def keyTyped(event) {
        }
      })
    } else {
      fail('Unrecognized event type ' + str(event_type))
    }
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

class Graphics {
  def __init(graphics, gui) {
    this.graphics = graphics
    this.gui = gui
  }

  def __get_width() {
    return this.gui._canvas.getSize()[0]
  }

  def __get_height() {
    return this.gui._canvas.getSize()[1]
  }

  def fill_rectangle(x, y, width, height, color) {
    this.graphics.setColor(color._as_swing_color())
    this.graphics.fillRect(x, y, width, height)
  }

  def draw_rectangle(x, y, width, height, color) {
    this.graphics.setColor(color._as_swing_color())
    this.graphics.drawRect(x, y, width, height)
  }

  def draw_string(x, y, s, font_name, font_size, color) {
    this.graphics.setColor(color._as_swing_color())
    this.graphics.setFont(font_name, 0, font_size)
    this.graphics.drawString(s, x, y)
  }
}

class Color {
  def __init(r, g, b, a = 1.0) {
    this._r = r
    this._g = g
    this._b = b
    this._a = a
  }

  def _as_swing_color() {
    return swing.Color(this._r, this._g, this._b, this._a)
  }

  def __repr() {
    return 'Color(' + ' ,'.join([
      this._r, this._g, this._b, this._a,
    ].map(repr)) + ')'
  }
}
