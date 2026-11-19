# Shared timing loop for the bench/rails repros, run each under both `ruby --yjit` and `jruby`
#
# NOTE: every case gets its own `while` loop method so the call site under test stays monomorphic,
# and the loop method is called in chunks since neither JRuby nor YJIT does OSR (a single long call
# would stay interpreted)
#
# Each case runs in a fresh process: on JRuby a case measured after another one can hit a C2
# deopt storm (`intrinsic_or_type_checked_inlining` traps until PerMethodRecompilationCutoff) and
# get stuck in C1 code, 3x slower. JRuby flags reach the child processes through JRUBY_OPTS.
#
#   N=10000000 WARMUP=5 ONLY=label-substring JRUBY_OPTS=-X-C jruby bench/rails/kind_of.rb
module RailsBench
  N = Integer(ENV.fetch('N', 10_000_000))
  WARMUP = Integer(ENV.fetch('WARMUP', 5)) # rounds of N iterations before measuring
  CHUNK = N / 100

  def self.run(cases)
    if (index = ENV['RAILS_BENCH_CASE'])
      label, code = cases.to_a.fetch(Integer(index))
      return measure(label, code)
    end

    puts RUBY_DESCRIPTION
    ruby = [RbConfig.ruby]
    ruby << '--yjit' if defined?(RubyVM::YJIT) && RubyVM::YJIT.enabled?
    cases.each_with_index do |(label, _), idx|
      next if ENV['ONLY'] && !label.include?(ENV['ONLY'])

      system({ 'RAILS_BENCH_CASE' => idx.to_s }, *ruby, $PROGRAM_NAME) || abort("failed: #{label}")
    end
  end

  def self.measure(label, code)
    # Rails sources are all frozen_string_literal, so literals in a case must not allocate
    Object.class_eval "# frozen_string_literal: true\ndef __rails_bench(n); i = 0; while i < n; #{code}; i += 1; end; end",
                      __FILE__, __LINE__
    (WARMUP * 100).times { __rails_bench(CHUNK) }
    best = Array.new(3) do
      start = Process.clock_gettime(Process::CLOCK_MONOTONIC)
      100.times { __rails_bench(CHUNK) }
      Process.clock_gettime(Process::CLOCK_MONOTONIC) - start
    end.min
    printf "  %-58s %8.2f ns/op\n", label, best * 1e9 / N
  end
end
