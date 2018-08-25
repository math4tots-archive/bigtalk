"""Some basic tests to sanity check that everything is ok.
"""

assert(true)
assert(true == true)
assert(true != false)
assert(not false)
assert(not not true)
assert.equal(true, true)
assert.equal(false, false)
assert(nil is nil)
assert(true is true)
assert(not (false is not false))
assert(true is not false)

assert(1 != 2)
assert.equal(14, 14)
assert.equal(-14, -14)
assert.equal('a', 'a')

(def scope1() {
  foo = % {
    10
  }
  assert.equal(foo, 10)
})()

import _globals as globals
import _globals

assert(_globals is globals)

assert.equal([1, 2, 3], [1, 2, 3])
assert.equal([[1], 2], [[1], 2])
assert([[1], 2] != [2, [1]])
assert.equal({1, 2, 3}, {1, 2, 3})
assert.equal({1, 2, 3}, {3, 2, 1})
assert.equal({1, [2]}, {[2], 1})

"""numbers"""
assert.equal(1 + 2, 3)
assert.equal(15 - 10, 5)
assert.equal(-5, 0 - 5)
assert.equal(10 * 7, 70)
assert.equal(9 / 2, 4.5)
assert.equal(9 % 5, 4)
assert.equal(16 // 3, 5)

"""functions"""
(() -> % {
  def foo() {
  }
  assert.equal(foo(), nil)

  def bar() {
    return 'result of bar'
  }
  assert.equal(bar(), 'result of bar')

  "test closure"
  def closure_test() {

    def get_y() {
      return y
    }
    y = 10

    assert.equal(get_y(), 10)

    y = 'hi'
    assert.equal(get_y(), 'hi')

    return x
  }
  x = 818
  assert.equal(closure_test(), 818)


  f = (a, b) -> a + b
  assert.equal(f(94, 29), 123)
  assert.equal(f('a', 'b'), 'ab')
})()

"""generators"""
(() -> % {
  def* gen() {
    yield 1
    yield 2
  }

  assert.equal(gen._ops().strip(), """
0: PushLiteral(Number)
1: DoYield()
2: Pop()
3: PushLiteral(Number)
4: DoYield()
5: Pop()
  """.strip())

  xs = []
  for i in gen() {
    xs.push(i)
  }
  assert.equal(xs, [1, 2])

  def* gen2() {
    yield [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
           1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1].size()
  }
  xs = []
  for x in gen2() {
    xs.push(x)
  }
  assert.equal(xs, [30])
})()

"""class"""
(() -> % {
  def classTest() {
    class MyClass {
      def __repr() {
        return '<MyClass instance>'
      }
    }

    x = MyClass()

    print(MyClass)
    print(x)
  }

  class A {
    def a() {
      return 'aa'
    }
    def x() {
      return 'xa'
    }
  }
  class B {
    def b() {
      return 'bb'
    }
    def x() {
      return 'xb'
    }
  }
  class C(A, B) {
    def c() {
      return 'cc'
    }
  }
  c = C()
  assert.equal(c.a(), 'aa')
  assert.equal(c.b(), 'bb')
  assert.equal(c.c(), 'cc')
  assert.equal(c.x(), 'xa')

  """
  #####################################
  """

  """Eventually we may only want to allow field
  names that are used during the initializer
  """
  class Foo {
  }

  foo = Foo()
  assert.equal(_keys(foo), [])
  foo.x = 10
  assert.equal(_keys(foo), ['x'])
})()

"""list"""
assert.equal([1, 2, 3], [1, 2, 3])
assert.equal([1, 2, 3].size(), 3)
assert.equal(
  [1, 2, 3].map((x) -> x + 1),
  [2, 3, 4])
assert.equal(
  [1, 2, 3].map(x -> x + 2),
  [3, 4, 5])

"""string"""
assert('a' < 'b')
assert(not ('a' > 'b'))
assert(not ('a' >= 'b'))
assert('a' <= 'b')
assert('a' <= 'a')
