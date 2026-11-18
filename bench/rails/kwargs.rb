# keyword argument calls as ActiveRecord 8.1 makes them on every query
require_relative 'harness'

class ConnectionHandler
  # active_record/connection_adapters/abstract/connection_handler.rb
  def retrieve_connection_pool(connection_name, role: :writing, shard: :default, strict: false)
    connection_name
  end
end

class Adapter
  # active_record/connection_adapters/abstract_adapter.rb
  def with_raw_connection(allow_retry: false, materialize_transactions: true)
    allow_retry
  end
end

module Arel
  # arel.rb -- rest + optional kwarg + kwrest
  def self.sql(sql_string, *positional_binds, retryable: false, **named_binds)
    sql_string
  end
end

def positional(connection_name, role, shard, strict) = connection_name

HANDLER = ConnectionHandler.new
ADAPTER = Adapter.new
SQL = 'SELECT 1'

RailsBench.run(
  'baseline: positional(name, :reading, :default, false)'      => "positional('primary', :reading, :default, false)",
  'retrieve_connection_pool(name) (all defaults)'              => "HANDLER.retrieve_connection_pool('primary')",
  'retrieve_connection_pool(name, role:, shard:)'              => "HANDLER.retrieve_connection_pool('primary', role: :reading, shard: :default)",
  'with_raw_connection(allow_retry: true)'                     => 'ADAPTER.with_raw_connection(allow_retry: true)',
  'Arel.sql(str, retryable: true)'                             => 'Arel.sql(SQL, retryable: true)',
  'Arel.sql(str)'                                              => 'Arel.sql(SQL)'
)
