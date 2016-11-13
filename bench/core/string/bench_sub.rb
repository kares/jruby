require 'benchmark'

TIMES = 5_000_000

Benchmark.bmbm do |x|

  x.report("'foo'.sub('bar', '') [#{TIMES}x]") do
    TIMES.times { 'foo'.sub('bar', '') }
  end

  x.report("'foo_bar_baz'.sub('baz', 'xxx') [#{TIMES}x]") do
    TIMES.times { 'foo-bar-baz'.sub('baz', 'xxx') }
  end

  x.report("'foo_bar_baz'.sub(/baz/, 'xxx') [#{TIMES}x]") do
    TIMES.times { 'foo-bar-baz'.sub(/baz/, 'xxx') }
  end

  x.report("('foo_bar_baz' * 100).sub('baz', '00') [#{TIMES}x]") do
    str = ('foo_bar_baz' * 100)
    TIMES.times do
      str.sub('baz', '00')
    end
  end

  x.report("('foo_bar_baz' * 100).sub(/baz/, '00') [#{TIMES}x]") do
    str = ('foo_bar_baz' * 100)
    TIMES.times do
      str.sub(/baz/, '00')
    end
  end

  x.report("((('foo' * 50)) + 'bazinga!').sub('bazinga!', '0') [#{TIMES}x]") do
    str = ((('foo' * 50)) + 'bazinga!')
    TIMES.times do
      str.sub('bazinga!', '0')
    end
  end

  x.report("((('foo' * 50)) + 'bazinga!').sub(/bazinga!/, '0') [#{TIMES}x]") do
    str = ((('foo' * 50)) + 'bazinga!')
    TIMES.times do
      str.sub(/bazinga!/, '0')
    end
  end

  x.report("('foo_bar_baz' * 100).sub('miss', '') [#{TIMES}x]") do
    str = ('foo_bar_baz' * 100)
    TIMES.times { str.sub('miss', '') }
  end

  x.report("('foo_bar_baz' * 100).sub(/miss/, '') [#{TIMES}x]") do
    str = ('foo_bar_baz' * 100)
    TIMES.times { str.sub(/miss/, '') }
  end

  x.report("('foo_bar_baz' * 100).sub('baZ', '') [#{TIMES}x]") do
    str = ('foo_bar_baz' * 100)
    TIMES.times { str.sub('baZ', '') }
  end

  x.report("('foo_bar_baz' * 100).sub(/baZ/, '') [#{TIMES}x]") do
    str = ('foo_bar_baz' * 100)
    TIMES.times { str.sub(/baZ/, '') }
  end

end
