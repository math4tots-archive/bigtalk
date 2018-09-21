"""Playing with ANSI escape codes
"""
print('\u001b[2J\u001b[3J')
print(
  "\u001b[1m BOLD \u001b[0m" +
  "\u001b[4m Underline \u001b[0m" +
  "\u001b[7m Reversed \u001b[0m")
print("\u001b[1m\u001b[4m\u001b[7m BOLD Underline Reversed \u001b[0m")

wait_for(10, () -> nil)
