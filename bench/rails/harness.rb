# Shared timing loop for the bench/rails repros, run each under both `ruby --yjit` and `jruby`
#
# NOTE: every case gets its own `while` loop method so the call site under test stays monomorphic,
# and the loop method is called in chunks since neither JRuby nor YJIT does OSR (a single long call
# would stay interpreted)
#
#   N=10000000 WARMUP=5 ONLY=label-substring jruby bench/rails/kind_of.rb
module RailsBench
  N = Integer(ENV.fetch('N', 10_000_000))
  WARMUP = Integer(ENV.fetch('WARMUP', 5)) # rounds of N iterations before measuring
  CHUNK = N / 100

  def self.run(cases)
    puts RUBY_DESCRIPTION
    cases.each_with_index do |(label, code), idx|
      next if ENV['ONLY'] && !label.include?(ENV['ONLY'])

      name = :"__rails_bench_#{idx}"
      # Rails sources are all frozen_string_literal, so literals in a case must not allocate
      Object.class_eval "# frozen_string_literal: true\ndef #{name}(n); i = 0; while i < n; #{code}; i += 1; end; end",
                        __FILE__, __LINE__
      (WARMUP * 100).times { send(name, CHUNK) }
      best = Array.new(3) do
        start = Process.clock_gettime(Process::CLOCK_MONOTONIC)
        100.times { send(name, CHUNK) }
        Process.clock_gettime(Process::CLOCK_MONOTONIC) - start
      end.min
      printf "  %-58s %8.2f ns/op\n", label, best * 1e9 / N
    end
  end
end
