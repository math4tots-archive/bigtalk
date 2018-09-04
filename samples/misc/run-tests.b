import _bt.basic_test

def foo() {

}

"""
assert.equal([1, 2, 3].size(), 3)

total = 0
for i in range(0, 10000000) {
  total = total + i
}
print('total = ' + str(total))
"""

def old_stuff() {
  frame = swing.Frame()
  frame.setTitle("Hello frame!")
  frame.setVisible(true)
  frame.setSize(600, 400)

  panel = swing.Panel()
  print(panel.__class)
  frame.getContentPane().add(panel)

  quitButton = swing.Button("Quit")
  quitButton.setPreferredSize(300, 200)
  quitButton.addActionListener(event -> % {
    print("quit button clicked")
    some_value[0] = 1 + some_value[0]
    canvas.repaint()
  })
  panel.add(quitButton)

  """
  button2 = swing.Button("second button")
  panel.add(button2)

  """

  panel.setBackground(color.blue)

  canvas = swing.Canvas(graphics -> % {
    print('graphics = ' + str(graphics))
    print('graphics.getClipBounds() = ' + str(graphics.getClipBounds()))
    graphics.drawString("Hello world!", 50, 50)
    graphics.drawString('Clicked ' + str(some_value) + ' times', 50, 100)
  })
  some_value = [0]
  canvas.setPreferredSize(250, 250)
  panel.add(canvas)

  print(swing)
  print('frame = ' + str(frame))
}
