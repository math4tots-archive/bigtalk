import gui.swing
import gui.swing.color


def main() {
  frame = swing.Frame()
  frame.setTitle("Canvas")

  canvas = swing.Canvas(graphics -> % {
    print('graphics.getClipBounds() = ' + str(graphics.getClipBounds()))
    graphics.drawString("Hello world!", 50, 50)
  })
  canvas.setBackground(color.green)
  canvas.addMouseListener(new MouseListener {
    def mouseClicked(event) {
      print('mouseClicked: ' + repr(event))
    }
    def mousePressed(event) {
      print('mousePressed: ' + repr(event))
    }
    def mouseReleased(event) {
      print('mouseReleased: ' + repr(event))
    }
    def mouseEntered(event) {
      print('mouseEntered: ' + repr(event))
    }
    def mouseExited(event) {
      print('mouseExited: ' + repr(event))
    }
  })

  frame.getContentPane().add(canvas)
  frame.setVisible(true)
  frame.setSize(600, 400)

  frame.addMouseWheelListener(event -> % {
    print('mouse wheel listener: ' + str(event))
  })
}

main()
